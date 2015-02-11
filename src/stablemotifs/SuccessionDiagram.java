package stablemotifs;

import fileOperations.FileToRead;
import fileOperations.FileToWrite;
import java.util.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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

public class SuccessionDiagram {
    
        DirectedGraph<String, DefaultEdge> graph;
        ArrayList<String []> sequences;
        ArrayList<String []> sequencesOriginal;
        ArrayList<String> motifs;
        ArrayList<String> motifsOriginal;
        ArrayList<String> attractors;
        ArrayList<ArrayList<String []>> motifsControlSet;
        ArrayList<String> names;
        ArrayList<String> functions;
        ArrayList<String[]> controlSets;
        ArrayList<String> controlSetsAttractors;

        
    public SuccessionDiagram (String sinksFilename,String transitionsFilename,String[] names,String[] functions) {
            
            FileToRead fr1=new FileToRead(sinksFilename);
            FileToRead fr2=new FileToRead(transitionsFilename);
            ArrayList<String[]> resultReduction;
            resultReduction=NetworkReduction.iterativeNetworkReduction(names, functions);
            String[] namesReduce=resultReduction.get(0);
            String[] functionsReduce=resultReduction.get(1);
            this.names=new ArrayList<String>(Arrays.asList(namesReduce));
            this.functions=new ArrayList<String>(Arrays.asList(functionsReduce));
            this.motifs=new ArrayList<String>();
            this.sequences=new ArrayList<String[]>();
            this.attractors=new ArrayList<String>();
            String line;
            String[] separatedLine,separatedLine2,sequence;
            
            while(fr1.hasNext()){
                line=fr1.nextLine();
                separatedLine=line.split("\t");
                if(!motifs.contains(separatedLine[0].replace("(","").replace(")",""))){motifs.add(separatedLine[0].replace("(","").replace(")",""));}
                if(!attractors.contains(separatedLine[1].replace("(","").replace(")",""))){attractors.add(separatedLine[1].replace("(", "").replace(")", ""));}
                separatedLine2=separatedLine[0].split(" ");
                sequence=new String[separatedLine2.length+1];
                for(int i=0;i<sequence.length-1;i++){sequence[i]=separatedLine2[i].replace("(","").replace(")","");}
                sequence[sequence.length-1]=separatedLine[1].replace("(","").replace(")","");
                sequences.add(sequence);
            }
            fr1.close();
            
            while(fr2.hasNext()){
                line=fr2.nextLine();
                separatedLine=line.split("\t");
                if(!motifs.contains(separatedLine[0].replace("(","").replace(")",""))){motifs.add(separatedLine[0].replace("(", "").replace(")", ""));}
                if(!motifs.contains(separatedLine[1].replace("(","").replace(")",""))){motifs.add(separatedLine[1].replace("(", "").replace(")", ""));}
            }
            fr2.close();
            motifsOriginal=new ArrayList<String>(motifs);
            sequencesOriginal=new ArrayList<String[]>(sequences);
            
            fr1=new FileToRead(sinksFilename);
            fr2=new FileToRead(transitionsFilename);
            this.graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class); 
            for(int i=0;i<motifs.size();i++){this.graph.addVertex(motifs.get(i));}
            for(int i=0;i<attractors.size();i++){this.graph.addVertex(attractors.get(i));}
            while(fr1.hasNext()){
                line=fr1.nextLine();
                separatedLine=line.split("\t");
                this.graph.addEdge(separatedLine[0].replace("(", "").replace(")", ""),separatedLine[1].replace("(", "").replace(")", ""));
            }
            fr1.close();
            while(fr2.hasNext()){
                line=fr2.nextLine();
                separatedLine=line.split("\t");
                this.graph.addEdge(separatedLine[0].replace("(", "").replace(")", ""),separatedLine[1].replace("(", "").replace(")", ""));
            }
            fr2.close();
            
        }
        
    private void shortenSequences(){
            ArrayList<String> motifCuts=motifsLeadToSingleAttractor();
            String motif,motif2,sequence,attractor;
            String[] sequenceSeparated,newSequence;
            for(int i=0;i<motifCuts.size();i++){
                attractor="";
                motif=motifCuts.get(i);
                for(int j=0;j<sequencesOriginal.size();j++){
                    sequence="";
                    sequenceSeparated=sequencesOriginal.get(j);
                    for(int k=0;k<sequenceSeparated.length-1;k++){sequence=sequence+" "+sequenceSeparated[k];}
                    sequenceSeparated=sequence.split(motif);
                    if(sequenceSeparated[0].equals(" ")){
                        sequences.remove(sequencesOriginal.get(j));
                        if(attractor.equals("")){sequenceSeparated=sequencesOriginal.get(j);attractor=sequenceSeparated[sequenceSeparated.length-1];}
                    }
                }
                for(int j=0;j<motifsOriginal.size();j++){                    
                    motif2=motifsOriginal.get(j);
                    if(!motif.equals(motif2)){
                        sequenceSeparated=motif2.split(motif);
                        if(sequenceSeparated[0].equals("")){motifs.remove(motifsOriginal.get(j));}
                    }
                }
                sequenceSeparated=motif.split(" ");
                newSequence=new String[sequenceSeparated.length+1];
                System.arraycopy(sequenceSeparated, 0, newSequence, 0, newSequence.length-1);
                newSequence[newSequence.length-1]=attractor;
                sequences.add(newSequence);                
            }
            

}
        
    private ArrayList<String> motifsLeadToSingleAttractor(){
            boolean leadsToSingleAttractor;
            boolean nextToSource;
            Set vertices=graph.vertexSet();
            Set<DefaultEdge> edges;
            ArrayList<String> sources=new ArrayList<String>();
            ArrayList<String> sinks=new ArrayList<String>();
            ArrayList<String> newSources;
            ArrayList<String> motifCuts=new ArrayList<String>();
            Iterator itr;
            GraphIterator dfitr;
            String node,sinkNode;
            itr=vertices.iterator();
            while(itr.hasNext()){
                node=(String) itr.next();
                if(graph.inDegreeOf(node)==0){sources.add(node);}
                if(graph.outDegreeOf(node)==0){sinks.add(node);}
            }
            do{ newSources=new ArrayList<String>();
                for(int i=0;i<sources.size();i++){
                    node=sources.get(i);
                    nextToSource=false;
                    if(graph.outDegreeOf(node)==1){
                        //Does not consider motifs in the diagram next to an attractor, since they necessarily lead to a single attractor
                        edges=graph.outgoingEdgesOf(node);
                        for (DefaultEdge e : edges) {
                           sinkNode=graph.getEdgeTarget(e);
                           if(sinks.contains(sinkNode)){nextToSource=true;}
                       }
                    }
                    if(!nextToSource){
                        sinkNode="";
                        leadsToSingleAttractor=true;
                        dfitr=new DepthFirstIterator(graph, node);
                        while(dfitr.hasNext() && leadsToSingleAttractor){
                            node=(String) dfitr.next();
                            if(graph.outDegreeOf(node)==0){
                                if(sinkNode.equals("")){sinkNode=node;}
                                else{leadsToSingleAttractor=false;}
                            }
                        }
                        if(leadsToSingleAttractor){motifCuts.add(sources.get(i));}
                        else{
                        node=sources.get(i);    
                        edges=graph.outgoingEdgesOf(node);
                        for (DefaultEdge e : edges) {
                            node=graph.getEdgeTarget(e);
                            if(graph.outDegreeOf(node)!=0){newSources.add(node);}
                        }
                        }
                    }
                }
                sources=new ArrayList<String>(newSources);
            }while(sources.size()>0);
            return motifCuts;
            
}
        
    private void findAllMotifControlSets(){
            String motif;
            ArrayList<String []> motifControlSet;
            int counter=0;
            motifsControlSet=new ArrayList<ArrayList<String []>>();
            for(int i=0;i<motifs.size();i++){
                counter++;                
                motif=motifs.get(i);                
                motifControlSet=findMotifControlSet(motif);
                motifsControlSet.add(motifControlSet);
                if(counter==100){System.out.println((i+1)+"/"+motifs.size()+" motifs done...");counter=0;}                
            }            
        }
        
    private ArrayList<String []> findMotifControlSet(String motif){
            String currentMotif;
            String[] pastMotifs;
            String[] motifSeparated;
            ArrayList<String> motifArray=new ArrayList<String>();
            ArrayList<Integer> motifArrayIndices=new ArrayList<Integer>();
            ArrayList<String> motifStates=new ArrayList<String>();
            String[] motifElements,motifPastElements,separatedLine; 
            int index,indexSet;
            String nodeNameState;
            boolean subsetContained;
            String[] motifNames,motifFunctions,motifNamesOriginal,motifFunctionsOriginal;
            ArrayList<ArrayList<Integer>> subsets;
            ArrayList<Integer> subset;
            ArrayList<ArrayList<Integer>> controlSetIndices=new ArrayList<ArrayList<Integer>>();
            ArrayList<String[]> resultReduction;
            ArrayList<String> namesReduceArray;
            ArrayList<String []> motifControlSets=new ArrayList<String[]>();
            String [] motifControlSetArray;
            String[] functionsReduce,namesReduce;
            
            motifSeparated=motif.split(" ");
            currentMotif=motifSeparated[motifSeparated.length-1];
            motifElements=currentMotif.split("-");
            if(motifElements.length==1){
                separatedLine=motifElements[0].split("=");
                motifControlSetArray=new String[motifElements.length];
                motifControlSetArray[0]=separatedLine[0]+"="+separatedLine[1];
                motifControlSets.add(motifControlSetArray); 
            }
            else{
                for(int i=0;i<motifElements.length;i++){
                    separatedLine=motifElements[i].split("=");
                    motifArray.add(separatedLine[0]);
                    motifStates.add(separatedLine[1]);
                    motifArrayIndices.add(names.indexOf(separatedLine[0]));
                }

                pastMotifs=Arrays.copyOf(motifSeparated, motifSeparated.length-1);
                functionsReduce=this.functions.toArray(new String[this.functions.size()]);
                namesReduce=this.names.toArray(new String[this.names.size()]);
                for(int i=0;i<pastMotifs.length;i++){
                    motifPastElements=pastMotifs[i].split("-");
                    for(int j=0;j<motifPastElements.length;j++){
                        separatedLine=motifPastElements[j].split("=");
                        index=names.indexOf(separatedLine[0]);
                        functionsReduce[index]=separatedLine[1];
                    }
                }
                resultReduction=NetworkReduction.iterativeNetworkReduction(namesReduce, functionsReduce);
                namesReduce=resultReduction.get(0);
                functionsReduce=resultReduction.get(1);
                namesReduceArray=new ArrayList<String>(Arrays.asList(namesReduce));
                motifNamesOriginal = motifArray.toArray(new String[motifElements.length]);
                motifFunctionsOriginal=new String[motifElements.length];
                for(int i=0;i<motifElements.length;i++){
                    index=namesReduceArray.indexOf(motifArray.get(i));
                    motifFunctionsOriginal[i]=functionsReduce[index];
                }
                for(int m=1;m<=motifElements.length-1;m++){
                    subsets=OtherMethods.SubsetsOfSize(motifElements.length, m);
                    for(int i=0;i<subsets.size();i++){
                        subset=subsets.get(i);
                        subsetContained=false;
                        for(int j=0;j<controlSetIndices.size();j++){
                            if(subset.containsAll(controlSetIndices.get(j))){subsetContained=true;break;}
                        }
                        if(!subsetContained){
                            motifFunctions=Arrays.copyOf(motifFunctionsOriginal, motifFunctionsOriginal.length);
                            motifNames=Arrays.copyOf(motifNamesOriginal, motifNamesOriginal.length);
                            for(int j=0;j<subset.size();j++){
                                indexSet=subset.get(j);
                                motifFunctions[indexSet]=motifStates.get(indexSet);                                
                            }
                            resultReduction=NetworkReduction.iterativeNetworkReductionSubnetwork(motifNames, motifFunctions);
                            if(resultReduction.get(0).length==0){controlSetIndices.add(subset);}                            
                        }
                    }                
                }
                if(controlSetIndices.isEmpty()){
                    subset=new ArrayList<Integer>();
                    for(int i=0;i<motifElements.length;i++){subset.add(new Integer(i));}
                    controlSetIndices.add(subset);
                }

                for(int j=0;j<controlSetIndices.size();j++){
                    subset=controlSetIndices.get(j);
                    motifControlSetArray=new String[subset.size()];
                    for(int k=0;k<subset.size();k++){
                        indexSet=subset.get(k);
                        nodeNameState=motifArray.get(indexSet)+"="+motifStates.get(indexSet);
                        motifControlSetArray[k]=nodeNameState;
                    }
                    motifControlSets.add(motifControlSetArray);   
                }            
            }
            
            return motifControlSets;
            
        }
   
    public void findControlSets(){
            
            ArrayList<String[]> tempControlSets=new ArrayList<String[]>();
            ArrayList<HashSet<String>> tempControlSet;
            ArrayList<Integer> resultControlSets;
            ArrayList<ArrayList<HashSet<String>>> attractorControlSets=new ArrayList<ArrayList<HashSet<String>>>();
            Map<String,Integer> map = new HashMap();
            ArrayList<String> tempControlSetsAttractors=new ArrayList<String>();
            ArrayList<String []> motifControlSet;
            ArrayList<String []> pastControlSet=new ArrayList<String[]>();
            ArrayList<String []> currentControlSet;
            String attractor;
            String motif;
            String[] sequenceNext;
            String[] sequence;
            int index;
            HashSet<String> set;
            System.out.println("Shortening stable motif sequences.");
            shortenSequences();
            System.out.println("Finding control sets for each stable motif...");
            findAllMotifControlSets();
            
            System.out.println("Creating control sets for each stable motif sequence.");
            for(int a=0;a<attractors.size();a++){attractorControlSets.add(new ArrayList<HashSet<String>>());}
            for(int s=0;s<sequences.size();s++){
                sequence=sequences.get(s);
                attractor=sequence[sequence.length-1];
                for(int i=1;i<sequence.length;i++){
                    motif="";
                    for(int j=0;j<i;j++){
                        if(j>0){motif=motif+" "+sequence[j];}
                        else{motif=motif+sequence[j];}
                        
                    }
                    index=motifs.indexOf(motif);
                    motifControlSet=this.motifsControlSet.get(index);
                    if(i==1){currentControlSet=new ArrayList<String[]>(motifControlSet);}
                    else{
                        currentControlSet=new ArrayList<String[]>();
                        for(String[] sequencePast: pastControlSet){
                            for(String[] sequencePresent: motifControlSet){
                                sequenceNext=Arrays.copyOf(sequencePast, sequencePresent.length+sequencePast.length);
                                System.arraycopy(sequencePresent, 0, sequenceNext, sequencePast.length, sequencePresent.length);
                                currentControlSet.add(sequenceNext);
                            }
                        }
                    }
                    pastControlSet=new ArrayList<String[]>(currentControlSet);
                }
                for(String[] sequencePast: pastControlSet){
                    set=new HashSet<String>(Arrays.asList(sequencePast));
                    index=attractors.indexOf(attractor);
                    attractorControlSets.get(index).add(set);
                    tempControlSets.add(sequencePast);
                    tempControlSetsAttractors.add(attractor);
                    map.put(index+"_"+(attractorControlSets.get(index).size()-1), new Integer(tempControlSets.size()-1));
                }                  
            }
            System.out.println("Removing duplicates control sets.");
            controlSets=new ArrayList<String[]>();
            controlSetsAttractors= new ArrayList<String>();
            for(int s=0;s<attractorControlSets.size();s++){
                tempControlSet=attractorControlSets.get(s);
                resultControlSets=removeDuplicateControlSets(tempControlSet);
                for(Integer result : resultControlSets){
                    controlSets.add(tempControlSets.get(map.get(s+"_"+result)));
                    controlSetsAttractors.add(attractors.get(s));
                }
            }
        
        }
    
    public void findControlSetsWithPartialSuccessionDiagram(){
             
        ArrayList<String[]> tempControlSets=new ArrayList<String[]>();
        ArrayList<HashSet<String>> tempControlSet;
        ArrayList<Integer> resultControlSets;
        ArrayList<ArrayList<HashSet<String>>> attractorControlSets=new ArrayList<ArrayList<HashSet<String>>>();
        Map<String,Integer> map = new HashMap();
        ArrayList<String> tempControlSetsAttractors=new ArrayList<String>();
        ArrayList<String []> motifControlSet;
        ArrayList<String []> pastControlSet=new ArrayList<String[]>();
        ArrayList<String []> currentControlSet;
        String attractor;
        String motif;
        String[] sequenceNext;
        String[] sequence;
        int index;
        HashSet<String> set;
        System.out.println("Finding control sets for each stable motif...");
        findAllMotifControlSets();

        System.out.println("Creating control sets for each stable motif sequence.");
        for(int a=0;a<attractors.size();a++){attractorControlSets.add(new ArrayList<HashSet<String>>());}
        for(int s=0;s<sequences.size();s++){
            sequence=sequences.get(s);
            attractor=sequence[sequence.length-1];
            for(int i=1;i<sequence.length;i++){
                motif="";
                for(int j=0;j<i;j++){
                    if(j>0){motif=motif+" "+sequence[j];}
                    else{motif=motif+sequence[j];}

                }
                index=motifs.indexOf(motif);
                motifControlSet=this.motifsControlSet.get(index);
                if(i==1){currentControlSet=new ArrayList<String[]>(motifControlSet);}
                else{
                    currentControlSet=new ArrayList<String[]>();
                    for(String[] sequencePast: pastControlSet){
                        for(String[] sequencePresent: motifControlSet){
                            sequenceNext=Arrays.copyOf(sequencePast, sequencePresent.length+sequencePast.length);
                            System.arraycopy(sequencePresent, 0, sequenceNext, sequencePast.length, sequencePresent.length);
                            currentControlSet.add(sequenceNext);
                        }
                    }
                }
                pastControlSet=new ArrayList<String[]>(currentControlSet);
            }
            for(String[] sequencePast: pastControlSet){
                set=new HashSet<String>(Arrays.asList(sequencePast));
                index=attractors.indexOf(attractor);
                attractorControlSets.get(index).add(set);
                tempControlSets.add(sequencePast);
                tempControlSetsAttractors.add(attractor);
                map.put(index+"_"+(attractorControlSets.get(index).size()-1), new Integer(tempControlSets.size()-1));
            }                  
        }
        System.out.println("Removing duplicates control sets.");
        controlSets=new ArrayList<String[]>();
        controlSetsAttractors= new ArrayList<String>();
        for(int s=0;s<attractorControlSets.size();s++){
            tempControlSet=attractorControlSets.get(s);
            resultControlSets=removeDuplicateControlSets(tempControlSet);
            for(Integer result : resultControlSets){
                controlSets.add(tempControlSets.get(map.get(s+"_"+result)));
                controlSetsAttractors.add(attractors.get(s));
            }
        }

    }
    
    public ArrayList<String[]> getControlSets(){
        return controlSets;
    }
    
    public ArrayList<String> getControlSetAttractors(){
        return controlSetsAttractors;
    }
    
    private ArrayList<Integer> removeDuplicateControlSets(ArrayList<HashSet<String>> tempControlSet) {
            ArrayList<Integer> noDuplicates=new ArrayList<Integer>();
            HashSet<String> set1;
            HashSet<String> set2;

            boolean superSet;
            for(int i=0;i<tempControlSet.size();i++){               
                set1=tempControlSet.get(i);
                superSet=false;
                for(int j=0;j<tempControlSet.size();j++){
                    if(i!=j){
                        set2=tempControlSet.get(j);
                        if(set1.containsAll(set2) && set1.size()!=set2.size()){superSet=true;break;}
                        else if(set1.containsAll(set2) && set1.size()==set2.size() && i>j){superSet=true;break;}
                    }
                }                
                if(!superSet){noDuplicates.add(new Integer(i));}
            }

            return noDuplicates;
        
    }
    
        public void writeStableMotifControlSets(String fileName){
            FileToWrite fw=new FileToWrite(fileName);
            String line;
            String[] str;
            for(int i=0;i<controlSets.size();i++){
                line="";
                str=controlSets.get(i);
                for(int j=0;j<str.length-1;j++){
                    line=line+str[j]+" ";
                }
                line=line+str[str.length-1]+"\t"+this.controlSetsAttractors.get(i);
                fw.writeLine(line);
            }
            fw.close();
        
        }

}
