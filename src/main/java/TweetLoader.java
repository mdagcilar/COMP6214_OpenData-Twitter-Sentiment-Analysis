import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;


import java.io.IOException;
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
        for(Tweet t : tweetmanager){
            allTweets.add(new TweetObject(tweet, Long.parseLong(t.getId()), -1, articles, convertDateToSqlDate(t.getDate()), t.getText()));
        }

        //initialize connection to the database. Saves opening and closing a connection when adding lots of data.
        Connection con = dbInterface.getRemoteConnection();

        this.init();
        Iterator<TweetObject> it = allTweets.iterator();
        int counter=0;

        //begin while loop
        while (it.hasNext()) {
            counter++;
            TweetObject t = it.next();
            String str = t.getTweetText();

            //set the mood for every tweet
            t.setTweetMoodValue(this.analyseTweets(str));

            //fetch the articles(if multiple) per tweet
            ArrayList<String> urls = this.getUrls(str);

            //analyze the general mood for every article
            if(urls.size() >=1) {
                this.articles = new ArrayList<Article>();
                for(int i=0; i<urls.size(); i++){
                    //set the mood for the article
                    Article a = new Article(t.getTweetID(), urls.get(i), t.getTweetMoodValue());
                    a.setArticleMood(this.analyseTweets(this.getArticleContent(urls.get(i))));
                    articles.add(a);
                }
                t.setRelatedArticles(articles);
            }

            System.out.println("\n*****");
            System.out.println(counter + " of " + allTweets.size());
            try{
                dbInterface.addSentimentEntry(con, t.getStockName(), t.getTweetMoodValue(), getAverageArticleMood(t), t.getTweetDate(), t.getTweetID());
            }catch(SQLException sqlexception){
                System.out.println("SQL Exception thrown: Failed to addSentimentEntry to database(CUSTOM ERROR MESSAGE)");
            }

            //print output to console
            System.out.println("Tweet: " + t.getTweetText());
            System.out.println("    -General mood of tweet " + counter + ": " + ", TweetMood: " + t.getTweetMoodValue() + ", AverageArticleMood: " + getAverageArticleMood(t) + ", date: " +t.getTweetDate() + ", TweetID: " + t.getTweetID());

            if(t.getRelatedArticles() !=null){
                for(int i=0; i<t.getRelatedArticles().size(); i++) {
                    System.out.println("      -Article " + i + ", general mood: " + t.getRelatedArticles().get(i).getArticleMood() + ", URL: " + t.getRelatedArticles().get(i).getArticleUrl());
                }
            }
            System.out.println();
        }   //end while loop

        //close the connection to the database
        try {
            con.close();
        }catch(SQLException sqlexception){
            System.out.println("Failed to close db connection");
        }catch(NullPointerException e){
            System.out.println("Failed to close db connection NullPointerException");
        }
    }
        //TODO: Eliminating all Retweets as well as tweets in languages different than English
        //TODO: Default article and tweet mood value = -1, later changed using setter methods
        //TODO: Stock name to be dynamically changed based on the user's stock name preference


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
    private java.sql.Date convertDateToSqlDate(Date utilDate){
        return new java.sql.Date(utilDate.getTime());
    }



    /*
     * Method to get the average article mood
     *
     * Returns an int containing the accumulative sum of the moods
     * divided by the number of articles
     */
    private float getAverageArticleMood(TweetObject t){
        float result =0;
        //if there are no articles to get a mood from, then return -1
        if(t.getRelatedArticles() == null){
            return -1;
        }
        for(int i=0; i<t.getRelatedArticles().size(); i++) {
            result += t.getRelatedArticles().get(i).getArticleMood();
        }
        return result/t.getRelatedArticles().size();
    }
}
