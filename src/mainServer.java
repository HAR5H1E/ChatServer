import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLServerSocketFactory;

public class mainServer {
	
	private static final int PORT = 3000;
	public static ConcurrentHashMap<String,ClientHandlerThread> serverClient = new ConcurrentHashMap<>();
	
	public static void main(String args[]) {
		DBManager.CreateTable();
		try {
			SSLServerSocketFactory ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			ServerSocket mainServer = ssl.createServerSocket(PORT);
			System.out.println("SERVER RUNNING AT PORT 8080");

			while (true) {
				Socket connection = mainServer.accept();
				System.out.println("Found a User !: " + connection.getInetAddress().getHostAddress().toString());
				Thread workerThread = new Thread(new ClientHandlerThread(connection));
				workerThread.start();
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}

