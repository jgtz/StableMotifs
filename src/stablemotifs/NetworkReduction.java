package stablemotifs;

import fileOperations.DeleteFile;
import fileOperations.FileToRead;
import fileOperations.FileToWrite;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import javax.script.ScriptException;
import quinemccluskeyalgorithm.Formula;


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

public class NetworkReduction {
    

    public static ArrayList<String[][]> FullNetworkReductionTop(String [] names, String[] functions, String networkName){
        
        int[] stabilizedNodes,notStabilizedNodes,sourceNodes,unaffectedNodes,binaryArray;
        String[] oldNames,newNames,oldFunctions,newFunctions,resultDummy,resultDummy2,originalFunctions,motifs;
        String[][] arrayFunctions,arrayDummy;
        Object[] obj;
        int index;
        String data2,data,line,currentReduction,currentSource="",reduction="";
        ArrayList<String[][]> attractors,dummyArray;
        ExpandedNetwork exp;
        Map<String, String> map;
        ArrayList<String> nameList=new ArrayList<String>(Arrays.asList(names));
        ArrayList<Integer> sources=new ArrayList<Integer>();
        boolean bool,bool2,bool3;
        File file =new File("StableMotifs-"+networkName+".txt"); 
        File file2 =new File("Diagram-"+networkName+".txt");
        File file3 =new File("DiagramSinks-"+networkName+".txt");
        FileWriter fileWritter = null;
        FileWriter fileWritter2= null;
        FileWriter fileWritter3= null;
        BufferedWriter bufferWritter,bufferWritter2,bufferWritter3;
        if(new File("StableMotifs-"+networkName+".txt").exists()){
            DeleteFile.deletefile("StableMotifs-"+networkName+".txt");
        }
        if(new File("Diagram-"+networkName+".txt").exists()){
            DeleteFile.deletefile("Diagram-"+networkName+".txt");
        }
        if(new File("DiagramSinks-"+networkName+".txt").exists()){
            DeleteFile.deletefile("DiagramSinks-"+networkName+".txt");
        }
        
        
        if(!file.exists()){ //if files dont exist, then create them
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file2.exists()){
        try {
            file2.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file3.exists()){
        try {
            file3.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
        originalFunctions=Arrays.copyOf(functions, functions.length);
        attractors=new ArrayList<String[][]>();
        
        for(int i=0;i<names.length;i++){
            if(originalFunctions[i].replace("("," ").replace(")"," ").trim().equals(names[i])){
                sources.add(i);
            }       
        }
        if(sources.size()>0){
            System.out.println("The number of source nodes without specified states is "+sources.size());
        }
        
        for(int m=0;m<(int) (Math.pow(2, sources.size())+0.1);m++){
            functions=Arrays.copyOf(originalFunctions, originalFunctions.length);
            if(sources.size()>0){
                binaryArray=new int[sources.size()];
                OtherMethods.intToBinary(m, binaryArray);                
                for(int l=0;l<sources.size();l++){
                    functions[sources.get(l)]=binaryArray[l]+"";
                }
                System.out.print("Source combination "+(m+1)+"/"+(int) (Math.pow(2, sources.size())+0.1)+":\t");
                currentSource="(";
                for(int l=0;l<sources.size();l++){
                    System.out.print(names[sources.get(l)]+"="+functions[sources.get(l)]+"\t");
                    if(l==0){currentSource=currentSource+names[sources.get(l)]+"="+functions[sources.get(l)];}
                    else{currentSource=currentSource+"-"+names[sources.get(l)]+"="+functions[sources.get(l)];}
                }
                System.out.print("\n");
                currentSource=currentSource+")";
            }
            
            oldNames=Arrays.copyOf(names, names.length);
            oldFunctions=Arrays.copyOf(functions, functions.length);
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
//                for(int i=0;i<oldFunctions.length;i++){
//                    System.out.println(oldNames[i]+"*="+oldFunctions[i]);
//                    oldFunctions[i]=createAndSimplifyRule(oldNames,ExpandedNetwork.createRegulatorsArray(oldFunctions[i])); 
//                }
                oldFunctions=createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                sourceNodes=(int []) obj[4];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                for(int i=0;i<sourceNodes.length;i++){
                    index=nameList.indexOf(oldNames[sourceNodes[i]]);
                    functions[index]=newFunctions[sourceNodes[i]];
                }

                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. In this case, we assign the final
                //form of the rules to the functions array
                if(unaffectedNodes.length==newNames.length){
                    for(int i=0;i<unaffectedNodes.length;i++){
                        index=nameList.indexOf(oldNames[unaffectedNodes[i]]);
                        functions[index]=newFunctions[unaffectedNodes[i]];
                    }
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);



            } while(unaffectedNodes.length!=oldNames.length);
            map = new HashMap<String, String>();
            for(int i=0;i<oldNames.length;i++){
                map.put(oldNames[i], oldFunctions[i]);
            }
            Arrays.sort(oldNames);
            for(int i=0;i<oldNames.length;i++){
                oldFunctions[i]=map.get(oldNames[i]);
            }
            exp=new ExpandedNetwork(oldNames, oldFunctions);
            exp.findStronglyConnectedComponets();       
            System.out.println("Finding stable motifs in this network...");
            exp.findStableStronglyConnectedComponets();
            bool=exp.getStableStronglyConnectedComponents();
            exp.findOscillations();
            exp.getOscillationsGA();
            arrayFunctions=exp.getNewFunctions();
                   
            if(bool){
                try {fileWritter = new FileWriter(file.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter = new BufferedWriter(fileWritter);
                try {fileWritter2 = new FileWriter(file2.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter2 = new BufferedWriter(fileWritter2);
                
                if(exp.getNumberOfStableMotifs()>1){
                    System.out.println("There are "+exp.getNumberOfStableMotifs()+" stable motifs in this network: ");
                }
                else if (exp.getNumberOfStableMotifs()==1){System.out.println("There is "+exp.getNumberOfStableMotifs()+" stable motif in this network: ");}
                else{
                    System.out.println("There are no stable motifs in this network.");
                    if(!currentSource.equals("")){
                        currentReduction=currentSource+"\n";    
                    }
                    else{currentReduction="";}
                    try {fileWritter3 = new FileWriter(file3.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter3 = new BufferedWriter(fileWritter3);
                    try {bufferWritter3.write(currentReduction);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                }
                data="";
                data2="";
                index=1;
                motifs=new String[arrayFunctions.length];
                for(int i=0;i<arrayFunctions.length;i++){
                    bool3=false;
                    line="";
                    motifs[i]="";
                    for(int j=0;j<arrayFunctions[i].length;j++){
                        if(arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("0") || arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("1")){
                            data=data+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            line=line+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            bool3=true;
                            if(motifs[i].equals("")){motifs[i]="("+oldNames[j]+"="+arrayFunctions[i][j];}
                            else{motifs[i]=motifs[i]+"-"+oldNames[j]+"="+arrayFunctions[i][j];}
                        }   
                    }
                    if(bool3){
                        System.out.println(index+"/"+exp.getNumberOfStableMotifs()+"\t"+line);
                        data=data+"\n";
                        motifs[i]=motifs[i]+")";
                        if(!currentSource.equals("")){
                            data2=data2+currentSource+"\t"+currentSource+" "+motifs[i]+"\n";                  
                        }
                        index++;
                    }
                }
                try {bufferWritter.write(data);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.write(data2);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}

                if(arrayFunctions.length-exp.getNumberOfStableMotifs()>0){
                    if(arrayFunctions.length-exp.getNumberOfStableMotifs()>1){
                        System.out.println("There are "+(arrayFunctions.length-exp.getNumberOfStableMotifs())+" oscillating motifs in this network that could display unstable or incomplete oscillations.");
                    }
                    else{
                        System.out.println("There is "+(arrayFunctions.length-exp.getNumberOfStableMotifs())+" oscillating motif in this network that could display unstable or incomplete oscillations.");
                    }
                }
                for(int i=0;i<arrayFunctions.length;i++){
                    System.out.println("Performing network reduction using motif "+(i+1)+"/"+arrayFunctions.length+"...");                    
                    if(!currentSource.equals("")){
                            if(motifs[i].equals("")){currentReduction=currentSource+" (OscillatingMotif)";}                    
                            else{currentReduction=currentSource+" "+motifs[i];}
                    }
                    else{
                        if(motifs[i].equals("")){currentReduction="(OscillatingMotif)";}                    
                        else{currentReduction=motifs[i];}
                    }
                    dummyArray=FullNetworkReduction(oldNames,arrayFunctions[i],networkName,currentReduction);
                    for(int j=0;j<dummyArray.size();j++){
                        arrayDummy=new String[2][dummyArray.get(j)[0].length];
                        arrayDummy[0]=Arrays.copyOf(dummyArray.get(j)[0], dummyArray.get(j)[0].length);
                        arrayDummy[1]=Arrays.copyOf(dummyArray.get(j)[1], dummyArray.get(j)[1].length);
                        newFunctions=Arrays.copyOf(functions,functions.length);
                        
                        for(int k=0;k<arrayDummy[0].length;k++){
                            index=nameList.indexOf(arrayDummy[0][k]);
                            newFunctions[index]=arrayDummy[1][k];
                        }
                        arrayDummy=new String[2][names.length];
                        arrayDummy[0]=Arrays.copyOf(names,names.length);
                        arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                        bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                        if(bool2){
                            attractors.add(arrayDummy);

                        }

                    }
                }
            }
            else{
                System.out.println("There are no stable motifs in this network.");
                if(!currentSource.equals("")){
                    currentReduction=currentSource+"\n";    
                }
                else{currentReduction="";}
                try {fileWritter3 = new FileWriter(file3.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter3 = new BufferedWriter(fileWritter3);
                try {bufferWritter3.write(currentReduction);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                for(int i=0;i<arrayFunctions.length;i++){
                    for(int j=0;j<arrayFunctions[i].length;j++){if(arrayFunctions[i][j].equals("UnstableOscillation")){arrayFunctions[i][j]="FullOscillation";}}
                    arrayDummy=new String[2][arrayFunctions[i].length];
                    arrayDummy[0]=Arrays.copyOf(oldNames,oldNames.length);
                    arrayDummy[1]=Arrays.copyOf(arrayFunctions[i], arrayFunctions[i].length);
                    newFunctions=Arrays.copyOf(functions,functions.length);
                    for(int k=0;k<arrayDummy[0].length;k++){
                        index=nameList.indexOf(arrayDummy[0][k]);
                        newFunctions[index]=arrayDummy[1][k];
                    }
                    arrayDummy=new String[2][names.length];
                    arrayDummy[0]=Arrays.copyOf(names,names.length);
                    arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                    bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                    if(bool2){
                        attractors.add(arrayDummy);
                    }
                }
            }

            if(arrayFunctions.length==0){
            arrayFunctions=new String[2][names.length];
            arrayFunctions[0]=Arrays.copyOf(names, names.length);
            arrayFunctions[1]=Arrays.copyOf(functions, functions.length);
            bool2=true;
                for(int k=0;k<attractors.size();k++){              
                    resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                    if(Arrays.equals(resultDummy2, arrayFunctions[1])){
                        bool2=false;
                        k=attractors.size();
                    }
                }
                if(bool2){
                    attractors.add(arrayFunctions);
                }
            }
        }

        return attractors;
        
    }
  
    
    public static ArrayList<String[][]> FullNetworkReductionTop(String [] names, String[] functions, String networkName, int maxCycleSize, int maxMotifSize){
        
        int[] stabilizedNodes,notStabilizedNodes,sourceNodes,unaffectedNodes,binaryArray;
        String[] oldNames,newNames,oldFunctions,newFunctions,resultDummy,resultDummy2,originalFunctions,motifs;
        String[][] arrayFunctions,arrayDummy;
        Object[] obj;
        int index,oscMotif;
        String data2,data,line,currentReduction,currentSource="",reduction="";
        ArrayList<String[][]> attractors,dummyArray;
        ExpandedNetwork exp;
        Map<String, String> map;
        ArrayList<String> nameList=new ArrayList<String>(Arrays.asList(names));
        ArrayList<Integer> sources=new ArrayList<Integer>();
        boolean bool,bool2,bool3;
        File file =new File("StableMotifs-"+networkName+".txt"); 
        File file2 =new File("Diagram-"+networkName+".txt");
        File file3 =new File("DiagramSinks-"+networkName+".txt");
        FileWriter fileWritter = null;
        FileWriter fileWritter2= null;
        FileWriter fileWritter3= null;
        BufferedWriter bufferWritter,bufferWritter2,bufferWritter3;
        if(new File("StableMotifs-"+networkName+".txt").exists()){
            DeleteFile.deletefile("StableMotifs-"+networkName+".txt");
        }
        if(new File("Diagram-"+networkName+".txt").exists()){
            DeleteFile.deletefile("Diagram-"+networkName+".txt");
        }
        if(new File("DiagramSinks-"+networkName+".txt").exists()){
            DeleteFile.deletefile("DiagramSinks-"+networkName+".txt");
        }
                
        if(!file.exists()){ //if files dont exist, then create them
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file2.exists()){
        try {
            file2.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file3.exists()){
        try {
            file3.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
        originalFunctions=Arrays.copyOf(functions, functions.length);
        attractors=new ArrayList<String[][]>();
        
        for(int i=0;i<names.length;i++){
            if(originalFunctions[i].replace("("," ").replace(")"," ").trim().equals(names[i])){
                sources.add(i);
            }       
        }
        if(sources.size()>0){
            System.out.println("The number of source nodes without specified states is "+sources.size());
        }
        
        for(int m=0;m<(int) (Math.pow(2, sources.size())+0.1);m++){
            functions=Arrays.copyOf(originalFunctions, originalFunctions.length);
            if(sources.size()>0){
                binaryArray=new int[sources.size()];
                OtherMethods.intToBinary(m, binaryArray);                
                for(int l=0;l<sources.size();l++){
                    functions[sources.get(l)]=binaryArray[l]+"";
                }
                System.out.print("Source combination "+(m+1)+"/"+(int) (Math.pow(2, sources.size())+0.1)+":\t");
                currentSource="(";
                for(int l=0;l<sources.size();l++){
                    System.out.print(names[sources.get(l)]+"="+functions[sources.get(l)]+"\t");
                    if(l==0){currentSource=currentSource+names[sources.get(l)]+"="+functions[sources.get(l)];}
                    else{currentSource=currentSource+"-"+names[sources.get(l)]+"="+functions[sources.get(l)];}
                }
                System.out.print("\n");
                currentSource=currentSource+")";
            }
            
            oldNames=Arrays.copyOf(names, names.length);
            oldFunctions=Arrays.copyOf(functions, functions.length);
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
//                for(int i=0;i<oldFunctions.length;i++){
//                    System.out.println(oldNames[i]+"*="+oldFunctions[i]);
//                    oldFunctions[i]=createAndSimplifyRule(oldNames,ExpandedNetwork.createRegulatorsArray(oldFunctions[i])); 
//                }
                oldFunctions=createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                sourceNodes=(int []) obj[4];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                for(int i=0;i<sourceNodes.length;i++){
                    index=nameList.indexOf(oldNames[sourceNodes[i]]);
                    functions[index]=newFunctions[sourceNodes[i]];
                }

                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. In this case, we assign the final
                //form of the rules to the functions array
                if(unaffectedNodes.length==newNames.length){
                    for(int i=0;i<unaffectedNodes.length;i++){
                        index=nameList.indexOf(oldNames[unaffectedNodes[i]]);
                        functions[index]=newFunctions[unaffectedNodes[i]];
                    }
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);



            } while(unaffectedNodes.length!=oldNames.length);
            map = new HashMap<String, String>();
            for(int i=0;i<oldNames.length;i++){
                map.put(oldNames[i], oldFunctions[i]);
            }
            Arrays.sort(oldNames);
            for(int i=0;i<oldNames.length;i++){
                oldFunctions[i]=map.get(oldNames[i]);
            }
            exp=new ExpandedNetwork(oldNames, oldFunctions);
            exp.findStronglyConnectedComponets();       
            System.out.println("Finding stable motifs in this network...");
            exp.findStableStronglyConnectedComponets(maxCycleSize, maxMotifSize);
            bool=exp.getStableStronglyConnectedComponents();
            exp.findOscillations();
            exp.getOscillationsGA();
            arrayFunctions=exp.getNewFunctions();
                   
            if(bool){
                try {fileWritter = new FileWriter(file.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter = new BufferedWriter(fileWritter);
                try {fileWritter2 = new FileWriter(file2.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter2 = new BufferedWriter(fileWritter2);
                
                if(exp.getNumberOfStableMotifs()>1){
                    System.out.println("There are "+exp.getNumberOfStableMotifs()+" stable motifs in this network: ");
                }
                else if (exp.getNumberOfStableMotifs()==1){System.out.println("There is "+exp.getNumberOfStableMotifs()+" stable motif in this network: ");}
                else{
                    System.out.println("There are no stable motifs in this network.");
                    if(!currentSource.equals("")){
                        currentReduction=currentSource+"\n";    
                    }
                    else{currentReduction="";}
                    try {fileWritter3 = new FileWriter(file3.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter3 = new BufferedWriter(fileWritter3);
                    try {bufferWritter3.write(currentReduction);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                }
                data="";
                data2="";
                index=1;
                motifs=new String[arrayFunctions.length];
                for(int i=0;i<arrayFunctions.length;i++){
                    bool3=false;
                    line="";
                    motifs[i]="";
                    for(int j=0;j<arrayFunctions[i].length;j++){
                        if(arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("0") || arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("1")){
                            data=data+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            line=line+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            bool3=true;
                            if(motifs[i].equals("")){motifs[i]="("+oldNames[j]+"="+arrayFunctions[i][j];}
                            else{motifs[i]=motifs[i]+"-"+oldNames[j]+"="+arrayFunctions[i][j];}
                        }   
                    }
                    if(bool3){
                        System.out.println(index+"/"+exp.getNumberOfStableMotifs()+"\t"+line);
                        data=data+"\n";
                        motifs[i]=motifs[i]+")";
                        if(!currentSource.equals("")){
                            data2=data2+currentSource+"\t"+currentSource+" "+motifs[i]+"\n";                  
                        }
                        index++;
                    }
                }
                try {bufferWritter.write(data);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.write(data2);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}

                if(arrayFunctions.length-exp.getNumberOfStableMotifs()>0){
                    if(arrayFunctions.length-exp.getNumberOfStableMotifs()>1){
                        System.out.println("There are "+(arrayFunctions.length-exp.getNumberOfStableMotifs())+" oscillating motifs in this network that could display unstable or incomplete oscillations.");
                    }
                    else{
                        System.out.println("There is "+(arrayFunctions.length-exp.getNumberOfStableMotifs())+" oscillating motif in this network that could display unstable or incomplete oscillations.");
                    }
                }
                oscMotif=1;
                for(int i=0;i<arrayFunctions.length;i++){
                    System.out.println("Performing network reduction using motif "+(i+1)+"/"+arrayFunctions.length+"...");                    
                    if(!currentSource.equals("")){
                            if(motifs[i].equals("")){currentReduction=currentSource+" (OscillatingMotif)";}                    
                            else{currentReduction=currentSource+" "+motifs[i];}
                    }
                    else{
                        if(motifs[i].equals("")){currentReduction="(OscillatingMotif)";}                    
                        else{currentReduction=motifs[i];}
                    }
                    dummyArray=FullNetworkReduction(oldNames,arrayFunctions[i],networkName,currentReduction,maxCycleSize, maxMotifSize);
                    for(int j=0;j<dummyArray.size();j++){
                        arrayDummy=new String[2][dummyArray.get(j)[0].length];
                        arrayDummy[0]=Arrays.copyOf(dummyArray.get(j)[0], dummyArray.get(j)[0].length);
                        arrayDummy[1]=Arrays.copyOf(dummyArray.get(j)[1], dummyArray.get(j)[1].length);
                        newFunctions=Arrays.copyOf(functions,functions.length);
                        
                        for(int k=0;k<arrayDummy[0].length;k++){
                            index=nameList.indexOf(arrayDummy[0][k]);
                            newFunctions[index]=arrayDummy[1][k];
                        }
                        arrayDummy=new String[2][names.length];
                        arrayDummy[0]=Arrays.copyOf(names,names.length);
                        arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                        bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                        if(bool2){
                            attractors.add(arrayDummy);

                        }

                    }
                }
            }
            else{
                System.out.println("There are no stable motifs in this network.");
                if(!currentSource.equals("")){
                    currentReduction=currentSource+"\n";    
                }
                else{currentReduction="";}
                try {fileWritter3 = new FileWriter(file3.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter3 = new BufferedWriter(fileWritter3);
                try {bufferWritter3.write(currentReduction);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                for(int i=0;i<arrayFunctions.length;i++){
                    for(int j=0;j<arrayFunctions[i].length;j++){if(arrayFunctions[i][j].equals("UnstableOscillation")){arrayFunctions[i][j]="FullOscillation";}}
                    arrayDummy=new String[2][arrayFunctions[i].length];
                    arrayDummy[0]=Arrays.copyOf(oldNames,oldNames.length);
                    arrayDummy[1]=Arrays.copyOf(arrayFunctions[i], arrayFunctions[i].length);
                    newFunctions=Arrays.copyOf(functions,functions.length);
                    for(int k=0;k<arrayDummy[0].length;k++){
                        index=nameList.indexOf(arrayDummy[0][k]);
                        newFunctions[index]=arrayDummy[1][k];
                    }
                    arrayDummy=new String[2][names.length];
                    arrayDummy[0]=Arrays.copyOf(names,names.length);
                    arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                    bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                    if(bool2){
                        attractors.add(arrayDummy);
                    }
                }
            }

            if(arrayFunctions.length==0){
            arrayFunctions=new String[2][names.length];
            arrayFunctions[0]=Arrays.copyOf(names, names.length);
            arrayFunctions[1]=Arrays.copyOf(functions, functions.length);
            bool2=true;
                for(int k=0;k<attractors.size();k++){              
                    resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                    if(Arrays.equals(resultDummy2, arrayFunctions[1])){
                        bool2=false;
                        k=attractors.size();
                    }
                }
                if(bool2){
                    attractors.add(arrayFunctions);
                }
            }
        }

        return attractors;
        
    }
  
    
    public static ArrayList<String[][]> FullNetworkReduction(String [] names, String[] functions, String networkName, String reduction){
        
        int[] stabilizedNodes,notStabilizedNodes,sourceNodes,unaffectedNodes,binaryArray;
        String[] oldNames,newNames,oldFunctions,newFunctions,resultDummy,resultDummy2,originalFunctions,motifs;
        String[][] arrayFunctions,arrayDummy;
        Object[] obj;
        int index;
        String data2,data,line,currentReduction,currentSource="";
        ArrayList<String[][]> attractors,dummyArray;
        ExpandedNetwork exp;
        Map<String, String> map;
        ArrayList<String> nameList=new ArrayList<String>(Arrays.asList(names));
        ArrayList<Integer> sources=new ArrayList<Integer>();
        boolean bool,bool2,bool3;
        File file =new File("StableMotifs-"+networkName+".txt"); 
        File file2 =new File("Diagram-"+networkName+".txt");
        File file3 =new File("DiagramSinks-"+networkName+".txt");
        FileWriter fileWritter = null;
        FileWriter fileWritter2= null;
        FileWriter fileWritter3= null;
        BufferedWriter bufferWritter,bufferWritter2,bufferWritter3;
                
        if(!file.exists()){ //if files dont exist, then create them
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file2.exists()){
        try {
            file2.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file3.exists()){
        try {
            file3.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
        originalFunctions=Arrays.copyOf(functions, functions.length);
        attractors=new ArrayList<String[][]>();
        
        for(int i=0;i<names.length;i++){
            if(originalFunctions[i].replace("("," ").replace(")"," ").trim().equals(names[i])){
                sources.add(i);
            }       
        }
                
        for(int m=0;m<(int) (Math.pow(2, sources.size())+0.1);m++){
            functions=Arrays.copyOf(originalFunctions, originalFunctions.length);
            if(sources.size()>0){
                    binaryArray=new int[sources.size()];
                    OtherMethods.intToBinary(m, binaryArray);                
                    for(int l=0;l<sources.size();l++){
                        functions[sources.get(l)]=binaryArray[l]+"";
                    }
                    currentSource="(";
                    for(int l=0;l<sources.size();l++){
                        if(l==0){currentSource=currentSource+names[sources.get(l)]+"="+functions[sources.get(l)];}
                        else{currentSource=currentSource+"-"+names[sources.get(l)]+"="+functions[sources.get(l)];}
                    }
                    currentSource=currentSource+")";
                    try {fileWritter2 = new FileWriter(file2.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter2 = new BufferedWriter(fileWritter2);
                    try {bufferWritter2.write(reduction+"\t"+reduction+" "+currentSource+"\n");
                        } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter2.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter2.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    
                }
            
            oldNames=Arrays.copyOf(names, names.length);
            oldFunctions=Arrays.copyOf(functions, functions.length);
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
//                for(int i=0;i<oldFunctions.length;i++){
//                    System.out.println(oldNames[i]+"*="+oldFunctions[i]);
//                    oldFunctions[i]=createAndSimplifyRule(oldNames,ExpandedNetwork.createRegulatorsArray(oldFunctions[i])); 
//                }
                oldFunctions=createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                sourceNodes=(int []) obj[4];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                for(int i=0;i<sourceNodes.length;i++){
                    index=nameList.indexOf(oldNames[sourceNodes[i]]);
                    functions[index]=newFunctions[sourceNodes[i]];
                }

                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. In this case, we assign the final
                //form of the rules to the functions array
                if(unaffectedNodes.length==newNames.length){
                    for(int i=0;i<unaffectedNodes.length;i++){
                        index=nameList.indexOf(oldNames[unaffectedNodes[i]]);
                        functions[index]=newFunctions[unaffectedNodes[i]];
                    }
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);



            } while(unaffectedNodes.length!=oldNames.length);
            map = new HashMap<String, String>();
            for(int i=0;i<oldNames.length;i++){
                map.put(oldNames[i], oldFunctions[i]);
            }
            Arrays.sort(oldNames);
            for(int i=0;i<oldNames.length;i++){
                oldFunctions[i]=map.get(oldNames[i]);
            }
            exp=new ExpandedNetwork(oldNames, oldFunctions);
            exp.findStronglyConnectedComponets();       
            exp.findStableStronglyConnectedComponets();
            bool=exp.getStableStronglyConnectedComponents();
            exp.findOscillations();
            exp.getOscillationsGA();
            arrayFunctions=exp.getNewFunctions();
                   
            if(bool){
                try {fileWritter = new FileWriter(file.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter = new BufferedWriter(fileWritter);
                try {fileWritter2 = new FileWriter(file2.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter2 = new BufferedWriter(fileWritter2);
                
                if(exp.getNumberOfStableMotifs()==0){
                    if(!currentSource.equals("")){
                        currentReduction=reduction+" "+currentSource+"\n";    
                    }
                    else{currentReduction=reduction+"\n";}
                    try {fileWritter3 = new FileWriter(file3.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter3 = new BufferedWriter(fileWritter3);
                    try {bufferWritter3.write(currentReduction);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                }
                data="";
                data2="";
                index=1;
                motifs=new String[arrayFunctions.length];
                for(int i=0;i<arrayFunctions.length;i++){
                    bool3=false;
                    line="";
                    motifs[i]="";
                    for(int j=0;j<arrayFunctions[i].length;j++){
                        if(arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("0") || arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("1")){
                            data=data+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            line=line+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            bool3=true;
                            if(motifs[i].equals("")){motifs[i]="("+oldNames[j]+"="+arrayFunctions[i][j];}
                            else{motifs[i]=motifs[i]+"-"+oldNames[j]+"="+arrayFunctions[i][j];}
                        }   
                    }
                    if(bool3){
                        data=data+"\n";
                        motifs[i]=motifs[i]+")";
                        if(!currentSource.equals("")){
                            data2=data2+reduction+" "+currentSource+"\t"+reduction+" "+currentSource+" "+motifs[i]+"\n";                  
                        }
                        else{
                            data2=data2+reduction+"\t"+reduction+" "+motifs[i]+"\n";
                        }
                        index++;
                    }
                }
                try {bufferWritter.write(data);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.write(data2);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}

                for(int i=0;i<arrayFunctions.length;i++){                                       
                    if(!currentSource.equals("")){
                            if(motifs[i].equals("")){currentReduction=reduction+" "+currentSource+" (OscillatingMotif)";}                    
                            else{currentReduction=reduction+" "+currentSource+" "+motifs[i];}
                    }
                    else{
                        if(motifs[i].equals("")){currentReduction=reduction+" (OscillatingMotif)";}                    
                        else{currentReduction=reduction+" "+motifs[i];}
                    }
                    dummyArray=FullNetworkReduction(oldNames,arrayFunctions[i],networkName,currentReduction);
                    for(int j=0;j<dummyArray.size();j++){
                        arrayDummy=new String[2][dummyArray.get(j)[0].length];
                        arrayDummy[0]=Arrays.copyOf(dummyArray.get(j)[0], dummyArray.get(j)[0].length);
                        arrayDummy[1]=Arrays.copyOf(dummyArray.get(j)[1], dummyArray.get(j)[1].length);
                        newFunctions=Arrays.copyOf(functions,functions.length);
                        
                        for(int k=0;k<arrayDummy[0].length;k++){
                            index=nameList.indexOf(arrayDummy[0][k]);
                            newFunctions[index]=arrayDummy[1][k];
                        }
                        arrayDummy=new String[2][names.length];
                        arrayDummy[0]=Arrays.copyOf(names,names.length);
                        arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                        bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                        if(bool2){
                            attractors.add(arrayDummy);

                        }

                    }
                }
            }
            else{
                if(!currentSource.equals("")){
                    currentReduction=reduction+" "+currentSource+"\n";    
                }
                else{currentReduction=reduction+"\n";}
                try {fileWritter3 = new FileWriter(file3.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter3 = new BufferedWriter(fileWritter3);
                try {bufferWritter3.write(currentReduction);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                for(int i=0;i<arrayFunctions.length;i++){
                    for(int j=0;j<arrayFunctions[i].length;j++){if(arrayFunctions[i][j].equals("UnstableOscillation")){arrayFunctions[i][j]="FullOscillation";}}
                    arrayDummy=new String[2][arrayFunctions[i].length];
                    arrayDummy[0]=Arrays.copyOf(oldNames,oldNames.length);
                    arrayDummy[1]=Arrays.copyOf(arrayFunctions[i], arrayFunctions[i].length);
                    newFunctions=Arrays.copyOf(functions,functions.length);
                    for(int k=0;k<arrayDummy[0].length;k++){
                        index=nameList.indexOf(arrayDummy[0][k]);
                        newFunctions[index]=arrayDummy[1][k];
                    }
                    arrayDummy=new String[2][names.length];
                    arrayDummy[0]=Arrays.copyOf(names,names.length);
                    arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                    bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                    if(bool2){
                        attractors.add(arrayDummy);
                    }
                }
            }

            if(arrayFunctions.length==0){
            arrayFunctions=new String[2][names.length];
            arrayFunctions[0]=Arrays.copyOf(names, names.length);
            arrayFunctions[1]=Arrays.copyOf(functions, functions.length);
            bool2=true;
                for(int k=0;k<attractors.size();k++){              
                    resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                    if(Arrays.equals(resultDummy2, arrayFunctions[1])){
                        bool2=false;
                        k=attractors.size();
                    }
                }
                if(bool2){
                    attractors.add(arrayFunctions);
                }
            }
        }

        return attractors;
        
    }
  

    public static ArrayList<String[][]> FullNetworkReduction(String [] names, String[] functions, String networkName, String reduction, int maxCycleSize, int maxMotifSize){
        
        int[] stabilizedNodes,notStabilizedNodes,sourceNodes,unaffectedNodes,binaryArray;
        String[] oldNames,newNames,oldFunctions,newFunctions,resultDummy,resultDummy2,originalFunctions,motifs;
        String[][] arrayFunctions,arrayDummy;
        Object[] obj;
        int index,oscMotif;
        String data2,data,line,currentReduction,currentSource="";
        ArrayList<String[][]> attractors,dummyArray;
        ExpandedNetwork exp;
        Map<String, String> map;
        ArrayList<String> nameList=new ArrayList<String>(Arrays.asList(names));
        ArrayList<Integer> sources=new ArrayList<Integer>();
        boolean bool,bool2,bool3;
        File file =new File("StableMotifs-"+networkName+".txt"); 
        File file2 =new File("Diagram-"+networkName+".txt");
        File file3 =new File("DiagramSinks-"+networkName+".txt");
        FileWriter fileWritter = null;
        FileWriter fileWritter2= null;
        FileWriter fileWritter3= null;
        BufferedWriter bufferWritter,bufferWritter2,bufferWritter3;
                
        if(!file.exists()){ //if files dont exist, then create them
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file2.exists()){
        try {
            file2.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(!file3.exists()){
        try {
            file3.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
        originalFunctions=Arrays.copyOf(functions, functions.length);
        attractors=new ArrayList<String[][]>();
        
        for(int i=0;i<names.length;i++){
            if(originalFunctions[i].replace("("," ").replace(")"," ").trim().equals(names[i])){
                sources.add(i);
            }       
        }
                
        for(int m=0;m<(int) (Math.pow(2, sources.size())+0.1);m++){
            functions=Arrays.copyOf(originalFunctions, originalFunctions.length);
            if(sources.size()>0){
                    binaryArray=new int[sources.size()];
                    OtherMethods.intToBinary(m, binaryArray);                
                    for(int l=0;l<sources.size();l++){
                        functions[sources.get(l)]=binaryArray[l]+"";
                    }
                    currentSource="(";
                    for(int l=0;l<sources.size();l++){
                        if(l==0){currentSource=currentSource+names[sources.get(l)]+"="+functions[sources.get(l)];}
                        else{currentSource=currentSource+"-"+names[sources.get(l)]+"="+functions[sources.get(l)];}
                    }
                    currentSource=currentSource+")";
                    try {fileWritter2 = new FileWriter(file2.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter2 = new BufferedWriter(fileWritter2);
                    try {bufferWritter2.write(reduction+"\t"+reduction+" "+currentSource+"\n");
                        } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter2.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter2.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    
                }
            
            oldNames=Arrays.copyOf(names, names.length);
            oldFunctions=Arrays.copyOf(functions, functions.length);
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
//                for(int i=0;i<oldFunctions.length;i++){
//                    System.out.println(oldNames[i]+"*="+oldFunctions[i]);
//                    oldFunctions[i]=createAndSimplifyRule(oldNames,ExpandedNetwork.createRegulatorsArray(oldFunctions[i])); 
//                }
                oldFunctions=createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                sourceNodes=(int []) obj[4];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                for(int i=0;i<sourceNodes.length;i++){
                    index=nameList.indexOf(oldNames[sourceNodes[i]]);
                    functions[index]=newFunctions[sourceNodes[i]];
                }

                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. In this case, we assign the final
                //form of the rules to the functions array
                if(unaffectedNodes.length==newNames.length){
                    for(int i=0;i<unaffectedNodes.length;i++){
                        index=nameList.indexOf(oldNames[unaffectedNodes[i]]);
                        functions[index]=newFunctions[unaffectedNodes[i]];
                    }
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);



            } while(unaffectedNodes.length!=oldNames.length);
            map = new HashMap<String, String>();
            for(int i=0;i<oldNames.length;i++){
                map.put(oldNames[i], oldFunctions[i]);
            }
            Arrays.sort(oldNames);
            for(int i=0;i<oldNames.length;i++){
                oldFunctions[i]=map.get(oldNames[i]);
            }
            exp=new ExpandedNetwork(oldNames, oldFunctions);
            exp.findStronglyConnectedComponets();       
            exp.findStableStronglyConnectedComponets(maxCycleSize, maxMotifSize);
            bool=exp.getStableStronglyConnectedComponents();
            exp.findOscillations();
            exp.getOscillationsGA();
            arrayFunctions=exp.getNewFunctions();
                   
            if(bool){
                try {fileWritter = new FileWriter(file.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter = new BufferedWriter(fileWritter);
                try {fileWritter2 = new FileWriter(file2.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter2 = new BufferedWriter(fileWritter2);
                
                if(exp.getNumberOfStableMotifs()==0){
                    if(!currentSource.equals("")){
                        currentReduction=reduction+" "+currentSource+"\n";    
                    }
                    else{currentReduction=reduction+"\n";}
                    try {fileWritter3 = new FileWriter(file3.getName(),true);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    bufferWritter3 = new BufferedWriter(fileWritter3);
                    try {bufferWritter3.write(currentReduction);
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {bufferWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                    try {fileWritter3.close();
                    } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                }
                data="";
                data2="";
                index=1;
                motifs=new String[arrayFunctions.length];
                for(int i=0;i<arrayFunctions.length;i++){
                    bool3=false;
                    line="";
                    motifs[i]="";
                    for(int j=0;j<arrayFunctions[i].length;j++){
                        if(arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("0") || arrayFunctions[i][j].replace("(","").replace(")","").trim().equals("1")){
                            data=data+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            line=line+oldNames[j]+"="+arrayFunctions[i][j]+"\t";
                            bool3=true;
                            if(motifs[i].equals("")){motifs[i]="("+oldNames[j]+"="+arrayFunctions[i][j];}
                            else{motifs[i]=motifs[i]+"-"+oldNames[j]+"="+arrayFunctions[i][j];}
                        }   
                    }
                    if(bool3){
                        data=data+"\n";
                        motifs[i]=motifs[i]+")";
                        if(!currentSource.equals("")){
                            data2=data2+reduction+" "+currentSource+"\t"+reduction+" "+currentSource+" "+motifs[i]+"\n";                  
                        }
                        else{
                            data2=data2+reduction+"\t"+reduction+" "+motifs[i]+"\n";
                        }
                        index++;
                    }
                }
                try {bufferWritter.write(data);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.write(data2);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter2.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}

                oscMotif=1;
                for(int i=0;i<arrayFunctions.length;i++){                                       
                    if(!currentSource.equals("")){
                            if(motifs[i].equals("")){currentReduction=reduction+" "+currentSource+" (OscillatingMotif)";}                    
                            else{currentReduction=reduction+" "+currentSource+" "+motifs[i];}
                    }
                    else{
                        if(motifs[i].equals("")){currentReduction=reduction+" (OscillatingMotif)";}                    
                        else{currentReduction=reduction+" "+motifs[i];}
                    }
                    dummyArray=FullNetworkReduction(oldNames,arrayFunctions[i],networkName,currentReduction,maxCycleSize, maxMotifSize);
                    for(int j=0;j<dummyArray.size();j++){
                        arrayDummy=new String[2][dummyArray.get(j)[0].length];
                        arrayDummy[0]=Arrays.copyOf(dummyArray.get(j)[0], dummyArray.get(j)[0].length);
                        arrayDummy[1]=Arrays.copyOf(dummyArray.get(j)[1], dummyArray.get(j)[1].length);
                        newFunctions=Arrays.copyOf(functions,functions.length);
                        
                        for(int k=0;k<arrayDummy[0].length;k++){
                            index=nameList.indexOf(arrayDummy[0][k]);
                            newFunctions[index]=arrayDummy[1][k];
                        }
                        arrayDummy=new String[2][names.length];
                        arrayDummy[0]=Arrays.copyOf(names,names.length);
                        arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                        bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                        if(bool2){
                            attractors.add(arrayDummy);

                        }

                    }
                }
            }
            else{
                if(!currentSource.equals("")){
                    currentReduction=reduction+" "+currentSource+"\n";    
                }
                else{currentReduction=reduction+"\n";}
                try {fileWritter3 = new FileWriter(file3.getName(),true);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                bufferWritter3 = new BufferedWriter(fileWritter3);
                try {bufferWritter3.write(currentReduction);
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {bufferWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                try {fileWritter3.close();
                } catch (IOException ex) {Logger.getLogger(NetworkReduction.class.getName()).log(Level.SEVERE, null, ex);}
                for(int i=0;i<arrayFunctions.length;i++){
                    for(int j=0;j<arrayFunctions[i].length;j++){if(arrayFunctions[i][j].equals("UnstableOscillation")){arrayFunctions[i][j]="FullOscillation";}}
                    arrayDummy=new String[2][arrayFunctions[i].length];
                    arrayDummy[0]=Arrays.copyOf(oldNames,oldNames.length);
                    arrayDummy[1]=Arrays.copyOf(arrayFunctions[i], arrayFunctions[i].length);
                    newFunctions=Arrays.copyOf(functions,functions.length);
                    for(int k=0;k<arrayDummy[0].length;k++){
                        index=nameList.indexOf(arrayDummy[0][k]);
                        newFunctions[index]=arrayDummy[1][k];
                    }
                    arrayDummy=new String[2][names.length];
                    arrayDummy[0]=Arrays.copyOf(names,names.length);
                    arrayDummy[1]=Arrays.copyOf(newFunctions,newFunctions.length);
                    bool2=true;
                        for(int k=0;k<attractors.size();k++){              
                            resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                            if(Arrays.equals(resultDummy2, arrayDummy[1])){
                                bool2=false;
                                k=attractors.size();
                            }
                        }
                    if(bool2){
                        attractors.add(arrayDummy);
                    }
                }
            }

            if(arrayFunctions.length==0){
            arrayFunctions=new String[2][names.length];
            arrayFunctions[0]=Arrays.copyOf(names, names.length);
            arrayFunctions[1]=Arrays.copyOf(functions, functions.length);
            bool2=true;
                for(int k=0;k<attractors.size();k++){              
                    resultDummy2=Arrays.copyOf(attractors.get(k)[1],attractors.get(k)[1].length);
                    if(Arrays.equals(resultDummy2, arrayFunctions[1])){
                        bool2=false;
                        k=attractors.size();
                    }
                }
                if(bool2){
                    attractors.add(arrayFunctions);
                }
            }
        }

        return attractors;
        
    }
      
    public static  ArrayList<String[]> iterativeNetworkReduction(String[] names,String[] originalFunctions){
        
        
        String[] newFunctions,newNames;
        int[] stabilizedNodes,notStabilizedNodes,unaffectedNodes;
        Object[] obj;
        int index;
        String[] oldNames=Arrays.copyOf(names, names.length);
        String[]  oldFunctions=Arrays.copyOf(originalFunctions, originalFunctions.length);
        ArrayList<String[]> result=new ArrayList<String[]>();
        
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
                oldFunctions=NetworkReduction.createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=NetworkReduction.sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);
                
                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. 

            } while(unaffectedNodes.length!=oldNames.length);
            
            result.add(oldNames);
            result.add(oldFunctions);
            return result;
    
    }
    
     public static  ArrayList<String[]> iterativeNetworkReductionSubnetwork(String[] names,String[] originalFunctions){
        
        
        String[] newFunctions,newNames;
        int[] stabilizedNodes,notStabilizedNodes,unaffectedNodes;
        Object[] obj;
        int index;
        String[] oldNames=Arrays.copyOf(names, names.length);
        String[]  oldFunctions=Arrays.copyOf(originalFunctions, originalFunctions.length);
        ArrayList<String[]> result=new ArrayList<String[]>();
        
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
                oldFunctions=NetworkReduction.createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=NetworkReduction.sourceReductionSubnetwork(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);
                
                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. 

            } while(unaffectedNodes.length!=oldNames.length);
            
            result.add(oldNames);
            result.add(oldFunctions);
            return result;
    
    }
       
    
    public static Object[] sourceReduction(String[] names,String[] functions){
         
        Scanner scanner;
        MatchResult result;
        String [] negations=new String[names.length];
        String[][][] regulators=new String[names.length][][];
        String[] stringDummy;
        String[][] stringdoubleDummy;
        int [] affectedNodes;
        int[] stabilizedNodes=new int[0];
        int[] notStabilizedNodes=new int[0];
        int[] unaffectedNodes,unaffectedNodes2;
        int[] sourceNodes;
        int [][] outputNodes;
        int index,number0and,number1and,number0or,number1or;

        //First we search the NetworkESM.txt file for all the names
        //of the nodes. The rules must be in the following form to work.
        //NodeI* = (NodeIi1 and not Node Ii2 and ... and NodeIin) or (NodeIj1 and not Node Ij2 and ... and NodeIjn) or ... or (NodeIz1 and not Node Iz2 and ... and NodeIzn)
        //..
        //
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"

        
        for(int i=0;i<names.length;i++){  
            negations[i]="~"+names[i]; //Name of the names of the negation to facilitate searching for the index
        }

        
        //Gets the regulators from each of the rules. Each of the elements separated by an or rules counts as a "regulator" since
        //it will be a composite node in the expanded network
        String[] splitted,splittedAnd;
        Pattern patternOr,patternAnd,patternOne;
        patternOr = Pattern.compile("\\s+(or)+\\s+"); //this will separate the part separated by ors
        patternAnd = Pattern.compile("\\s+(and)+\\s+"); //this will separate the part separated by ands
        patternOne = Pattern.compile("\\s+"); //this is for the case when there is something like A*=(B and C)
        
        for(int i=0;i<names.length;i++){
            splitted=patternOr.split(functions[i]);
            regulators[i]=new String[splitted.length][];
            if(patternOne.split(functions[i]).length==1){
                splitted=patternOne.split(functions[i]);
            }        
            for(int j=0;j<splitted.length;j++){
                if(splitted[j].equals("")){
                    System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                    System.exit(0);
                }
                splitted[j]=splitted[j].replace("(", ""); //we remove the parenthesis, which are not needed for this format
                splitted[j]=splitted[j].replace(")", "");
                splittedAnd=patternAnd.split(splitted[j]); //this will divide the different nodes making up the composite node
                if(patternOne.split(splitted[j]).length==1){
                    splittedAnd=patternOne.split(splitted[j]);
                }
                for(int k=0;k<splittedAnd.length;k++){
                    if(splittedAnd[k].equals("")){
                        System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                        System.exit(0);
                    }
                    scanner=new Scanner(splittedAnd[k]);
                    if(null!=scanner.findInLine("\\s*(\\w+)\\s+(.+)")){
                        //this makes sure we dont have something like "node1 node2"
                        scanner=new Scanner(splittedAnd[k]);
                        if(null!=scanner.findInLine("\\s*([~]*\\w+)\\s*")){
                            //this removes the spaces after and before the name
                            result = scanner.match();
                            splittedAnd[k]=result.group(1);
                        }
                        else{
                            System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                            System.exit(0);
                        }
                    } 
                    else{
                        scanner=new Scanner(splittedAnd[k]);
                        if(null!=scanner.findInLine("\\s*([~]*\\w+)\\s*")){
                            //this removes the spaces after and before the name
                            result = scanner.match();
                            splittedAnd[k]=result.group(1);
                        }
                        else{
                            System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                            System.exit(0);
                        }
                    }
                }
                regulators[i][j]=Arrays.copyOf(splittedAnd, splittedAnd.length);
                //The result is that regulators[i][j] has a String[] array with the nodes making up its regulator
                //if its a composite node then it consists only on one String, otherwise it consists of many
            }
            
        }
        
        //Here we want to create an array with the outputs of each node. This will facilitate the network reduction process
        //since on it we have to look for the nodes that stabilize due to a source nodes
        outputNodes=new int[names.length][0];        
        for(int i=0;i<names.length;i++){                
                for(int j=0;j<regulators[i].length;j++){
                        for(int k=0;k<regulators[i][j].length;k++){
                            if(!"1".equals(regulators[i][0][0]) && !"0".equals(regulators[i][0][0]) && !"FullOscillation".equals(regulators[i][0][0]) && !"UnstableOscillation".equals(regulators[i][0][0]) && !"IncompleteOscillation".equals(regulators[i][0][0])){
                                if(regulators[i][j][k].startsWith("~")){
                                    index=Arrays.asList(negations).indexOf(regulators[i][j][k]);
                                }
                                else{
                                    index=Arrays.asList(names).indexOf(regulators[i][j][k]);
                                }
                                //the new node index has as an output the node i, we first increase the lenght of the
                                //array of the node index and and the node i in the end.
                                Arrays.sort(outputNodes[index]);                             
                                if(Arrays.binarySearch(outputNodes[index], i)<0){
                                    outputNodes[index]=Arrays.copyOf(outputNodes[index],outputNodes[index].length+1);
                                    outputNodes[index][outputNodes[index].length-1]=i;
                                }
                                
                            }
                        }
                }
        }
        
        //Here we substitute the value of each source node in the regulators array
        affectedNodes=new int[0]; //This will contain the index of the nodes affected by the source nodes
        Arrays.sort(affectedNodes);
        sourceNodes=new int[0]; //This will contain the index of the source nodes so that it doesn't have to look for them again
        for(int i1=0;i1<names.length;i1++){
            if(regulators[i1].length==1 && regulators[i1][0].length==1){
                if("1".equals(regulators[i1][0][0]) || "0".equals(regulators[i1][0][0])){
                    sourceNodes=Arrays.copyOf(sourceNodes,sourceNodes.length+1);
                    sourceNodes[sourceNodes.length-1]=i1;
                    for(int i=0;i<outputNodes[i1].length;i++){ //outputNodes[i1][i] is an output node of node i1
                        if(Arrays.binarySearch(affectedNodes, outputNodes[i1][i])<0)
                        {
                            affectedNodes=Arrays.copyOf(affectedNodes,affectedNodes.length+1);
                            affectedNodes[affectedNodes.length-1]=outputNodes[i1][i];
                            Arrays.sort(affectedNodes);
                        }
                        for(int j=0;j<regulators[outputNodes[i1][i]].length;j++){
                            for(int k=0;k<regulators[outputNodes[i1][i]][j].length;k++){
                                if(regulators[outputNodes[i1][i]][j][k].equals(names[i1])){
                                    if("1".equals(regulators[i1][0][0])){regulators[outputNodes[i1][i]][j][k]="1";}
                                    else{regulators[outputNodes[i1][i]][j][k]="0";}
                                }
                                if(regulators[outputNodes[i1][i]][j][k].equals(negations[i1])){
                                    if("1".equals(regulators[i1][0][0])){regulators[outputNodes[i1][i]][j][k]="0";}
                                    else{regulators[outputNodes[i1][i]][j][k]="1";}
                                }

                            }
                        }
                        
                    }
                }
            }
        }
        

        for(int i=0;i<affectedNodes.length;i++){
            number0or=0;
            number1or=0;            
            for(int j=0;j<regulators[affectedNodes[i]].length;j++){
                number0and=0;
                number1and=0;
                for(int k=0;k<regulators[affectedNodes[i]][j].length;k++){
                    if(regulators[affectedNodes[i]][j][k].equals("0")){number0and++;break;}
                    if(regulators[affectedNodes[i]][j][k].equals("1")){number1and++;}
                }
                if(number1and==regulators[affectedNodes[i]][j].length){number1or++;break;}
                if(number0and>0){number0or++;}
                                
            }
            if(number0or==regulators[affectedNodes[i]].length){
                stabilizedNodes=Arrays.copyOf(stabilizedNodes,stabilizedNodes.length+1);
                stabilizedNodes[stabilizedNodes.length-1]=affectedNodes[i];
                regulators[affectedNodes[i]]=new String[1][1];
                regulators[affectedNodes[i]][0][0]="0";
            }
            else if(number1or>0){
                stabilizedNodes=Arrays.copyOf(stabilizedNodes,stabilizedNodes.length+1);
                stabilizedNodes[stabilizedNodes.length-1]=affectedNodes[i];
                regulators[affectedNodes[i]]=new String[1][1];
                regulators[affectedNodes[i]][0][0]="1";
            }
            else{
                notStabilizedNodes=Arrays.copyOf(notStabilizedNodes,notStabilizedNodes.length+1);
                notStabilizedNodes[notStabilizedNodes.length-1]=affectedNodes[i];
            }
            
            
        }
        
        unaffectedNodes=new int[names.length];
        unaffectedNodes2=new int[0];
        for(int i=0;i<names.length;i++){unaffectedNodes[i]=1;}
        for(int i=0;i<affectedNodes.length;i++){unaffectedNodes[affectedNodes[i]]=0;}
        for(int i=0;i<sourceNodes.length;i++){unaffectedNodes[sourceNodes[i]]=0;}
        for(int i=0;i<names.length;i++){
            if(unaffectedNodes[i]==1)
            {
                unaffectedNodes2=Arrays.copyOf(unaffectedNodes2, unaffectedNodes2.length+1);
                unaffectedNodes2[unaffectedNodes2.length-1]=i;
            }
        }
        
        for(int i=0;i<notStabilizedNodes.length;i++){
            number0or=0;
            stringdoubleDummy=new String[0][0];
            for(int j=0;j<regulators[notStabilizedNodes[i]].length;j++){
                number0and=0;
                stringDummy=new String[0];
                for(int k=0;k<regulators[notStabilizedNodes[i]][j].length;k++){
                    if(regulators[notStabilizedNodes[i]][j][k].equals("0")){number0and++;break;}
                    if(regulators[notStabilizedNodes[i]][j][k].equals("1")){}
                    else{
                        stringDummy=Arrays.copyOf(stringDummy, stringDummy.length+1);
                        stringDummy[stringDummy.length-1]=regulators[notStabilizedNodes[i]][j][k];
                    }
                }
                if(number0and>0){number0or++;}
                else{
                    stringdoubleDummy=Arrays.copyOf(stringdoubleDummy, stringdoubleDummy.length+1);
                    stringdoubleDummy[stringdoubleDummy.length-1]=Arrays.copyOf(stringDummy, stringDummy.length);
                }
                                
            }
            regulators[notStabilizedNodes[i]]=Arrays.copyOf(stringdoubleDummy, stringdoubleDummy.length);
               
        }
        
        //Here we are putting the resulting functions for the reduced network
        stringDummy=new String[names.length];
        for(int i=0;i<names.length;i++){
            stringDummy[i]=createRule(regulators[i]);
        }
        return new Object[]{stringDummy,stabilizedNodes,notStabilizedNodes,unaffectedNodes2,sourceNodes};
    
}
    
    public static Object[] sourceReductionSubnetwork(String[] names,String[] functions){
         
        Scanner scanner;
        MatchResult result;
        String [] negations=new String[names.length];
        String[][][] regulators=new String[names.length][][];
        String[] stringDummy;
        String[][] stringdoubleDummy;
        int [] affectedNodes;
        int[] stabilizedNodes=new int[0];
        int[] notStabilizedNodes=new int[0];
        int[] unaffectedNodes,unaffectedNodes2;
        int[] sourceNodes;
        int [][] outputNodes;
        int index,number0and,number1and,number0or,number1or;

        //First we search the NetworkESM.txt file for all the names
        //of the nodes. The rules must be in the following form to work.
        //NodeI* = (NodeIi1 and not Node Ii2 and ... and NodeIin) or (NodeIj1 and not Node Ij2 and ... and NodeIjn) or ... or (NodeIz1 and not Node Iz2 and ... and NodeIzn)
        //..
        //
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"

        
        for(int i=0;i<names.length;i++){  
            negations[i]="~"+names[i]; //Name of the names of the negation to facilitate searching for the index
        }

        
        //Gets the regulators from each of the rules. Each of the elements separated by an or rules counts as a "regulator" since
        //it will be a composite node in the expanded network
        String[] splitted,splittedAnd;
        Pattern patternOr,patternAnd,patternOne;
        patternOr = Pattern.compile("\\s+(or)+\\s+"); //this will separate the part separated by ors
        patternAnd = Pattern.compile("\\s+(and)+\\s+"); //this will separate the part separated by ands
        patternOne = Pattern.compile("\\s+"); //this is for the case when there is something like A*=(B and C)
        
        for(int i=0;i<names.length;i++){
            splitted=patternOr.split(functions[i]);
            regulators[i]=new String[splitted.length][];
            if(patternOne.split(functions[i]).length==1){
                splitted=patternOne.split(functions[i]);
            }        
            for(int j=0;j<splitted.length;j++){
                if(splitted[j].equals("")){
                    System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                    System.exit(0);
                }
                splitted[j]=splitted[j].replace("(", ""); //we remove the parenthesis, which are not needed for this format
                splitted[j]=splitted[j].replace(")", "");
                splittedAnd=patternAnd.split(splitted[j]); //this will divide the different nodes making up the composite node
                if(patternOne.split(splitted[j]).length==1){
                    splittedAnd=patternOne.split(splitted[j]);
                }
                for(int k=0;k<splittedAnd.length;k++){
                    if(splittedAnd[k].equals("")){
                        System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                        System.exit(0);
                    }
                    scanner=new Scanner(splittedAnd[k]);
                    if(null!=scanner.findInLine("\\s*(\\w+)\\s+(.+)")){
                        //this makes sure we dont have something like "node1 node2"
                        scanner=new Scanner(splittedAnd[k]);
                        if(null!=scanner.findInLine("\\s*([~]*\\w+)\\s*")){
                            //this removes the spaces after and before the name
                            result = scanner.match();
                            splittedAnd[k]=result.group(1);
                        }
                        else{
                            System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                            System.exit(0);
                        }
                    } 
                    else{
                        scanner=new Scanner(splittedAnd[k]);
                        if(null!=scanner.findInLine("\\s*([~]*\\w+)\\s*")){
                            //this removes the spaces after and before the name
                            result = scanner.match();
                            splittedAnd[k]=result.group(1);
                        }
                        else{
                            System.out.println("Formatting error in the following line:\n"+functions[i]+"\nwhich is part of the update funciton of the node "+names[i]);
                            System.exit(0);
                        }
                    }
                }
                regulators[i][j]=Arrays.copyOf(splittedAnd, splittedAnd.length);
                //The result is that regulators[i][j] has a String[] array with the nodes making up its regulator
                //if its a composite node then it consists only on one String, otherwise it consists of many
            }
            
        }
        
        //Here we want to create an array with the outputs of each node. This will facilitate the network reduction process
        //since on it we have to look for the nodes that stabilize due to a source nodes
        outputNodes=new int[names.length][0];        
        for(int i=0;i<names.length;i++){                
                for(int j=0;j<regulators[i].length;j++){
                        for(int k=0;k<regulators[i][j].length;k++){
                            if(!"1".equals(regulators[i][0][0]) && !"0".equals(regulators[i][0][0]) && !"FullOscillation".equals(regulators[i][0][0]) && !"UnstableOscillation".equals(regulators[i][0][0]) && !"IncompleteOscillation".equals(regulators[i][0][0])){
                                if(regulators[i][j][k].startsWith("~")){
                                    index=Arrays.asList(negations).indexOf(regulators[i][j][k]);
                                }
                                else{
                                    index=Arrays.asList(names).indexOf(regulators[i][j][k]);
                                }
                                //the new node index has as an output the node i, we first increase the lenght of the
                                //array of the node index and and the node i in the end.
                                if(index!=-1){
                                    Arrays.sort(outputNodes[index]);                             
                                    if(Arrays.binarySearch(outputNodes[index], i)<0){
                                        outputNodes[index]=Arrays.copyOf(outputNodes[index],outputNodes[index].length+1);
                                        outputNodes[index][outputNodes[index].length-1]=i;
                                    }
                                }
                            }
                        }
                }
        }
        
        //Here we substitute the value of each source node in the regulators array
        affectedNodes=new int[0]; //This will contain the index of the nodes affected by the source nodes
        Arrays.sort(affectedNodes);
        sourceNodes=new int[0]; //This will contain the index of the source nodes so that it doesn't have to look for them again
        for(int i1=0;i1<names.length;i1++){
            if(regulators[i1].length==1 && regulators[i1][0].length==1){
                if("1".equals(regulators[i1][0][0]) || "0".equals(regulators[i1][0][0])){
                    sourceNodes=Arrays.copyOf(sourceNodes,sourceNodes.length+1);
                    sourceNodes[sourceNodes.length-1]=i1;
                    for(int i=0;i<outputNodes[i1].length;i++){ //outputNodes[i1][i] is an output node of node i1
                        if(Arrays.binarySearch(affectedNodes, outputNodes[i1][i])<0)
                        {
                            affectedNodes=Arrays.copyOf(affectedNodes,affectedNodes.length+1);
                            affectedNodes[affectedNodes.length-1]=outputNodes[i1][i];
                            Arrays.sort(affectedNodes);
                        }
                        for(int j=0;j<regulators[outputNodes[i1][i]].length;j++){
                            for(int k=0;k<regulators[outputNodes[i1][i]][j].length;k++){
                                if(regulators[outputNodes[i1][i]][j][k].equals(names[i1])){
                                    if("1".equals(regulators[i1][0][0])){regulators[outputNodes[i1][i]][j][k]="1";}
                                    else{regulators[outputNodes[i1][i]][j][k]="0";}
                                }
                                if(regulators[outputNodes[i1][i]][j][k].equals(negations[i1])){
                                    if("1".equals(regulators[i1][0][0])){regulators[outputNodes[i1][i]][j][k]="0";}
                                    else{regulators[outputNodes[i1][i]][j][k]="1";}
                                }

                            }
                        }
                        
                    }
                }
            }
        }
        

        for(int i=0;i<affectedNodes.length;i++){
            number0or=0;
            number1or=0;            
            for(int j=0;j<regulators[affectedNodes[i]].length;j++){
                number0and=0;
                number1and=0;
                for(int k=0;k<regulators[affectedNodes[i]][j].length;k++){
                    if(regulators[affectedNodes[i]][j][k].equals("0")){number0and++;break;}
                    if(regulators[affectedNodes[i]][j][k].equals("1")){number1and++;}
                }
                if(number1and==regulators[affectedNodes[i]][j].length){number1or++;break;}
                if(number0and>0){number0or++;}
                                
            }
            if(number0or==regulators[affectedNodes[i]].length){
                stabilizedNodes=Arrays.copyOf(stabilizedNodes,stabilizedNodes.length+1);
                stabilizedNodes[stabilizedNodes.length-1]=affectedNodes[i];
                regulators[affectedNodes[i]]=new String[1][1];
                regulators[affectedNodes[i]][0][0]="0";
            }
            else if(number1or>0){
                stabilizedNodes=Arrays.copyOf(stabilizedNodes,stabilizedNodes.length+1);
                stabilizedNodes[stabilizedNodes.length-1]=affectedNodes[i];
                regulators[affectedNodes[i]]=new String[1][1];
                regulators[affectedNodes[i]][0][0]="1";
            }
            else{
                notStabilizedNodes=Arrays.copyOf(notStabilizedNodes,notStabilizedNodes.length+1);
                notStabilizedNodes[notStabilizedNodes.length-1]=affectedNodes[i];
            }
            
            
        }
        
        unaffectedNodes=new int[names.length];
        unaffectedNodes2=new int[0];
        for(int i=0;i<names.length;i++){unaffectedNodes[i]=1;}
        for(int i=0;i<affectedNodes.length;i++){unaffectedNodes[affectedNodes[i]]=0;}
        for(int i=0;i<sourceNodes.length;i++){unaffectedNodes[sourceNodes[i]]=0;}
        for(int i=0;i<names.length;i++){
            if(unaffectedNodes[i]==1)
            {
                unaffectedNodes2=Arrays.copyOf(unaffectedNodes2, unaffectedNodes2.length+1);
                unaffectedNodes2[unaffectedNodes2.length-1]=i;
            }
        }
        
        for(int i=0;i<notStabilizedNodes.length;i++){
            number0or=0;
            stringdoubleDummy=new String[0][0];
            for(int j=0;j<regulators[notStabilizedNodes[i]].length;j++){
                number0and=0;
                stringDummy=new String[0];
                for(int k=0;k<regulators[notStabilizedNodes[i]][j].length;k++){
                    if(regulators[notStabilizedNodes[i]][j][k].equals("0")){number0and++;break;}
                    if(regulators[notStabilizedNodes[i]][j][k].equals("1")){}
                    else{
                        stringDummy=Arrays.copyOf(stringDummy, stringDummy.length+1);
                        stringDummy[stringDummy.length-1]=regulators[notStabilizedNodes[i]][j][k];
                    }
                }
                if(number0and>0){number0or++;}
                else{
                    stringdoubleDummy=Arrays.copyOf(stringdoubleDummy, stringdoubleDummy.length+1);
                    stringdoubleDummy[stringdoubleDummy.length-1]=Arrays.copyOf(stringDummy, stringDummy.length);
                }
                                
            }
            regulators[notStabilizedNodes[i]]=Arrays.copyOf(stringdoubleDummy, stringdoubleDummy.length);
               
        }
        
        //Here we are putting the resulting functions for the reduced network
        stringDummy=new String[names.length];
        for(int i=0;i<names.length;i++){
            stringDummy[i]=createRule(regulators[i]);
        }
        return new Object[]{stringDummy,stabilizedNodes,notStabilizedNodes,unaffectedNodes2,sourceNodes};
    
}
    
    public static void writeReducedNetwork(ArrayList<String[]> attractorsReduction, String networkName, String type, String[] names, String[] originalFunctions){
    
        String [] oldNames,oldFunctions,newFunctions,newNames,resultDummy;
        int [] stabilizedNodes,notStabilizedNodes,unaffectedNodes;
        Object[] obj;
        int index;
        ArrayList<String> nameList=new ArrayList<String>(Arrays.asList(names));
        FileToWrite attrsred;
        
        for(int k=0;k<attractorsReduction.size();k++){
            oldNames=Arrays.copyOf(names, names.length);
            oldFunctions=Arrays.copyOf(originalFunctions, originalFunctions.length);
            resultDummy=attractorsReduction.get(k);
            for(int j=0;j<resultDummy.length;j++){
                if(resultDummy[j].equalsIgnoreCase("0") || resultDummy[j].equalsIgnoreCase("1"))
                {oldFunctions[j]=resultDummy[j];}
            }
            do {
                //This simplifies the rules so that, for example
                //A*=B or (not B)       gets simplified into    A*=1
                //A*=B or (not B or C)  gets simplified into    A*=B or C
                oldFunctions=NetworkReduction.createAndSimplifyRuleDNF(oldNames,oldFunctions);
                obj=NetworkReduction.sourceReduction(oldNames,oldFunctions);
                newFunctions=(String []) obj[0];
                stabilizedNodes=(int []) obj[1];
                notStabilizedNodes=(int []) obj[2];
                unaffectedNodes=(int []) obj[3];
                newNames=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                oldFunctions=new String[unaffectedNodes.length+stabilizedNodes.length+notStabilizedNodes.length];
                index=0;
                for(int i=0;i<unaffectedNodes.length;i++){
                    newNames[index]=oldNames[unaffectedNodes[i]];
                    oldFunctions[index]=newFunctions[unaffectedNodes[i]];
                    index++;
                }
                for(int i=0;i<stabilizedNodes.length;i++){
                    newNames[index]=oldNames[stabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[stabilizedNodes[i]];
                    index++;
                }
                for(int i=0;i<notStabilizedNodes.length;i++){
                    newNames[index]=oldNames[notStabilizedNodes[i]];
                    oldFunctions[index]=newFunctions[notStabilizedNodes[i]];
                    index++;
                }

                //If we go to the next cycle, this will be the new list of names, which includes all nodes that were not
                //used as sources in the previous step
                oldNames=Arrays.copyOf(newNames, newNames.length);
                
                //In case the new nodes are exactly the same as the unaffected nodes (that is, no new nodes
                //were affected in this reduction step) then while cycle will stop. 

            } while(unaffectedNodes.length!=oldNames.length);
            attrsred=new FileToWrite(type+"-"+networkName+"/"+type+"-ReducedNetwork-"+(k+1)+".txt");
            for(int j=0;j<oldNames.length;j++){
                attrsred.writeLine(oldNames[j]+" *= "+oldFunctions[j].replace("~","not "));
            }
            attrsred.close();
        }
        
        
        
    }
    
    public static String createRule(String[][] regulators){
         
         String rule="";
         String composite;
         for(int i=0;i<regulators.length;i++){
             composite="(";
             for(int j=0;j<regulators[i].length;j++){
                 composite=composite+" "+regulators[i][j];
                 if(j<regulators[i].length-1){
                     composite=composite+" and";
                 }
             }
             composite=composite+" )";
             rule=rule+composite;
             if(i<regulators.length-1){
                rule=rule+" or ";
             }
         }
         return rule;
                
}
       
    public static String[] createAndSimplifyRuleDNF(String names[],String functions[]){
        String[][] regulators;
        String[] functionsSimplified;
        Formula f;
        int [][] statesON,simplifiedON;
        String functionEvaluate;
        int[] booleanConfiguration;
        int countZeros,Ninput,randomInt;
        boolean[] booleanState;
        boolean booleanRandom=false;
        Set<String> regulatorsList=new HashSet<String>();
        
        functionsSimplified=new String[names.length];
        regulators=new String[names.length][];
        String[] splitted;
        Pattern pattern = Pattern.compile("[()\\s]+");
        for(int i=0;i<names.length;i++){
            splitted=pattern.split(functions[i].replace("~","not "));
            for(int j=0;j<splitted.length;j++){
                if(!splitted[j].equals("") && !splitted[j].equals("and") && !splitted[j].equals("or") && !splitted[j].equals("not") && !splitted[j].equals("0") && !splitted[j].equals("False") && !splitted[j].equals("1") && !splitted[j].equals("True") && !splitted[j].equals("Random")){
                    regulatorsList.add(splitted[j]);
                }
            }
            regulators[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
            regulatorsList=new HashSet<String>();
        }
        
        for(int i=0;i<names.length;i++){
        booleanConfiguration=new int[regulators[i].length];
        booleanState=new boolean[regulators[i].length];
        
            
        statesON=new int[0][0];
        countZeros=0;
        Ninput=(int)(Math.pow(2,regulators[i].length)+0.1);
        for(int j=0;j<Ninput;j++){
                functionEvaluate=" "+functions[i].replace("~","not ")+" ";
                OtherMethods.intToBinary(j,booleanConfiguration);
                OtherMethods.intToBoolean(booleanConfiguration,booleanState);                
                for(int k=0;k<regulators[i].length;k++){
                    functionEvaluate=functionEvaluate.replace(" "+regulators[i][k]+" "," "+booleanState[k]+" ");
                    //since a space was added after and before a parenthesis, now the nodes have to have a space
                    //preceding them and after them. Note that this solves the problem of having nodes with names
                    //such as IL2 and IL2R, since it will not be possible to confuse them since theyh will have a
                    //space before and after their names
                }
                
                functionEvaluate=functionEvaluate.replace("and", "&&").replace("or", "||").replace("not", "!").replace("True",""+true).replace("1",""+true).replace("0",""+false).replace("False",""+false);
                try {
                booleanRandom=OtherMethods.EvalStr(functionEvaluate,booleanRandom);
                } catch(ScriptException se) {
                    System.out.println("Formatting error in the function for "+names[i]+":\n"+functions[i]);
                    System.exit(0);
                }
                randomInt=booleanRandom?1:0;
                if(randomInt==1){
                    countZeros++;
                    statesON=Arrays.copyOf(statesON, statesON.length+1);
                    statesON[statesON.length-1]=Arrays.copyOf(booleanConfiguration, booleanConfiguration.length);
                }
                
            }
            
            if(countZeros==0){functionsSimplified[i]=" 0";}
            else if(countZeros==Ninput){functionsSimplified[i]=" 1";}
            else{
                f = Formula.readintArray(statesON);
                f.reduceToPrimeImplicants();
                simplifiedON=f.toArray();
                functionsSimplified[i]=OtherMethods.wildcardToFunction(simplifiedON,regulators[i]);
            }
            
        }
       
        return functionsSimplified;
                
}
     
    public static ArrayList<ArrayList<String[]>> removeDuplicateQuasiattractors(ArrayList<String[]> attractorsResult, String[] names){
        
        boolean boolUnstable;
        int N=names.length;
        String function;
        String[] functions;
        String[] resultDummy;
        ArrayList<String[]> finalAttractorsReduction;
        ArrayList<String[]> unstableAttractors;
        ArrayList<String[]> unstableAttractorsFinal;
        ArrayList<String[]> attractorsReduction;
        ArrayList<ArrayList<String[]>> result=new ArrayList<ArrayList<String[]>>();
        finalAttractorsReduction=new ArrayList<String[]>();
        unstableAttractors=new ArrayList<String[]>();
        unstableAttractorsFinal=new ArrayList<String[]>();
        attractorsReduction=new ArrayList<String[]>();
        
        for(int i=0;i<attractorsResult.size();i++){
            functions=attractorsResult.get(i);          
            boolUnstable=false;
            resultDummy=new String[N];
            for(int j=0;j<names.length;j++){
                function=functions[j].replace("("," ").replace(")"," ").trim();
                if(function.equals("UnstableOscillation")){function="X";boolUnstable=true;}
                else if(function.equals("FullOscillation")){function="X";}
                else if(function.equals("IncompleteOscillation")){function="X";}
                else if(function.equals("0")){function="0";}
                else if(function.equals("1")){function="1";}
                else{function="X";}
                resultDummy[j]=function;
            }
            if(!boolUnstable){finalAttractorsReduction.add(resultDummy);}
            else{unstableAttractors.add(resultDummy);unstableAttractorsFinal.add(resultDummy);}
            attractorsReduction.add(resultDummy);     
        }
        attractorsReduction=removeDuplicates(attractorsReduction);
        unstableAttractors=removeDuplicates(unstableAttractors);
        unstableAttractorsFinal=removeDuplicates(unstableAttractorsFinal);
        finalAttractorsReduction=removeDuplicates(finalAttractorsReduction);
        if(unstableAttractors.size()+finalAttractorsReduction.size()!=attractorsReduction.size()){
            unstableAttractorsFinal=new ArrayList<String[]>();
            for(int i=0;i<unstableAttractors.size();i++){
                boolUnstable=true;
                functions=Arrays.copyOf(unstableAttractors.get(i), unstableAttractors.get(i).length);
                for(int j=0;j<finalAttractorsReduction.size();j++){
                    if(Arrays.equals(functions, finalAttractorsReduction.get(j))){
                        boolUnstable=false;
                        break;
                    }
                }
                if(boolUnstable){
                    unstableAttractorsFinal.add(functions);
                }
            }
        }
        result.add(finalAttractorsReduction);
        result.add(unstableAttractorsFinal);
        return result;
  
    }
    
   public static ArrayList<String[]> removeDuplicates(ArrayList<String[]> attractorsReduction){
        ArrayList<String[]> attractors= new  ArrayList<String[]>();
        ArrayList<String> nonduplicates= new  ArrayList<String>();
        String[] functions,functions2;
        boolean equal,condition1,condition2;
        for(int i=0;i<attractorsReduction.size();i++){
            nonduplicates.add(i+"");
        }
        
        for(int i=0;i<attractorsReduction.size();i++){
            if(nonduplicates.contains(i+"")){
                functions=attractorsReduction.get(i);
                for(int j=i+1;j<attractorsReduction.size();j++){
                    if(nonduplicates.contains(j+"")){
                        functions2=attractorsReduction.get(j);
                        equal=true;
                        for(int k=0;k<functions.length;k++){
                            condition1=functions2[k].equals(functions[k]);
                            condition2=!"0".equals(functions2[k])&&!"1".equals(functions2[k])&&!"0".equals(functions[k])&&!"1".equals(functions[k]);
                            if(!condition1 && !condition2){
                                equal=false;
                                break;
                            }
                        }
                        if(equal){
                            nonduplicates.remove(j+"");
                        }
                    }
                }
            }
        }
        
        for(int i=0;i<attractorsReduction.size();i++){
            if(nonduplicates.contains(i+"")){
                functions=attractorsReduction.get(i);
                attractors.add(Arrays.copyOf(functions, functions.length));
            }
        }
        
        return attractors;
    
    }


    public static ArrayList<String[]> getAttractorsCorrespondingToMotifSequence(ArrayList<String[]> attractors, String[] nodeNames, String networkName){
        
        ArrayList<HashSet<String>> attractorSet=new  ArrayList<HashSet<String>>();
        HashSet<String> set;
        FileToRead fr=new FileToRead("DiagramSinks-"+networkName+".txt"); //This is the file with the series of motifs that lead to an attactor
        String line,lineOriginal;
        String[] separatedLine;
        String[] motifAttractor;
        ArrayList<String[]> motifsAttractors=new ArrayList <String[]>();
        int index;
        
        //We create an array of sets with, with each set corresponding to String of the node names/states in an attactor
        //The form of the Strings is name=state
        for(int i=0;i<attractors.size();i++){
            attractorSet.add(new HashSet<String>());
            for(int j=0;j<attractors.get(i).length;j++){
                attractorSet.get(i).add(nodeNames[j]+"="+attractors.get(i)[j].replace("(","").replace(")","").trim());
            }
        }
        
        while(fr.hasNext()){
            motifAttractor=new String[2];
            lineOriginal=fr.nextLine();
            line=lineOriginal.replace("(","").replace(")","").replace("-"," ").replace(" ","\t");
            separatedLine=line.split("\t");
            set=new HashSet<String>(Arrays.asList(separatedLine));
            index=-1;
            for(int i=0;i<attractors.size();i++){
                if(attractorSet.get(i).containsAll(set)){
                    index=i;
                    break;
                }
            }
            if(index>=0){
                motifAttractor[0]=lineOriginal;
                motifAttractor[1]=""+index;
                motifsAttractors.add(motifAttractor);
            }    
        }
        
        fr.close();
        return motifsAttractors;
    }
    
}