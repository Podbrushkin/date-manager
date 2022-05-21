package napominalka;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Napominalka {
	private DatesNamesContainer container = new DatesNamesContainer();
	// private TreeMap<LocalDate, String> datesNames = DatesNamesContainer.getDatesNames();
	private final Font defFont = Font.decode(null);
	private float scaleRatio = Toolkit.getDefaultToolkit().getScreenResolution()/96;
	private float scaleAdditional = 2.5f;
	private float newFontSize = defFont.getSize() * scaleRatio * scaleAdditional;
	private JFrame frame;
	private Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
	private ArrayList<JTextField> textFields = new ArrayList<>();
	// private final Font scaledFont = defFont.deriveFont(newFontSize);
	private final Font scaledFont = new Font("Calibri", Font.PLAIN, (int)newFontSize);
	
	public static void main(String[] args) {
		new Napominalka().buildGui();
		
	}
	
	private void buildGui() {
		setUIFont(new FontUIResource(scaledFont));
		Runtime.getRuntime().addShutdownHook(new Thread(()->new Exporter().writeToFile(container.getDatesNames())));
		addTrayIcon();
		frame = new JFrame("Напоминалка");
		frame.addWindowStateListener(new WindowStateListener() {
		   public void windowStateChanged(WindowEvent we) {
				if (we.getNewState() == JFrame.ICONIFIED) {
					frame.setVisible(false);
					frame.setExtendedState(JFrame.NORMAL);
				} else
				if (we.getNewState() == WindowEvent.WINDOW_CLOSING) {
					System.out.println("FIRE!");
					new Exporter().writeToFile(container.getDatesNames());
				}
		   }
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setIconImage(image);
		
		// mainWindow.setLayout(new GridLayout(datesNames.size(),2));
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
		GridLayout grid = new GridLayout(container.getDatesNames().size(), 2);
		grid.setVgap(10);
		grid.setHgap(20);
		var mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(background);
		
		var tmpMap = new TreeMap<>(container.getDatesNames());
		while (!tmpMap.isEmpty()) {
			var entry = tmpMap.pollFirstEntry();
			var textField = new JTextField(entry.getKey().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
			textField.setMargin(new Insets(20,20,0,0));
			textField.setEditable(false);
			
			// System.out.println("getMargin:"+textField.getMargin());
			mainPanel.add(textField);
			textFields.add(textField);
			
			textField = new JTextField(entry.getValue().toString());
			textField.setMargin(new Insets(20,20,0,0));
			textField.setEditable(false);
			if (entry.getKey().withYear(0).equals(LocalDate.now().withYear(0))) {
				textField.setBackground(new Color(50,255,50));
				String message = String.format("Ближайшая дата: Сегодня! (%s)", entry.getValue().toString());
				SystemTray.getSystemTray().getTrayIcons()[0].setToolTip(message);
			} 
			else if (entry.getKey().withYear(0).equals(container.getClosestDateInFuture().getKey().withYear(0))) {
				textField.setBackground(new Color(200,200,200));
			}
			
			textField.addActionListener(ae -> {
				JTextField c = (JTextField) ae.getSource();
				c.setEditable(false);
				c.getCaret().setVisible(false);
				revalidate();
			});
			textField.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton()==1 && e.getClickCount()>1) {
						JTextField c = (JTextField) e.getComponent();
						if (!c.isEditable()) {
							c.setEditable(true);
							c.getCaret().setVisible(true);
							// c.setBackground(null);
						}
					}
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
			});
			
			mainPanel.add(textField);
			textFields.add(textField);
			
		}
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void revalidate() {
		int changes = 0;
		for (int i = 0; i < textFields.size()-1; i=i+2) {
			
			String dateStr = textFields.get(i).getText();
			String nameStr = textFields.get(i+1).getText();
			
			if (!container.contains(dateStr, nameStr)) {
				boolean changed = container.overwrite(dateStr, nameStr);
				if (changed) changes++;
			}
		}
		if (changes > 0) new Exporter().writeToFile(container.getDatesNames());
		System.out.println("Number of changes: "+changes);
	}
	
	private void addTrayIcon() {
		
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			
			
			SystemTray tray = SystemTray.getSystemTray();
			
			
			PopupMenu popup = new PopupMenu("Название попуп меню");
			popup.setFont(scaledFont.deriveFont(scaledFont.getSize()*0.6f));
			
			MenuItem mainItem = new MenuItem("Развернуть");
			
			mainItem.addActionListener((ae) -> {
				frame.setVisible(true);
				frame.setExtendedState(JFrame.NORMAL);
			});
			popup.add(mainItem);
			
			popup.addSeparator();
			MenuItem exitItem = new MenuItem("Выйти");
			
			exitItem.addActionListener((ae) -> System.exit(0));
			popup.add(exitItem);
			
			
			trayIcon = new TrayIcon(image, "Напоминалка", popup);
			trayIcon.setImageAutoSize(true);
			
			var entry = container.getClosestDateInFuture();
			trayIcon.setToolTip(String.format("Ближайшая дата: %s %s", entry.getKey(), entry.getValue()));
			
			trayIcon.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton()==1) {
						if (frame.isVisible()) frame.setVisible(false);
						else {
							frame.setVisible(true);
							frame.setExtendedState(JFrame.NORMAL);
							// frame.setAlwaysOnTop(true);
							// frame.setAlwaysOnTop(false);
						}
					}
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