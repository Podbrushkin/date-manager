package napominalka;

import java.util.Comparator;
import java.time.LocalDate;

public class MyDateWoYearComparator implements Comparator<LocalDate> {
	public int compare(LocalDate ld1, LocalDate ld2) {
				if (ld1.withYear(0).isBefore(ld2.withYear(0))) return -1;
				if (ld1.withYear(0).isAfter(ld2.withYear(0))) return 1;
				
				if (ld1.withYear(0).equals(ld2.withYear(0))) {
					if (ld1.isBefore(ld2)) return -1;
					if (ld1.isAfter(ld2)) return 1;
				}
				return 0;
			}
	
	
}