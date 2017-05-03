import java.util.ArrayList;
import java.util.Date;

/**
 * Created by damyanrusinov on 04/04/2017.
 */
public class Tweet {

    private String stockName;
    private long tweetID;
    private int tweetMoodValue;
    private int articleMoodValue;
    private ArrayList<Article> relatedArticles;
    private Date date;
    private String tweetText;

    public Tweet(String stockName, long tweetID, int tweetMoodValue, int articleMoodValue, ArrayList<Article> relatedArticles, Date date, String tweetText) {
        this.stockName = stockName;
        this.tweetID = tweetID;
        this.tweetMoodValue = tweetMoodValue;
        this.articleMoodValue = articleMoodValue;
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

    public int getArticleMoodValue() {
        return this.articleMoodValue;
    }

    public ArrayList<Article> getRelatedArticles() {
        return this.relatedArticles;
    }

    public Date getTweetDate() {
        return this.date;
    }

    public String getTweetText() {
        return this.tweetText;
    }

    public void setTweetMoodValue(int value) {
        this.tweetMoodValue = value;
    }

    public void setArticleMoodValue(int value) {
        this.articleMoodValue = value;
    }

    public void setRelatedArticles(ArrayList<Article> a) {
        this.relatedArticles = a;
    }
}
