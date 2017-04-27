import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBInterface {
	
	private static String RDS_HOSTNAME = "odi.cs91hp1txu0l.eu-west-2.rds.amazonaws.com";
	private static String RDS_PORT = "1433";
	private static String RDS_USERNAME = "odi20";
	private static String RDS_PASSWORD = "sepisepi";
	
	private static String RDS_DB_NAME = "ODI";
	
	private static String SENTIMENT_ANALYSIS_TABLE_NAME = "Sentiment_Analysis";
	private static String SENTIMENT_ANALYSIS_STOCK_COL = "Stock";
	private static String SENTIMENT_ANALYSIS_TWITTER_COL = "Twitter_Mood";
	private static String SENTIMENT_ANALYSIS_ARTICLE_COL = "Article_Mood";
	private static String SENTIMENT_ANALYSIS_DATE_COL = "Date";
	
	private static String STOCK_PREDICTIONS_TABLE_NAME = "Predictions";
	private static String STOCK_PREDICTIONS_STOCK_COL = "Stock";
	private static String STOCK_PREDICTIONS_PREDICTION_COL = "Prediction";
	private static String STOCK_PREDICTIONS_DATE_COL = "Date";
	
	
	private static boolean DEBUG = true;
	
	
	private static Connection getRemoteConnection() {
		try {
			String jdbcUrl = "jdbc:sqlserver://" + RDS_HOSTNAME + ":" + RDS_PORT + ";databaseName=" + RDS_DB_NAME;
			if(DEBUG){ System.out.println("Connecting to: " + jdbcUrl + "..."); }
			Connection con = DriverManager.getConnection(jdbcUrl, RDS_USERNAME, RDS_PASSWORD);
			if(DEBUG){ System.out.println("Remote connection successful."); }
		    return con;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void executeQuery(String query) throws SQLException {
		Connection con = getRemoteConnection();
		if(con != null){
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				stmt.execute(query);
			} catch (SQLException e ) {
		    	e.printStackTrace();
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    } 
		}
	}
	

	public static List<SentimentAnalysisEntry> readSentimentTable() throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "SELECT * FROM " + SENTIMENT_ANALYSIS_TABLE_NAME;
		    List<SentimentAnalysisEntry> entries = new ArrayList<SentimentAnalysisEntry>();
		    try {
		        stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(query);
		        while (rs.next()) {
		        	String stock = rs.getString(SENTIMENT_ANALYSIS_STOCK_COL);
		            String twitterMood = rs.getString(SENTIMENT_ANALYSIS_TWITTER_COL);
		            String articleMood = rs.getString(SENTIMENT_ANALYSIS_ARTICLE_COL);
		            Date date = rs.getDate(SENTIMENT_ANALYSIS_DATE_COL);
		            if(DEBUG){ System.out.println(stock + "\t" + twitterMood + "\t" + articleMood + "\t" + date); }
		            
		            entries.add(new SentimentAnalysisEntry(stock, twitterMood, articleMood, date));
		        }
		        return entries;
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    	return null;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    }
		}
		else return null;
	}
	
	
	public static List<PredictionEntry> readPredictionsTable() throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "SELECT * FROM " + STOCK_PREDICTIONS_TABLE_NAME;
		    List<PredictionEntry> entries = new ArrayList<PredictionEntry>();
		    try {
		        stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(query);
		        while (rs.next()) {
		        	String stock = rs.getString(STOCK_PREDICTIONS_STOCK_COL);
		        	float prediction = rs.getFloat(STOCK_PREDICTIONS_PREDICTION_COL);
		            Date date = rs.getDate(STOCK_PREDICTIONS_DATE_COL);
		            if(DEBUG){ System.out.println(stock + "\t" + prediction + "\t" + date); }
		            
		            entries.add(new PredictionEntry(stock, prediction, date));
		        }
		        return entries;
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    	return null;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    }
		}
		else return null;
	}
	

	public static void main(String[] args) {
		
		try {
			List<SentimentAnalysisEntry> sentimentEntries = DBInterface.readSentimentTable();
			for(int i = 0; i < sentimentEntries.size(); i++) {
				System.out.println(sentimentEntries.get(i).stock + "\t" + sentimentEntries.get(i).twitterMood + "\t" + sentimentEntries.get(i).articleMood + "\t" + sentimentEntries.get(i).date);
	        }
			
			List<PredictionEntry> predictionEntries = DBInterface.readPredictionsTable();
			for(int i = 0; i < predictionEntries.size(); i++) {
	            System.out.println(predictionEntries.get(i).stock + "\t" + predictionEntries.get(i).prediction + "\t" + predictionEntries.get(i).date);
	        }
			
			//executeQuery("CREATE TABLE Predictions (Stock varchar(255), Prediction float, Date DATE );");
			//executeQuery("INSERT INTO Predictions (Stock, Prediction, Date) VALUES ('test', '1.234', '2017-04-27');");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}

/*TODO
 * functions to write to each table
 * functions to clear each table (or remove specific entries)
 * create table for passing article URLs, and access functions for this
*/
