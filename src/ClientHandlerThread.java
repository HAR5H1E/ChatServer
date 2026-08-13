import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.mindrot.jbcrypt.BCrypt;

public class ClientHandlerThread implements Runnable {

	private final Socket connection;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String Id;

	public ClientHandlerThread(Socket connection) {
		this.connection = connection;
		this.Id = "";
		try {

			this.connection.setSoTimeout(900000);
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void closeConection() {
		try {
			if (mainServer.serverClient.containsKey(this.Id)) {
				System.out.println("Removed User: " + this.Id);
				mainServer.serverClient.remove(this.Id);
			}

			this.connection.close();
			output.close();
			input.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public synchronized void sendMessage(ClientHandlerThread sender, String msg) {
		try {
			if (msg != null) {
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				String TimeStamp = now.format(format);
				output.writeObject("[" + TimeStamp + "] " + sender.Id + " : " + msg);
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

		try {
			int val = this.UserLogin();
			if (val == 0) {
				this.MainPage();
			}

			output.close();
			input.close();
		} catch (SocketTimeoutException e) {
			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Has TimedOut");

			try {
				output.writeObject("You have TimedOut");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.closeConection();
		} catch (IOException e) {

			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Has Left the server");
			this.closeConection();
		} catch (ClassNotFoundException e) {

			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Class Error");
			this.closeConection();
		} catch (Exception e) {

			System.out.println(this.connection.getInetAddress().getCanonicalHostName() + " Disconected form server");
			this.closeConection();
		}

	}

	private int UserLogin() throws Exception, IOException, SocketTimeoutException, ClassNotFoundException {

		while (true) {
			output.writeObject("LOGIN OR REGISTER");
			String Choice = (String) input.readObject();

			if (Choice.toLowerCase().startsWith("register")) {

				output.writeObject("ENTER UserName");
				String User = (String) input.readObject();

				while (User.toLowerCase().equals("null") || DBManager.SearchUserAvailability(User)) {

					output.writeObject("[SERVER] UserNames Unavailable");
					output.writeObject("[SERVER] ENTER UserName");
					User = (String) input.readObject();

				}

				output.writeObject("[SERVER] ENTER Password");
				String newPassword = (String) input.readObject();
				while (newPassword.toLowerCase().equals("null")) {
					output.writeObject("[SERVER] Cannot Enter Password as Null");
					output.writeObject("[SERVER] ENTER Password");
					newPassword = (String) input.readObject();
				}

				String UncID = UUID.randomUUID().toString();
				UncID = UncID.replace("-", "");

				String HashPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

				DBManager.InsertRow(User, HashPassword, UncID);
				this.Id = User;

				output.writeObject("REGISTERED!");
				output.writeObject("YOUR UUID IS: " + UncID);
				mainServer.serverClient.put(this.Id, this);
				break;

			} else if (Choice.toLowerCase().startsWith("login")) {

				boolean IsAuth = false;
				int PassCount = 0;

				while (PassCount < 5) {
					output.writeObject("[SERVER] ENTER UserName");
					String User = (String) input.readObject();

					output.writeObject("[SERVER] ENTER Password");
					String password = (String) input.readObject();
					String val = DBManager.Search(User);

					if (val != null && BCrypt.checkpw(password, val) && !mainServer.serverClient.containsKey(User)) {
						PassCount = 0;
						this.Id = User;
						if (this.Id != null && !this.Id.trim().isEmpty()) {

							mainServer.serverClient.put(this.Id, this);
						}
						IsAuth = true;

						break;
					} else {
						PassCount++;
						if (PassCount < 5) {
							output.writeObject("[SERVER] Incorrect credentials or User session Exists. Try again ("
									+ (5 - PassCount) + " attempts left)");
						}
					}

				}

				if (!IsAuth) {
					output.writeObject("[SERVER] Too Many Tries Exiting Program");
					this.closeConection();
					return 1;
				} else {
					break;
				}
			} else {
				output.writeObject("[SERVER] Invalid Either Login or Register");
			}

		}

		return 0;

	}

	private int MainPage() throws Exception, IOException, SocketTimeoutException, ClassNotFoundException {
		output.writeObject("[SERVER] WELCOME TO THE MAIN PAGE \n[SERVER] (press help for commands or quit to exit)");
		while (true) {
			String Choice = (String) input.readObject();
			Choice = Choice.trim();
			if (Choice.startsWith("@")) {
				String[] msg = Choice.split(" ", 2);
				String recip = msg[0].substring(1);
				if (msg.length == 2) {
					if (DBManager.getContacts(this.Id, recip)) {
						ClientHandlerThread recipId = mainServer.serverClient.get(recip);
						if (recipId != null) {
							recipId.sendMessage(this, msg[1]);

						} else {
							output.writeObject("[SERVER] RECIPIENT OFFLINE - TRY WHEN ONLINE");
						}
					} else {
						output.writeObject("[SERVER] RECIPIENT NOT IN YOUR CONTACTS LIST(Use cmd /add)");
					}
				} else {
					output.writeObject("[SERVER] USAGE: @username message");
				}
			} else if (Choice.startsWith("/")) {
				String[] msg = Choice.split(" ", 2);
				String cmd = msg[0].substring(1);
				if (cmd.toLowerCase().equals("details")) {
					String[] Details = DBManager.getInfo(this.Id);
					output.writeObject("[SERVER] DETAILS\n" + "[SERVER] Name: " + Details[0] + "\n[SERVER] UUID" + Details[1]);
				} else if (cmd.toLowerCase().equals("delete")) {
					output.writeObject("[SERVER] ENTER Password");
					String password = (String) input.readObject();
					String val = DBManager.Search(this.Id);

					if (val != null && BCrypt.checkpw(password, val)) {
						if (DBManager.Delete(this.Id)) {
							output.writeObject("[SERVER] REMOVING USER");
							output.writeObject("r--ShutDown--r");
						} else {
							output.writeObject("[SERVER] ERROR COULD NOT DELETE USER");

						}
					} else {
						output.writeObject("[SERVER] INCORRECT PASSWORD");
					}

				} else if (cmd.toLowerCase().equals("add")) {
					output.writeObject("[SERVER] ENTER ContactName");
					String name = (String) input.readObject();
					output.writeObject("[SERVER] ENTER UUID");
					String UUID = (String) input.readObject();

					boolean isAuth = DBManager.checkContact(name, UUID);

					if (isAuth) {
						output.writeObject("[SERVER] Contact Added");
						DBManager.addContact(this.Id, name);
					} else {
						output.writeObject("[SERVER] no contact");
					}
				} else if (cmd.toLowerCase().startsWith("contacts")) {

					List<String> contacts = DBManager.searchContactList(this.Id);
					int i = 0;
					if (contacts != null) {
						if (contacts.size() != 0) {
							for (String val : contacts) {
								i++;
								output.writeObject(i + ". " + val);
							}
						} else {
							output.writeObject("[SERVER] No Contacts Found");
						}
					} else {
						output.writeObject("[SERVER] Couldn't get contacts (maybe empty?).");
					}
				}

			} else if (Choice.toLowerCase().startsWith("help")) {

				output.writeObject("[SERVER] Commands\n@username Message\n/Details\n/add\n/contacts\n/delete");

			} else {
				output.writeObject("[SERVER] Incorrect format");
			}

		}

	}

}
