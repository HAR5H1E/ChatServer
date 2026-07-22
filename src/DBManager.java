import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	
	private static final String URL = "jdbc:sqlite:Users.db";
	
	public static boolean CreateTable() {
		String SqlQuery = "CREATE TABLE IF NOT EXISTS users ("
				+"	userID text PRIMARY KEY,"
				+"  Password text not NULL,"
				+"	numID  text not NULL,"
				+");";
		
		try{
			Connection conn = DriverManager.getConnection(URL);
			Statement Statment = conn.createStatement();
			Statment.execute(SqlQuery);
			return true;
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			return false;
			
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public static boolean InsertRow(String Name,String Password,String UUID) {
		String SqlQuery ="INSERT INTO users VALUES (?,?,?)";
		try {
			
			Connection conn = DriverManager.getConnection(URL);
			PreparedStatement Statment = conn.prepareStatement(SqlQuery);
			Statment.setString(1,Name);
			Statment.setString(2,Password);
			Statment.setString(3,UUID);
			Statment.execute();
			return true;
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	
		
	}
	
	public static boolean Search(String Name,String Password) {
		String SqlQuery = "SELECT 1 FROM users WHERE userID = ? AND Password = ?";
		try {
			
			Connection conn = DriverManager.getConnection(URL);
			PreparedStatement Statment = conn.prepareStatement(SqlQuery);
			Statment.setString(1,Name);
			Statment.setString(2,Password);
			ResultSet rs = Statment.executeQuery();
			return rs.next();
			
		}catch(SQLException e) {
			e.printStackTrace();
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	
	}

}
