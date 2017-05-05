import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;


import java.io.IOException;
import java.net.*;
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
    private List<TweetObject> allTweets = new ArrayList<TweetObject>();
    private ArrayList urlsPerTweet;
    private ArrayList<Article> articles = null;


    //pattern to match urls contained within a tweet
    private final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);



    protected void generateTweets(String tweet) {
        TwitterCriteria query = TwitterCriteria.create()
                .setQuerySearch(tweet)
                .setMaxTweets(500)          //How many tweets to retrieve in every call to Twitter.
                .setSince("2017-04-01")
                .setUntil("2017-05-01");

        List<Tweet> tweetmanager = TweetManager.getTweets(query);
        System.out.println("Total tweets found for: [" + tweet + "] -->" + tweetmanager.size());

        //reverse the Tweets order so the dates go forward.
        Collections.reverse(tweetmanager);

        //Get all the tweets from the TweetManager and make individual TweetObject Objects
        for(int i=0; i<tweetmanager.size(); i++){
            Tweet t = tweetmanager.get(i);

            TweetObject myTweet = new TweetObject("FTSE-to-be changed", Long.parseLong(t.getId()), -1, articles, convertDateToSqlDate(t.getDate()), t.getText());
            allTweets.add(myTweet);
        }

        System.out.println("************All Tweets have been added to the ArrayList");

        this.init();
        Iterator<TweetObject> it = allTweets.iterator();

        int counter=0;
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
            if(t.getRelatedArticles() !=null){
                //push data to the database
//                try{
//                    DBInterface dbInterface = new DBInterface();
//                    dbInterface.addSentimentEntry("FTSE100", t.getTweetMoodValue(), getAverageArticleMood(t), t.getTweetDate(), t.getTweetID());
//                }catch(SQLException sqlexception){
//                    System.out.println("SQL Exception thrown: Failed to addSentimentEntry to database(CUSTOM ERROR MESSAGE)");
//                }
                //print output to console
                System.out.println("\n*******************"+ "\nGeneral mood of tweet " + counter + " : " + t.getTweetMoodValue() + ", tweetID: " + t.getTweetID()+ ", tweetMood: " + t.getTweetMoodValue() + ", number of articles: " + t.getRelatedArticles().size()  + ", date: " +t.getTweetDate());
                System.out.println("The same tweet also has " + t.getRelatedArticles().size() + " relevant articles: ");
                System.out.println("The general mood from the articles: " + getAverageArticleMood(t));
                for(int i=0; i<t.getRelatedArticles().size(); i++) {
                    System.out.println("Article " + i + " general mood: " + t.getRelatedArticles().get(i).getArticleMood() + ", URL: " + t.getRelatedArticles().get(i).getArticleUrl());
                }
                System.out.println("*******************");
            } else {
                System.out.println("\n*******************"+ "\nGeneral mood of tweet " + counter + " : " + t.getTweetMoodValue() + ", tweetID: " + t.getTweetID()+ ", tweetMood: " + t.getTweetMoodValue() + ", number of articles: " + null + ", location: "  + ", date: " +t.getTweetDate());
            }


        }
        //TODO: Eliminating all Retweets as well as tweets in languages different than English
        //TODO: Default article and tweet mood value = -1, later changed using setter methods
        //TODO: Stock name to be dynamically changed based on the user's stock name preference
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
                //null would represent all the articles that ar of wrong format or whose output we won't be using
                return null;
            }

            String text = ArticleExtractor.INSTANCE.getText(url);
            return text;
        } catch (MalformedURLException m) {
            System.out.println("The url failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (IOException i) {
            System.out.println("Open connection failed unexpectedly(CUSTOM ERROR MESSAGE");
        } catch (BoilerpipeProcessingException b) {
            System.out.println("The header query failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (NoClassDefFoundError e) {
            System.out.println("Check Nekohtml library. (CUSTOM ERROR MESSAGE)");
        }
        System.out.println("Shouldnt be here");
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
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        return sqlDate;
    }



    /*
     * Method to get the average article mood
     *
     * Returns an int containing the accumulative sum of the moods
     * divided by the number of articles
     */
    private float getAverageArticleMood(TweetObject t){
        float result =0;

        for(int i=0; i<t.getRelatedArticles().size(); i++) {
            result += t.getRelatedArticles().get(i).getArticleMood();
        }
        return result/t.getRelatedArticles().size();
    }
}
