import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.lanterna.TerminalPosition;
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
	private static final int LineLimit = 15;
	private static TextBox chatFeed;
	private static MultiWindowTextGUI gui;
	private static  TextBox CMDINPUT;
	
	
	
	public static void Scroll(TextBox chat) {
	    String[] lines = chat.getText().split("\n", -1);
	    int lastLine = lines.length - 1;
	    int lastCol = lines[lastLine].length();
	    chat.setCaretPosition(lastLine, lastCol);
	    chat.invalidate();
	}
	
	public static void UpdateChatListener(String input)  {
		gui.getGUIThread().invokeLater(()->{
			if (!input.isEmpty()) {
				String curr = chatFeed.getText()+"\n";
				chatFeed.addLine(input);
				chatFeed.takeFocus(); 
		        
				chatFeed.setCaretPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
				if (CMDINPUT!=null) {
		        	CMDINPUT.takeFocus();
		        }
				
			}
		});
	}
	
	public static void UpdateChatUser(String input) {
		gui.getGUIThread().invokeLater(()->{
			if (!input.isEmpty()) {
				String curr = chatFeed.getText()+"\n";
				chatFeed.addLine("> "+ input);
				chatFeed.takeFocus(); 
		        chatFeed.setCaretPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
		        
		        if (CMDINPUT!=null) {
		        	CMDINPUT.takeFocus();
		        }
				
			}
		});
	}
	

	public static void main(String[] args) throws IOException {
		try {
			
			Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
	        Screen screen = new TerminalScreen(terminal);
	        screen.startScreen();

	        BasicWindow window = new BasicWindow("ChatBox");
	        
	        chatFeed = new TextBox(new TerminalSize(80, 15),TextBox.Style.MULTI_LINE);
	        chatFeed.setTheme(new SimpleTheme(TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
	        chatFeed.setReadOnly(true); 
	        
	        CMDINPUT = new TextBox(new TerminalSize(80, 1),TextBox.Style.MULTI_LINE);
	        CMDINPUT.setTheme(new SimpleTheme(TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
	      
	        
	        Border cmdInputBordered = CMDINPUT.withBorder(Borders.singleLine("Input"));

	        Panel MainContainer = new Panel(new LinearLayout(Direction.VERTICAL));
	        MainContainer.addComponent(chatFeed);
	        MainContainer.addComponent(new EmptySpace(new TerminalSize(1,1)));
	        MainContainer.addComponent(cmdInputBordered);
	      
	        

	        window.setComponent(MainContainer);
	        window.setFocusedInteractable(CMDINPUT);
	        window.setHints(Arrays.asList(BasicWindow.Hint.EXPANDED));

	        ClientServer.gui = new MultiWindowTextGUI(
	            screen, 
	            new DefaultWindowManager(), 
	            new EmptySpace(TextColor.ANSI.BLACK)
	        );
	        
			Socket server = new Socket("localhost",PORT);
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

			UpdateChatListener("\nType your message below (type 'quit' to exit):");
			CMDINPUT.setInputFilter((interactedComponent, key)->{
				if(key.getKeyType() == KeyType.Enter) {
					String message = CMDINPUT.getText().trim();
					if (!message.isEmpty()) {
						if (!message.toLowerCase().equals("quit")) {

							try {
								output.writeObject(message);
								output.flush();
								UpdateChatUser(message);
								CMDINPUT.setText("");
								
							} catch (IOException e) {
								UpdateChatUser("ERROR MESSAGE COULD NOT BE SENT");
							}
							
						}else {
							try {
								server.close();
								output.close();
								input.close();
							} catch (IOException e) {
								
								e.printStackTrace();
							}
							
							window.close();
						}
		
						
					}
					return false;
					
				}
				
				return true;
			});
			
			 gui.addWindowAndWait(window);
			 
			 screen.stopScreen();
			


		} catch (IOException e) {
			System.out.println("Error Server Connection");
		}

	}

}
