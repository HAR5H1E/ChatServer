import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class GUIView {

    public static void main(String[] args) throws IOException {

        Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        BasicWindow window = new BasicWindow("ChatBox");
        
        TextBox chatFeed = new TextBox(new TerminalSize(75, 15),TextBox.Style.MULTI_LINE);
        chatFeed.setTheme(new SimpleTheme(TextColor.ANSI.GREEN_BRIGHT, TextColor.ANSI.BLACK));
        chatFeed.setReadOnly(true); 
        
        TextBox CMDINPUT = new TextBox(new TerminalSize(74, 1),TextBox.Style.MULTI_LINE);
        CMDINPUT.setTheme(new SimpleTheme(TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK));
        CMDINPUT.setText("> ");
        CMDINPUT.setCaretPosition(0, 2);
        CMDINPUT.setInputFilter((interactedComponent, key) -> {
        	
        	if (key.getKeyType() == KeyType.Enter) {
        		
                String text = CMDINPUT.getText().trim();
                System.out.println(text.length());
                if (!text.isEmpty()) {
                    String current = chatFeed.getText();
                    LocalDateTime now = LocalDateTime.now();
    				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    				String TimeStamp = now.format(format);
                    chatFeed.setText(current + "\n["+TimeStamp+" You]: " + text);
                    CMDINPUT.setText("> ");
                    CMDINPUT.setCaretPosition(0, 2);
                    
                }
                return false; 
            }
            return true;
        });
        
        Border cmdInputBordered = CMDINPUT.withBorder(Borders.singleLine("Input"));

        Panel MainContainer = new Panel(new LinearLayout(Direction.VERTICAL));
        MainContainer.addComponent(chatFeed);
        MainContainer.addComponent(new EmptySpace(new TerminalSize(1,1)));
        MainContainer.addComponent(cmdInputBordered);
      
        

        window.setComponent(MainContainer);
        window.setFocusedInteractable(CMDINPUT);
        window.setHints(Arrays.asList(BasicWindow.Hint.EXPANDED));

        MultiWindowTextGUI gui = new MultiWindowTextGUI(
            screen, 
            new DefaultWindowManager(), 
            new EmptySpace(TextColor.ANSI.BLACK)
        );
        
        gui.addWindowAndWait(window);
    }
}