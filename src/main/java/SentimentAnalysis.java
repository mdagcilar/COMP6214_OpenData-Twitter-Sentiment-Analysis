import twitter4j.*;

/**
 * SentimentAnalysis.java
 */

public class SentimentAnalysis {

    public static void main(String[] args){
        Twitter twitter = TwitterFactory.getSingleton();
        Query query = new Query("dow jones");
        try {
            QueryResult result = twitter.search(query);
            for (Status status : result.getTweets()) {
                System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
            }
        }catch (TwitterException e){
            System.out.println("Twitter query failed");
        }
    }
}
