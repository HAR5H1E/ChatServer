import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class mainServer {
	
	private static final int PORT = 8080;
	public static ConcurrentHashMap<String,ClientHandlerThread> serverClient = new ConcurrentHashMap<>();
	
	public static void main(String args[]) {
		DBManager.CreateTable();
		try {
			ServerSocket mainServer = new ServerSocket(PORT);
			System.out.println("SERVER RUNNING AT PORT 8080");
			
			while (true) {
				Socket connection = mainServer.accept();
				System.out.println("Found a User !: "+ connection.getInetAddress().getHostAddress());
				Thread workerThread = new Thread(new ClientHandlerThread(connection)) ;
				workerThread.start();
			}
			
			
			

		} catch (IOException e) {
			
			System.out.println(e.getMessage());
		}
	}

}
