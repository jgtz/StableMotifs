package stablemotifs;

import fileOperations.FileToRead;
import fileOperations.FileToWrite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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

public class OtherMethods {
    
    public static ArrayList<ArrayList<Integer>> SubsetsOfSize(int arraySize,int subsetSize){
    
        ArrayList<ArrayList<Integer>> subsets=new ArrayList<ArrayList<Integer>>();
        Integer[] countArray=new Integer[subsetSize];
        boolean subsetsLeft=true;
        boolean nextSet;
        int updateIndex;
        for(int i=0;i<subsetSize;i++){
            countArray[i]=subsetSize-1-i;            
        }
        
        do{ 
            subsets.add(new ArrayList<Integer>(Arrays.asList(countArray))); 
            nextSet=false;
            updateIndex=0;
            do{               
               countArray[updateIndex]++; 
               if(countArray[updateIndex]<arraySize-updateIndex){
                   if(updateIndex<subsetSize-1){
                        if(countArray[updateIndex+1]<countArray[updateIndex]){nextSet=true;} // l<k<j<i
                        else{updateIndex=updateIndex+1;}
                   }
                   else{nextSet=true;}                   
               }
               else{updateIndex=updateIndex+1;}
               if(nextSet&&updateIndex>0){
                   for(int i=0;i<updateIndex;i++){
                       countArray[updateIndex-i-1]=countArray[updateIndex-i]+1;
                   }                   
               }
            }
            while(updateIndex<=subsetSize-1&&!nextSet);
            if(updateIndex>subsetSize-1){subsetsLeft=false;}            
        }
        while(subsetsLeft);        
        return subsets;
    
    
    }
    
        public static int[] getRandomOrder(int N){
        int[] normalOrder=new int[N];
        int[] newOrder=new int[N];
        int index,temp;
        
        for(int n = 0; n < N; ++n){
            normalOrder[n]=n;
            newOrder[n]=n;
        }
        for(int n = 0; n < N; ++n){
            index=(int)((N-n)*Math.random());
            temp=normalOrder[N-n-1];
            newOrder[n]=normalOrder[index];
            normalOrder[N-n-1]=normalOrder[index];
            normalOrder[index]=temp;
        }
        return Arrays.copyOf(newOrder, N);
        
        
        
    }
    
    public static int searchIndex(String search, String[] dictionary){
		int i = -1;
		for(int n = 0; n < dictionary.length; ++n){
			if(search.equals(dictionary[n])){
				i = n;
				break;
			}
		}
		return i;
	}    
    
    public static ArrayList<String[]> leaveLargestPQA(ArrayList<String[]> attractorsReduction){
        ArrayList<String[]> attractors= new  ArrayList<String[]>();
        ArrayList<HashSet> attractorsUndeterminedNodes= new  ArrayList<HashSet>();
        HashSet set;
        boolean contained;
        for(int i=0;i<attractorsReduction.size();i++){
            attractorsUndeterminedNodes.add(new HashSet());
            for(int j=0;j<attractorsReduction.get(i).length;j++){                
                if(!attractorsReduction.get(i)[j].equalsIgnoreCase("0") && !attractorsReduction.get(i)[j].equalsIgnoreCase("1")){
                    attractorsUndeterminedNodes.get(i).add(j);
                }
            }
            attractorsUndeterminedNodes.get(i);           
        }
        
        for(int i=0;i<attractorsUndeterminedNodes.size();i++){
                set=attractorsUndeterminedNodes.get(i);
                contained=false;
                for(int j=0;j<attractorsUndeterminedNodes.size();j++){
                    if(i!=j){
                        if(attractorsUndeterminedNodes.get(j).containsAll(set)){
                            contained=true;
                            break;
                        }
                    }
                }
                if(!contained){
                    attractors.add(attractorsReduction.get(i));
                }
        }

        return attractors;
    
    }
    
    
    public static String wildcardToFunction(int[][] wildcard, String[] inputs){
		String function = " ";
                String ANDpart;
		for(int i = 0; i < wildcard.length; i++){
                    ANDpart="( ";
                    for(int j = 0; j < wildcard[i].length; j++){         
                        if(wildcard[i][j]!=-1){
                            if(!ANDpart.equals("( ")){
                                ANDpart=ANDpart+" and ";
                            }
                            if(wildcard[i][j]==1){ANDpart=ANDpart+inputs[j];}
                            else{ANDpart=ANDpart+"~"+inputs[j];}                            
                        }                                                    
                    }                   
                    ANDpart=ANDpart+" )";
                    function=function+ANDpart;
                    if(i<wildcard.length-1){function=function+" or ";}
		}
		return function;
	}
            
    public static int binaryToInt(int[] binaryArray){
		int integerState = 0;
		int power = 1;

		for(int n = 0; n < binaryArray.length; ++n){
			integerState += (power*binaryArray[n]);
			power *= 2;
		}
		return integerState;
	}
        
    public static void intToBinary(int integerState, int[] binaryArray){
		for(int m = 0; m < binaryArray.length; ++m)
                    binaryArray[m] = 0;
		if(integerState > 0){
                    int p = 1;
                    int m = 0;
                    int res = integerState;
                    while(p <= integerState){
                        p *= 2;
			++m;
                    }
                    p /= 2;
                    --m;
                    while(m >= 0){
                        binaryArray[m] = res/p;
			if(p > 0)
                            res %= p;
			p /= 2;
			--m;
                    }
		}
	}
        
    public static Integer[] intToBinary(int integerState, int L){
                Integer[] binaryArray=new Integer[L];
		for(int m = 0; m < binaryArray.length; ++m){
                    binaryArray[m] = 0;
                }
		if(integerState > 0){
                    int p = 1;
                    int m = 0;
                    int res = integerState;
                    while(p <= integerState){
                        p *= 2;
			++m;
                    }
                    p /= 2;
                    --m;
                    while(m >= 0){
                        binaryArray[m] = res/p;
			if(p > 0)
                            res %= p;
			p /= 2;
			--m;
                    }
		}
                return binaryArray;
	}        
           
    public static boolean EvalStr(String expression, boolean booleanResult) throws ScriptException{
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        booleanResult=((Boolean) engine.eval(expression)).booleanValue();
        
        return booleanResult;
        
    }
  
    public static void intToBoolean(int[] intArray, boolean[] booleanArray){
        for(int i=0;i<intArray.length;i++){
            if(intArray[i]==0){booleanArray[i]=false;}
            else{booleanArray[i]=true;}
        }
    }
 
    public static int[] orderLowesttoHighest(int [] array, int initialPosition, int finalPosition){
    int shift;
    int index,element;
    int[] order=new int[array.length];
    int[] newArray=new int[array.length];
    for(int position=initialPosition;position<=finalPosition;position++){
        order[position]=position;
        newArray[position]=array[position];
    }
    
    for(int position=initialPosition+1;position<=finalPosition;position++){
        if(newArray[position-1]>newArray[position]){
            shift=0;
            while(newArray[position-1-shift]>newArray[position]){
                shift++;
                if(position-1-shift<0){break;}
            }
            index=order[position];
            element=newArray[position];
            for(int i=0;i<shift;i++){
                order[position-i]=order[position-i-1];
                newArray[position-i]=newArray[position-i-1];
            }
            order[position-shift]=index;
            newArray[position-shift]=element;
        }
    }
    return order;
}
    
    public static int[] orderHighesttoLowest(int [] array, int initialPosition, int finalPosition){
    int[] order;
    int[] reversedOrder;
    int counter;
    order=orderLowesttoHighest(array,initialPosition,finalPosition);
    reversedOrder=Arrays.copyOf(order, array.length);
    counter=finalPosition;
    for(int i=initialPosition;i<=finalPosition;i++){
        reversedOrder[counter]=order[i];
        counter--;
    }
    
    return reversedOrder;
}    

    public static int[] orderArraybyOrder(int [] array, int initialPosition, int finalPosition, int[] order){
    int [] newArray= new int[array.length];
        
    for(int i=initialPosition;i<=finalPosition;i++)
    {
        newArray[i]=array[order[i]];
    }
    
    return newArray;
}

    public static String[] orderArraybyOrder(String [] array, int initialPosition, int finalPosition, int[] order){
    String [] newArray= new String[array.length];
        
    for(int i=initialPosition;i<finalPosition;i++)
    {
        newArray[i]=array[order[i]];
    }
    
    return newArray;
}
    
    public static String[] ArrayByLength(String [] names){
        int N=names.length;
        String[] orderedNames;
        int [] lengths=new int[N];
        
        for(int n = 0; n < N; ++n){
            lengths[n]=names[n].length();        
        }
        
        orderedNames=orderArraybyOrder(names,0,N,orderHighesttoLowest(lengths,0,N-1));
        
        return orderedNames;
            
            
     }

    public static Network RecreateNetwork(String directory){
        
        Network network=new Network(directory);
        FileToRead fr=new FileToRead("Functions-"+directory+".txt");
        int N,index=0;        
        String node;
        String[] namesNetwork,names,functionsNetwork;
        while(fr.hasNext()){
            fr.nextLine();
            index++;
        }
        fr.close();
        N=index;       
        fr=new FileToRead("Names-"+directory+".txt");
        names=new String[N];
        namesNetwork=new String[N];
        functionsNetwork=new String[N];
        for(int i=0;i<N;i++){
            namesNetwork[i]=fr.nextLine();
        }
        fr.close();
        fr=new FileToRead("Functions-"+directory+".txt");
        for(int i=0;i<N;i++){
            functionsNetwork[i]=fr.nextLine();
        } 
        network.setFunctions(functionsNetwork);
        network.setNames(namesNetwork);
        fr.close();
        return network;
    }
    
    public static void getControlSets(String[] sequenceAndAttractors,String[] edgeSources){
    
        String[] attractor=new String[sequenceAndAttractors.length];
        String[] sequences=new String[sequenceAndAttractors.length];
        String[][] separatedSequences=new String[sequenceAndAttractors.length][];
        String[] reducedSequence1,reducedSequence2;
        ArrayList <String[]> shortenedStrings=new ArrayList<String[]>();
        ArrayList <String> shortenedAttractors=new ArrayList<String>();
        ArrayList <String[]> finalSequences=new ArrayList<String[]>();
        ArrayList <String> finalAttractors=new ArrayList<String>();
        ArrayList <String[]> resultsSequences=new ArrayList<String[]>();
        ArrayList <String> resultsAttractors=new ArrayList<String>();
        ArrayList <String[]> consistentSequences;
        ArrayList <Integer> indexConsistentSequences;
        ArrayList <Integer> indices;
        HashSet<String> sequence1,sequence2;
        int index,indexAttractor,counter;
        boolean bool;
        String edgeSource,edgeTarget;
        
        for(int i=0;i<sequenceAndAttractors.length;i++){
            attractor[i]=sequenceAndAttractors[i].split("\t")[0];
            sequences[i]=sequenceAndAttractors[i].split("\t")[1];
            separatedSequences[i]=sequences[i].split(" ");
        }

        for(int i=0;i<separatedSequences.length;i++){
            consistentSequences=new ArrayList<String[]>();
            indexConsistentSequences=new ArrayList<Integer>();
            for(int j=0;j<separatedSequences.length;j++){
                if(separatedSequences[j][0].equals(separatedSequences[i][0]) && i!=j){
                    consistentSequences.add(separatedSequences[j]);
                    indexConsistentSequences.add(new Integer (j));
                }
            }
            index=separatedSequences[i].length-1;
            bool=true;
            for(int j=1;j<separatedSequences[i].length;j++){                
                reducedSequence1=Arrays.copyOf(separatedSequences[i],separatedSequences[i].length-j);                   
                for(int k=0;k<consistentSequences.size();k++){       
                    if(consistentSequences.get(k).length>=separatedSequences[i].length-j){
                        reducedSequence2=Arrays.copyOf(consistentSequences.get(k),separatedSequences[i].length-j);
                        indexAttractor=indexConsistentSequences.get(k);
                        if(Arrays.equals(reducedSequence2, reducedSequence1) && !attractor[i].equals(attractor[indexAttractor])){
                            bool=false;break;
                        }                        
                    }
                }
                if(!bool){index=j-1;break;}
            }
            reducedSequence1=Arrays.copyOf(separatedSequences[i],separatedSequences[i].length-index);
            bool=true;
            for(int j=0;j<shortenedStrings.size();j++){
                if(Arrays.equals(shortenedStrings.get(j), reducedSequence1)){bool=false;break;}
            }
            if(bool){shortenedStrings.add(reducedSequence1);shortenedAttractors.add(attractor[i]);}
        
        }
        
//        for(int i=0;i<shortenedStrings.size();i++){
//            System.out.print(shortenedAttractors.get(i)+"\t");
//            for(int j=0;j<shortenedStrings.get(i).length;j++){System.out.print(shortenedStrings.get(i)[j]+" ");}
//            System.out.print("\n");
//        }
        
        for(int i=0;i<shortenedStrings.size();i++){
            edgeSource=shortenedStrings.get(i)[0];
            indices=new ArrayList<Integer>();indices.add(new Integer(0));
            for(int j=1;j<shortenedStrings.get(i).length-1;j++){
                edgeSource=edgeSource+" "+shortenedStrings.get(i)[j];
                
                counter=0;
                bool=false;
                for(int k=0;k<edgeSources.length;k++){
                    if(edgeSources[k].equals(edgeSource)){
                        counter++;
                        if(counter>1){bool=true;break;}
                    }
                }
                if(bool){indices.add(new Integer(j));}
            }
            if(shortenedStrings.get(i).length>1){indices.add(new Integer(shortenedStrings.get(i).length-1));}
            reducedSequence1=new String[indices.size()];
            for(int j=0;j<reducedSequence1.length;j++){
                reducedSequence1[j]=shortenedStrings.get(i)[indices.get(j)];
            }
            
            bool=true;
            for(int j=0;j<finalSequences.size();j++){
                if(Arrays.equals(finalSequences.get(j), reducedSequence1)){bool=false;break;}
            }
            if(bool){finalSequences.add(reducedSequence1);finalAttractors.add(shortenedAttractors.get(i));}
                        
        }
        
//        for(int i=0;i<finalAttractors.size();i++){
//            System.out.print(finalAttractors.get(i)+"\t");
//            for(int j=0;j<finalSequences.get(i).length;j++){System.out.print(finalSequences.get(i)[j]+" ");}
//            System.out.print("\n");
//        }
        
        for(int i=0;i<finalAttractors.size();i++){
            sequence1=new HashSet<String>(Arrays.asList(finalSequences.get(i)));
            bool=true;
            for(int j=0;j<finalAttractors.size();j++){
                if(i!=j && finalSequences.get(i).length>finalSequences.get(j).length){
                    sequence2=new HashSet<String>(Arrays.asList(finalSequences.get(j)));
                    if(sequence1.containsAll(sequence2)){bool=false;}
                }            
            }
            if(bool){
                bool=true;
                sequences=finalSequences.get(i);
                Arrays.sort(sequences);
                for(int j=0;j<resultsSequences.size();j++){
                    if(Arrays.equals(sequences, resultsSequences.get(j))){bool=false;}
                }
                if(bool){resultsSequences.add(sequences);resultsAttractors.add(finalAttractors.get(i));}  
            }
        }
        
        for(int i=0;i<resultsAttractors.size();i++){
            System.out.print(resultsAttractors.get(i)+"\t");
            for(int j=0;j<resultsSequences.get(i).length;j++){System.out.print(resultsSequences.get(i)[j]+" ");}
            System.out.print("\n");
        }
        
        
    }
                 

    public static void simplifySequences(String sinksFilename,String transitionsFilename,String[] motifsToRemove,String[][] attractorsToMerge){
    
        FileToRead fr1=new FileToRead(sinksFilename);
        FileToRead fr2=new FileToRead(transitionsFilename);
        FileToWrite fw;
        ArrayList<String> transitions=new ArrayList<String>();
        ArrayList<String> sinks=new ArrayList<String>();
        String line;
        String[] lineSplit;
        
        while(fr1.hasNext()){
            line=fr1.nextLine();
            line=line.replace("(OscillatingMotif)", "").replace("  ", " ");
            for(int i=0;i<attractorsToMerge[0].length;i++){
                line=line.replace(attractorsToMerge[0][i], attractorsToMerge[1][i]);
            }
            for(String motif : motifsToRemove){
                 line=line.replace(motif, "").replace("  ", " ");
            }
            line=line.trim().replace(" \t","\t").replace("\t ","\t");
            if(!sinks.contains(line)){
                sinks.add(line);
            }
        }
        fr1.close();
        while(fr2.hasNext()){
            line=fr2.nextLine();
            if(!line.contains("(OscillatingMotif)")){
                for(String motif : motifsToRemove){
                    line=line.replace(motif, "").replace("  ", " ").trim().replace(" \t","\t").replace("\t ","\t");                    
                }                
                if(!transitions.contains(line) && line.contains("\t")){
                    lineSplit=line.split("\t");
                    if(!lineSplit[0].equals(lineSplit[1])){transitions.add(line);}
                }
            }
        }
        fr2.close();
        
        fw=new FileToWrite(sinksFilename.split(".txt")[0]+"Modified.txt");
        for(String printLine: sinks){fw.writeLine(printLine);}
        fw.close();
        fw=new FileToWrite(transitionsFilename.split(".txt")[0]+"Modified.txt");
        for(String printLine: transitions){fw.writeLine(printLine);}
        fw.close();

    }

    
    public static boolean containsOscillatingMotifs(String sinksFilename,String transitionsFilename){
        
        FileToRead fr1=new FileToRead(sinksFilename);
        FileToRead fr2=new FileToRead(transitionsFilename);
        String line;
        boolean oscMotifs=false;
        while(fr1.hasNext() && !oscMotifs){
            line=fr1.nextLine();
            oscMotifs=line.contains("(OscillatingMotif)");
        }
        fr1.close();
        while(fr2.hasNext() && !oscMotifs){
            line=fr2.nextLine();
            oscMotifs=line.contains("(OscillatingMotif)");
        }
        fr2.close();
        
        return oscMotifs;
    }
}
