package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;

public class Exporter {
	
	public void writeToFile(Map<LocalDate, String> datesNames) {
		File file = new File("tsv.txt");
		try {
			file.createNewFile();
		} catch (IOException e) {System.err.println("Failed to create tsv file");}
		
		/* for (int i = 0; i < 20 && !file.canWrite(); i++) {
			System.out.printf("file:%s file.canWrite():%s",file,file.canWrite());
			file = new File("tsv"+i+".txt");
		} */
		
		try (var pw = new PrintWriter(file, "utf-8")) {
			for (var entry : datesNames.entrySet()) {
				pw.println(entry.getKey()+"\t"+entry.getValue());
			}
		} catch (IOException e) {
			System.err.println("Failed to write to file.");
		}
		
	}
	
}