import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class mainServer {
	
	private static final int PORT = 8080;
	
	public static void main(String args[]) {
		DBManager.CreateTable();
		try {
			ServerSocket mainServer = new ServerSocket(PORT);
			System.out.println("SERVER RUNNING AT PORT 8080");
			Socket connection = mainServer.accept();
			
			

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
