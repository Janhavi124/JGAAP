package com.jgaap.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;

//import com.google.common.io.Files;
import com.jgaap.JGAAPConstants;
import com.jgaap.backend.API;
import com.jgaap.backend.AnalysisDrivers;
import com.jgaap.backend.CSVIO;
import com.jgaap.backend.DistanceFunctions;
import com.jgaap.backend.EventCullers;
import com.jgaap.backend.EventDrivers;
import com.jgaap.backend.ExperimentEngine;
import com.jgaap.backend.ExperimentEngine.Experiment;
import com.jgaap.backend.Utils;
import com.jgaap.classifiers.CentroidDriver;
import com.jgaap.classifiers.WEKASMO;
import com.jgaap.distances.CosineDistance;
import com.jgaap.distances.IntersectionDistance;
import com.jgaap.distances.ManhattanDistance;
import com.jgaap.eventDrivers.CharacterEventDriver;
import com.jgaap.eventDrivers.CharacterNGramEventDriver;
import com.jgaap.eventDrivers.DefinitionsEventDriver;
import com.jgaap.eventDrivers.MNLetterWordEventDriver;
import com.jgaap.eventDrivers.NaiveWordEventDriver;
import com.jgaap.eventDrivers.POSNGramEventDriver;
import com.jgaap.eventDrivers.PartOfSpeechEventDriver;
import com.jgaap.eventDrivers.RareWordsEventDriver;
import com.jgaap.eventDrivers.VowelInitialWordEventDriver;
import com.jgaap.generics.AnalysisDriver;
import com.jgaap.generics.DistanceFunction;
import com.jgaap.generics.EventCuller;
import com.jgaap.generics.EventDriver;
import com.jgaap.generics.NeighborAnalysisDriver;
import com.jgaap.util.Document;

public class JGAAPTester {
	static API jgaap = API.getInstance();
	static String userdirectory = System.getProperty("user.dir");
	static String expTarget = userdirectory + "/logs4/";
	static String logTarget = userdirectory + "/logs4/";
	static PrintStream sys = System.out;
	static PrintStream exp;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		sys.println(userdirectory);
		sys.println("Working");
		// correlationExperiment();
		// createExperimentCSV(new
		// File("H:/Java/workspace/JGAAPMods/Texts/Mysteries"), .2);
		// createExperimentCSV(new
		// File("H:/Java/workspace/JGAAPMods/Texts/SciFi"), .2);

		
		//
		// // loadAAACProblem("A");
		//exp = new PrintStream((new File(expTarget + "exp.csv")));
		// exp.println("Experiments");
		//sanFranciscoMethods();
		//exp.close();
		// ExperimentEngine.runExperiment(expTarget + "expNGrams.csv",
		// "English"); //
		// log
		// // file
		// // generation
		// @SuppressWarnings("unchecked")
		// List<File> allFiles = new ArrayList<File>();
		// File dir = new File("tmp");
		// getFilesRecursive(dir, allFiles);
		// System.out.println(allFiles);
		// for (File f : allFiles)
		// Files.move(f, new File(logTarget + f.getName()));
		//MultiLog ml = new MultiLog(logTarget, "scifi", false);
		
		//Covariance.process(ml, true);
		System.out.println("Cov processed");
		//double[][] results = Correlation.process(ml);
		//double[] pVector = Correlation.pVector(ml);

		
		 //*****For Loading multilength experiment file
//		exp = new PrintStream((new File(expTarget + "exp.csv")));
//		List<File> files = new ArrayList<File>();
//		getFilesRecursive(new File("logs4"), files);
//		
//		for(int i=0; i<files.size(); i++){
//			File f = files.get(i);
//			if((f.getName()).contains("csv"))
//					files.remove(i--);
//		}
//		
//		multiLengthExperiments(files);
//		exp.close();
//		
		//BasicConfigurator.configure();
		//ExperimentEngine.runExperiment(expTarget+"/exp2.csv", "English");
		//renameAndMoveFiles("tmp");
		//getFilesRecursive(new File("logs4"), files);
        
        String logTarget = userdirectory + "/research/juloa/JGAAP/Texts";
		
		File mys = new File(logTarget+"/Mystery");
		File sci = new File(logTarget+"/SciFi");
		ArrayList<MultiLog> logs = new ArrayList<MultiLog>();
		
		for(File f:mys.listFiles()){
			logs.add(new MultiLog(f.getPath(), "Mys"+f.getName()));
		}
		
		for(File f:mys.listFiles()){
			logs.add(new MultiLog(f.getPath(), "Sci"+f.getName()));
		}
		
		System.out.println("data Processed");
		
		for(MultiLog ml: logs){
			//Correlation.process(ml);
			File csvFile = new File(ml.name + "_pvec.csv");
			PrintWriter pw = new PrintWriter(csvFile);
			// print header row
			String toWrite = Arrays.toString(Correlation.pVector(ml));
			pw.println(toWrite);
			pw.flush();
			pw.close();

            ml.exportCSV();

			Thread.sleep(10);
		
		}
		
	}
	
	private static void renameAndMoveFiles(String rootDir) throws IOException{
		List<File> files = new ArrayList<File>();
		getFilesRecursive(new File(rootDir), files);
		
		
		Scanner sc;
		for(File f:files){
			sc = new Scanner(f);
			String header = sc.nextLine();
			String match = Util.getFirstFound(header, "[0-9]+");
			
			int wCount = match==null? 0 : Integer.parseInt(match);
			
			System.out.println(wCount);
			String genre = header.contains("SciFi") ? "SciFi" : "Mystery";
			
			//Files.createDirectories(logTarget +"/"+genre+"/"+wCount+"/", null);
			
			
			File dest = new File(logTarget +"/"+genre+"/"+wCount+"/"+ f.getName());
			Files.copy(f.toPath(),dest.toPath());
		}
	}
	
	private static void runIndividualExp(String expDotCSV, String language) throws Exception{
		Scanner sc;
		List<List<String>> csv = null;
		try {
			sc = new Scanner(expDotCSV);
			csv = CSVIO.readCSV(expDotCSV);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Experiment> ls = new ArrayList<Experiment>();
		
		for(List<String> row:csv){
			String number = row.get(0);
			String[] canonicizers = row.get(1).trim().split("\\s*&\\s*");
			String[] events = row.get(2).trim().split("\\s*&\\s*");
			String analysis = row.get(3).trim();
			String distance = row.get(4).trim();	
			String documentsPath = row.get(5).trim();
			String fileName = ExperimentEngine.fileNameGen(Arrays.asList(canonicizers), events, analysis
					+ (distance.isEmpty() ? "" : "-" + distance), "exp1", number);

			ls.add(new Experiment(canonicizers, events, analysis,distance, documentsPath, fileName));
		}
		
		for(Experiment exp:ls)
			exp.call();
		
	}

	private static void multiLengthExperiments(List<File> loadDotCSVs) {

		ArrayList<EventDriver> eds = new ArrayList<EventDriver>();
		eds.add(new NaiveWordEventDriver());
		eds.add(new PartOfSpeechEventDriver());
		eds.add(new CharacterEventDriver());
		eds.add(new VowelInitialWordEventDriver());

		EventDriver ed = new POSNGramEventDriver();
		ed.setParameter("N", 2);

		eds.add(ed);

		ed = new MNLetterWordEventDriver();
		ed.setParameter("M", 3);
		ed.setParameter("N", 4);

		eds.add(ed);

		List<DistanceFunction> dfs = new ArrayList<DistanceFunction>();
		dfs.add(new IntersectionDistance());
		dfs.add(new CosineDistance());
		//dfs.add(new KendallCorrelationDistance());

		String canon = ",Normalize Whitespace" + '&' + "Unify Case,";

		AnalysisDriver ad = new CentroidDriver();
		
		int i=1;
		
		for(File f:loadDotCSVs){
			for (DistanceFunction df : dfs){
				for (EventDriver evd : eds) {
					String eventString = evd.displayName();
					if (evd instanceof POSNGramEventDriver)
						eventString += "|N:" + evd.getParameter("N");
					if (evd instanceof MNLetterWordEventDriver)
						eventString += "|N:" + evd.getParameter("N") + "|M:" + evd.getParameter("M");
	
					exp.println(i++ +canon + eventString + "," + ad.displayName() + ","
							+ df.displayName() + "," + f.getPath());
				}
			}
		}

	}
	/*
	private static void correlationExperiment() throws IOException {

		// BasicConfigurator.configure();
		// ExperimentEngine.runExperiment(expTarget+"/exp.csv", "English"); //
		// log
		// file
		// generation
		// @SuppressWarnings("unchecked")
		// List<File> allFiles = new ArrayList<File>();
		// File dir = new File("H:/Java/workspace/JGAAPMods/tmp");
		// getFilesRecursive(dir, allFiles);
		// System.out.println(allFiles);
		// for (File f : allFiles)
		// Files.move(f, new File(logTarget + f.getName()));
		MultiLog ml = new MultiLog(logTarget, "scifi", false);
		double[][] results = Covariance.process(ml);

	}
	//*/

	private static void sanFranciscoMethods() throws FileNotFoundException {
		int[] ns = { 3, 4, 6, 8 };
		// String canonicizers = "Normalize Whitespace&Unify Case";

		ArrayList<EventDriver> eds = new ArrayList<EventDriver>();
		eds.add(new NaiveWordEventDriver());
		eds.add(new RareWordsEventDriver());
		eds.add(new CharacterEventDriver());
		eds.add(new CharacterNGramEventDriver());
		for (int i = 0; i < 4; i++) {
			CharacterNGramEventDriver ed = new CharacterNGramEventDriver();
			ed.setParameter("N", ns[i]);
			eds.add(ed);
		}
		List<DistanceFunction> dfs = new ArrayList<DistanceFunction>();
		dfs.add(new IntersectionDistance());
		dfs.add(new CosineDistance());
		dfs.add(new ManhattanDistance());
		int count = 0;
		AnalysisDriver cd = new CentroidDriver();
		AnalysisDriver smo = new WEKASMO();
		for (EventDriver ed : eds) {
			for (DistanceFunction df : dfs) {
				String eventString = ed.displayName()
						+ (ed instanceof CharacterNGramEventDriver ? "|N:" + ed.getParameter("N") : "");
				exp.println(",Normalize Whitespace" + '&' + "Unify Case," + eventString + "," + cd.displayName() + ","
						+ df.displayName() + "," + "H:/Java/workspace/JGAAPMods/Texts/Scifi/load.csv");
			}
		}

		for (EventDriver ed : eds) {
			String eventString = ed.displayName()
					+ (ed instanceof CharacterNGramEventDriver ? "|N:" + ed.getParameter("N") : "");
			exp.println(",Normalize Whitespace & Unify Case," + ed.displayName() + eventString + "," + smo.displayName()
					+ ",," + "H:/Java/workspace/JGAAPMods/Texts/SciFi/load.csv");
		}

	}

	private static File createExperimentCSV(File expDirectory, double testRatio) throws FileNotFoundException { // create

		File[] authors = expDirectory.listFiles();
		File f;
		PrintStream output = new PrintStream(f = new File(expDirectory.getPath() + "/load.csv"));

		for (File author : authors) {
			String cor = author.getName();
			File[] works = author.listFiles();
			for (int i = 0; i < works.length; i++) {
				String path = works[i].getPath().replace(",", "");
				if (i < works.length * testRatio)
					output.println("," + path + ", Correct: " + cor);
				else
					output.println(cor + "," + path + ",");
			}
		}

		return f;
	}

	private static void getFilesRecursive(File pFile, List<File> list) {
		System.out.println(pFile.getName()+pFile.listFiles().length);
		for (File files : pFile.listFiles()) {
			if (files.isDirectory()) {
				getFilesRecursive(files, list);
			} else {
				list.add(files);
			}
		}
	}

	private static void tryAllAnalyzers() {
		// int load = 5;
		List<EventDriver> eds = EventDrivers.getEventDrivers();// .subList(0,load);
		List<AnalysisDriver> ads = AnalysisDrivers.getAnalysisDrivers();// .subList(0,load);
		List<DistanceFunction> dfs = DistanceFunctions.getDistanceFunctions();// .subList(0,load);
		List<EventCuller> ecs = EventCullers.getEventCullers();// .subList(0,load);
		int i = 0;
		for (AnalysisDriver ad : ads) {
			if (ad instanceof NeighborAnalysisDriver)
				for (DistanceFunction df : dfs) {
					for (EventDriver ed : eds) {
						for (EventCuller ec : ecs) {
							try {
								exp.println("exp # " + (i++) + "-, " + "Normalize Whitespace&Unify Case,"
										+ ed.displayName() + "#" + ec.displayName() + "," + ad.displayName() + ","
										+ df.displayName() + "," + JGAAPConstants.JGAAP_RESOURCE_PACKAGE
										+ "aaac/problem" + "A" + "/load" + "A" + ".csv");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			else {
				for (EventDriver ed : eds) {
					for (EventCuller ec : EventCullers.getEventCullers()) {
						try {
							exp.println("exp # " + (i++) + "-, " + "Normalize Whitespace&Unify Case," + ed.displayName()
									+ "#" + ec.displayName() + "," + ad.displayName() + ","
									+ JGAAPConstants.JGAAP_RESOURCE_PACKAGE + "aaac/problem" + "A" + "/load" + "A"
									+ ".csv");
							// go();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private static void tryEachEventDriver(List<EventDriver> eds) {
		for (EventDriver ed : eds) {
			jgaap.addEventDriver(ed);
			for (EventCuller ec : EventCullers.getEventCullers()) {
				jgaap.addEventCuller(ec, ed);
				try {
					go();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jgaap.removeEventCuller(ec);
			}
		}
	}

	private static void tryAllSetsOfEventDrivers(List<EventDriver> eds) {
		if (eds.isEmpty() || jgaap.getEventDrivers().size() > 5) {
			long start = System.currentTimeMillis();
			try {
				go();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (!eds.isEmpty()) {
			tryEachEventDriver(eds.subList(1, eds.size()));
			EventDriver ed = eds.get(0);
			if (!(ed instanceof DefinitionsEventDriver))
				jgaap.addEventDriver(ed);
			for (EventCuller ec : EventCullers.getEventCullers()) {
				jgaap.addEventCuller(ec, ed);
				tryEachEventDriver(eds.subList(1, eds.size()));
				jgaap.removeEventCuller(ec);
			}
			jgaap.removeEventDriver(ed);

		}
	}

	private static void go() throws Exception {

	}

	private static void loadAAACProblem(String problem) {
		String filepath = JGAAPConstants.JGAAP_RESOURCE_PACKAGE + "aaac/problem" + problem + "/load" + problem + ".csv";
		List<Document> documents = Collections.emptyList();
		try {
			documents = Utils.getDocumentsFromCSV(CSVIO.readCSV(com.jgaap.JGAAP.class.getResourceAsStream(filepath)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Document document : documents) {
			jgaap.addDocument(document);
		}
		// UpdateKnownDocumentsTree();
		// UpdateUnknownDocumentsTable();

	}

	private void loadSciFiNovels() throws Exception {
		List<Document> docs = new ArrayList<Document>();
		File[] authorX = (new File("E:/Java/workspace/JGAAPMods/docs/SciFi/AuthorX")).listFiles();
		File[] authorY = (new File("E:/Java/workspace/JGAAPMods/docs/SciFi/AuthorY")).listFiles();
		for (int i = 0; i < authorX.length; i++) {
			if (i <= authorX.length / 2)
				docs.add(new Document(authorX[i].getAbsolutePath(), "Azimov"));
			else
				docs.add(new Document(authorX[i].getAbsolutePath(), ""));

		}
		for (int i = 0; i < authorY.length; i++) {
			if (i < authorY.length / 2)
				docs.add(new Document(authorY[i].getAbsolutePath(), "Heinlein"));
			else
				docs.add(new Document(authorY[i].getAbsolutePath(), ""));
		}
		for (Document d : docs) {
			jgaap.addDocument(d);
		}
	}
	
	public static class Util {
		  
		  public static String getFirstFound(String contents, String regex) {
		    List<String> founds = getFound(contents, regex);
		    if (isEmpty(founds)) {
		        return null;
		    }
		    return founds.get(0);
		}
		  public static List<String> getFound(String contents, String regex) {
		      if (isEmpty(regex) || isEmpty(contents)) {
		          return null;
		      }
		      List<String> results = new ArrayList<String>();
		      Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
		      Matcher matcher = pattern.matcher(contents);
		      
		      while (matcher.find()) {
		          if (matcher.groupCount() > 0) {
		              results.add(matcher.group(1));
		          } else {
		              results.add(matcher.group());
		          }
		      }
		      return results;
		  } 
		  public static boolean isEmpty(List<String> list) {
		    if (list == null || list.size() == 0) {
		        return true;
		    }
		    if (list.size() == 1 && isEmpty(list.get(0))) {
		        return true;
		    }
		    return false;
		}
		  public static boolean isEmpty(String str) {
		      if (str != null && str.trim().length() > 0) {
		          return false;
		      }
		      return true;
		  }
		}


}
