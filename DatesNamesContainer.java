package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;

public class DatesNamesContainer {
	
	private static TreeMap<LocalDate, String> datesNames = new TreeMap<>();
	
	private static void fillDatesNames() {
		datesNames.putAll(new Parser().parseTsv(Path.of(".\\tsv.txt")));
	}
	
	public static TreeMap<LocalDate, String> getDatesNames() {
		if (datesNames.size() == 0) fillDatesNames();
		return new TreeMap<LocalDate, String>(datesNames);
	}
}