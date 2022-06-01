package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

public class Exporter {
	public static File dirForData = new File(System.getProperty("user.home")+File.separator+".napominalka");
	public void writeToFile(Map<LocalDate, String> datesNames) {
		writeToFile(datesNames, "data");
		
	}
	
	public void writeToFile(Map<LocalDate, String> datesNames, File directory, String fileName) {
		if (!directory.isDirectory()) throw new IllegalArgumentException("can't write here: "+directory);
		fileName = fileName.replaceAll("[\\\\/]","");
		var file = new File(directory, fileName+".tsv");
		try {
			file.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create tsv file");
			return;
		}
		
		
		
		for (int i = 0; i < 20 && !file.canWrite(); i++) {
			System.out.printf("file:%s file.canWrite():%s",file,file.canWrite());
			file = new File(directory, fileName+i+".tsv");
		}
		if (!file.canWrite()) throw new IllegalArgumentException("can't write here: "+file);
		try (var pw = new PrintWriter(file, "utf-8")) {
			for (var entry : datesNames.entrySet()) {
				pw.println(entry.getKey()+"\t"+entry.getValue());
			}
		} catch (IOException e) {
			System.err.println("Failed to write to file.");
		}
		
	}
	
	public void writeToFile(Map<LocalDate, String> datesNames, String fileName) {
		if (!dirForData.isDirectory()) dirForData.mkdir();
		System.out.println("Writing to folder "+dirForData);
		writeToFile(datesNames, dirForData, fileName);
		
	}
}