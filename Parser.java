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
import java.util.StringJoiner;
import java.util.Objects;
import java.util.stream.Collectors;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;


public class Parser {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"));
	private DateTimeFormatter dtfAsIs = DateTimeFormatter.ofPattern("d LLLL yyyy", Locale.forLanguageTag("ru"));
	
	public static void main(String[] args) throws IOException {
		System.out.printf("args:%s (%s)\n",Arrays.toString(args), args.length);
		if (args.length == 1 && args[0].equals("--test")) {
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
		} else if (args.length == 1) {
			System.out.println("parseSmallToken():"+new Parser().parseSmallToken(args[0]));
			System.exit(0);
		} else if (args.length == 2 && args[0].equals("--folder")) {
			var workDirPath = Path.of(args[1]);
			System.out.println("Directory to parse = " + workDirPath);
			// Files.list(workDirPath).forEach(line -> System.out.println(line));
			List<Path> files = Files.walk(workDirPath).filter(Files::isRegularFile).filter(Files::isReadable).toList();
			log.trace(""+files);
			
			var datesNames = new HashMap<LocalDate,String>();
			var parser = new Parser();
			for (Path p : files) {
				if (p.toString().endsWith(".xml")) {
					datesNames.putAll(parser.parseXmlFile(Files.newBufferedReader(p)));
				} 
				// else dates.addAll(parser.parseFile(p));
			}
			System.out.printf("Parsed dates: %s\n",datesNames.size());
			for (var entry : datesNames.entrySet()) {
				// System.out.printf("%s = %s\n",entry.getKey(), entry.getValue());
			}
			new Exporter().writeToFile(new TreeMap(datesNames), "famous.tsv");
		} else if (args.length == 2 && args[0].equals("--zip")) {
			var datesNames = new Parser().getAllFromDirOrZip(Path.of(args[1]));
			datesNames = new TreeMap<LocalDate, String>(datesNames);
			for (var entry : datesNames.entrySet()) {
				System.out.printf("%s = %s\n",entry.getKey(), entry.getValue());
			}
		}
		/* String workDir = System.getProperty("user.dir");
		Path workDirPath = Path.of(workDir);
		
		
		
		System.out.println("Parsed Dates&Values: "+ parser.parseTsv(Path.of(workDir+"\\tsv.txt")));  */
	}
	Map<LocalDate, String> getAllFromDirOrZip(Path pathRoot) throws IOException{
		final Map<LocalDate, String> datesNames = new HashMap<LocalDate, String>();
		if (pathRoot == null) return datesNames;
		List<Path> paths = new LinkedList<Path>();
		System.out.println("pathRoot:"+pathRoot);
		// FileSystems.newFileSystem(pathToZip, Collections.emptyMap())
		FileSystem fs = null;
		String contentType = Files.probeContentType(pathRoot);
		if (pathRoot.toFile().isDirectory()) {
			paths.add(pathRoot);
		} else if (contentType.endsWith("x-zip-compressed")||pathRoot.toString().endsWith(".docx")) {
			try {
				fs = FileSystems.newFileSystem(pathRoot);
				for (Path p : fs.getRootDirectories()) {
					paths.add(p);
				}
			} catch (IOException e) {e.printStackTrace();}
		} else paths.add(pathRoot);
		System.out.println("paths:"+paths);
		paths.forEach(root -> {
			// Files.walk(root).forEach(path -> Files.copy(path, toDirectory));
			try {
				Files.walk(root).forEach(path -> {
					try {
						String type = ""+Files.probeContentType(path);
						// System.out.println("probeContentType:\t\t\t\t\t\t\t\t"+Files.probeContentType(path));
						String pathStr = path.toString();
						// System.out.println("pathStr"+pathStr);
						if (type.endsWith("xml")) {
							try (var br = Files.newBufferedReader(path)) {
								datesNames.putAll(parseXmlFile(br));
							} catch (IOException e) {e.printStackTrace();}
							
						}
						else if (type.endsWith("x-zip-compressed")||pathStr.endsWith(".docx")) {
							
							datesNames.putAll(getAllFromDirOrZip(path));
							
						} 
						else if (type.startsWith("text")){
							
							datesNames.putAll(parseTextFile(path));
						}
					} catch (IOException e) {e.printStackTrace();}
					
				});
			} catch (IOException e) {e.printStackTrace();}
		});
		if (datesNames.size()>0) System.out.println("datesNames.size():"+datesNames.size());
		return datesNames;
	}
	
	private Map<LocalDate, String> parseTextFile(Path pathToFile) {
		log.warn("parsing file:"+pathToFile);
		Map<LocalDate, String> datesNames = new HashMap<LocalDate, String>();
		try {
			var ch1 = UniversalDetector.detectCharset(pathToFile);
			Charset ch2 = Charset.forName(ch1);
			Files.lines(pathToFile, ch2).forEach(line -> {
				var entry = parseLine(line);
				if (entry != null) datesNames.put(entry.getKey(), entry.getValue());
			});
		} catch (IOException e) {e.printStackTrace();}
		
		return datesNames;
	}
	
	private Map<LocalDate, String> parseXmlFile(BufferedReader br) {
		var localDatesNames = new HashMap<LocalDate, String>();
		var sb = new StringBuilder();
		try {
			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
		} catch (IOException e) {e.printStackTrace();}
		
		var stringList = sb.toString().replace(">", ">\n").lines().filter(s -> !s.startsWith("<"))
							.map(s -> s.replaceAll("<....>","")).toList();
		var sj = new StringJoiner(" ");
		for (String s : stringList) {
			
			sj.add(s.strip());
			// System.out.println(s);
		}
		// if (sj.length() > 200) System.out.println(sj.toString().substring(0,200));
		
		var st = new StringTokenizer(sj.toString(), ".;:");
		while (st.hasMoreTokens()) {
			var line = st.nextToken();
			// if (line.contains("1870")) log.debug("line:"+line);
			var entry = parseLine(line);
			if (entry != null)
				localDatesNames.put(entry.getKey(), entry.getValue());
		}
		// System.out.println("parseXmlFile:"+localDates);
		return localDatesNames;
	}
	
	public Map.Entry<LocalDate, String> parseLine(String line) {
		
		try {
			var st = new StringTokenizer(line);
			var ld = parseSmallToken(st.nextToken("\t"));
			if (ld != null) {
				var desc = st.nextToken("\t");
				log.debug("Fast-scanned:{} {}",ld,desc);
				return Map.entry(ld, desc);
			}
		} catch (Exception e) {}
		
		boolean logs = false;
		// if (line.contains("Петруччо")) logs = true;
		if (logs) log.debug("input:"+line);
		
		var localDatesNames = new HashMap<LocalDate, String>();
		var sc = new Scanner(line.strip());
		var firstSpottedYear = sc.findInLine("[0-9]{4}");
		// if (firstSpottedYear == null) return null;
		
		if (logs) log.debug("firstSpottedYear FOUND!:"+firstSpottedYear);
		sc = new Scanner(line.strip()).useDelimiter(",|\\p{javaWhitespace}");
		var tokensList = sc.tokens().toList();
		if (logs) log.debug("tokensList.size():"+tokensList.size());
		if (logs) log.debug("tokensList:"+tokensList);
		// LocalDate ld;
		// var localDates = new HashSet<LocalDate>();
		
		for (int i = 0; i < tokensList.size(); i++) {
			var token = tokensList.get(i);
			// if (logs) log.debug("token:"+token);
			if ((firstSpottedYear != null) && token.contains(firstSpottedYear)) {
				
				if (i >=2) {
					final String[] partWithDate = new String[3];
					partWithDate[0] = tokensList.get(i-2);
					partWithDate[1] = tokensList.get(i-1);
					partWithDate[2] = tokensList.get(i);
					var ld = parseSmallToken(String.format("%s %s %s",partWithDate[0],partWithDate[1],partWithDate[2]));
					if (ld != null) {
						
						// localDates.add(ld);
						// ld = null;
						String desc = new Scanner(line.strip()).tokens()
							.filter(s -> !s.equals(partWithDate[0]) && !s.equals(partWithDate[1]) && !s.equals(partWithDate[2]))
							.collect(Collectors.joining(" "));
						
						localDatesNames.put(ld, desc);
					}
				}
				if (i <= tokensList.size()-3) { 
					final String[] partWithDate = new String[3];
					partWithDate[0] = tokensList.get(i);
					partWithDate[1] = tokensList.get(i+1);
					partWithDate[2] = tokensList.get(i+2);
					if (logs) log.debug("ПОПЫТКА ПАРСИТЬ partWithDate:"+Arrays.toString(partWithDate));
					var ld = parseSmallToken(String.format("%s %s %s",partWithDate[0],partWithDate[1],partWithDate[2]));
					if (ld != null) {
						log.debug("ЗАПАРСИЛОСЬ:"+ld);
						// localDates.add(ld);
						// ld = null;
						String desc = new Scanner(line.strip()).tokens()
							.filter(s -> !s.equals(partWithDate[0]) && !s.equals(partWithDate[1]) && !s.equals(partWithDate[2]))
							.collect(Collectors.joining(" "));
						if (logs) log.debug("Описание:"+desc);
						localDatesNames.put(ld, desc);
					}
				}
				
				
				/* if (i >= 2)
					return parseSmallToken(String.format("%s %s %s",tokensList.get(i-2), tokensList.get(i-1), tokensList.get(i)));
				else if (i >= tokensList.size()-2)
					return parseSmallToken(String.format("%s %s %s",tokensList.get(i), tokensList.get(i+1), tokensList.get(i+2))); */
			}
			else if (token.matches(".*[0-9]{1,2}.*") && i <= tokensList.size()-3) {
				final String[] partWithDate = new String[3];
				partWithDate[0] = tokensList.get(i);
				partWithDate[1] = tokensList.get(i+1);
				partWithDate[2] = tokensList.get(i+2);
				// partWithDate[2] = firstSpottedYear == null ? tokensList.get(i+2) : firstSpottedYear;
				var tokenToParse = String.format("%s %s %s",partWithDate[0],partWithDate[1],partWithDate[2]);
				var tokenToParse2 = tokenToParse.replaceAll("[^\\p{javaLowerCase}\\p{javaUpperCase}\\d ]","");
				var ld = parseSmallToken(tokenToParse2);
				if (logs) log.debug("ПОПЫТКА ПАРСИТЬ:{} >>> {}",tokenToParse,tokenToParse2);
				if (ld == null && firstSpottedYear != null) {
					tokenToParse = String.format("%s %s %s",partWithDate[0],partWithDate[1],firstSpottedYear);
					tokenToParse2 = tokenToParse.replaceAll("[^\\p{javaLowerCase}\\p{javaUpperCase}\\d ]","");
					if (logs) log.debug("ПОПЫТКА ПАРСИТЬ:{} >>> {}",tokenToParse,tokenToParse2);
					ld = parseSmallToken(tokenToParse2);
				}
				if (ld != null) {
						// localDates.add(ld);
						// ld = null;
						log.debug("ЗАПАРСИЛОСЬ::{} {} >{}<",token,ld,line);
						String desc = new Scanner(line.strip()).tokens()
							.filter(s -> !s.equals(partWithDate[0]) && !s.equals(partWithDate[1]) && !s.equals(partWithDate[2]))
							.collect(Collectors.joining(" "));
						// log.debug("Описание:"+desc);
						if (desc.length()>1) localDatesNames.put(ld, desc);
				}
			} else if (logs) log.trace("проскочил:"+token);
		}
		if (localDatesNames.size()==1) {
			var entry = localDatesNames.entrySet().iterator().next();
			String cleanVal = entry.getValue().replaceAll("[^\\p{javaLowerCase}\\p{javaUpperCase} ,]","").replaceAll(" год","");
			cleanVal = cleanVal.replaceAll(",+$","").strip();
			cleanVal = cleanVal.replaceAll(",+$","").strip();
			localDatesNames.put(entry.getKey(), cleanVal);
		}
		if (logs) log.debug("map:"+localDatesNames);
		if (localDatesNames.size() == 1) return localDatesNames.entrySet().iterator().next();
		if (localDatesNames.size() > 1)
			log.warn("Map dismissed:"+localDatesNames.toString().substring(0,50));
		return null;
	}
	
	
	
	
	
	/* public Set<LocalDate> parseFile(Path file) throws IOException {
		Set<LocalDate> dates = new TreeSet<LocalDate>();
		Charset charset = Charset.forName(UniversalDetector.detectCharset(file));
		List<String> lines = Files.lines(file, charset).map(l -> l.strip()).filter(l -> l.length() > 3).toList();
		
		for (String line : lines) {
			//---FIRST TRY full line
			var ld = parseSmallToken(line);
			if (ld != null) dates.add(ld);
			ld = null;
			//---SECOND TRY comma-separated duplicated, can delete
			// if (line.split(",", -1).length-1 >= 1) {
				// var st = new StringTokenizer(line, ",");
				// while (st.hasMoreTokens()) {
					// var tk = st.nextToken();
					// dates.addAll(parseSmallToken(tk));
				// }
			// }
			
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
	} */
	
	
	
	public LocalDate parseSmallToken(String token) {
		// log.trace("input:"+token);
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
		if (tokenCount <= 2) return null;
		// log.trace("sj:"+sj);
		token = sj.toString();
		
		// token = token.strip();
		// var tempDates = new TreeSet<LocalDate>();
		if (token.length() <= 6) return null;
		//1st and 2nd tries allow parsing from russian
		try {
			var ld = LocalDate.parse(token.toLowerCase(), dtf);
			if (ld != null) {
				log.trace("Запарсилось:"+ld);
				return ld;
			}
			// tempDates.add(ld);
		} catch (java.time.format.DateTimeParseException e) {}
		
		
		try {
			var ld = LocalDate.parse(token.toLowerCase(), dtfAsIs);
			if (ld != null) { 
				log.trace("Запарсилось:"+ld);
				return ld;
			}
			// tempDates.add(ld);
		} catch (java.time.format.DateTimeParseException e) {}
		
		
		// log.trace("parsingMID:"+token);
		
		try {
			var tokenA = token.replace("-", " ");
			var ld = DateParserUtils.parseDateTime(token).toLocalDate();
			if (ld != null) {
				log.trace("Запарсилось:"+ld);
				return ld;
				// System.out.printf("%s\t parsed from: %s\n",ld,token);
				// tempDates.add(ld);
			}
		} catch (java.time.format.DateTimeParseException e) {}
		// if (tempDates.size() > 0)
		// log.trace("%s\t parsed from: %s\n",tempDates,token);
		
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