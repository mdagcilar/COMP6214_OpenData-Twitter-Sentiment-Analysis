import java.sql.Date;

public class WordCountEntry {

	public String stock, word;
	public int count;
	public Date date;
	
	public WordCountEntry(String stock, String word, int count, Date date){
		this.stock = stock;
		this.word = word;
		this.count = count;
		this.date = date;
	}
}
