package com.jgaap.experiments;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * Separates the given document into specified smaller parts, making multiple
 * smaller documents from the original. Assumes .txt
 * 
 * @author Derek S. Prijatelj
 */
public class docPartition{
    

    protected static void multiPartition(int[] wordCount, String dir,
        String export){
        for (int i = 0; i < wordCount.length; i++){
            strict(wordCount[i], dir, export + "/wordCount" + wordCount[i], 0);
        }
    }

    protected static void multiPartition(int[] wordCount, String dir,
        String export, int maxParts){
        for (int i = 0; i < wordCount.length; i++){
            strict(wordCount[i], dir, export + "/wordCount" + wordCount[i],
                maxParts);
        }
    }

    /**
     * Strict partition of documents. Every partition has exactly the words
     * specified, otherwise is discarded from partition set.
     *
     * @param wordCount number of words to be in each partition
     * @param dirOfDocs path to directory of documents to be partitioned
     * @param exportPath relative path to export documents
     */
    protected static void strict(int wordCount, String dirOfDocs,
            String exportPath, int maxParts){
        if (maxParts <= 0){
            maxParts = Integer.MAX_VALUE - 1;
        }
        
        partition(wordCount, dirOfDocs, exportPath, true, maxParts);
    }

    /**
     * Strict partition of documents. Every partition has exactly the words
     * specified, otherwise is discarded from partition set.
     *
     * @param wordCount number of words to be in each partition
     * @param dirOfDocs path to directory of documents to be partitioned
     * @param exportPath relative path to export documents
     */
    protected static void lenient(int wordCount, String dirOfDocs,
            String exportPath, int maxParts){
        if (maxParts == 0){
            maxParts = Integer.MAX_VALUE - 1;
        }
        partition(wordCount, dirOfDocs, exportPath, false, maxParts);
    }

    /**
     * Find and access directory of documents
     * 
     * @param dirOfDocs relative path to directory of documents for partitioning
     * @return File object of directory of documents, null if path is not to a
     * directory.
     */
    private static File accessDir(String dirOfDocs){
        try{
            File dir = new File(dirOfDocs);
            
            if (!dir.isDirectory()){
                throw new NotADirectory(
                    "The submitted path is not a directory. Must be given a "
                    + "directory containing the plain text documents to be "
                    + "partitioned."
                    );
            } else {
                return dir;
            }
        } catch (NotADirectory e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Partitions the documents in the provided relative file path based on the
     * indicated word count per partiton and stores the resulting partitions
     * into the desired relative file export path. Assumes all separate files to
     * be partitioned are in their own directories by author.
     * 
     * @param wordCount number of words to be in each partition
     * @param dirOfDocs path to directory of documents to be partitioned
     * @param exportPath path to export documents
     * @param strict if true, strict partitioning occurs where every partition
     * has exactly the words specified and all partitions that have less words
     * are discarded. If false, documents are partitioned the same, but if a
     * partition has less words than indicated it will still be saved.
     */
    private static void partition(int wordCount, String dirOfDocs,
            String exportPath, boolean strict, int maxParts){
        File dir = accessDir(dirOfDocs);
        int parts, locAuthor;
        String fileName, fName, author;
        HashMap<String, Integer> numOfParts = new HashMap<>();
        HashMap<String, String> fileAuthor = new HashMap<>(); // TODO patch is ugly, restructure entire code to allow recursion and simplify this.
        File load = null;

        // loop through all files in dir, and execute partitionFile() if .txt
        for (final File file : dir.listFiles()){
            fileName = file.getName();

            if (fileName.equals("load.csv")){
                load = new File(file.getPath());
            } else if (file.isDirectory()){
                System.out.println("Working in dir: " + file.getName());

                // make dir for author

                for (final File f: file.listFiles()){
                    fName = f.getName();

                    System.out.println("file: " + fName);
                    
                    if (!f.isFile() || !fName.endsWith(".txt")){
                        continue;
                    } else {
                        
                        // make dir for file
                        
                        parts = partitionFile(wordCount, f,
                            exportPath +"/"+ file.getName() +"/"+ f.getName(),
                            strict, maxParts);

        //System.out.println("\n\n   *%*%*%*   "+f.getPath()+"\n\n");

                        numOfParts.put(f.getName(), parts);
                        locAuthor = f.getPath().lastIndexOf("Author");
                        author = f.getPath().substring(locAuthor, locAuthor+7);
                        fileAuthor.put(f.getName(), author);
                    }
                }
            }
        }
        
        System.out.println("Now createLoadCSV() . . .");
        createLoadCSV(load, numOfParts, exportPath, fileAuthor);
        System.out.println("newLoad.csv created!");
    }

    /**
     * partitions the given plain text file into multipl plain text files with
     * at maximum as many words in the file as specified
     *
     * @param wordCount max number of words in plain text file
     * @param file the file being partitioned
     * @param exportPath relative path to export the partitioned files
     * @param strict if strict, only files with exactly the specified amount of
     * words will be created.
     *
     * @return returns the number of partitions from file
     */
    private static int partitionFile(int wordCount, File file,
            String exportPath, boolean strict, int maxParts){

        File subDir = createSubDir(file, exportPath);
        int numWords = 0, subDocNum = 1;
        File subDoc = new File(subDir.getPath() + "_" + subDocNum + ".txt");
        boolean makingWord = false;
        int cint;
        char c;

        try {
            FileReader input = new FileReader(file);
            BufferedReader buffRead = new BufferedReader(input);

    System.out.println("subDoc = "+subDoc.getPath());

            PrintWriter writer = new PrintWriter(subDoc, "UTF-8");

            while((cint = buffRead.read()) != -1 && subDocNum < maxParts+1){

    //System.out.println("loop Reading File");

                c = (char)cint;
                writer.write(c);


                if (!Character.isWhitespace(c) && !makingWord){
                    makingWord = true;
                } else if (Character.isWhitespace(c) && makingWord){
                    numWords++;
                    makingWord = false;
                }

                if (numWords >= wordCount){
                    writer.close();
                    
                    subDocNum++;
                    if (subDocNum < maxParts+1){
                        subDoc = new File(subDir.getPath() + "_" + subDocNum
                            + ".txt");
                        writer = new PrintWriter(subDoc, "UTF-8");
                    }
                    numWords = 0;
                }
            }
            
            writer.close();
            buffRead.close();
            input.close();

            if (numWords != 0 && strict){

            System.out.println("\n     delete subDoc "+ subDoc.getName()+"\n");

                subDoc.delete();
                subDocNum--;
            } 
        } catch (IOException e){
            e.printStackTrace();
        }


        if (subDocNum < maxParts){
        System.out.println("    File partitioned into "+ subDocNum + " parts");
            return subDocNum;
        } else {
        System.out.println("    File partitioned into "+ (subDocNum-1) +" parts");
            return subDocNum-1;
        }
    }

    /**
     * Creates the sub directory and it's parent directory, if they do not
     * already exist.
     */
    private static File createSubDir(File file, String exportPath){
        File export = new File(exportPath.substring(0, exportPath.length()-4));

        if (!export.isDirectory()){
            export.mkdirs();
        }
        
        String name = file.getName();
        File subDir = new File(export, name.substring(0, name.length()-4));

        /* unneccesary and useless (error otherwise)
        if (!subDir.isDirectory()){
            subDir.mkdirs();
        }
        */

        //System.out.println("\n\n"+subDir.getPath());
        //System.out.println(export.getPath()+"\n\n");

        return subDir;
    }

    /**
     * Creates newLoad.csv file for JGAAP to use for accessing all files in
     * appropriate directories. May be only our situation specific, and not
     * general due to how it parses the orignal load.csv
     *
     * @param load file object of original load.csv
     * @param map of partitioned parent file with its number of partitions
     */
    private static void createLoadCSV(File load,
            HashMap<String, Integer> numOfParts, String exportPath,
            HashMap<String, String> fileAuthor){
        if (load == null){
            // create load.csv from scratch
            return;
        } 

        String name = load.getPath();
        File tmp = new File(
            //name.substring(0, name.lastIndexOf("\\")+1) + "newLoad.csv"
            exportPath + "/" + "newLoad.csv"
            );

    //System.out.println("\n\nexportPath = "+exportPath+"\n\n");

        try{
            FileReader in = new FileReader(load);
            BufferedReader bfr = new BufferedReader(in);

            PrintWriter out = new PrintWriter(tmp, "UTF-8");
            String line, path, filePart;
            String[] str;
            Integer parts;

            while((line = bfr.readLine()) != null){
                str = line.split(",");
                
                parts = null;

                for (Map.Entry<String, Integer> entry : numOfParts.entrySet()){
                    if (str[1].contains(entry.getKey())) {
                        parts = entry.getValue();
                        break;
                    }
                }

                //parts = numOfParts.get(str[1]);
                
                if (parts != null){
                    filePart = str[1].substring(str[1].lastIndexOf("\\")+1,
                        str[1].length()-4);
                    path = exportPath.replace('/', '\\') + "\\"
                        + fileAuthor.get(filePart+".txt") + "\\" 
                        + filePart;
                        
                        //str[1].substring(12, str[1].length()-4);
                    
                    //filePart = path.substring(path.lastIndexOf("\\")+1);
                    

        //System.out.println("\n\n"+ path);
        //System.out.println(filePart +"\n\n");


                    for (int i = 1; i < parts+1; i++){
                        str[1] = path + "\\" + filePart + "_" + i + ".txt";
                       
                        if (str.length == 3){
                            out.println(str[0] + "," + str[1] + "," + str[2]);
                        } else {
                            out.println(str[0] + "," + str[1] + ",");
                        }
                        /*
                        if (str[2].equals("")){
                            out.println(",");
                        } else {
                            out.println("");
                        }
                        */
                    }
                } else {
                    out.println(line);
                }
            }

            out.close();
            bfr.close();
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Command Line Interface
     */
    protected static void cli(String[] args){
        //int wordCount[] = {10000, 5000, 2500, 1000, 500, 250, 100};
        int wordCount[] = {1000};
        
        if (args.length == 2){
            multiPartition(wordCount, args[0], args[1]);
        } else if (args.length == 3){
            multiPartition(wordCount, args[0], args[1],
                Integer.parseInt(args[2]));
        }
    }
    public static void main(String[] args){
        cli(args); 

        // javac com/jgaap/experiments/docPartition.java com/jgaap/experiments/NotADirectory.java 

        // java com/jgaap/experiments/docPartition ../Texts/SciFi/ Texts/SciFi 1
       
        // arg[0]: dir to partition, arg[1] export dir, args[2] maxPartition docs
        // note it uses strict partition inherently
    }
}
