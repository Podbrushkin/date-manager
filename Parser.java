package napominalka;

import org.mozilla.universalchardet.UniversalDetector;
import com.github.sisyphsu.dateparser.DateParserUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.*;
import java.util.StringTokenizer;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.io.IOException;


public class Parser {
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));
	private DateTimeFormatter dtfAsIs = DateTimeFormatter.ofPattern("d LLLL yyyy", Locale.forLanguageTag("ru"));
	
	public static void main(String[] args) throws IOException {
		System.out.println("args:"+Arrays.toString(args));
		System.out.println("parseSmallToken():"+new Parser().parseSmallToken(args[0]));
		/* String workDir = System.getProperty("user.dir");
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
		
		System.out.println("Parsed Dates&Values: "+ parser.parseTsv(Path.of(workDir+"\\tsv.txt")));  */
	}
	
	public Set<LocalDate> parseFile(Path file) throws IOException {
		Set<LocalDate> dates = new TreeSet<LocalDate>();
		Charset charset = Charset.forName(UniversalDetector.detectCharset(file));
		List<String> lines = Files.lines(file, charset).map(l -> l.strip()).filter(l -> l.length() > 3).toList();
		
		for (String line : lines) {
			//---FIRST TRY full line
			var ld = parseSmallToken(line);
			if (ld != null) dates.add(ld);
			ld = null;
			//---SECOND TRY comma-separated duplicated, can delete
			/* if (line.split(",", -1).length-1 >= 1) {
				var st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					var tk = st.nextToken();
					dates.addAll(parseSmallToken(tk));
				}
			} */
			
			//---THIRD TRY space-separated
			var st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				ld = parseSmallToken(tk);
				if (ld != null) dates.add(ld);
				ld = null;
			}
			
			//---FOURTH TRY comma- or tab-separated
			st = new StringTokenizer(line, ",\t");
			while (st.hasMoreTokens()) {
				String tk = st.nextToken();
				ld = parseSmallToken(tk);
				if (ld != null) dates.add(ld);
			}
		}
		
		dates.removeIf(d -> Objects.isNull(d) || d.getYear() == 0);
		return dates;
	}
	
	//	PLS FIX: it works inconsistently if parser is reused!
	private int recursionHook = 0;
	public LocalDate parseSmallToken(String token) {
		// System.out.println("parsingTOP:"+token);
		token = token.strip();
		var tempDates = new TreeSet<LocalDate>();
		if (token.length() <= 6) return null;
		//1st and 2nd tries allow parsing from russian
		try {
			var ld = LocalDate.parse(token.toLowerCase(), dtf);
			if (ld != null) return ld;
			// tempDates.add(ld);
		} catch (java.time.format.DateTimeParseException e) {}
		
		if (tempDates.size() == 0) {
			try {
				var ld = LocalDate.parse(token.toLowerCase(), dtfAsIs);
				if (ld != null) return ld;
				// tempDates.add(ld);
			} catch (java.time.format.DateTimeParseException e) {}
		}
		
		// System.out.println("parsing:"+token);
		
		try {
			var tokenA = token.replace("-", " ");
			var ld = DateParserUtils.parseDateTime(token).toLocalDate();
			if (ld != null) {
				return ld;
				// System.out.printf("%s\t parsed from: %s\n",ld,token);
				// tempDates.add(ld);
			}
		} catch (java.time.format.DateTimeParseException e) {}
		if (tempDates.size() > 0)
		System.out.printf("%s\t parsed from: %s\n",tempDates,token);
		
		if (recursionHook>3) {
			recursionHook = 0;
			System.out.println("parser: return null :(");
			return null;
		}
		recursionHook++;
		var st = new StringTokenizer(token,"- \t");
		var sj = new StringJoiner(" ");
		while (st.hasMoreTokens()) {
			var tk = st.nextToken();
			if (tk.length() > 2) {
				try {
					tk = String.format("%04d" , Integer.parseInt(tk));
				} catch (Exception e) { }
			}
			sj.add(tk);
		}
		// System.out.println("sj:"+sj);
		return parseSmallToken(sj.toString());
		
	}
	
	public boolean isValidDate(String date) {
		var locDate = parseSmallToken(date);
		if (locDate == null || locDate.getYear() == 0) return false;
		else return true;
	}
	
	public Map<LocalDate, String> parseTsv(Path file) {
		try {
			Map<LocalDate, String> datesNames = new TreeMap<>();
			Charset charset = Charset.forName(UniversalDetector.detectCharset(file));
			List<String> lines = Files.lines(file, charset).map(l -> l.strip()).filter(l -> l.length() > 3).toList();
			
			for (String line : lines) {
				
				var st = new StringTokenizer(line, "\t");
				String dateStr = st.nextToken();
				LocalDate ld = parseSmallToken(dateStr);
				System.out.println("ld:"+ld+","+dateStr);
				String name = "";
				try {
					name = st.nextToken();
				} catch (Exception e) {}
				datesNames.put(ld, name);
				
			}
			// System.out.println("prsdatesNames.size():"+datesNames.size());
			return datesNames;
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to read from tab-separated file: "+ioe);
		}
	}
}