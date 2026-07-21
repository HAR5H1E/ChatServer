import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class mainServer {
	
	private static final int PORT = 8080;
	private static ConcurrentHashMap<String,ClientHandlerThread> Clients = new ConcurrentHashMap<>();
	public static void main(String args[]) {
		try {
			ServerSocket mainServer = new ServerSocket(PORT);
			System.out.println("SERVER RUNNING AT PORT 8080");
			Socket connection = mainServer.accept();
			
			

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
