/**
 * Created by damyanrusinov on 01/05/2017.
 */
public class Article {

    private long tweetID;
    private String articleUrl;
    private int articleMood;

    public Article(long tweetID, String articleUrl, int articleMood) {
        this.tweetID = tweetID;
        this.articleUrl = articleUrl;
        this.articleMood = articleMood;
    }

    public void setTweetID(int id) {
        this.tweetID = id;
    }

    public void setArticleUrl(String url) {
        this.articleUrl = url;
    }

    public void setArticleMood(int mood) {
        this.articleMood = mood;
    }

    public long getTweetID() {
        return this.tweetID;
    }

    public String getArticleUrl() {
        return this.articleUrl;
    }

    public int getArticleMood() {
        return this.articleMood;
    }
}
