import java.sql.*;

public class DBInterface {
	
	private static String RDS_HOSTNAME = "odi.cs91hp1txu0l.eu-west-2.rds.amazonaws.com";
	private static String RDS_PORT = "1433";
	private static String RDS_DB_NAME = "TEST";
	private static String RDS_USERNAME = "odi20";
	private static String RDS_PASSWORD = "sepisepi";
	private static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	
	private static Connection getRemoteConnection() {
		try {
			String jdbcUrl = "jdbc:sqlserver://" + RDS_HOSTNAME + ":" + RDS_PORT + ";databaseName=" + RDS_DB_NAME;
			System.out.println("Connecting to: " + jdbcUrl + "...");
			Connection con = DriverManager.getConnection(jdbcUrl, RDS_USERNAME, RDS_PASSWORD);
			System.out.println("Remote connection successful.");
		    return con;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void createDatabase(Connection con, String dbName) throws SQLException{
		Statement stmt = null;
		String query = "CREATE DATABASE " + dbName;
		 try {
	        stmt = con.createStatement();
	        stmt.execute(query);
		 } catch (SQLException e) {
			e.printStackTrace();
		}
		 finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	public static void createTable(Connection con, String tbName) throws SQLException {
		Statement stmt = null;
	    String query = "CREATE TABLE " + tbName + " ( COMPANY_ID int, TWITTER_MOOD varchar(255), ARTICLE_MOOD varchar(255) );";
	    try {
	        stmt = con.createStatement();
	        stmt.execute(query);
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}

	public static void viewTable(Connection con, String tbName) throws SQLException {
	    Statement stmt = null;
	    String query = "SELECT * FROM " + tbName;
	    try {
	        stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	int companyID = rs.getInt("CompanyID");
	            String twitterMood = rs.getString("TwitterMood");
	            String articleMood = rs.getString("ArticleMood");
	            System.out.println(companyID + "\t" + twitterMood + "\t" + articleMood);
	        }
	    } catch (SQLException e ) {
	    	e.printStackTrace();
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	public static void executeQuery(Connection con, String query) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			 while (rs.next()) {
	            System.out.println(rs.getString(""));
	        }
		} catch (SQLException e ) {
	    	e.printStackTrace();
	    } finally {
	        if (stmt != null) { stmt.close(); }
	    }
	}
	

	public static void main(String[] args) {
		
		Connection c = DBInterface.getRemoteConnection();
		try {
			//DBInterface.createDatabase(c, "TEST");
			//DBInterface.createTable(c, "test");
			DBInterface.viewTable(c, "test");
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

}
