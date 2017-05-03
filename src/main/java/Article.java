import java.util.ArrayList;

/**
 * Created by damyanrusinov on 01/05/2017.
 */
public class Article {

    private long tweetID;
    private String articleUrl;
    private int generalMood;

    public Article(long tweetID, String articleUrl, int generalMood) {
        this.tweetID = tweetID;
        this.articleUrl = articleUrl;
        this.generalMood = generalMood;
    }

    public void setTweetID(int id) {
        this.tweetID = id;
    }

    public void setArticleUrl(String url) {
        this.articleUrl = url;
    }

    public void setGeneralMood(int mood) {
        this.generalMood = mood;
    }

    public long getTweetID() {
        return this.tweetID;
    }

    public String getArticleUrl() {
        return this.articleUrl;
    }

    public int getGeneralMood() {
        return this.generalMood;
    }
}
