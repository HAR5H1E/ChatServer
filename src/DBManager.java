import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

	private static final String URL = "jdbc:sqlite:Users.db";

	
	private static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

	public static boolean CreateUserTable() {
		String sqlQuery = "CREATE TABLE IF NOT EXISTS users (" + "userID TEXT PRIMARY KEY, "
				+ "Password TEXT NOT NULL, " + "numID TEXT NOT NULL" + ");";

		try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {

			statement.execute(sqlQuery);
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean CreateContactTable() {

        String sqlQuery = "CREATE TABLE IF NOT EXISTS contacts ("
                + "userID TEXT NOT NULL, "
                + "ContactID TEXT NOT NULL, "
                + "FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE, "
                + "FOREIGN KEY (ContactID) REFERENCES users(userID) ON DELETE CASCADE, "
                + "UNIQUE(userID, ContactID)"
                + ");";

        try (Connection conn = getConnection(); 
             Statement statement = conn.createStatement()) {

            statement.execute(sqlQuery);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public static boolean CreateChatHisTable() {
		String sqlQuery = "CREATE TABLE IF NOT EXISTS chatHistory (" 
				+ "userID TEXT NOT NULL, "
                + "ContactID TEXT NOT NULL, "
				+ "Message TEXT NOT NULL, "
                + "DateTime Text NOT NULL"
				+");";

		try (Connection conn = getConnection(); 
				Statement statement = conn.createStatement()) {

			statement.execute(sqlQuery);
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static synchronized boolean InsertChatRow(String name, String Contact, String Message,String Date) {
		String sqlQuery = "INSERT INTO chatHistory (userID, ContactID, Message, DateTime) VALUES (?, ?, ?, ?)";

		try (Connection conn =getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);
			statement.setString(2, Contact);
			statement.setString(3, Message);
			statement.setString(4, Date);
			statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static synchronized List<String[]> searchHistory(String name) {
		String sqlQuery = "SELECT userID,ContactID,Message,DateTime FROM chatHistory WHERE ContactID = ? ";
		List<String[]> contacts = new ArrayList<>();
		try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);

			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					String[] Infor = {rs.getString("userID"),
							rs.getString("ContactID"),
							rs.getString("Message"),
							rs.getString("DateTime")};
					contacts.add(Infor);
				}
				
				return contacts;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static synchronized boolean DeleteHistory(String name) {
		String SQLquery = "DELETE FROM chatHistory WHERE ContactID = ?";
		try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(SQLquery)) {

			statement.setString(1, name);
			statement.executeUpdate();
			return true;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean InsertRow(String name, String password, String uuid) {
		String sqlQuery = "INSERT INTO users (userID, Password, numID) VALUES (?, ?, ?)";

		try (Connection conn =getConnection();
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

		try (Connection conn =getConnection();
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
		try (Connection conn =getConnection();
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
		String sqlQuery = "SELECT userID,numID FROM users WHERE userID = ?";

		try (Connection conn =getConnection();
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
	
	public static synchronized boolean checkContact(String name,String UUID) {
		String[] Info = getInfo(name);
		
		if (Info != null) {
			if (UUID.equals(Info[1])) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
	
	public static synchronized boolean addContact(String name,String Recip) {
		String sqlQuery = "INSERT INTO Contacts(userID,ContactID ) VALUES (?, ?)";

		try (Connection conn =getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);
			statement.setString(2, Recip);
			statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static synchronized List<String> searchContactList(String name) {
		String sqlQuery = "SELECT ContactID FROM Contacts WHERE userID = ? ";
		List<String> contacts = new ArrayList<>();
		try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);

			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					contacts.add(rs.getString("ContactID"));
				}
				
				return contacts;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static synchronized boolean getContacts(String name,String recipId) {
		String sqlQuery = "SELECT 1 FROM Contacts WHERE userID = ? AND ContactID = ?";

		try (Connection conn = getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlQuery)) {

			statement.setString(1, name);
			statement.setString(2, recipId);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return true;
				}
			}
			return false;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static synchronized boolean Delete(String name) {
		String SQLquery = "DELETE FROM users WHERE userID = ?";
		try (Connection conn = getConnection();
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