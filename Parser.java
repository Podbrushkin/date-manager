package napominalka;

import org.mozilla.universalchardet.UniversalDetector;
import com.github.sisyphsu.dateparser.DateParserUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.StringTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.io.IOException;


public class Parser {
	public Set<LocalDate> parseFromCurrentFolder() {
		
		
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		
		String workDir = System.getProperty("user.dir");
		Path workDirPath = Path.of(workDir);
		
		System.out.println("Working Directory = " + workDir);
		// Files.list(workDirPath).forEach(line -> System.out.println(line));
		List<Path> files = Files.list(workDirPath).filter(p -> Files.isRegularFile(p)).filter(p -> Files.isReadable(p)).toList();
		System.out.println(files);
		
		Set<LocalDate> dates = new TreeSet<LocalDate>();
		var parser = new Parser();
		for (Path p : files) {
			dates.addAll(parser.parseFile(p));
		}
		System.out.println("Parsed dates: \n" + dates);
	}
	
	public Set<LocalDate> parseFile(Path file) throws IOException {
		Set<LocalDate> dates = new TreeSet<LocalDate>();
		Charset charset = Charset.forName(UniversalDetector.detectCharset(file));
		// System.out.println("Detected charset: " +charset);
		List<String> lines = Files.lines(file, charset).map(l -> l.strip()).filter(l -> l.length() > 3).toList();
		var dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
		for (String line : lines) {
			// String[] tokens = line.split("[ \t\r\n]");
			LocalDate ldt;
			//if line has 1+ commas
			// (zero&more of not-commas, comma, zero&more of not-commas) 2 and more times in string
			// if (line.matches("^([^,]*,[^,]*){2,}$")) {
			if (line.split(",", -1).length-1 >= 1) {
				// System.out.printf("\nLine matched: >>%s<<", line);
				var st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					try {
						var tk = st.nextToken().strip();
						// System.out.println("token is: "+tk);
						ldt = LocalDate.parse(tk, dtf);
						ldt = DateParserUtils.parseDateTime(tk).toLocalDate();
						// if (ldt != null) dates.add(ldt.toLocalDate());
						if (ldt != null) {
							System.out.printf("LDT parsed: %s, from line:%s\n",ldt,line);
							dates.add(ldt);
							ldt = null;
						}
					} catch (java.time.format.DateTimeParseException e) {}
				}
			}
			
			try {
				ldt = DateParserUtils.parseDateTime(line.strip()).toLocalDate();
				if (ldt != null) {
					System.out.printf("LDT parsed: %s, from line:%s\n",ldt,line);
					dates.add(ldt);
					ldt = null;
				}
			} catch (java.time.format.DateTimeParseException e) {}
			
			var st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String curToken = st.nextToken();
				/* String compound = curToken;
				if (curToken.chars().anyMatch( Character::isDigit ) && st.hasMoreTokens()) {
					compound = curToken + st.nextToken();
				} */
				try {
					ldt = DateParserUtils.parseDateTime(curToken).toLocalDate();
					if (ldt != null) {
						System.out.printf("LDT parsed: %s, from line:%s\n",ldt,line);
						dates.add(ldt);
						ldt = null;
					}
				} catch (java.time.format.DateTimeParseException e) {}
				
			}
		}
		dates.removeIf(d -> d.getYear() == 0);
		return dates;
	}
}