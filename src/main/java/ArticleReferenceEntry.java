import java.sql.Date;

public class ArticleReferenceEntry {
	
	public String stock, url;
	public Date date;
	
	public ArticleReferenceEntry(String stock, String url, Date date){
		this.stock = stock;
		this.url = url;
		this.date = date;
	}

}
