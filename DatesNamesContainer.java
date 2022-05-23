package napominalka;

import java.util.*;
import java.time.LocalDate;
import java.nio.file.Path;

public class DatesNamesContainer {
	private Parser parser = new Parser();
	private TreeMap<LocalDate, String> datesNames = new TreeMap<>();
	
	public DatesNamesContainer() {
		fillDatesNames();
		System.out.println("datesNames.size():"+datesNames.size());
	}
	
	private void fillDatesNames() {
		datesNames.putAll(parser.parseTsv(Path.of(".\\tsv.txt")));
	}
	
	public SortedMap<LocalDate, String> getDatesNames() {
		// if (datesNames.size() == 0) fillDatesNames();
		return Collections.unmodifiableSortedMap(datesNames);
	}
	
	public boolean contains(String date, String name) {
		var locDate = parser.parseSmallToken(date);
		if (datesNames.containsKey(locDate)) {
			if (name.equals(datesNames.get(locDate))) return true;
		}
		return false;
	}
	public boolean overwriteIfExists(String date, String name) {
		if (!parser.isValidDate(date)) return false;
		var locDate = parser.parseSmallToken(date);
		if (!datesNames.containsKey(locDate) && !datesNames.containsValue(name)) return false;
		if (Collections.frequency(datesNames.values(), name) > 1) return false;
		if (!datesNames.containsKey(locDate)) {
			LocalDate existingDate = getDateByName(name);
			if (!locDate.equals(existingDate)) {
				System.out.println("gonna to remove");
				datesNames.remove(existingDate, name);
			}
		} else if (datesNames.get(locDate).equals(name)) {
			return false;
		}
		datesNames.put(locDate, name);
		System.out.printf("changed:%s %s\n", locDate, name);
		return true;
	}
	
	public LocalDate getDateByName(String name) {
		LocalDate date = datesNames.entrySet().stream().filter((e) -> e.getValue().equals(name)).toList().get(0).getKey();
		return date;
	}
	
	public boolean overwriteDate(String oldDate, String newDate) {
		return false;
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