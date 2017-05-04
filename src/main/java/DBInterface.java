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
	private static String SENTIMENT_ANALYSIS_TWEET_ID_COL = "Tweet_ID";
	
	private static String STOCK_PREDICTIONS_TABLE_NAME = "Predictions";
	private static String STOCK_PREDICTIONS_STOCK_COL = "Stock";
	private static String STOCK_PREDICTIONS_PREDICTION_COL = "Prediction";
	private static String STOCK_PREDICTIONS_DATE_COL = "Date";
	
	private static String WORD_COUNT_TABLE_NAME = "Word_Count";
	private static String WORD_COUND_STOCK_COL = "Stock";
	private static String WORD_COUNT_WORD_COL = "Word";
	private static String WORD_COUNT_COUNT_COL = "Count";
	private static String WORD_COUNT_DATE_COL = "Date";
	
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
	
	
	public static boolean executeQuery(String query) throws SQLException {
		Connection con = getRemoteConnection();
		if(con != null){
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				return stmt.execute(query);
			} catch (SQLException e ) {
		    	e.printStackTrace();
		    	return false;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    } 
		}
		else return false;
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
		            float twitterMood = rs.getFloat(SENTIMENT_ANALYSIS_TWITTER_COL);
		            float articleMood = rs.getFloat(SENTIMENT_ANALYSIS_ARTICLE_COL);
		            Date date = rs.getDate(SENTIMENT_ANALYSIS_DATE_COL);
		            long tweetID = rs.getLong(SENTIMENT_ANALYSIS_TWEET_ID_COL);
		            if(DEBUG){ System.out.println(stock + "\t" + twitterMood + "\t" + articleMood + "\t" + date + "\t" + tweetID); }
		            
		            entries.add(new SentimentAnalysisEntry(stock, twitterMood, articleMood, date, tweetID));
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
	
	
	public static List<WordCountEntry> readWordCountTable() throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "SELECT * FROM " + WORD_COUNT_TABLE_NAME;
		    List<WordCountEntry> entries = new ArrayList<WordCountEntry>();
		    try {
		        stmt = con.createStatement();
		        ResultSet rs = stmt.executeQuery(query);
		        while (rs.next()) {
		        	String stock = rs.getString(WORD_COUND_STOCK_COL);
		        	String word = rs.getString(WORD_COUNT_WORD_COL);
		        	int count = rs.getInt(WORD_COUNT_COUNT_COL);
		            Date date = rs.getDate(WORD_COUNT_DATE_COL);
		            if(DEBUG){ System.out.println(stock + "\t" + word + "\t" + count + "\t" + date); }
		            
		            entries.add(new WordCountEntry(stock, word, count, date));
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
	
	
	
	public static boolean addSentimentEntry(String stock, float twitterMood, float articleMood, String date, long tweetID) throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "INSERT INTO " + SENTIMENT_ANALYSIS_TABLE_NAME + 
		    		" (" + SENTIMENT_ANALYSIS_STOCK_COL + ", " + SENTIMENT_ANALYSIS_TWITTER_COL + ", " + SENTIMENT_ANALYSIS_ARTICLE_COL + ", " + SENTIMENT_ANALYSIS_DATE_COL + ", " + SENTIMENT_ANALYSIS_TWEET_ID_COL +
		    		") VALUES ('" + stock + "', '" + twitterMood + "', '" + articleMood + "', '" + date + "', '" + tweetID + "');";
		    if(DEBUG) { System.out.println("QUERY: " + query); }
		    try {
		        stmt = con.createStatement();
		        return stmt.execute(query);
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    	return false;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    }
		}
		else return false;
	}
	
	
	public static boolean addPredictionEntry(String stock, float prediction, String date) throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "INSERT INTO " + STOCK_PREDICTIONS_TABLE_NAME + 
		    		" (" + STOCK_PREDICTIONS_STOCK_COL + ", " + STOCK_PREDICTIONS_PREDICTION_COL + ", " + STOCK_PREDICTIONS_DATE_COL +
		    		") VALUES ('" + stock + "', '" + prediction + "', '" + date + "');";
		    if(DEBUG) { System.out.println("QUERY: " + query); }
		    try {
		        stmt = con.createStatement();
		        return stmt.execute(query);
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    	return false;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    }
		}
		else return false;
	}
	
	
	public static boolean addWordCountEntry(String stock, String word, int count, String date) throws SQLException{
		Connection con = getRemoteConnection();
		if(con != null){
		    Statement stmt = null;
		    String query = "INSERT INTO " + WORD_COUNT_TABLE_NAME + 
		    		" (" + WORD_COUND_STOCK_COL + ", " + WORD_COUNT_WORD_COL + ", " + WORD_COUNT_COUNT_COL + ", " + WORD_COUNT_DATE_COL + 
		    		") VALUES ('" + stock + "', '" + word + "', '" + count + "', '" + date + "');";
		    if(DEBUG) { System.out.println("QUERY: " + query); }
		    try {
		        stmt = con.createStatement();
		        return stmt.execute(query);
		    } catch (SQLException e ) {
		    	e.printStackTrace();
		    	return false;
		    } finally {
		        if (stmt != null) { stmt.close(); }
		        con.close();
		    }
		}
		else return false;
	}
	
	
	

	public static void main(String[] args) {
		
		try {
			//executeQuery("TRUNCATE TABLE Sentiment_Analysis;");
			//executeQuery("TRUNCATE TABLE Predictions;");
			
			//executeQuery("CREATE TABLE Predictions (Stock varchar(255), Prediction float, Date DATE );");
			//executeQuery("CREATE TABLE Article_Refs (Stock varchar(255), URL varchar(255), Date DATE );");
			//executeQuery("CREATE TABLE Word_Count (Stock varchar(255), Word varchar(255), Count INTEGER, Date DATE );");
			
			//DBInterface.addSentimentEntry("test", (float) 5.0, (float) 5.0, "2017-04-30", 123456789);
			//DBInterface.addPredictionEntry("test", (float) 5.555, "2017-04-30");
			//DBInterface.addWordCountEntry("test", "hello", 10, "2017-05-03");
			
			List<SentimentAnalysisEntry> sentimentEntries = DBInterface.readSentimentTable();
			for(int i = 0; i < sentimentEntries.size(); i++) {
				System.out.println(sentimentEntries.get(i).stock + "\t" + sentimentEntries.get(i).twitterMood + "\t" + sentimentEntries.get(i).articleMood + "\t" + sentimentEntries.get(i).date + "\t" + sentimentEntries.get(i).tweetID);
	        }
			
			List<PredictionEntry> predictionEntries = DBInterface.readPredictionsTable();
			for(int i = 0; i < predictionEntries.size(); i++) {
	            System.out.println(predictionEntries.get(i).stock + "\t" + predictionEntries.get(i).prediction + "\t" + predictionEntries.get(i).date);
	        }
			
			List<WordCountEntry> wordCountEntries = DBInterface.readWordCountTable();
			for(int i = 0; i < wordCountEntries.size(); i++) {
	            System.out.println(wordCountEntries.get(i).stock + "\t" + wordCountEntries.get(i).word + "\t" + wordCountEntries.get(i).count + "\t" + wordCountEntries.get(i).date);
	        }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}

/*TODO
 * Create word count table
*/
