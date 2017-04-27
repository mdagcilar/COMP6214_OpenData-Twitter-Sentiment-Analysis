import java.sql.Date;

public class SentimentAnalysisEntry {

	public String stock, twitterMood, articleMood;
	public Date date;
	
	public SentimentAnalysisEntry(String stock, String twitterMood, String articleMood, Date date){
		this.stock = stock;
		this.twitterMood = twitterMood;
		this.articleMood = articleMood;
		this.date = date;
	}
}
