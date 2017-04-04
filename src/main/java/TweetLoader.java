import twitter4j.*;

import java.util.*;

/**
 * TweetLoader.java
 */

public class TweetLoader {

    Twitter twitter = TwitterFactory.getSingleton();
    Set<Tweet> allTweets = new HashSet<Tweet>();

    public void generateTweets(String tweet) {
        Query query = new Query(tweet);
        query.setSince("2017-01-04");

        query.setCount(500);
        try {
            loadNextPage(query, tweet);
        }catch (TwitterException e){
            System.out.println("Twitter query failed");
        }
    }

    public void loadNextPage(Query query, String tweet) throws TwitterException{
        long lowestTweetId = Long.MAX_VALUE;
        int tweetsPerPage = 0;
        int total = 0;
        do {
            QueryResult queryResult = twitter.search(query);
            tweetsPerPage = queryResult.getTweets().size();
            System.out.println("Please wait while I'm loading the tweets..");

            for (Status tw : queryResult.getTweets()) {
                //Eliminating all Retweets as well as tweets in languages different than English
                if(tw.getRetweetCount() == 0 && tw.getLang().equals("en")) {
                    total++;
                    Tweet t = new Tweet(tw.getUser().getScreenName(),tw.getText());
                    allTweets.add(t);
                    if (tw.getId() < lowestTweetId) {
                        lowestTweetId = tw.getId();
                        query.setMaxId(lowestTweetId);
                    }
                }

            }

        } while (tweetsPerPage != 0 && tweetsPerPage % 100 == 0);

        System.out.println("Total tweets found for: [" + tweet + "] -->" + total + "->" + allTweets.size());
        Iterator<Tweet> it = allTweets.iterator();
        int counter = 0;
        while(it.hasNext()){
            counter++;
            System.out.println(counter + ". " + it.next().getTweet());
        }
    }

}
