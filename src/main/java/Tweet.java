import java.util.ArrayList;

/**
 * Created by damyanrusinov on 04/04/2017.
 */
public class Tweet {

    private String stockName;
    private long tweetID;
    private int tweetMoodValue;
    private ArrayList<Article> relatedArticles;
    private String date;
    private String tweetText;

    public Tweet(String stockName, long tweetID, int tweetMoodValue, ArrayList<Article> relatedArticles, String date, String tweetText) {
        this.stockName = stockName;
        this.tweetID = tweetID;
        this.tweetMoodValue = tweetMoodValue;
        this.relatedArticles = relatedArticles;
        this.date = date;
        this.tweetText = tweetText;
    }

    public String getStockName() {
        return this.stockName;
    }

    public long getTweetID() {
        return tweetID;
    }

    public int getTweetMoodValue() {
        return this.tweetMoodValue;
    }

    public ArrayList<Article> getRelatedArticles() {
        return this.relatedArticles;
    }

    public String getTweetDate() {
        return this.date;
    }

    public String getTweetText() {
        return this.tweetText;
    }

    public void setTweetMoodValue(int value) {
        this.tweetMoodValue = value;
    }

    public void setRelatedArticles(ArrayList<Article> a) {
        this.relatedArticles = a;
    }
}
