package napominalka;

// import org.slf4j.*;
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
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));
	private DateTimeFormatter dtfAsIs = DateTimeFormatter.ofPattern("d LLLL yyyy", Locale.forLanguageTag("ru"));
	
	public static void main(String[] args) throws IOException {
		System.out.println("args:"+Arrays.toString(args));
		if (args.length > 0)
			System.out.println("parseSmallToken():"+new Parser().parseSmallToken(args[0]));
		String[] testDataRu = {"1 января 2001","2 февраль 2002","3 Марта 903","4 апреля 0904"," 05 Май 905",
							"06 июня 0906", "07 июль 0403", "08 августа 20"};
		String[] testDataEng = {"1 jan 1991","2 february 1992", "03 march 1993", "4 April 1994"," 5 may 0995", "06 June 996",
							"7 july 23"};
		String[] testDataDig = {"1 1 1801", "2-2-1802","3-03-1803","04-04-1804", "05.5.1805",
							"1806.6.6", "1807-07-07", "1808 08 08", "1809.09.09", "10 10 100", "11.11.111"};
		for (int i = 0; i < testDataRu.length; i++) {
			System.out.printf("%s=\t%s\n",testDataRu[i],new Parser().parseSmallToken(testDataRu[i])); 
		}
		for (int i = 0; i < testDataEng.length; i++) {
			System.out.printf("%s=\t%s\n",testDataEng[i],new Parser().parseSmallToken(testDataEng[i])); 
		}
		for (int i = 0; i < testDataDig.length; i++) {
			System.out.printf("%s=\t%s\n",testDataDig[i],new Parser().parseSmallToken(testDataDig[i])); 
		}
	
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
	public LocalDate parseSmallToken(String token) {
		log.trace("parsingTOP:"+token);
		var st = new StringTokenizer(token,"- .");
		var sj = new StringJoiner(" ");
		int tokenCount = 0;
		boolean firstWasYear = false;
		while (st.hasMoreTokens()) {
			var tk = st.nextToken();
			tokenCount++;
			if (tokenCount==1 && tk.length()==4) firstWasYear = true;
			if (tk.length() > 2) {
				try {
					tk = String.format("%04d" , Integer.parseInt(tk));
				} catch (Exception e) {}
			}
			if (tokenCount == 3 && !firstWasYear) {
				try {
					tk = String.format("%04d" , Integer.parseInt(tk));
				} catch (Exception e) { }
			}
			
			sj.add(tk);
		}
		log.trace("sj:"+sj);
		token = sj.toString();
		
		// token = token.strip();
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
		
		log.trace("parsingMID:"+token);
		
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
		log.trace("%s\t parsed from: %s\n",tempDates,token);
		
		
		
		
		
		return null;
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
				log.trace("ld:"+ld+","+dateStr);
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