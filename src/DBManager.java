import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

	private static final String URL = "jdbc:sqlite:Users.db";
	private static final String CONTACTURL = "jdbc:sqlite:Users.db";

	public static boolean CreateTable() {
		String sqlQuery = "CREATE TABLE IF NOT EXISTS users (" + "userID TEXT PRIMARY KEY, "
				+ "Password TEXT NOT NULL, " + "numID TEXT NOT NULL" + ");";

		try (Connection conn = DriverManager.getConnection(URL); Statement statement = conn.createStatement()) {

			statement.execute(sqlQuery);
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean InsertRow(String name, String password, String uuid) {
		String sqlQuery = "INSERT INTO users (userID, Password, numID) VALUES (?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(URL);
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);
			statement.setString(2, password);
			statement.setString(3, uuid);
			statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized String Search(String name) {
		String sqlQuery = "SELECT Password FROM users WHERE userID = ?";

		try (Connection conn = DriverManager.getConnection(URL);
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return rs.getString("Password");
				}
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static synchronized Boolean SearchUserAvailability(String name) {
		String SQLquery = "SELECT 1 FROM users WHERE userID = ?";
		try (Connection conn = DriverManager.getConnection(URL);
				PreparedStatement statement = conn.prepareStatement(SQLquery)) {

			statement.setString(1, name);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return true;
				} 
				else {
					return false;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}

	public static synchronized String[] getInfo(String name) {
		String sqlQuery = "SELECT userID, numID FROM users WHERE userID = ?";

		try (Connection conn = DriverManager.getConnection(URL);
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return new String[] { rs.getString("userID"), rs.getString("numID") };
				}
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static synchronized boolean Delete(String name) {
		String SQLquery = "DELETE FROM users WHERE userID = ?";
		try (Connection conn = DriverManager.getConnection(URL);
				PreparedStatement statement = conn.prepareStatement(SQLquery)) {

			statement.setString(1, name);
			statement.executeUpdate();
			return true;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}