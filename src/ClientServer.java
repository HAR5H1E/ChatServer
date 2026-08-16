import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class ClientServer {
	
	private static final int PORT = 3000;
	private static final int LineLimit = 13;
	private static TextBox chatFeed;
	private static MultiWindowTextGUI gui;
	private static  TextBox CMDINPUT;
	
	
	
	public static void Scroll(TextBox chat) {
	    String[] lines = chat.getText().split("\n", -1);
	    if (lines.length>LineLimit) {
	    	String[] text = Arrays.copyOfRange(lines,lines.length - LineLimit , lines.length );
	    	String finalText = String.join("\n", text);
	    	chat.setText(finalText);
	    }
	    
	}
	
	public static void UpdateChatListener(String input)  {
		gui.getGUIThread().invokeLater(()->{
			if (!input.isEmpty()) {
				chatFeed.addLine("[SERVER] "+input);
				Scroll(chatFeed);
			
				
			}
		});
	}
	
	public static void UpdateChatUser(String input) {
		gui.getGUIThread().invokeLater(()->{
			if (!input.isEmpty()) {
				chatFeed.addLine("[You] > "+ input);
				Scroll(chatFeed);
			}else {
				chatFeed.addLine("[You] > ");
				Scroll(chatFeed);
			}
		});
	}
	

	public static void main(String[] args) throws IOException {

		Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();

		BasicWindow window = new BasicWindow("ChatBox");

		chatFeed = new TextBox(new TerminalSize(80, 15), TextBox.Style.MULTI_LINE);
		chatFeed.setTheme(new SimpleTheme(TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
		chatFeed.setReadOnly(true);

		CMDINPUT = new TextBox(new TerminalSize(80, 1), TextBox.Style.MULTI_LINE);
		CMDINPUT.setTheme(new SimpleTheme(TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));

		Border cmdInputBordered = CMDINPUT.withBorder(Borders.singleLine("Input"));

		Panel MainContainer = new Panel(new LinearLayout(Direction.VERTICAL));
		MainContainer.addComponent(chatFeed);
		MainContainer.addComponent(new EmptySpace(new TerminalSize(1, 1)));
		MainContainer.addComponent(cmdInputBordered);

		window.setComponent(MainContainer);
		window.setFocusedInteractable(CMDINPUT);
		window.setHints(Arrays.asList(BasicWindow.Hint.EXPANDED));

		ClientServer.gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
				new EmptySpace(TextColor.ANSI.BLACK));

		try {
			Socket server = new Socket("localhost", PORT);
			ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());
			output.flush();
			ObjectInputStream input = new ObjectInputStream(server.getInputStream());
			Thread ListenerThread = new Thread(() -> {
				try {
					while (true) {
						String inputServer = (String) input.readObject();

						if (inputServer.equals("r--ShutDown--r")) {
							server.close();
							output.close();
							input.close();
						} else if (inputServer.equals("r--Clear--r")) {
							chatFeed.setText("");
						} else {
							System.out.println(inputServer);
							UpdateChatListener(inputServer);

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

			UpdateChatListener("Type your message below (type 'quit' to exit):");
			CMDINPUT.setInputFilter((interactedComponent, key) -> {
				if (key.getKeyType() == KeyType.Enter) {
					String message = CMDINPUT.getText().trim();

					if (!message.toLowerCase().equals("quit")) {

						try {
							if (!message.isEmpty()) {
								output.writeObject(message);
								output.flush();
							}
							UpdateChatUser(message);
							CMDINPUT.setText("");

						} catch (IOException e) {
							UpdateChatUser("ERROR MESSAGE COULD NOT BE SENT");
						}

					} else {
						try {
							server.close();
							output.close();
							input.close();
						} catch (IOException e) {

							e.printStackTrace();
						}

						window.close();
					}

					return false;

				}

				return true;
			});

			gui.addWindowAndWait(window);

			screen.stopScreen();

		} catch (IOException e) {
			
			
			UpdateChatListener("Error Server Connection");
			gui.addWindowAndWait(window);
		}

	}

}
