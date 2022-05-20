package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;

public class DatesNamesContainer {
	private Parser parser = new Parser();
	private TreeMap<LocalDate, String> datesNames = new TreeMap<>();
	
	private void fillDatesNames() {
		datesNames.putAll(parser.parseTsv(Path.of(".\\tsv.txt")));
	}
	
	public SortedMap<LocalDate, String> getDatesNames() {
		if (datesNames.size() == 0) fillDatesNames();
		return Collections.unmodifiableSortedMap(datesNames);
	}
	
	public boolean contains(String date, String name) {
		var locDate = parser.parseSmallToken(date);
		if (datesNames.containsKey(locDate)) {
			if (name.equals(datesNames.get(locDate))) return true;
		}
		return false;
	}
	public boolean overwrite(String date, String name) {
		var locDate = parser.parseSmallToken(date);
		if (!datesNames.containsKey(locDate)) return false;
		
		datesNames.put(locDate, name);
		return true;
	}
	
	public Map.Entry<LocalDate, String> getClosestDateInFuture() {
		var today = LocalDate.now();
		var currentYear = today.getYear();
		for (var entry : datesNames.entrySet()) {
			
			if (entry.getKey().withYear(currentYear).isAfter(today))
				return entry;
			
		}
		return datesNames.firstEntry();
	}
}