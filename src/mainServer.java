import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mainServer {

	private static final int PORT = 3000;
	public static ConcurrentHashMap<String, ClientHandlerThread> serverClient = new ConcurrentHashMap<>();
	private static final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

	public static void main(String args[]) {
		DBManager.CreateUserTable();
		DBManager.CreateContactTable();
		DBManager.CreateChatHisTable();
		try {

			ServerSocket mainServer = new ServerSocket(PORT);
			System.out.println("SERVER RUNNING AT PORT: " + PORT);

			while (true) {
				Socket connection = mainServer.accept();

				System.out.println("Found a User !: " + connection.getInetAddress().getHostAddress());
				exec.submit(new ClientHandlerThread(connection));
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());

		} finally {
			exec.close();
		}
	}

}
