package napominalka;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.*;
import java.awt.event.*;
import java.util.TreeMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Napominalka {
	private DatesNamesContainer container = new DatesNamesContainer();
	// private TreeMap<LocalDate, String> datesNames = DatesNamesContainer.getDatesNames();
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private final Font defFont = Font.decode(null);
	private float scaleRatio = Toolkit.getDefaultToolkit().getScreenResolution()/96;
	private float scaleAdditional = 2.5f;
	private float newFontSize = defFont.getSize() * scaleRatio * scaleAdditional;
	private JFrame frame;
	private Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
	private ArrayList<JTextField> textFields = new ArrayList<>();
	// private final Font scaledFont = defFont.deriveFont(newFontSize);
	private final Font scaledFont = new Font("Calibri", Font.PLAIN, (int)newFontSize);
	private JPanel mainPanel;
	private int descriptionMaxLength = 10;
	private int descriptionAvgLength = 10;
	
	public static void main(String[] args) {
		System.setProperty("sun.java2d.uiScale", "1");
		try {
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		System.out.println("Locale:"+Locale.getDefault());
		
		
		
		// var prop = UIManager.getString("FileChooser.lookInLabelText", Locale.getDefault());
		// System.out.println("prop:"+prop);
		// Locale.setDefault(Locale.RUSSIAN); 
		// System.out.println("Locale:"+Locale.getDefault());
		
		new Napominalka().buildGui();
		
	}
	
	
	private void buildGui() {
		setUIFont(new FontUIResource(scaledFont));
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			new Exporter().writeToFile(container.getDatesNames());
			// System.out.println("textFields.size():"+textFields.size());
		}));
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
		// frame.getPreferredSize().getWidth();
		
		// frame.setResizable(false);
		frame.setIconImage(image);
		
		
		
		// mainWindow.setLayout(new GridLayout(datesNames.size(),2));
		// BorderLayout layout = new BorderLayout();
		// JPanel background = new JPanel(layout);
		JPanel background = new JPanel(new GridLayout(0,1));
		// JPanel background = new JPanel(new FlowLayout());
		background.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
		// GridLayout grid = new GridLayout(container.getDatesNames().size(), 2);
		GridLayout grid = new GridLayout(0, 1);
		grid.setVgap(10);
		grid.setHgap(20);
		mainPanel = new JPanel(grid);
		// mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
		
		addTextfieldsToPanelNew(mainPanel);
		double minTfLength = textFields.stream().mapToDouble(tf -> tf.getPreferredSize().getWidth()).min().getAsDouble();
		double maxTfLength = textFields.stream().mapToDouble(tf -> tf.getPreferredSize().getWidth()).max().getAsDouble();
		double maxTfHeight = textFields.stream().mapToDouble(tf -> tf.getPreferredSize().getHeight()).max().getAsDouble();
		System.out.println("maxTfLength:"+maxTfLength);
		int framePreferredWidth = (int)Math.max(minTfLength*2.2, minTfLength+maxTfLength*1.1);
		int frameMinimalHeight = (int)Math.max(maxTfHeight*textFields.size()*0.8, screenSize.getHeight()*0.15);
		int framePreferredHeight = (int)Math.min((screenSize.getHeight()*0.7), frameMinimalHeight);
		var fDimension = new Dimension(framePreferredWidth, framePreferredHeight);
		frame.setPreferredSize(fDimension);
		
		/* var addButton = new JButton("+");
		addButton.setFont(scaledFont.deriveFont(Font.BOLD, scaledFont.getSize()*1.2f));
		addButton.setMargin(new Insets(20,0,0,0));
		addButton.addActionListener((ae) -> {
			var newjp = new MyJPanel(Map.entry(LocalDate.of(1900, 12, 1), "описание"));
			textFields.add(newjp.getDateTextField());
			textFields.add(newjp.getNameTextField());
			mainPanel.add(newjp);
			frame.pack();
			// mainPanel.repaint();
			// mainPanel.revalidate();
			// background.revalidate();
		}); */
		var jsp = new JScrollPane(mainPanel);
		var jsbar = jsp.getVerticalScrollBar();
		jsbar.setPreferredSize(new Dimension((int)(jsbar.getPreferredSize().getWidth()*scaleAdditional), 0));
		jsbar.setUnitIncrement((int)newFontSize);
		jsp.getHorizontalScrollBar().setUnitIncrement((int)newFontSize);
		// jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// background.add(addButton, BorderLayout.SOUTH);
		
		background.add(jsp, BorderLayout.WEST);
		frame.getContentPane().add(background);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}

	
	private void addTextfieldsToPanelNew(JPanel mainPanel) {
		mainPanel.removeAll();
		var trayIcon = SystemTray.getSystemTray().getTrayIcons()[0];
		trayIcon.setToolTip("");
		
		LocalDate closestDate = container.getClosestDateInFuture().getKey().withYear(0);
		
		descriptionAvgLength = (int) container.getDatesNames().values().stream().mapToInt(v -> v.length()).average().orElse(10);
		descriptionMaxLength = container.getDatesNames().values().stream().mapToInt(v -> v.length()).max().orElse(10);
		var tmpMap = new TreeMap<LocalDate,String>(new MyDateWoYearComparator());
		tmpMap.putAll(container.getDatesNames());
		var tfields = new ArrayList<JTextField>();
		while (!tmpMap.isEmpty()) {
			var entry = tmpMap.pollFirstEntry();
			var myjp = new MyJPanel(entry);
			// int descLength = myjp.getNameTextField().getText().length();
			// if (descriptionMaxLength < descLength) descriptionMaxLength = descLength;
			tfields.add(myjp.getDateTextField());
			tfields.add(myjp.getNameTextField());
			mainPanel.add(myjp);
			
			
			
			if (entry.getKey().withYear(0).equals(closestDate)) {
				myjp.getNameTextField().setBackground(new Color(200,200,200));
				if (!trayIcon.getToolTip().contains("Сегодня!")) {
					String message = String.format("Ближайшая дата: %s %s", entry.getKey(), entry.getValue());
					trayIcon.setToolTip(message);
				}
			}
			if (entry.getKey().withYear(0).equals(LocalDate.now().withYear(0))) {
				myjp.getNameTextField().setBackground(new Color(50,255,50));
				String message = String.format("Ближайшая дата: Сегодня! (%s)", entry.getValue());
				if (trayIcon.getToolTip().contains("Сегодня!")) message = message + "+++";
				trayIcon.setToolTip(message);
			}
		}
		// frame.setMaximumSize(frame.getPreferredSize());
		// frame.setMaximumSize(new Dimension(20,20));
		textFields = tfields;
		
		
	}
	
	private void saveChangesToContainer() {
		int changes = 0;
		for (int i = 0; i < textFields.size()-1; i=i+2) {
			
			String dateStr = textFields.get(i).getText();
			String nameStr = textFields.get(i+1).getText();
			
			boolean changed = container.overwriteIfExists(dateStr, nameStr);
			if (changed) changes++;
			
		}
		// System.out.println("saveChanges:"+container.getDatesNames());
		// System.out.println("saveChanges:"+textFields.forEach().);
		// textFields.forEach(tf -> System.out.print(tf.getText()+" "));
		// System.out.printf("tfnumber and datesnames num:%s %s",textFields.size(), container.getDatesNames().size());
		if (changes > 0) new Exporter().writeToFile(container.getDatesNames());
		System.out.println("Number of changes: "+changes);
	}
	
	
	private void addTrayIcon() {
		
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			
			SystemTray tray = SystemTray.getSystemTray();
			
			PopupMenu popup = new PopupMenu();
			popup.setFont(scaledFont.deriveFont(scaledFont.getSize()*0.6f));
			
			MenuItem importItem = new MenuItem("Импорт...");
			importItem.addActionListener((ae) -> {
				prepareFileChooser();
				var fileChooser = new JFileChooser();
				// System.out.println("ui:"+fileChooser.getUI().getClass());
				var fcUI = (BasicFileChooserUI) fileChooser.getUI();
				// fcUI.getAccessoryPanel().removeAll();
				var d = new Dimension((int)(screenSize.getWidth()*0.4), (int)(screenSize.getHeight()*0.5));
				fileChooser.setPreferredSize(d);
				// System.out.println(System.getProperty("os.name"));
				if (System.getProperty("os.name").contains("Windows"))
					fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home") + "/Desktop"));
				
				fileChooser.setFileView(new javax.swing.filechooser.FileView() {
					public Icon getIcon(java.io.File f) {
						var imgIcon = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(f);
						int w = (int)(imgIcon.getIconWidth()*scaleRatio*1.4);
						int h = (int)(imgIcon.getIconHeight()*scaleRatio*1.4);
						var imgageIcon = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(f);
						// System.out.println(imgIcon.getIconWidth());
						// System.out.println(imgIcon.getIconHeight());
						// int size = UIManager.getInt("FileChooser.iconsSize");
						return new ImageIcon(imgageIcon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
					}
				});
				
				
				// fileChooser.setFont(scaledFont.deriveFont(scaledFont.getSize()*0.01f));
				// System.out.println("fcLocale:"+fileChooser.getLocale());
				// System.out.println("fcLocaleLang:"+fileChooser.getLocale().getDisplayLanguage());
				int response = fileChooser.showOpenDialog(frame);
				if (response == JFileChooser.APPROVE_OPTION) {
					container.fillDatesNames(fileChooser.getSelectedFile().toPath());
					addTextfieldsToPanelNew(mainPanel);
					mainPanel.revalidate();
				}
				
			});
			popup.add(importItem);
			
			if (System.getProperty("os.name").contains("Windows")) {
				var desktopDir = new java.io.File(System.getProperty("user.home") + "/Desktop");
				MenuItem exportItem = new MenuItem("Экспорт на раб.стол");
			
				exportItem.addActionListener((ae) -> {
					new Exporter().writeToFile(container.getDatesNames(), desktopDir, "NapominalkaData");
				});
				popup.add(exportItem);
				
				
			}
			
			MenuItem mainItem = new MenuItem("Развернуть");
			
			mainItem.addActionListener((ae) -> {
				frame.setVisible(true);
				frame.setExtendedState(JFrame.NORMAL);
				frame.setAlwaysOnTop(true);
				frame.setAlwaysOnTop(false);
			});
			popup.add(mainItem);
			
			popup.addSeparator();
			MenuItem exitItem = new MenuItem("Выйти");
			
			exitItem.addActionListener((ae) -> System.exit(0));
			popup.add(exitItem);
			
			
			trayIcon = new TrayIcon(image, "Напоминалка", popup);
			trayIcon.setImageAutoSize(true);
			
			// var entry = container.getClosestDateInFuture();
			// trayIcon.setToolTip(String.format("Ближайшая дата: %s %s", entry.getKey(), entry.getValue()));
			
			trayIcon.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton()==1) {
						if (frame.isVisible()) frame.setVisible(false);
						else {
							frame.setVisible(true);
							frame.setExtendedState(JFrame.NORMAL);
							frame.setAlwaysOnTop(true);
							frame.setAlwaysOnTop(false);
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
	
	private void prepareFileChooser() {
		boolean beenHere = (boolean) UIManager.get("FileChooser.readOnly");
		if (beenHere) return;
		UIManager.put("FileChooser.readOnly", Boolean.TRUE); 
		UIManager.put("FileChooser.noPlacesBar", Boolean.TRUE); 
		UIManager.put("FileChooser.detailsViewActionLabelText", "Таблица");
		UIManager.put("FileChooser.listViewActionLabelText", "Список");
		UIManager.put("FileChooser.openDialogTitleText", "Открыть");
		UIManager.put("FileChooser.lookInLabelText", "");
		UIManager.put("FileChooser.fileNameLabelText", "Имя файла:");
		UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла:");
		UIManager.put("FileChooser.openButtonText", "Открыть");
		UIManager.put("FileChooser.cancelButtonText", "Отмена");
		UIManager.put("FileChooser.acceptAllFileFilterText", "Любые файлы");
		UIManager.put("FileChooser.upFolderToolTipText", "Наверх");
		UIManager.put("FileChooser.viewMenuButtonToolTipText", "Вид");
		// var imageIcon = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(new java.io.File("."));
		// UIManager.put("FileChooser.iconsSize", Integer.valueOf((int)(imageIcon.getIconWidth()*scaleRatio*1.4)));
		
		// UIManager.put("FileChooser.listViewWindowsStyle", Boolean.TRUE); 
		// UIManager.put("FileChooser.usesSingleFilePane", Boolean.FALSE);
		// var lafDefaults = new TreeMap<String,Object>();
		// UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		UIDefaults defaults = UIManager.getDefaults();
		
		for (var enumm = defaults.keys(); enumm.hasMoreElements(); ) {
			Object key = enumm.nextElement();
			Object value = defaults.get(key);
			String keyStr = key.toString();
			// lafDefaults.put(keyStr,value);
			
			if ((keyStr.startsWith("FileChooser.") || keyStr.startsWith("FileView.")) && keyStr.contains("Icon")) {
				try {
				var imgIcon = (ImageIcon) value;
				int w = (int)(imgIcon.getIconWidth()*scaleRatio*1.4);
				int h = (int)(imgIcon.getIconHeight()*scaleRatio*1.4);
				
				UIManager.put(key, new ImageIcon(imgIcon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT)));
				} catch (Exception e) {}
			}
			// System.out.printf("%s %s\n",key, value);
		}
		// lafDefaults.entrySet().forEach(e -> System.out.printf("%s = %s\n",e.getKey(),e.getValue()));
	}
	
	/* private class MyFileView extends javax.swing.filechooser.FileView {
		static ImageIcon imgIcon = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(new java.io.File("."));
		final int w = (int)(imgIcon.getIconWidth()*scaleRatio*1.4);
		final int h = (int)(imgIcon.getIconHeight()*scaleRatio*1.4);
		
		public Icon getIcon(java.io.File f) {
			
			
			var imgageIcon = (ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(f);
			// System.out.println(imgIcon.getIconWidth());
			// System.out.println(imgIcon.getIconHeight());
			// int size = UIManager.getInt("FileChooser.iconsSize");
			return new ImageIcon(imgageIcon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
		}
	} */

	private class EditMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if (e.getButton()==1 && e.getClickCount()>1) {
				JTextField c = (JTextField) e.getComponent();
				if (!c.isEditable()) {
					if (textFields.stream().filter(tf -> tf.isEditable()).count() > 0) return;
					c.setEditable(true);
					c.getCaret().setVisible(true);
				}
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}

	private class MyJPanel extends JPanel {
		private JTextField dateTf;
		private JTextField nameTf;
		private JPopupMenu jPopupMenu;
		
		public MyJPanel(Map.Entry<LocalDate, String> entry) {
			jPopupMenu = new JPopupMenu();
			var jMenuItemAdd = new JMenuItem("Создать");
			jMenuItemAdd.addActionListener((ae) -> {
				var newjp = new MyJPanel(Map.entry(LocalDate.now(), "Сегодня!"));
				// newjp.getNameTextField().setBackground(new Color(250,250,250));
				newjp.getNameTextField().setEditable(true);
				newjp.getNameTextField().requestFocusInWindow();
				textFields.add(newjp.getDateTextField());
				textFields.add(newjp.getNameTextField());
				mainPanel.add(newjp);
				mainPanel.revalidate();
				// var d = frame.getSize();
				// frame.pack();
				// frame.setSize(d);
				// newjp.requestFocusInWindow();
				var jsp = (JScrollPane) mainPanel.getParent().getParent();
				// jsp.revalidate();
				var jvsb =  jsp.getVerticalScrollBar();
				// jvsb.revalidate();
				// jvsb.setValue(jvsb.getMaximum()+jvsb.getUnitIncrement()*2);
				SwingUtilities.invokeLater(() -> {jvsb.setValue(jvsb.getMaximum());});
			});
			jPopupMenu.add(jMenuItemAdd);
			var jMenuItemDel = new JMenuItem("Удалить");
			jPopupMenu.add(jMenuItemDel);
			jMenuItemDel.addActionListener((ae) -> {
				var jm = (JMenuItem) ae.getSource();
				var jpm = (JPopupMenu) jm.getParent();
				var myjtf = (JTextField) jpm.getInvoker();
				var myjpanel = (MyJPanel) myjtf.getParent();
				String selDateStr = myjpanel.getDateTextField().getText();
				LocalDate selectedDate = new Parser().parseSmallToken(selDateStr);
				System.err.printf("Request to remove: %s %s",myjpanel.getDateTextField().getText(), myjpanel.getNameTextField().getText());
				container.remove(selectedDate);
				new Exporter().writeToFile(container.getDatesNames());
				// int dtfIndex = textFields.indexOf(myjpanel.getDateTextField());
				// textFields.remove(dtfIndex); textFields.remove(dtfIndex+1);
				// saveChangesToContainer();
				
				addTextfieldsToPanelNew(mainPanel);
				mainPanel.revalidate();
				frame.revalidate();
				// frame.pack();
			});
			jPopupMenu.addSeparator();
			var jMenuItemDelAll = new JMenuItem("Очистить");
			jPopupMenu.add(jMenuItemDelAll);
			jMenuItemDelAll.addActionListener((ae) -> {
				var jm = (JMenuItem) ae.getSource();
				var jpm = (JPopupMenu) jm.getParent();
				var myjtf = (JTextField) jpm.getInvoker();
				var myjpanel = (MyJPanel) myjtf.getParent();
				String selDateStr = myjpanel.getDateTextField().getText();
				LocalDate selectedDate = new Parser().parseSmallToken(selDateStr);
				System.err.printf("Request to remove all\n");
				container.clear();
				new Exporter().writeToFile(container.getDatesNames());
				// int dtfIndex = textFields.indexOf(myjpanel.getDateTextField());
				// textFields.remove(dtfIndex); textFields.remove(dtfIndex+1);
				// saveChangesToContainer();
				mainPanel.removeAll();
				addTextfieldsToPanelNew(mainPanel);
				mainPanel.revalidate();
				frame.revalidate();
				// frame.pack();
			});
			
			
			this.dateTf = new JTextField(entry.getKey().format(DateTimeFormatter.ofPattern("d MMMM y")), 10);
			dateTf.setMargin(new Insets(20,20,0,0));
			// dateTf.setMargin(new Insets(20,0,0,0));
			dateTf.setEditable(false);
			// dateTf.setInheritsPopupMenu(true);
			dateTf.add(jPopupMenu);
			dateTf.setComponentPopupMenu(jPopupMenu);
			dateTf.addMouseListener(new EditMouseListener());
			dateTf.addActionListener(ae -> {	//change date or remove entry
				
				JTextField c = (JTextField) ae.getSource();
				if (!c.getText().equals("") && !new Parser().isValidDate(c.getText())) return;
				if (c.getText().equals("")) {
					container.remove(entry.getKey());
				} else if (new Parser().isValidDate(c.getText())) {
					saveChangesToContainer();
				}
				c.setEditable(false);
				c.getCaret().setVisible(false);
				addTextfieldsToPanelNew(mainPanel);
				frame.pack();
				// mainPanel.revalidate();
				
			});
			
			this.nameTf = new JTextField(entry.getValue(), Math.max(10,(int)(descriptionMaxLength*0.63)));
			nameTf.setMargin(new Insets(20,20,0,0));
			// nameTf.setMargin(new Insets(20,0,0,0));
			nameTf.setEditable(false);
			// nameTf.setInheritsPopupMenu(true);
			nameTf.add(jPopupMenu);
			nameTf.setComponentPopupMenu(jPopupMenu);
			
			
			
			nameTf.addActionListener(ae -> {
				JTextField c = (JTextField) ae.getSource();
				c.setEditable(false);
				c.getCaret().setVisible(false);
				// System.out.println("submitName:"+container.getDatesNames());
				saveChangesToContainer();
			});
			nameTf.addMouseListener(new EditMouseListener());
			
			this.add(dateTf);
			this.add(nameTf);
			
			
			// this.setComponentPopupMenu(jPopupMenu);
			
			/* this.setComponentPopupMenu(new JPopupMenu() {
				var jMenuItem = new JMenuItem("Удалить");
				public JPopupMenu() {
					this.add(jMenuItem);
					jMenuItem.addActionListener((ae) -> {
						System.out.println(ae.getSource());
					});
				}
			}); */
			
			/* this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					// if (e.getButton()==3) {
						var comp = e.getComponent();
						jPopupMenu.show(comp, e.getX(), e.getY());
					// }
				}
			}); */
			
			
			
		}
		
		public JTextField getDateTextField() { return dateTf; }
		public JTextField getNameTextField() { return nameTf; }
	}
}