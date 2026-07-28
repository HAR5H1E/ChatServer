import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientServer {
	
	private static final int PORT = 8080;
	private static final AtomicBoolean isPresent = new AtomicBoolean(false);
	
	public static void Print() {
		System.out.print("> ");
		System.out.flush();
		isPresent.set(true);
	}

	public static void main(String[] args) {
		try {
			Socket server = new Socket("localhost", PORT);
			ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
			output.flush();
			ObjectInputStream input = new ObjectInputStream(server.getInputStream());
			Scanner userInput = new Scanner(System.in);
			String message = "";
			Thread ListenerThread = new Thread(() -> {
				try {
					while (true) {
						String inputServer = (String) input.readObject();
						if (isPresent.get()) {
							isPresent.set(false);
						}

						if (inputServer.equals("r--ShutDown--r")) {
							server.close();
							output.close();
							input.close();
						} else {
							System.out.println("\r" + inputServer);
							Print();
						}
					}

				} catch (Exception e) {
					System.out.println("Connection to server closed.");
					System.exit(0);
					return;
				}

			});
			ListenerThread.setDaemon(true);
			ListenerThread.start();

			System.out.println("\nType your message below (type 'quit' to exit):");
			while (!message.toLowerCase().equals("quit")) {
				message = userInput.nextLine();
				output.writeObject(message);

			}

			server.close();
			userInput.close();
			output.close();
			input.close();

		} catch (IOException e) {
			System.out.println("Error Server Connection");
		}

	}

}
