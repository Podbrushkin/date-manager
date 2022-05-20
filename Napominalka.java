package napominalka;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Napominalka {
	private TreeMap<LocalDate, String> datesNames = DatesNamesContainer.getDatesNames();
	private final Font defFont = Font.decode(null);
	private float scaleRatio = Toolkit.getDefaultToolkit().getScreenResolution()/96;
	private float scaleAdditional = 2.5f;
	private float newFontSize = defFont.getSize() * scaleRatio * scaleAdditional;
	
	// private final Font scaledFont = defFont.deriveFont(newFontSize);
	private final Font scaledFont = new Font("Calibri", Font.PLAIN, (int)newFontSize);
	public static void main(String[] args) {
		
		
		new Napominalka().buildGui();
		// printAvailableFonts();
		// System.out.println("getScreenResolution:"+Toolkit.getDefaultToolkit().getScreenResolution());
		// System.out.println("scaledFont:"+scaledFont);
		
	}
	
	private void buildGui() {
		// UIManager.put("defaultFont", defFont.deriveFont(30f));
		// UIManager.getLookAndFeelDefaults().put("defaultFont", defFont.deriveFont(30f));
		// UIManager.getDefaults().put("defaultFont", defFont.deriveFont(30f));
		/* UIManager.put("TextField.font", new Font("Arial", Font.BOLD, 17));
		UIManager.put("MenuItem.font", new Font("Arial", Font.BOLD, 17));
		UIManager.put("Menu.font", new Font("Arial", Font.BOLD, 17));
		UIManager.put("PopupMenu.font", new Font("Arial", Font.BOLD, 17)); */
		setUIFont(new FontUIResource(scaledFont));
		var frame = new JFrame("Напоминалка");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(640, 480);
		// mainWindow.setLayout(new GridLayout(datesNames.size(),2));
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
		GridLayout grid = new GridLayout(datesNames.size(), 2);
		grid.setVgap(1);
		grid.setHgap(10);
		var mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(background);
		
		var tmpMap = new TreeMap<>(datesNames);
		while (!tmpMap.isEmpty()) {
			var entry = tmpMap.pollFirstEntry();
			var textField = new JTextField(entry.getKey().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
			textField.setEditable(false);
			mainPanel.add(textField);
			textField = new JTextField(entry.getValue().toString());
			textField.setEditable(false);
			mainPanel.add(textField);
			
		}
		addTrayIcon();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void addTrayIcon() {
		
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			
			
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
			
			// create a action listener to listen for default action executed on the tray icon
			/* ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("listenre action fired");
					// execute default action of the application
					// ...
				}
			}; */
			
			PopupMenu popup = new PopupMenu("Название попуп меню");
			popup.setFont(scaledFont.deriveFont(scaledFont.getSize()*0.6f));
			
			MenuItem mainItem = new MenuItem("Also Exit");
			
			mainItem.addActionListener((ae) -> System.exit(0));
			popup.add(mainItem);
			
			popup.addSeparator();
			MenuItem exitItem = new MenuItem("Exit");
			
			exitItem.addActionListener((ae) -> System.exit(0));
			popup.add(exitItem);
			
			
			trayIcon = new TrayIcon(image, "Напоминалка", popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					System.out.println("Cliked!"+e.getButton());
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
			});
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}
		}
		
	}
	
	public static void setUIFont (FontUIResource f){
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
		  Object key = keys.nextElement();
		  Object value = UIManager.get(key);
		  if (value instanceof javax.swing.plaf.FontUIResource)
			UIManager.put(key, f);
		  }
    }
	
	private static void printAvailableFonts() {
		var lge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// System.out.println(".getAllFonts():"+Arrays.toString(lge.getAllFonts()));
		// System.out.println(".getAvailableFontFamilyNames():"+Arrays.toString(lge.getAvailableFontFamilyNames()));
		
		for (String name : lge.getAvailableFontFamilyNames()) {
			System.out.println(name);
		}
	}

}