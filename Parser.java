package napominalka;

import org.mozilla.universalchardet.UniversalDetector;
import com.github.sisyphsu.dateparser.DateParserUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
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
		
		Set<LocalDate> dates = new HashSet<LocalDate>();
		var parser = new Parser();
		for (Path p : files) {
			dates.addAll(parser.parseFile(p));
		}
		System.out.println("Parsed dates: \n" + dates);
	}
	
	public Set<LocalDate> parseFile(Path file) throws IOException {
		Set<LocalDate> dates = new HashSet<LocalDate>();
		Charset charset = Charset.forName(UniversalDetector.detectCharset(file));
		
		List<String> lines = Files.lines(file, charset).toList();
		
		for (String line : lines) {
			// String[] tokens = line.split("[ \t\r\n]");
			LocalDateTime ldt = DateParserUtils.parseDateTime(curToken);
			if (ldt != null) dates.add(ldt.toLocalDate());
			
			var st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String curToken = st.nextToken();
				/* String compound = curToken;
				if (curToken.chars().anyMatch( Character::isDigit ) && st.hasMoreTokens()) {
					compound = curToken + st.nextToken();
				} */
				try {
					LocalDateTime ldt = DateParserUtils.parseDateTime(curToken);
					if (ldt != null) dates.add(ldt.toLocalDate());
				} catch (java.time.format.DateTimeParseException e) {}
				
			}
			
			
		}
		return dates;
	}
}