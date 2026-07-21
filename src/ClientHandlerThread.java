import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandlerThread implements Runnable {
	
	private final Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String Id;
	
	public ClientHandlerThread(Socket connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());
			
			output.writeObject("ENTER ID");
			this.Id = (String) input.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
