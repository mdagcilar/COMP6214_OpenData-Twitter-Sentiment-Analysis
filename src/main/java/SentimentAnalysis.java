public class SentimentAnalysis {

    public static void main(String[] args) {
        TweetLoader load = new TweetLoader();
        load.generateTweets("FTSE100");
    }
}
