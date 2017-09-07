package com.jgaap.experiments;
/**
 * Given a file path to a directory, loops through all files and converts
 * all valid JGAAP docs into an ArrayList of LogData objects stored along
 * with the name of the grouping of logs, either the dir name or given name.
 *
 * @author Derek S. Prijatelj
 *
 * TODO The Logs are NOT in order as expected. they go by 0-9 of first, but then
 * if it has any other numbers following it, they are processed first. So 10
 * comes right after 1, and before the teens and all that before 2. This is due
 * to how the files are read in by the machine.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;

import com.jgaap.experiments.LogData.TestData;

import java.lang.IndexOutOfBoundsException;
import java.text.Collator;

class MultiLog {
	public String name;
	public ArrayList<LogData> logs = new ArrayList<>();

	public MultiLog() {
	}

	public MultiLog(String pathToDir) {
		this(pathToDir, "");
	}

	public MultiLog(String pathToDir, String name) {
		//this(pathToDir, name, false);
	    try{
	        File mlDir = new File(pathToDir);
	        if (!mlDir.isDirectory()) {
	            throw new NotADirectory();
	        }
	        if (name.isEmpty()) {
	            this.name = mlDir.getName().trim();
	        } else {
	            this.name = name.trim();
	        }
	        
	        ArrayList <File> files = new ArrayList<>();
	        getFilesRecursive(mlDir, files);
	        
	        for (File file:files){
	            logs.add(new LogData(file));
	        }
	        
	        Collections.sort(logs); // causes problems if  not single dir of .txts
        } catch(NotADirectory | InvalidLogFileType | InvalidLogStructure
                | ResultContainsNaN e){
            e.printStackTrace();
        }
	}
	
	/*
	public void print() {
		System.out.println("\nMultiLog: " + name);
		for (int i = 0; i < logs.size(); i++) {
			logs.get(i).print();
			System.out.println();
		}
	}
	//*/

   /**                                                                         
     * Exports a csv table of MLog's methods individual binary success/failure
     * authorship attribution for each test document.
     * 
     * TODO Need to ensure the data represented on in the CSV is correct, as in
     * the data 
     */
    public void exportCSV() {
        try {
            File csvFile;
            if (name.isEmpty()){
                csvFile = new File("UnnamedMultiLog_binVotes.csv");
            } else {
                csvFile = new File(name + "_binVotes.csv");
            }
            PrintWriter pw = new PrintWriter(csvFile);

            int testsSize = logs.get(0).tests.size(); // #methods
            int resultsSize = logs.get(0).tests.first().results.size(); // #testdocs
            int methodCount = logs.size();
            String logName;
            HashSet<String> logSuffix = new HashSet<>();
            logSuffix.add(logs.get(0).tests.first().questionedDoc);
            
            
            // print header row (Methods) based on first log's methods
            pw.print(name + ",");
            for (int i = 0; i < logs.size(); i++) {
                logName = logs.get(i).tests.first().questionedDoc;
                
                /*
                if (methodCount == 0 && logSuffix.add(logName)){
                    
                    System.out.println("logSuffix " + logSuffix.toString());
                    
                    methodCount = i;
                    break;
                }
                */
                
                pw.print(logs.get(i).printMethod());
                if (i < logs.size() - 1)
                    pw.print(",");
            }
            pw.println();
            
            System.out.println("logs = " + logs.size());
            System.out.println("methodCount = " + methodCount);
            System.out.println("tests = " + testsSize);
            System.out.println("results = " + resultsSize);
            
            String qDoci, qDocCurrent, methodi,methodCurrent;
            //methodCurrent = logs.get(0).printMethod();

            ArrayList<TestData> testDoc;
            
            // Print by row.
            int i, j = 0;
            for (TestData test : logs.get(0).tests){
                pw.print(test.questionedDoc + ","); // row header
                
                qDocCurrent = test.questionedDoc;
                
                i = 0;
                for (LogData method : logs){
                    testDoc = new ArrayList<TestData>(method.tests);
                    
                    qDoci = testDoc.get(j).questionedDoc;
                    methodi = method.printMethod();
                    
                    /*
                    System.out.println("Size of testDoc.results: " + testDoc.get(j).results.size());
                    System.out.println("qDoci = " + qDoci);
                    System.out.println(methodi + "\n");
                    //*/
                    
                    // Error check for reliable data representation in table
                    //*
                    if (!qDocCurrent.equals(qDoci)){
                        System.err.println("Error: questionedDocs do not "
                                + "match!\n"
                                + qDocCurrent + " != "
                                + qDoci
                                );
                    }
                    /*
                    if (!methodCurrent.equals(methodi)){
                        System.err.println("Error: methods of docs do not "
                                + "match!\n"
                                + methodCurrent + "\n!=\n"
                                + methodi + "\n"
                                );
                    }
                    //*/
                    
                    pw.print(isCorrect(testDoc.get(j))? '1':'0');
                    if (i < methodCount - 1)
                        pw.print(",");
                    i++;
                }
                
                pw.println();
                j++;
            }
            
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if correct author is first in results of test in specific log
     * 
     * @param i log number
     * @param t test number
     * @return true if the correct author to questioned doc is ranked first
     */
    /*
    private boolean isCorrect(int i, int t){
        String s1[] = logs.get(i).tests.get(t).author.trim().split(" ");
        String s2 = logs.get(i).tests.get(t).results.get(0).author;
        if (s1.length <= 1) {
            //if (!defectFiles.contains(ml.logs.get(i).name))
            //   defectFiles.add(ml.logs.get(i).name);
            return false;
        }

        if (s2.contains(" ")) {
            s2 = (s2.split(" "))[0];
        }

        return s1[1].equals(s2) && logs.get(i).tests.get(t).results.get(0).rank
                != logs.get(i).tests.get(t).results.get(1).rank;
    }
    //*/
    private boolean isCorrect(TestData t){
        String s1[] = t.author.trim().split(" ");
        String s2 = t.results.get(0).author;
        if (s1.length <= 1) {
            //if (!defectFiles.contains(ml.logs.get(i).name))
            //   defectFiles.add(ml.logs.get(i).name);
            return false;
        }

        if (s2.contains(" ")) {
            s2 = (s2.split(" "))[0];
        }

        return s1[1].equals(s2) && t.results.get(0).rank
                != t.results.get(1).rank;
    }
    
    private static void getFilesRecursive(File pFile, ArrayList<File> list) {
        //System.out.println(pFile.getName()+pFile.listFiles().length);
        for (File files : pFile.listFiles()) {
            if (files.isDirectory()) {
                getFilesRecursive(files, list);
            } else if (files.getName().substring(
                    files.getName().lastIndexOf('.') ).equals(".txt")){
                list.add(files);
            }
        }
    }

    public static void main(String args[]){
        int size[] = {0, 100, 250, 500, 1000, 2500, 5000, 10000};
        MultiLog ml;
        
        /*
        ml = new MultiLog("../../SciFi/0", "Sci_0");
        ml.exportCSV();
        //*/
        //*
        for (int i : size){
            System.out.println(i);
            //
            ml = new MultiLog("../../SciFi/" + i, "Sci_" + i);
            ml.exportCSV();
            
            ml = new MultiLog("../../Mystery/" + i, "Mys_" + i);
            ml.exportCSV();
            //
        }
        //*/
    }
}
