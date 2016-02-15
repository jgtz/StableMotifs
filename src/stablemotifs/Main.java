package stablemotifs;

import fileOperations.DeleteFile;
import java.util.ArrayList;
import java.util.Arrays;
import javax.script.ScriptException;


/**
 *
 * @author Jorge G. T. Zañudo

Copyright (c) 2013-2015 Jorge G. T. Zañudo and Réka Albert.
 
The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 

public class Main {
        
    public static void main(String[] args) throws ScriptException {
        
        String fileName=args[0];
        int maxCycleLength=0;
        int maxMotifSize=0;
        if(args.length>1){
            maxCycleLength=Integer.parseInt(args[1]);
            maxMotifSize=Integer.parseInt(args[2]);
        }    
        long startTime,estimatedTime;
        String[] simplifiedFunctions;
        String[] names;
        String networkName;
        String sinksFilename,transitionsFilename;
        ArrayList<String[]> finalAttractorsReduction;
        ArrayList<ArrayList<String[]>> resultNoDuplicates;
        ArrayList<String[]> unstableAttractorsFinal;
        ArrayList<String[]> stableMotifs;
        ArrayList<String[][]> attractors;
        ArrayList<String[]> attractorsResult;
        ArrayList<String[]> motifsAttractorsDiagram;
        String[] motifsToRemove;
        String[][] attractorsToMerge;
        
        Network nw;
        SuccessionDiagram succeDiag;
        
        networkName=fileName.split("\\.")[0];
        System.out.println("\nFilename: "+fileName);
        System.out.println("Creating Boolean table directory: "+networkName);
        ReadWriteFiles.createTablesFromBooleanRules(networkName, fileName);
        System.out.println("Boolean table directory created.");
        System.out.println("Creating functions and names files.");
        nw=OtherMethods.RecreateNetwork(networkName);
        nw.findNodeOutputs(); 
        names=nw.getNames();
        simplifiedFunctions=nw.getFunctions();
        System.out.println("Functions and names files created.");
        startTime = System.nanoTime();
        System.out.println("Performing network reduction...");
        if(args.length>1){
            attractors=NetworkReduction.FullNetworkReductionTop(names, simplifiedFunctions,networkName,maxCycleLength,maxMotifSize);
        }
        else{
            attractors=NetworkReduction.FullNetworkReductionTop(names, simplifiedFunctions,networkName);
        }
        System.out.println("Network reduction complete.");
        estimatedTime = System.nanoTime() - startTime;       
        System.out.println("Removing duplicate quasi-attractors.");
        attractorsResult=new ArrayList<String[]>();        
        for(int i=0;i<attractors.size();i++){attractorsResult.add(attractors.get(i)[1]);}
        resultNoDuplicates=NetworkReduction.removeDuplicateQuasiattractors(attractorsResult,names);
        finalAttractorsReduction=resultNoDuplicates.get(0);
        unstableAttractorsFinal=resultNoDuplicates.get(1);
        System.out.println("Total number of quasi-attractors: "+(finalAttractorsReduction.size()+unstableAttractorsFinal.size()));
        System.out.println("Number of putative quasi-attractors: "+unstableAttractorsFinal.size());
        System.out.println("Total time for finding quasi-attractors: "+((double) estimatedTime)/((double) 1000000000)+" s");
        System.out.println("Writing TXT files with quasi-attractors and stable motifs.");
        stableMotifs=ReadWriteFiles.getStableMotifsFromFileAndRemoveDuplicates("StableMotifs-"+networkName+".txt");
        ReadWriteFiles.writeStableMotifsAndQuasiAttractorFiles(networkName,names,stableMotifs,finalAttractorsReduction,unstableAttractorsFinal);
        DeleteFile.deletefile("StableMotifs-"+networkName+".txt");
        NetworkReduction.writeReducedNetwork(finalAttractorsReduction, networkName, "QA", names, simplifiedFunctions);
        NetworkReduction.writeReducedNetwork(unstableAttractorsFinal, networkName, "PQA", names, simplifiedFunctions);
        System.out.println("Starting analyis of stable motif succession diagram.");
        startTime = System.nanoTime();
        System.out.println("Identifying quasi-attractors corresponding to stable motif sequences.");
        motifsAttractorsDiagram=NetworkReduction.getAttractorsCorrespondingToMotifSequence(finalAttractorsReduction, names, networkName);
        ReadWriteFiles.writeAttractorsCorrespondingToMotifSequence(motifsAttractorsDiagram,networkName);
        DeleteFile.deletefile("DiagramSinks-"+networkName+".txt");
        sinksFilename=networkName+"-DiagramSequencesAttractors.txt";
        transitionsFilename="Diagram-"+networkName+".txt";
        if(OtherMethods.containsOscillatingMotifs(sinksFilename, transitionsFilename)){
            motifsToRemove=new String[0];
            attractorsToMerge=new String[0][0];          
            OtherMethods.simplifySequences(sinksFilename, transitionsFilename, motifsToRemove, attractorsToMerge);
            transitionsFilename="Diagram-"+networkName+"Modified.txt";
            sinksFilename=networkName+"-DiagramSequencesAttractorsModified.txt";
        }        
        succeDiag=new SuccessionDiagram (sinksFilename,transitionsFilename,names,simplifiedFunctions);
        if(unstableAttractorsFinal.isEmpty() && args.length==1){succeDiag.findControlSets();}
        else{succeDiag.findControlSetsWithPartialSuccessionDiagram();}
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Total time for finding stable motif control sets: "+((double) estimatedTime)/((double) 1000000000)+" s");
        System.out.println("Writing TXT files with stable motif control sets.");
        succeDiag.writeStableMotifControlSets(networkName+"-StableMotifControlSets.txt");
        System.out.println("Done!");
        

    }
   
}

    

