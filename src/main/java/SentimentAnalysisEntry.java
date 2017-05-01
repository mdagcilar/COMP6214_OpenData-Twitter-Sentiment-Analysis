import java.sql.Date;

public class SentimentAnalysisEntry {

	public String stock;
	public float twitterMood, articleMood;
	public Date date;
	public int tweetID;
	
	public SentimentAnalysisEntry(String stock, float twitterMood, float articleMood, Date date, int tweetID){
		this.stock = stock;
		this.twitterMood = twitterMood;
		this.articleMood = articleMood;
		this.date = date;
		this.tweetID = tweetID;
	}
}
