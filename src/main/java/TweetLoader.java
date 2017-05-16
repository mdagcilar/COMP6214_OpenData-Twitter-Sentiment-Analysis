import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * TweetLoader.java
 */

public class TweetLoader {

    private static StanfordCoreNLP pipeline;
    private List<TweetObject> allTweets;
    private ArrayList urlsPerTweet;
    private ArrayList<Article> articles = null;
    private DBInterface dbInterface = new DBInterface();
    private Set<String> enStopwords = new HashSet<String>();
    public HashMap<String, Integer> commonWords = new HashMap<String, Integer>();



    //pattern to match urls contained within a tweet
    private final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);


    protected void generateTweets(String tweet, String dateSince, String dateUntil, int maxTweets) {
        TwitterCriteria query = TwitterCriteria.create()
                .setQuerySearch(tweet)
                .setMaxTweets(maxTweets)          //How many tweets to retrieve in every call to Twitter.
                .setSince(dateSince)
                .setUntil(dateUntil);

        List<Tweet> tweetmanager = TweetManager.getTweets(query);
        System.out.println("Total tweets found for: [" + tweet + "] --> " + tweetmanager.size());

        //reverse the Tweets order so the dates go forward.
        Collections.reverse(tweetmanager);

        //reset allTweets ArrayList
        allTweets = new ArrayList<TweetObject>();

        //Get all the tweets from the TweetManager and make individual TweetObject Objects
        for (Tweet t : tweetmanager) {
            allTweets.add(new TweetObject(tweet, Long.parseLong(t.getId()), -1, articles, convertDateToSqlDate(t.getDate()), t.getText()));
        }

        //initialize connection to the database. Saves opening and closing a connection when adding lots of data.
        Connection con = dbInterface.getRemoteConnection();

        //generate english stopwords once
        enStopwords = getEnglishStopWords();

        this.init();
        Iterator<TweetObject> it = allTweets.iterator();
        int counter = 0;

        //begin while loop
        while (it.hasNext()) {
            counter++;
            TweetObject t = it.next();
            String str = t.getTweetText();

            //get the common words
            this.generateCommonWords(str);

            //set the mood for every tweet
            t.setTweetMoodValue(this.analyseTweets(str));

            //fetch the articles(if multiple) per tweet
            ArrayList<String> urls = this.getUrls(str);

            //analyze the general mood for every article
            if (urls.size() >= 1) {
                this.articles = new ArrayList<Article>();
                for (int i = 0; i < urls.size(); i++) {
                    //set the mood for the article
                    Article a = new Article(t.getTweetID(), urls.get(i), t.getTweetMoodValue());
                    a.setArticleMood(this.analyseTweets(this.getArticleContent(urls.get(i))));
                    articles.add(a);
                }
                t.setRelatedArticles(articles);
            }

            //push data to db
            System.out.println("\n*****");
            System.out.println(counter + " of " + allTweets.size());
//            try {
//                dbInterface.addSentimentEntry(con, t.getStockName(), t.getTweetMoodValue(), getAverageArticleMood(t), t.getTweetDate(), t.getTweetID());
//            } catch (SQLException sqlexception) {
//                System.out.println("SQL Exception thrown: Failed to addSentimentEntry to database(CUSTOM ERROR MESSAGE)");
//            }

//          print output to console
            System.out.println("Tweet: " + t.getTweetText());
            System.out.println("    -General mood of tweet " + counter + ": " + ", TweetMood: " + t.getTweetMoodValue() + ", AverageArticleMood: " + getAverageArticleMood(t) + ", date: " + t.getTweetDate() + ", TweetID: " + t.getTweetID());

            if (t.getRelatedArticles() != null) {
                for (int i = 0; i < t.getRelatedArticles().size(); i++) {
                    System.out.println("      -Article " + i + ", general mood: " + t.getRelatedArticles().get(i).getArticleMood() + ", URL: " + t.getRelatedArticles().get(i).getArticleUrl());
                }
            }

        }   //end while loop
        this.getMostCommonWordsByEntry(20);

        //close the connection to the database
        try {
            con.close();
        } catch (SQLException sqlexception) {
            System.out.println("Failed to close db connection");
        } catch (NullPointerException e) {
            System.out.println("Failed to close db connection NullPointerException");
        }
    }

    private static void init() {
        pipeline = new StanfordCoreNLP("MyPropFile.properties");
    }

    /* Returns an int which represents the sentiment of the text
     *   0: "Very Negative"
     *   1: "Negative"
     *   2: "Neutral"
     *   3: "Positive"
     *   4: "Very Positive"
     */
    private int analyseTweets(String tweet) {
        int mainSentiment = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }


    private String getArticleContent(String urlString) {
        try {
            URL url = new URL(urlString);
            //avoid getting follow-up twitter links as we need article links only
            if (url.toString().contains("twitter")) {
                System.out.println("Disregarding twitter follow-up links.");
                //null would represent all the articles that are of wrong format or whose output we won't be using
                return null;
            }
            return ArticleExtractor.INSTANCE.getText(url);
        } catch (MalformedURLException m) {
            System.out.println("!!!!!The url failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (BoilerpipeProcessingException b) {
            System.out.println("!!!!!The header query failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (NoClassDefFoundError e) {
            System.out.println("!!!!!Check Nekohtml library. (CUSTOM ERROR MESSAGE)");
        }
        return null;
    }

    private ArrayList<String> getUrls(String str) {
        urlsPerTweet = new ArrayList<String>();
        Matcher matcher = urlPattern.matcher(str);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end(0);
            urlsPerTweet.add(str.substring(matchStart, matchEnd));
        }
        return urlsPerTweet;
    }


    /*
     * Method to convert java.util.Date into java.sql.Date
     * This is because the util.Date includes also includes the time
     * and a SQL data type DATE is meant to be date-only, with no time-of-day and no time zone.
     */
    private java.sql.Date convertDateToSqlDate(Date utilDate) {
        return new java.sql.Date(utilDate.getTime());
    }


    /*
     * Method to get the average article mood
     *
     * Returns an int containing the accumulative sum of the moods
     * divided by the number of articles
     */
    private float getAverageArticleMood(TweetObject t) {
        float result = 0;
        //if there are no articles to get a mood from, then return -1
        if (t.getRelatedArticles() == null) {
            return -1;
        }
        for (int i = 0; i < t.getRelatedArticles().size(); i++) {
            result += t.getRelatedArticles().get(i).getArticleMood();
        }
        return result / t.getRelatedArticles().size();
    }

    public void generateCommonWords(String tweet) {
        //get words from tweet(split tweet into words)
        ArrayList words = new ArrayList();
        String[] arr = tweet.split(" ");
        for (int i = 0; i < arr.length; i++) {
            //ignore numbers and conjugations to optimise common words generation
            if (!arr[i].matches(".*\\d+.*") && !enStopwords.contains(arr[i].toString().toLowerCase())) {
                words.add(arr[i].toLowerCase());
            }
        }
        for (int i = 0; i < words.size(); i++) {
            if (this.commonWords.size() == 0) {
                commonWords.put(words.get(i).toString(), 1);
            } else {
                String currentWord = words.get(i).toString();
                //if our hashmap contains the word, update its entry(occurrence) number
                if (this.commonWords.containsKey(currentWord)) {
                    for (Map.Entry<String, Integer> word : this.commonWords.entrySet()) {
                        if (word.getKey().equals(currentWord)) {
                            int newValue = word.getValue() + 1;
                            word.setValue(newValue);
                        }
                    }
                } else {
                    //otherwise, push it and assign occurrence = 1
                    commonWords.put(currentWord, 1);
                }
            }
        }
    }

    public HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Custom Comparator
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    //entry represents the number of common words to return
    //e.g. the top 20 words
    public List<String> getMostCommonWordsByEntry(int entry) {
        int x = 0;
        ArrayList<String> twentyWords = new ArrayList<String>();
        // sort the hashmap with common words in the end(OR LEAVE IT HERE IF
        // you would like to save time and not process the tweet and article mood
        HashMap<String,Integer> sortedMap = this.sortByValues(this.commonWords);
        for (Map.Entry<String, Integer> word : sortedMap.entrySet()) {

            if(sortedMap.size() >= entry && x < entry) {
                System.out.println(x + " " +word.getKey() + "->" + word.getValue());
                twentyWords.add(word.getKey());
                x++;
            }
        }

        return twentyWords;
    }


    public Set<String> getEnglishStopWords() {
        File file = new File("resources/englishstopwords.txt");
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                enStopwords.add(scanner.nextLine());
            }
        }catch (FileNotFoundException e){
            System.out.println("File englishstopwords.txt NOT FOUND");
        }
        return enStopwords;
    }
}