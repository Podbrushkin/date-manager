package napominalka;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;
import java.time.LocalDate;

public class Napominalka {
	private TreeMap<LocalDate, String> datesNames = DatesNamesContainer.getDatesNames();
	
	public static void main(String[] args) {
		
		
		new Napominalka().buildGui();
		
		
	}
	
	private void buildGui() {
		var frame = new JFrame("Напоминалка");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(640, 480);
		// mainWindow.setLayout(new GridLayout(datesNames.size(),2));
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		GridLayout grid = new GridLayout(datesNames.size(), 2);
		grid.setVgap(1);
		grid.setHgap(2);
		var mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(background);
		
		for (int i = 0; i < datesNames.size(); i++) {
			var entry = datesNames.pollFirstEntry();
			var textField = new JTextField(entry.getKey().toString());
			textField.setEditable(false);
			mainPanel.add(textField);
			textField = new JTextField(entry.getValue().toString());
			textField.setEditable(false);
			mainPanel.add(textField);
			
		}
		
		frame.pack();
		frame.setVisible(true);
	}
}