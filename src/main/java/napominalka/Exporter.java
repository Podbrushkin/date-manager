package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import javax.swing.JOptionPane;

@lombok.extern.slf4j.Slf4j
public class Exporter {
	// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
	public static File dirForData = new File(System.getProperty("user.home")+File.separator+".napominalka");
	private static File defaultDataFile = new File(dirForData, "data.tsv");
	
	public void writeToFile(Map<LocalDate, String> datesNames) {
		writeToFile(datesNames, defaultDataFile);
	}
	
	private void writeToFile(Map<LocalDate, String> datesNames, File file) {
		String msg = String.format("[datesNames.size()=%s, file=%s]", 
			datesNames.size(), file);
		log.info("Request to file writing with args: {}", msg);
		
		/* try {
			file.createNewFile();
			if (!file.canWrite()) throw new IOException("File created, but not writable: "+file);
		} catch (IOException e) {
			log.error("Failed to create tsv file. args={}, file={}\t Exception: {}", 
				msg, file, e);
			JOptionPane.showMessageDialog(null, "Ошибка создания файла: "+file+msg,
				"Error",JOptionPane.ERROR_MESSAGE);
			return;
		} */
		
		try (var pw = new PrintWriter(file, "utf-8")) {
			for (var entry : datesNames.entrySet()) {
				pw.println(entry.getKey()+"\t"+entry.getValue());
			}
		} catch (IOException e) {
			// System.err.println("Failed to write to file.");
			String msgTmp = String.format("PrintWriter failure! args=%s", msg);
			log.error(msgTmp+"\t{}",e);
			JOptionPane.showMessageDialog(null, "Ошибка записи в файл через PrintWriter: "+file+"\t"+e,
				"Error",JOptionPane.ERROR_MESSAGE);
			throw new IllegalArgumentException("can't write here: "+file);
		}
	}
	
	public void writeToFolder(Map<LocalDate, String> datesNames, File directory) {
		if (!directory.exists()) directory.mkdir();
		if (directory.isDirectory()) {
			var file = new File(directory, "napominalka.tsv");
			writeToFile(datesNames, file);
		}
	}
	
	public static File getDefaultDataFile() {
		return defaultDataFile;
	}
	
	/* public void writeToFile(Map<LocalDate, String> datesNames, File directory, String fileName) {
		
		
		if (!directory.isDirectory()) {
			String msg2 = String.format("%s Директория не подходит для записи: directory.isDirectory()=%s, args=%s",
				this.getClass(), directory.isDirectory(), msg);
			JOptionPane.showMessageDialog(null, msg2,"Error",JOptionPane.ERROR_MESSAGE);
			log.error(msg2);
			throw new IllegalArgumentException("can't write here: "+directory);
		}
		fileName = fileName.replaceAll("[\\\\/]","");
		var file = new File(directory, fileName+".tsv");
		
		
	} */
	
	/* public void writeToFile(Map<LocalDate, String> datesNames, String fileName) {
		if (!dirForData.isDirectory()) dirForData.mkdir();
		log.info("Writing to folder "+dirForData);
		writeToFile(datesNames, dirForData, fileName);
		
	} */
}