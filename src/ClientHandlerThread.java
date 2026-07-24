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
	
	public synchronized void sendMessage(String msg) {
		try {
			if (msg != null) {
				output.writeObject(this.Id+": "+msg);
				output.flush();
			}
		}catch(Exception e) {
			System.out.println("Error sending message");
		}
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
					mainServer.serverClient.put(this.Id, this);
					break;
				
				}else if(Choice.toLowerCase().startsWith("login")) {
					
					boolean IsAuth = false;
					int PassCount = 0;
					
					while (PassCount < 5) {
						output.writeObject("ENTER UserName");
						this.Id = (String) input.readObject();
						
						output.writeObject("ENTER Password");
						this.Password = (String) input.readObject();
						boolean val = DBManager.Search(this.Id, this.Password);
						
						System.out.println(val);
						if (val) {
							output.writeObject("LOGGED!");
							PassCount = 0;
							
							if (this.Id != null && !this.Id.trim().isEmpty()) {
								mainServer.serverClient.put(this.Id, this);
							}
							IsAuth = true;
							break;
						}else {
							PassCount++;
							if (PassCount < 5) {
								output.writeObject("Incorrect credentials. Try again (" + (5 - PassCount) + " attempts left)");
							}
						}
						
					}
					
					if (!IsAuth) {
						output.writeObject("Too Many Tries Exiting Program");
						this.connection.close();
						return;
					}else {
						break;
					}
					
				}else if ((Choice.toLowerCase().startsWith("quit"))) {
					this.connection.close();
					break;
					
				}else {
					output.writeObject("Invalid Either Login or Register");
				}
				
			
			}
			
			output.writeObject("LOGIN SUCCESFUL");
			output.writeObject("Commands: @username message");
			
			while (true) {
				String Choice = (String) input.readObject();
				if (Choice.startsWith("@")) {
					String[] msg = Choice.split(" ", 2);
					String recip = msg[0].substring(1);
					if (msg.length==2) {
						ClientHandlerThread recipId = mainServer.serverClient.get(recip);
						if (recipId != null) {
							recipId.sendMessage(msg[1]);
							
						}else {
							output.writeObject("RECIPIENT OFFLINE; TRY WHEN ONLINE");
						}
					}
					
				}
				else {
					output.writeObject("Incorrect format");
				}
			}
			
		} catch (IOException e) {
			System.out.println(this.connection.getInetAddress().toString() +" Disconected form server");
		} catch (ClassNotFoundException e) {
			System.out.println(this.connection.getInetAddress().toString() +" Disconected form server");
		}catch (Exception e) {
			System.out.println(this.connection.getInetAddress().toString() +" Disconected form server");
		}
		
		
	}

}
