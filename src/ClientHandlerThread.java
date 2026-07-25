import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

public class ClientHandlerThread implements Runnable {

	private final Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String Id;
	private String Password;

	public ClientHandlerThread(Socket connection) {
		this.connection = connection;
		try {
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void sendMessage(ClientHandlerThread sender, String msg) {
		try {
			if (msg != null) {
				output.writeObject(sender.Id + ": " + msg);
				output.flush();
			}
		} catch (Exception e) {
			try {
				sender.output.writeObject("ERROR SENDING MESSAGE");
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			}
		}
	}

	@Override
	public void run() {
		this.UserLogin();
		this.MainPage();

	}

	private void UserLogin() {
		try {

			while (true) {
				output.writeObject("LOGIN OR REGISTER");
				String Choice = (String) input.readObject();

				if (Choice.toLowerCase().startsWith("register")) {

					output.writeObject("ENTER UserName");
					this.Id = (String) input.readObject();

					output.writeObject("ENTER Password");
					this.Password = (String) input.readObject();
					while (this.Password.toLowerCase().equals("null")) {
						output.writeObject("Cannot Enter Password as Null");
						output.writeObject("ENTER Password");
						this.Password = (String) input.readObject();
					}

					String UncID = UUID.randomUUID().toString();
					UncID = UncID.replace("-", "");

					String HashPassword = BCrypt.hashpw(this.Password, BCrypt.gensalt());

					DBManager.InsertRow(this.Id, HashPassword, UncID);

					output.writeObject("REGISTERED!");
					output.writeObject("YOUR UUID IS: " + UncID);
					mainServer.serverClient.put(this.Id, this);
					break;

				} else if (Choice.toLowerCase().startsWith("login")) {

					boolean IsAuth = false;
					int PassCount = 0;

					while (PassCount < 5) {
						output.writeObject("ENTER UserName");
						String User = (String) input.readObject();

						output.writeObject("ENTER Password");
						String password = (String) input.readObject();
						String val = DBManager.Search(User);
						if (val != null && BCrypt.checkpw(password, val)) {
							PassCount = 0;
							this.Id = User;
							this.Password = password;
							if (this.Id != null && !this.Id.trim().isEmpty()) {
								mainServer.serverClient.put(this.Id, this);
							}
							IsAuth = true;
							break;
						} else {
							PassCount++;
							if (PassCount < 5) {
								output.writeObject(
										"Incorrect credentials. Try again (" + (5 - PassCount) + " attempts left)");
							}
						}

					}

					if (!IsAuth) {
						output.writeObject("Too Many Tries Exiting Program");
						output.writeObject("r--ShutDown--r");
						this.connection.close();
						return;
					} else {
						break;
					}

				} else if ((Choice.toLowerCase().startsWith("quit"))) {
					if (mainServer.serverClient.containsKey(this.Id)) {
						mainServer.serverClient.remove(this.Id);
					}
					this.connection.close();
					break;
				} else {
					output.writeObject("Invalid Either Login or Register");
				}

			}

		} catch (IOException e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Has Left the server");
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Class Error");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Disconected form server");
			System.out.println(e.getMessage());
		}

	}

	private void MainPage() {
		try {
			output.writeObject("WELCOME TO THE MAIN PAGE (press help for commands or quit to exit)");
			while (true) {
				String Choice = (String) input.readObject();
				if (Choice.startsWith("@")) {
					String[] msg = Choice.split(" ", 2);
					String recip = msg[0].substring(1);
					if (msg.length == 2) {
						ClientHandlerThread recipId = mainServer.serverClient.get(recip);
						if (recipId != null) {
							recipId.sendMessage(this, msg[1]);

						} else {
							output.writeObject("RECIPIENT OFFLINE; TRY WHEN ONLINE");
						}
					}
				} else if (Choice.startsWith("/")) {
					String[] msg = Choice.split(" ", 2);
					String cmd = msg[0].substring(1);
					System.out.println(cmd);
					if (cmd.toLowerCase().equals("details")) {
						String[] Details = DBManager.getInfo(this.Id);
						System.out.println(Details[0]);
						output.writeObject("DETAILS\n" + "Name: " + Details[0] + "\nUUID" + Details[1]);
					}

				} else if (Choice.toLowerCase().startsWith("help")) {

					output.writeObject("Commands\n@username Message\n/Details");

				} else if ((Choice.toLowerCase().startsWith("quit"))) {
					this.connection.close();
					mainServer.serverClient.remove(this.Id);
					break;

				} else {
					output.writeObject("Incorrect format");
				}

			}
		} catch (IOException e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + "Has Left the server");
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + "Class Error");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Disconected form server");
			System.out.println(e.getMessage());
		}
	}

}
