package napominalka;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Napominalka {
	private TreeMap<LocalDate, String> datesNames = DatesNamesContainer.getDatesNames();
	
	public static void main(String[] args) {
		
		
		new Napominalka().buildGui();
		
		
	}
	
	private void buildGui() {
		// UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Arial", Font.BOLD, 24));
		UIManager.put("TextField.font", new Font("Arial", Font.BOLD, 17));
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
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}