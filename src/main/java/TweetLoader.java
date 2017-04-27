import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import twitter4j.*;

import java.util.*;

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

    Twitter twitter = TwitterFactory.getSingleton();
    Set<Tweet> allTweets = new HashSet<Tweet>();
    static StanfordCoreNLP pipeline;

    public void generateTweets(String tweet) {
        Query query = new Query(tweet);
        query.setSince("2017-01-04");

        query.setCount(500);
        try {
            loadNextPage(query, tweet);
        } catch (TwitterException e) {
            System.out.println("Twitter query failed");
        }
    }

    public void loadNextPage(Query query, String tweet) throws TwitterException {
        long lowestTweetId = Long.MAX_VALUE;
        int tweetsPerPage = 0;
        do {
            this.init();
            QueryResult queryResult = twitter.search(query);
            tweetsPerPage = queryResult.getTweets().size();
            System.out.println("Please wait while I'm loading the tweets..");

            for (Status tw : queryResult.getTweets()) {
                //Eliminating all Retweets as well as tweets in languages different than English
                if (tw.getRetweetCount() == 0 && tw.getLang().equals("en")) {
                    Tweet t = new Tweet(tw.getUser().getScreenName(), tw.getText());
                    allTweets.add(t);
                    if (tw.getId() < lowestTweetId) {
                        lowestTweetId = tw.getId();
                        query.setMaxId(lowestTweetId);
                    }
                }
            }

        } while (tweetsPerPage != 0 && tweetsPerPage % 100 == 0);

        System.out.println("Total tweets found for: [" + tweet + "] -->" + allTweets.size());
        Iterator<Tweet> it = allTweets.iterator();
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            String str = it.next().getTweet();
            System.out.println(counter + ". " + str + " General mood: " + this.analyseTweets(str));
        }
    }


    public static void init() {
        pipeline = new StanfordCoreNLP("MyPropFile.properties");
    }

    public int analyseTweets(String tweet) {
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

}
