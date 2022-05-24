package napominalka;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.nio.file.Path;

public class DatesNamesContainer {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
	private Parser parser = new Parser();
	private TreeMap<LocalDate, String> datesNames = new TreeMap<>();
	
	public DatesNamesContainer() {
		fillDatesNames();
		// System.out.println("datesNames.size():"+datesNames.size());
	}
	
	private void fillDatesNames() {
		try {
			datesNames.putAll(parser.parseTsv(Path.of(".\\tsv.txt")));
		} catch (Exception e) {
			System.out.println("Failed to parse from tsv");
			datesNames.put(LocalDate.now(), "сегодня");
		}
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
		log.debug("dns in:"+datesNames);
		if (!parser.isValidDate(date)) return false;
		// System.out.printf("datesNamesCont:%s %s\n",date,name);
		var locDate = parser.parseSmallToken(date);
		// System.out.printf("datesNamesCont2!!!:%s %s\n",date,locDate);
		if (Collections.frequency(datesNames.values(), name) > 1) return false;
		if (!datesNames.containsKey(locDate) && !datesNames.containsValue(name)) {
			datesNames.put(locDate, name);
			return true;
		}
		if (!datesNames.containsKey(locDate)) {
			LocalDate existingDate = getDateByName(name);
			if (!locDate.equals(existingDate)) {
				log.trace("gonna to remove");
				datesNames.remove(existingDate, name);
			}
		} else if (datesNames.get(locDate).equals(name)) {
			return false;
		}
		datesNames.put(locDate, name);
		log.trace("changed:%s %s\n", locDate, name);
		return true;
	}
	
	public void remove(LocalDate locDate) {
		datesNames.remove(locDate);
	}
	
	public LocalDate getDateByName(String name) {
		LocalDate date = datesNames.entrySet().stream().filter((e) -> e.getValue().equals(name)).toList().get(0).getKey();
		return date;
	}
	
	public Map.Entry<LocalDate, String> getClosestDateInFuture() {
		var today = LocalDate.now();
		var currentYear = today.getYear();
		
		// var tmpMap = new TreeMap<>(getDatesNames());
		// var treeset = tmpMap.keySet().stream().map(d -> d.withYear(currentYear)).collect(Collectors.toCollection(() -> new TreeSet<>()));
		// System.out.println(treeset);
		// System.out.println("=================");
		
		var tmpMap = new TreeMap<LocalDate, String>(new Comparator<LocalDate>() {
			public int compare(LocalDate ld1, LocalDate ld2) {
				if (ld1.withYear(0).isBefore(ld2.withYear(0))) return -1;
				if (ld1.withYear(0).equals(ld2.withYear(0))) return 0;
				if (ld1.withYear(0).isAfter(ld2.withYear(0))) return 1;
				return 0;
			}
		});
		tmpMap.putAll(datesNames);
		// System.out.println(tmpMap);
		for (var entry : tmpMap.entrySet()) {
			
			if (entry.getKey().withYear(currentYear).isAfter(today)){
				// System.out.printf("%s IS after %s!\n",entry.getKey().withYear(2022),today);
				return entry;
			}
			// System.out.printf("%s is not after %s\n",entry.getKey().withYear(2022),today);
		}
		return datesNames.firstEntry();
	}
}