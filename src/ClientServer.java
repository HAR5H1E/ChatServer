import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientServer {
	
	private static final int PORT = 8080;

	public static void main(String[] args) {
		try {
			Socket server = new Socket("localhost",PORT);
			ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
			output.flush();
			ObjectInputStream input = new ObjectInputStream(server.getInputStream());
			Scanner userInput = new Scanner(System.in);
			String message = "";
			Thread ListenerThread = new Thread(()->{
				try {
					while (true) {
						String inputServer = (String) input.readObject();
						System.out.println("\n" + inputServer);
			            System.out.print("> ");
					}
					
				}catch(Exception e) {
					System.out.println("\nConnection to server closed.");
				}
				
			});
			ListenerThread.setDaemon(true);
			ListenerThread.start();
			
			System.out.println("\nType your message below (type 'quit' to exit):");
			while(!message.toLowerCase().equals("quit")) {
				
				message = userInput.nextLine();	
				output.writeObject(message);

				
				
			}
			
			userInput.close();
			server.close();
			System.out.println("Disconnect");
			
			
			
		}catch(IOException e) {
			System.out.println("Error Sever Connection");
			System.out.println(e.getMessage());
		}
		

	}

}
