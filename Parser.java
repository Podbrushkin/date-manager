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
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.io.IOException;


public class Parser {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));
	DateTimeFormatter dtfAsIs = DateTimeFormatter.ofPattern("d LLLL yyyy", Locale.forLanguageTag("ru"));
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
		List<String> lines = Files.lines(file, charset).map(l -> l.strip()).filter(l -> l.length() > 3).toList();
		
		for (String line : lines) {
			//---FIRST TRY full line
			dates.addAll(parseSmallToken(line));
			
			//---SECOND TRY comma-separated
			if (line.split(",", -1).length-1 >= 1) {
				var st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					var tk = st.nextToken();
					dates.addAll(parseSmallToken(tk));
				}
			}
			
			//---THIRD TRY space-separated
			var st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				if (tk.length() < 6) continue;
				dates.addAll(parseSmallToken(tk));
			}
			
			//---FOURTH TRY comma- or tab-separated
			st = new StringTokenizer(line, ",\t");
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				if (tk.length() < 6) continue;
				dates.addAll(parseSmallToken(tk));
			}
		}
		
		dates.removeIf(d -> d.getYear() == 0);
		return dates;
	}
	
	
	
	private Set<LocalDate> parseSmallToken(String token) {
		token = token.strip();
		var tempDates = new TreeSet<LocalDate>();
		//1st and 2nd tries allow parsing from russian
		try {
			var ld = LocalDate.parse(token.toLowerCase(), dtf);
			tempDates.add(ld);
		} catch (java.time.format.DateTimeParseException e) {}
		
		if (tempDates.size() == 0) {
			try {
				var ld = LocalDate.parse(token.toLowerCase(), dtfAsIs);
				tempDates.add(ld);
			} catch (java.time.format.DateTimeParseException e) {}
		}
		
		try {
			var ld = DateParserUtils.parseDateTime(token).toLocalDate();
			if (ld != null) {
				// System.out.printf("%s\t parsed from: %s\n",ld,token);
				tempDates.add(ld);
			}
		} catch (java.time.format.DateTimeParseException e) {}
		if (tempDates.size() > 0)
		System.out.printf("%s\t parsed from: %s\n",tempDates,token);
		return tempDates;
	}
}