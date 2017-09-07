package com.jgaap.experiments;
/**
 * Given JGAAP log file, parses all useful data from log. Parser is highly
 * dependent based on in text qualities of the log file. Log must only have one
 * single method tested. Log is identified by its method
 *
 * @author Derek S. Prijatelj
 */

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public class LogData implements Comparable<LogData>{
	public class Tuple {
		public int rank;
		public String author;
		public double value;

		public Tuple() {}
		public Tuple(int rank, String author, double value) {
			this.rank = rank;
			this.author = author.trim();
			this.value = value;
		}
	}

	/**
	 * Stores all information of a single JGAAP test.
	 * 
	 * TODO This object should be the one to process all the data from the file
	 * in its own constructors for each test. Then the LogData strings the tests
	 * together neatly in an ArrayList. If there is an empty line after every
	 * test, this should be implementable. This may not be necessary though,
	 * only prettier.
	 * 
	 * TODO switch from ArrayList to TreeSet and sort Lexically
	 */
	public class TestData implements Comparable<TestData>{
		public String author;
		public String questionedDoc;
		public TreeSet<String> canonicizers;
		public TreeSet<EventDriver> eventDrivers;
		public TreeSet<String> analysis;
		public ArrayList<Tuple> results;

		public TestData() {}
		public TestData(String author, String docName) {
			this.author = author.trim();
			questionedDoc = docName.trim();
			canonicizers = new TreeSet<>();
			eventDrivers = new TreeSet<>();
			analysis = new TreeSet<>();
			results = new ArrayList<>();
		}
        public int compareTo(TestData o) {
            return questionedDoc.compareTo(o.questionedDoc);
        }
	}

	public class EventDriver implements Comparable<EventDriver>{
		public String name;
		public ArrayList<String> eventCullers;

		public EventDriver() {}
		public EventDriver(String name) {
			this.name = name.trim();
			eventCullers = new ArrayList<>();
		}
		public int compareTo(EventDriver o){
		    return name.compareTo(o.name);
		}
	}

	public String name; // either name of single log file or dir of logs.
	public TreeSet<TestData> tests = new TreeSet<>();

	/**
	 * Normal compareTo, but assumes that LogData is identified by its method
	 */
	public int compareTo(LogData o){
	    return printMethod().compareTo(o.printMethod());
	}
	
	public LogData() {}

	public LogData(String filePath) throws InvalidLogFileType, InvalidLogStructure, NotADirectory, ResultContainsNaN {
	    name = filePath.trim();
	    if (filePath.substring(filePath.lastIndexOf('.') ).equals(".txt")){
	        File log = new File(filePath);
	        parseBegin(log);
	    } else {
	        throw new InvalidLogFileType();
	    }
	}
	
	public LogData(File file) throws InvalidLogFileType, InvalidLogStructure, NotADirectory, ResultContainsNaN {
        name = file.getPath();
        if (file.getPath().substring(file.getPath().lastIndexOf('.') ).equals(".txt")){
            parseBegin(file);
        } else {
            throw new InvalidLogFileType();
        }
    }

	/**
	 * Given a log file, begins the parsing process if valid log file.
	 *
	 * @param logFile
	 *            Log file to be parsed.
	 */
	private void parseBegin(File logFile) throws InvalidLogFileType, InvalidLogStructure, ResultContainsNaN {
		try {
			Scanner sc = new Scanner(logFile);
			String line, author, docName;
			TestData test;
			// loops through all blank lines until all log tests processed
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				if (!line.isEmpty() && line.length() >= 3 && line.contains(" ")) {

					author = line.substring(0, line.lastIndexOf(' '));
					docName = line.substring(line.lastIndexOf(' ')+1);

					test = new TestData(author, docName);
					//tests.add(new TestData(author, docName));
					tests.add(test);
					line = sc.nextLine();

					if (line.length() >= 12 && (line.substring(0, 12)).equals("Canonicizers") && sc.hasNextLine()) {
						//parseCanonicizer(sc, tests.get(tests.size() - 1));
					    parseCanonicizer(sc, test); // last added
					} else {
						sc.close();
						throw new InvalidLogStructure();
					}
				} else if (!line.isEmpty()) {
					sc.close();
					throw new InvalidLogStructure();
				}
			}
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * TODO The means of handling and rethrowing an error may be incorrect
		 * here.
		 */
		catch (InvalidLogStructure e) {
			System.err.println("Error: Invalid Log Structure or Syntax");
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Parses the Canonicizer part of the current test in the log file. Updates
	 * the current TestData object in tests.
	 *
	 * @param sc
	 *            Scanner used to read the log file
	 * @param test
	 *            TestData object being added to
	 * 
	 *            TODO May lead to error if there exists any canonicizer that
	 *            starts with "EventDrivers"
	 */
	private void parseCanonicizer(Scanner sc, TestData test) throws IOException, InvalidLogStructure, ResultContainsNaN {

		String line = (sc.nextLine()).trim();

		while (!(line.length() >= 12 && (line.substring(0, 12)).equals("EventDrivers")) && !line.isEmpty()) {
			test.canonicizers.add(line);
			if (sc.hasNextLine())
				line = (sc.nextLine()).trim();
			else {
				sc.close();
				throw new InvalidLogStructure();
			}
		}

		if (line.length() >= 12 && (line.substring(0, 12)).equals("EventDrivers") && sc.hasNextLine()) {
			parseEventDrivers(sc, test);
		} else {
			sc.close();
			throw new InvalidLogStructure();
		}
	}

	private static boolean isEventCuller(String line) {
		return line.length() >= 9 && !(line.substring(9)).isEmpty() && (line.substring(9)).charAt(0) == ' ' && !(line.length() >= 8 && (line.substring(0, 8)).equals("Analysis"));
	}

	/**
	 * Parses the EventDriver part of the current test in the log file. Updates
	 * the current TestData object in tests.
	 *
	 * @param sc
	 *            Scanner used to read the log file
	 * @param test
	 *            TestData object being added to
	 *
	 *            TODO: Appropriately handle the hierarchy of data of Event
	 *            Handlers TODO May lead to error if there exists any
	 *            EventDrivers that starts with "Analysis"
	 */
	private void parseEventDrivers(Scanner sc, TestData test) throws IOException, InvalidLogStructure, ResultContainsNaN {

		// Uses the actual number of spaces to determine hierarchy.
		String line = sc.nextLine();
		/*
		 * EventDrivers object? cuz screw ugly potential arraylists?
		 * EventDriver: - name - Event Cullers ArrayList <String> ()
		 */
		// EventDriver ed = new EventDriver(line.trim());

		while (!(line.length() >= 8 && (line.substring(0, 8)).equals("Analysis")) && !line.isEmpty()) {
			if (sc.hasNextLine()) { // Event Driver

				EventDriver ed = new EventDriver(line.trim());
				if (sc.hasNextLine()) {
					line = sc.nextLine();
					while (isEventCuller(line)) {// Even Cull
						if (line.length() >= 17) {
							line = line.substring(17);
							if (line.charAt(0) == ' ') { // Specific Event
															// Culler
								ed.eventCullers.add(line.trim());
							}
						}
						// *
						if (sc.hasNextLine()) {
							line = sc.nextLine();
						} else {
							throw new InvalidLogStructure();
						}
						// */
					}
				} else {
					throw new InvalidLogStructure();
				}
				test.eventDrivers.add(ed);

			} else {
				System.out.println(line);
				sc.close();
				throw new InvalidLogStructure();
			}
		}

		// System.out.println(line);

		if (line.length() >= 8 && (line.substring(0, 8)).equals("Analysis") && sc.hasNextLine()) {
			parseAnalysis(sc, test);
		} else {
			sc.close();
			throw new InvalidLogStructure();
		}
	}

	/**
	 * Parses the Analysis part of the current test in the log file. Updates the
	 * current TestData object in tests.
	 *
	 * @param sc
	 *            Scanner used to read the log file
	 * @param test
	 *            TestData object being added to
	 */
	private void parseAnalysis(Scanner sc, TestData test) throws IOException, InvalidLogStructure, ResultContainsNaN {

		String line = (sc.nextLine()).trim();

		while (!(line.length() >= 2 && (line.substring(0, 2)).equals("1.")) && !line.isEmpty()) {
			test.analysis.add(line);
			if (sc.hasNextLine())
				line = (sc.nextLine()).trim();
			else {
				sc.close();
				throw new InvalidLogStructure();
			}
		}

		if (line.length() >= 2 && (line.substring(0, 2)).equals("1.")) {
			parseResults(sc, test, line);
		} else {
			// System.out.println("\n"+ name); // May not start with "1."
			sc.close();
			throw new InvalidLogStructure();
		}
	}

	/**
	 * Parses the analyzed results of the current test in the log file. Updates
	 * the current TestData object in tests. The results are in the order
	 * presented in the file.
	 *
	 * @param sc
	 *            Scanner used to read the log file
	 * @param test
	 *            TestData object being added to
	 */
	private void parseResults(Scanner sc, TestData test, String line) throws IOException, InvalidLogStructure, ResultContainsNaN {

		String[] strArr;
		do {
			// tokenize the content. 2nd token is author name, 3rd prob value
			strArr = line.split(" ");
			if (strArr[2].equals("NaN") || strArr[2].equals("Infinity")) {
				// TODO Check if strArr[2] result value is NaN, Throw Exception!
				throw new ResultContainsNaN();
			}
			test.results.add(new Tuple(Integer.parseInt(strArr[0].substring(0, strArr[0].length() - 1)), strArr[1], Double.parseDouble(strArr[2])));

			if (sc.hasNextLine())
				line = (sc.nextLine()).trim();
			else
				break;
		} while (!line.isEmpty());
	}

	/**
	 * Prints out the entire Log's data in order, as seen in the format of the
	 * actual log.
	 *
	 * EventDrivers will obviously be incorrect due to the above todo.
	 */
	/*
	public void print() {
		System.out.println("\nLog: " + name);
		for (int i = 0; i < tests.size(); i++) {
			System.out.println("\nAuthor: " + tests.get(i).author);
			
			System.out.println("\nQuestioned Document: " + tests.get(i).questionedDoc);

			System.out.println("Canonicizers:");
			for (String canonicizer: tests.get(i).canonicizers) {
				System.out.println("\t" + canonicizer);
			}

			System.out.println("EventDrivers:");
			for (EventDriver ed: tests.get(i).eventDrivers) {
				System.out.println("\t" + ed.name);
				if (!ed.eventCullers.isEmpty()) {
					System.out.println("\t\tEventCullers:");
				}
				for (String culler : ed.eventCullers) {
					System.out.println("\t\t\t" + culler);
				}
			}

			System.out.println("Analysis:");
			for (String ana : tests.get(i).analysis) {
				System.out.println("\t" + ana);
			}

			for (int j = 0; j < tests.get(i).results.size(); j++) {
				System.out.println((j + 1) + ". " + tests.get(i).results.get(j).author + " " + tests.get(i).results.get(j).value);
			}
		}
	}
	//*/
	
	/**
	 * Assumes all tests have the same exact linguistic analysis method
	 * 
	 * cannonicizers_eventDrivers_analysis
	 * each item in the three categories will be separated by & if
	 * multiple items.
	 * 
	 * returns string representation of method used
	 */
	public String printMethod(){
		String method = new String();
		
		// Canonicizers
		int i = 0, j = 0;
		int size = tests.first().canonicizers.size(), size2;
		for (String canonicizer : tests.first().canonicizers){
			method += canonicizer.replaceAll(",", "");
			if (i < size-1)
				method += "&";
			i++;
		}
		
		// EventDrivers and EventCullers
		size = tests.first().eventDrivers.size();
		
		if (size > 0)
	        method += "#";
		
		i = 0;
		for (EventDriver ed : tests.first().eventDrivers){
		    method += ed.name.replaceAll(",", "");
		    
		    size2 = ed.eventCullers.size();
		    if (size2 > 0){
                method += "*";
                
                j = 0;
                for (String culler: ed.eventCullers){
                    method += culler.replaceAll(",", "");
                    
                    if (j < size2-1)
                        method += "*";
                    j++;
                }
		    }
		    
			if (i < size-1)
				method += "&";
			i++;
		}

		// Analysis
		size = tests.first().analysis.size();
		
		if (size > 0)
			method += "#";
		
		i = 0;
		for (String ana : tests.first().analysis){
			method += ana.replaceAll(",", "");
			
			if (i < size-1)
				method += "&";
			i++;
		}
		
		return method;
	}
}