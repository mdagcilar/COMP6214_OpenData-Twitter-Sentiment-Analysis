import java.sql.Date;

public class PredictionEntry {

	public String stock;
	public float prediction;
	public Date date;
	
	public PredictionEntry(String stock, float prediction, Date date){
		this.stock = stock;
		this.prediction = prediction;
		this.date = date;
	}
}
