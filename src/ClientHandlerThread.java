import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientHandlerThread implements Runnable {
	
	private final Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String Id;
	private String Password;
	
	public ClientHandlerThread(Socket connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());
			
			while (true){
				output.writeObject("LOGIN OR REGISTER");
				String Choice = (String) input.readObject();
				
				if (Choice.toLowerCase().startsWith("register")) {
				
					output.writeObject("ENTER UserName");
					this.Id = (String) input.readObject();
					
					output.writeObject("ENTER Password");
					this.Password = (String) input.readObject();
					
					String UncID = UUID.randomUUID().toString();
					UncID = UncID.replace("-", "");
					
					DBManager.InsertRow(this.Id,this.Password,UncID);
					
		
					output.writeObject("REGISTERED!");
					output.writeObject("YOUR UUID IS: "+UncID);
					break;
				
				}else if(Choice.toLowerCase().startsWith("login")) {
					output.writeObject("ENTER UserName");
					this.Id = (String) input.readObject();
					
					output.writeObject("ENTER Password");
					this.Password = (String) input.readObject();
					int PassCount = 0;
					
					while (PassCount < 5) {
						output.writeObject("ENTER UserName");
						this.Id = (String) input.readObject();
						
						output.writeObject("ENTER Password");
						this.Password = (String) input.readObject();
						boolean val = DBManager.Search(this.Id, this.Password);
						
						if (val) {
							output.writeObject("LOGGED!");
							PassCount = 0;
							break;
						}else {
							output.writeObject("Incorrect Try Again");
							PassCount++;
						}
						
					}
					
					if (PassCount !=0) {
						output.writeObject("Too Many Tries Exiting Program");
						this.connection.close();
					}

					
				}else {
					output.writeObject("Username or Password is Incorrect");
				}
			
			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
