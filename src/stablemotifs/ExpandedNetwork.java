package stablemotifs;

import de.normalisiert.utils.graphs.ElementaryCyclesSearchforStableSCC;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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

public class ExpandedNetwork {
    
    private int Nexpanded;  //This is the number of nodes in the expanded network
    private String[] namesExpandedNetwork;
    private ArrayList<String> namesExpandedNetworkList;
    private ArrayList<String> namesList;
    private String[] numberedNames;
    private String[] originalNames;
    private String[] originalFunctions;
    private String[][] newFunctions;
    private String[][] ArbitrarySizeMotifsFunctions;
    private ArrayList<String> numberedNamesList;
    private ArrayList<ArrayList<String>> stableSCC;
    private ArrayList<ArrayList<String>> stableSCCresult;
    private String[][] stronglyConnectedComponents;
    private String[] oscillatingSCC;
    private ArrayList<ArrayList<String>> numberedStronglyConnectedComponents;
    private ArrayList<ArrayList<String>>  stableStates;
    private ArrayList<ArrayList<String>>  stableValues;
    private ArrayList<Boolean>  stableType;
    private ArrayList<ArrayList<String>>  oscillatingStates;
    private ArrayList<ArrayList<String>>  oscillationType;
    private int[][] adjacencyList;
    private int[][] nodeInputs;
    private Dictionary dictionary;
    private ArrayList<String> compositeNodes;
    private ArrayList<ArrayList<String>> cycles;
    
    public ExpandedNetwork (String [] names, String [] functions) {
    
    this.originalNames=Arrays.copyOf(names, names.length);
    this.originalFunctions=Arrays.copyOf(functions, functions.length);
    this.namesList=new ArrayList<String>(Arrays.asList(originalNames));
    Set<String> namesList=new HashSet<String>();
    Set<String> regulatorsList;
    Set<String> compositeList;
    Set<String[][]> regulatorsArrayList;
    Set<String> [] negationRegulatorsList;
    Set<Integer> indexList;
    String [] namesExpanded,complementary,negations;
    String[][][] regulators,regulatorsNegations;
    String[][] regulatorsExpanded,regulatorsNegationsExpanded;
    Integer [] dummyInt;
    String[] stringDummy;
    String[][] stringdoubleDummy;
    String newNode;
    int [] indexArray,countArray;
    int [][] networkMatrix;
    int index,indexNegations,N,indexi,indexj;
    int [] relevantComposites;
    Boolean boolDummy1,boolDummy2,boolDummy3;

    //The rules must be in the following form to work.
    //NodeI* = (NodeIi1 and not Node Ii2 and ... and NodeIin) or (NodeIj1 and not Node Ij2 and ... and NodeIjn) or ... or (NodeIz1 and not Node Iz2 and ... and NodeIzn)
    //..
    //

    regulators=new String[names.length][][]; //the regulators of every node are saved here
    negations=new String[names.length]; //we will store the name of the negation of the regulators here
    complementary= new String[0];
    namesExpanded=Arrays.copyOf(names, names.length);
    regulatorsExpanded=new String[names.length][];
    regulatorsNegationsExpanded=new String[names.length][];
    regulatorsNegations=new String[negations.length][][];
    negationRegulatorsList=new HashSet[names.length];  //Now we create the composite nodes and the negative nodes


    for(int i=0;i<names.length;i++){
        namesList.add("~"+names[i]); //We now add the negations of the nodes to the name List
    }


    //In case one of the nodes has no regulators, gives back a warning
    //In such a case the function assigned to that node will be the identity
    //function
    for(int i=0;i<names.length;i++){  
        if(functions[i]==null){
            System.out.println("Found no function for node "+names[i]+". I will set the identity as the function");
            regulators[i]=new String[1][1];
            regulators[i][0][0]=names[i];
            functions[i]=names[i];
        }
        negations[i]="~"+names[i]; //Name of the names of the negation to facilitate searching for the index
        negationRegulatorsList[i]=new HashSet<String>();   //Set with the regulators of the negations

    }


    //Gets the regulators from each of the rules. Each of the elements separated by an or rules counts as a "regulator" since
    //it will be a composite node in the expanded network
   
    for(int i=0;i<names.length;i++){        
        regulators[i]=createRegulatorsArray(functions[i].replace("("," ( ").replace(")"," ) ").replace("not ","~"));
    }        



    for(int i=0;i<names.length;i++){
        regulatorsList=new HashSet<String>();
        for(int j=0;j<regulators[i].length;j++){
            //This create the name of the composite node and creates a new composite node if it doesnt exist

            boolDummy3=true; //If the composite node contains a node and its negations, in which case its irrelevant
            indexList=new HashSet<Integer>(); //We use a set to store the indexes in case the elements in a composite node
                                                //get repeated

            if(regulators[i][j].length>1){
                for(int k=0;k<regulators[i][j].length;k++){
                    if(!regulators[i][j][k].startsWith("~")){
                        index=Arrays.asList(names).indexOf(regulators[i][j][k]);
                        indexList.add(index);
                        if(indexList.contains(-index-1)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }

                    }
                    else if(regulators[i][j][k].startsWith("~")){
                        index=Arrays.asList(negations).indexOf(regulators[i][j][k]);
                        indexList.add(-index-1);
                        if(indexList.contains(index)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                    }

                    else{
                        System.out.println("Couldn't find node "+regulators[i][j][k]+" in list of names");
                        System.exit(0);
                    }
                }

                if(boolDummy3){ //In case the composite node contains a node and its negations then we skip everything
                    dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
                    indexArray=new int[dummyInt.length]; 
                    for(int k=0;k<dummyInt.length;k++){indexArray[k]=dummyInt[k].intValue();}

                    indexArray=OtherMethods.orderArraybyOrder(indexArray, 0, indexArray.length-1,OtherMethods.orderHighesttoLowest(indexArray, 0, indexArray.length-1));
                    //We are ordering the input nodes of the composite node for higher to lower to have a unique name for each combination
                    index=indexArray[0];
                    if(index>=0){
                        newNode=namesExpanded[index];
                        }
                    else{
                        newNode=negations[-index-1];
                    }
                    for(int k=1;k<regulators[i][j].length;k++){
                        index=indexArray[k];
                        if(index>=0){
                            newNode=newNode+"_"+namesExpanded[index];
                        }
                        else{
                            newNode=newNode+"_"+negations[-index-1];
                        }
                    }
                    if(!namesList.contains(newNode)){
                        //if it hasn't been added before we add it to the list of node names
                        namesList.add(newNode);
                        complementary=Arrays.copyOf(complementary,complementary.length+1);
                        complementary[complementary.length-1]=newNode;
                        namesExpanded=Arrays.copyOf(namesExpanded,namesExpanded.length+1);
                        namesExpanded[namesExpanded.length-1]=newNode;
                        regulatorsExpanded=Arrays.copyOf(regulatorsExpanded,regulatorsExpanded.length+1);
                        regulatorsExpanded[regulatorsExpanded.length-1]=Arrays.copyOf(regulators[i][j], regulators[i][j].length);
                    }
                    regulatorsList.add(newNode);
                }

            }
            //This is for the case the input node is not a composite node, but may still be a negative node
            else{
                if(!regulators[i][j][0].startsWith("~")){

                }
                else if(regulators[i][j][0].startsWith("~")){

                }
                else{
                        System.out.println("Couldn't find node "+regulators[i][j][0]+" in list of names");
                        System.exit(0);
                }
                regulatorsList.add(regulators[i][j][0]);

            }

        }

        //We now add all the regulators of this node to its corresponding array
        regulatorsExpanded[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);

    }

    //In this part we look for the negation of all nodes. The idea is that something like
    //A*= (B and not C) or (D and not E) is transformed to
    //not A= (not B and not D) or (not B and E) or (C and not D) or (C and E)
    //What the following code does is transform the A* into the not A* function
    for(int i=0;i<negations.length;i++){
        regulatorsList=new HashSet<String>();
        regulatorsArrayList=new HashSet<String[][]>(); //We will store all the composite nodes affecting the negative nodes
                                                    //here since we don't actually now how many nodes will each composite node
                                                    //have, nor how many composite nodes per negation node will there be. The first row
                                                    //contains the elements, the second the name of the composite node
        countArray=new int[regulators[i].length];

        boolDummy1=true;
        indexNegations=0;
        while(boolDummy1){
            compositeList=new HashSet<String>();
            indexList=new HashSet<Integer>();
            boolDummy3=true;
            for(int j=0;j<regulators[i].length;j++){

                if(regulators[i][j][countArray[j]].startsWith("~")){
                    compositeList.add(regulators[i][j][countArray[j]].substring(1));
                    //with this we only add the name of the negative regulator, which is what we want since we are taking the negation of the function
                    index=Arrays.asList(negations).indexOf(regulators[i][j][countArray[j]]);
                    indexList.add(index);
                    if(indexList.contains(-index-1)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                }
                else{
                    compositeList.add("~"+regulators[i][j][countArray[j]]);
                    //with this we add the negation of the regulator, which is what we want since we are taking the negation of the function
                    index=Arrays.asList(names).indexOf(regulators[i][j][countArray[j]]);
                    indexList.add(-index-1);
                    //since the regulator is negative, we need to distinguish it for the positive node
                    if(indexList.contains(index)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                }

            }

            index=0;
            boolDummy2=true;          
            while(boolDummy2){
                countArray[index]++;
                if(countArray[index]>=regulators[i][index].length){
                    //If it reaches the maximum allowed values of the index, it will now try to increase the next entry in the array
                    //, similar to when 09 transforms into 10 when you sum 01 to 09.
                    countArray[index]=0;
                    index++;
                }
                else{
                    boolDummy2=false; //When a valid index for the countArray index is obtained it stops searching. The numbers in
                    //countArray will be used in then next cycle controlled by boolDummy1 to create the following composite node
                }

                if(index==regulators[i].length){
                    boolDummy1=false;  //This means that we have explored all possible composite nodes for the negations of the nodes
                    boolDummy2=false;
                }

            }

            if(boolDummy3){ //In case the composite node contains a node and its negations then we skip everything


                    dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
                    indexArray=new int[dummyInt.length];
                    //regulatorsArrayList.add((String[])compositeList.toArray(new String[compositeList.size()]));

                    for(int k=0;k<dummyInt.length;k++){indexArray[k]=dummyInt[k].intValue();}

                    indexArray=OtherMethods.orderArraybyOrder(indexArray, 0, indexArray.length-1,OtherMethods.orderHighesttoLowest(indexArray, 0, indexArray.length-1));
                    //We are ordering the input nodes of the composite node for higher to lower to have a unique name for each combination
                    index=indexArray[0];
                    if(index>=0){
                        newNode=namesExpanded[index];
                        }
                    else{
                        newNode=negations[-index-1];
                    }
                    for(int k=1;k<indexArray.length;k++){
                        index=indexArray[k];
                        if(index>=0){
                            newNode=newNode+"_"+namesExpanded[index];
                        }
                        else{
                            newNode=newNode+"_"+negations[-index-1];
                        }
                    }
                    if(!namesList.contains(newNode)){
                        //if it hasn't been added before we add it to the list of node names                            
                        namesList.add(newNode);
                        complementary=Arrays.copyOf(complementary,complementary.length+1);
                        complementary[complementary.length-1]=newNode;
                        namesExpanded=Arrays.copyOf(namesExpanded,namesExpanded.length+1);
                        namesExpanded[namesExpanded.length-1]=newNode;
                        regulatorsExpanded=Arrays.copyOf(regulatorsExpanded,regulatorsExpanded.length+1);
                        regulatorsExpanded[regulatorsExpanded.length-1]=Arrays.copyOf((String[])compositeList.toArray(new String[compositeList.size()]), compositeList.size());
                    }
                    if(!regulatorsList.contains(newNode)){
                        regulatorsList.add(newNode);
                        stringdoubleDummy=new String[2][];
                        stringdoubleDummy[0]=(String[])compositeList.toArray(new String[compositeList.size()]);
                        stringdoubleDummy[1]=new String[1];stringdoubleDummy[1][0]=newNode;
                        regulatorsArrayList.add(stringdoubleDummy);
                    }
                    indexNegations++;
            }


        }
        regulatorsNegationsExpanded[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
        //We now add all the regulators of this node to its corresponding array. The composite nodes in this array are just a single name

        regulatorsNegations[i]=new String[regulatorsArrayList.size()][];
        Iterator it=regulatorsArrayList.iterator();
        while(it.hasNext()){
            stringdoubleDummy=(String[][]) it.next();
            index=Arrays.asList(regulatorsNegationsExpanded[i]).indexOf(stringdoubleDummy[1][0]);
            regulatorsNegations[i][index]= Arrays.copyOf(stringdoubleDummy[0], stringdoubleDummy[0].length);
        }




    }

    //We need to remove the composite input nodes that are irrelevant in the negations. For example if we have
    //~A*= B or (~C and B) , this is actually equivalent to ~A*=B , but because of the way we go the function of the
    //negation we would have both

    for(int i=0;i<negations.length;i++){

            indexArray=new int[regulatorsNegationsExpanded[i].length];
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){ //We do this for every input node

                    for(int l=0;l<regulatorsNegations[i].length;l++){ //We check every other input node to see if this node is irrelevant

                        if(regulatorsNegations[i][j].length < regulatorsNegations[i][l].length && indexArray[l]==0 && indexArray[j]==0){
                            //For the node ij no te be irrelevant it must be contained in the il node. For it to be irrelevant we check if every node in
                            //ij is in il
                            boolDummy2=true;
                            index=0;
                            while(index<regulatorsNegations[i][j].length && boolDummy2){
                                //if it finds the nodes ij index inside il, then ij could be contained in il
                                boolDummy2=Arrays.asList(regulatorsNegations[i][l]).contains(regulatorsNegations[i][j][index]);
                                index++;
                            }
                            //if it found the node ijindex for all nodes, then ij is cointained in il. Then il is irrelevant
                            if(boolDummy2){
                                indexArray[l]=1;
                            }
                        }

                    }

            }
            index=0; //We get the number of relevant regulators
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){if(indexArray[j]==0){index++;}}
            stringDummy=new String[index];
            stringdoubleDummy=new String[index][];
            index=0;
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){
                if(indexArray[j]==0){
                    stringDummy[index]=regulatorsNegationsExpanded[i][j];
                    stringdoubleDummy[index]=Arrays.copyOf(regulatorsNegations[i][j], regulatorsNegations[i][j].length);
                    index++;
                }
            }
            
            regulatorsNegationsExpanded[i]=Arrays.copyOf(stringDummy,stringDummy.length);            
            regulatorsNegations[i]=Arrays.copyOf(stringdoubleDummy,stringdoubleDummy.length);
            if(regulatorsExpanded[i][0].equals("UnstableOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="UnstableOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="UnstableOscillation";
            }
            if(regulatorsExpanded[i][0].equals("IncompleteOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="IncompleteOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="IncompleteOscillation";
            }
            if(regulatorsExpanded[i][0].equals("FullOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="FullOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="FullOscillation";
            }

    }


    N=namesExpanded.length+negations.length; //This the total number of nodes in the expanded network
    networkMatrix=new int[N][N]; //This matrix will contain all the connectivity information of the network
    for(int i=0;i<names.length;i++){
        for(int j=0;j<regulatorsExpanded[i].length;j++){           
            if(!regulatorsExpanded[i][j].equals("UnstableOscillation")&&!regulatorsExpanded[i][j].equals("FullOscillation")&&!regulatorsExpanded[i][j].equals("IncompleteOscillation")){
                //We first check if the regulator is a positive or composite
                index=Arrays.asList(namesExpanded).indexOf(regulatorsExpanded[i][j]);
                if(index==-1 ){
                    index=-1*Arrays.asList(negations).indexOf(regulatorsExpanded[i][j])-1;
                    //If the node is negative we need to search in the negative list
                }

                if(index>=names.length){
                    index=index+names.length; //We will put the composite nodes after the normal and negative ones
                }
                else if(index<0){
                    index=-1*index-1; //We will put the negative nodes before the positive ones
                }
                else{
                    index=index+names.length;
                }
                networkMatrix[i+names.length][index]=1; //We will put the negative nodes before the positive ones
            }
        }
    }

    for(int i=0;i<negations.length;i++){
        for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){
            if(!regulatorsNegationsExpanded[i][j].equals("UnstableOscillation")&&!regulatorsNegationsExpanded[i][j].equals("FullOscillation")&&!regulatorsNegationsExpanded[i][j].equals("IncompleteOscillation")){
                //We first check if the regulator is a positive or composite
                index=Arrays.asList(namesExpanded).indexOf(regulatorsNegationsExpanded[i][j]);
                if(index==-1){
                    index=-1*Arrays.asList(negations).indexOf(regulatorsNegationsExpanded[i][j])-1;
                }
                if(index>=names.length){
                    index=index+names.length; //We will put the composite nodes after the normal and negative ones
                }
                else if(index<0){
                    index=-1*index-1; //We will put the negative nodes before the positive ones
                }
                else{
                    index=index+names.length;
                }
                networkMatrix[i][index]=1; //We will put the negative nodes before the positive ones
            }
        }

    }

    for(int i=names.length;i<namesExpanded.length;i++){
        for(int j=0;j<regulatorsExpanded[i].length;j++){
            //We first check if the regulator is a positive or composite
            index=Arrays.asList(namesExpanded).indexOf(regulatorsExpanded[i][j]);
            if(index==-1){
                index=-1*Arrays.asList(negations).indexOf(regulatorsExpanded[i][j])-1;
            }
            if(index>=names.length){
                index=index+names.length; //We will put the composite nodes after the normal and negative ones
            }
            else if(index<0){
                index=-1*index-1; //We will put the negative nodes before the positive ones
            }
            else{
                index=index+names.length;
            }
            networkMatrix[i+names.length][index]=1; //The composite nodes go after the normal and composite ones
        }

    }
    
    this.Nexpanded=N;
    relevantComposites=new int[N];
    for(int i=0;i<2*names.length;i++){relevantComposites[i]=1;}
    for(int i=2*names.length;i<N;i++){
        index=0;
        boolDummy1=true;
        while(boolDummy1 && index<2*names.length){
            if(networkMatrix[index][i]==1)
            {boolDummy1=false;} //if the composite node has at least an output, then it is relevant
            index++;
        }
        if(boolDummy1){
            relevantComposites[i]=0;
            this.Nexpanded--;
        }
        else{
            relevantComposites[i]=1;
        }

    }
    
    this.adjacencyList=new int[Nexpanded][];
    //This will be the output, and will contain the adjacency List of the expanded network
    //The adjacency list is an array which contains the outputs of every nodes. The first
    //dimension in the array represents the same node as in the namesExpandedFinal array
    //, the second dimension represents the indices of those nodes,
    //that are direct successors of the node.
    this.nodeInputs=new int[Nexpanded][];
    this.namesExpandedNetwork=new String[Nexpanded];
    this.numberedNames=new String[Nexpanded];
    
    indexi=0;
    for(int i=0;i<N;i++){
        if(relevantComposites[i]==1){
            if(i<names.length){this.namesExpandedNetwork[indexi]=negations[i];}
            else if (i<2*names.length){this.namesExpandedNetwork[indexi]=names[i-names.length];}
            else{this.namesExpandedNetwork[indexi]=namesExpanded[i-names.length];}
            
            indexj=0;
            indexList=new HashSet<Integer>();
            for(int j=0;j<N;j++){
                if(relevantComposites[j]==1){
                    if(networkMatrix[j][i]==1){indexList.add(indexj);}
                    indexj++;
                }
            }
            dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
            adjacencyList[indexi]=new int[dummyInt.length];
            for(int k=0;k<dummyInt.length;k++){adjacencyList[indexi][k]=dummyInt[k].intValue();}
            indexi++;
        }
        
    }
    this.namesExpandedNetworkList=new ArrayList(Arrays.asList(namesExpandedNetwork));
    
    indexi=0;
    for(int i=0;i<N;i++){
        if(relevantComposites[i]==1){
            indexj=0;
            indexList=new HashSet<Integer>();
            for(int j=0;j<N;j++){
                if(relevantComposites[j]==1){
                    if(networkMatrix[i][j]==1){indexList.add(indexj);}
                    indexj++;
                }
            }
            dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
            nodeInputs[indexi]=new int[dummyInt.length];
            for(int k=0;k<dummyInt.length;k++){nodeInputs[indexi][k]=dummyInt[k].intValue();}
            indexi++;
        }
        
    }
    
    for(int i=0;i<Nexpanded;i++){
        if(i<names.length){
            numberedNames[i]=""+(-i-1);            
        }
        else if (i<2*names.length){
            numberedNames[i]=""+(i-names.length+1);
        }
        else{
            numberedNames[i]=""+(i-2*names.length+1)+".5";
        }
    }
    this.numberedNamesList=new ArrayList(Arrays.asList(numberedNames));
    this.dictionary = new Hashtable();
    for(int i=0;i<numberedNames.length;i++){
            dictionary.put(numberedNames[i],i);
    }
    compositeNodes=new ArrayList<String>();
    for(int i=0;i<numberedNames.length;i++){
            if(Double.parseDouble(numberedNames[i])!=Math.rint(Double.parseDouble(numberedNames[i]))){
                compositeNodes.add(numberedNames[i]);
            }
   }
            
        
        
    }

    public ExpandedNetwork (String [] names, String [] functions, int[] targetAttractor) {
    
    this.originalNames=Arrays.copyOf(names, names.length);
    this.originalFunctions=Arrays.copyOf(functions, functions.length);
    this.namesList=new ArrayList<String>(Arrays.asList(originalNames));
    Set<String> namesList=new HashSet<String>();
    Set<String> regulatorsList;
    Set<String> compositeList;
    Set<String[][]> regulatorsArrayList;
    Set<String> [] negationRegulatorsList;
    Set<Integer> indexList;
    String [] namesExpanded,complementary,negations;
    String[][][] regulators,regulatorsNegations;
    String[][] regulatorsExpanded,regulatorsNegationsExpanded;
    Integer [] dummyInt;
    String[] stringDummy;
    String[][] stringdoubleDummy;
    String newNode;
    int [] indexArray,countArray;
    int [][] networkMatrix;
    int index,indexNegations,N,indexi,indexj;
    int [] relevantComposites;
    Boolean boolDummy1,boolDummy2,boolDummy3,boolDummy;
    ArrayList<String[]> temporaryStrings;
    
    //The rules must be in the following form to work.
    //NodeI* = (NodeIi1 and not Node Ii2 and ... and NodeIin) or (NodeIj1 and not Node Ij2 and ... and NodeIjn) or ... or (NodeIz1 and not Node Iz2 and ... and NodeIzn)
    //..
    //

    regulators=new String[names.length][][]; //the regulators of every node are saved here
    negations=new String[names.length]; //we will store the name of the negation of the regulators here
    complementary= new String[0];
    namesExpanded=Arrays.copyOf(names, names.length);
    regulatorsExpanded=new String[names.length][];
    regulatorsNegationsExpanded=new String[names.length][];
    regulatorsNegations=new String[negations.length][][];
    negationRegulatorsList=new HashSet[names.length];  //Now we create the composite nodes and the negative nodes


    for(int i=0;i<names.length;i++){
        namesList.add("~"+names[i]); //We now add the negations of the nodes to the name List
    }


    //In case one of the nodes has no regulators, gives back a warning
    //In such a case the function assigned to that node will be the identity
    //function
    for(int i=0;i<names.length;i++){  
        if(functions[i]==null){
            System.out.println("Found no function for node "+names[i]+". I will set the identity as the function");
            regulators[i]=new String[1][1];
            regulators[i][0][0]=names[i];
            functions[i]=names[i];
        }
        negations[i]="~"+names[i]; //Name of the names of the negation to facilitate searching for the index
        negationRegulatorsList[i]=new HashSet<String>();   //Set with the regulators of the negations

    }


    //Gets the regulators from each of the rules. Each of the elements separated by an or rules counts as a "regulator" since
    //it will be a composite node in the expanded network
   
    for(int i=0;i<names.length;i++){        
        regulators[i]=createRegulatorsArray(functions[i].replace("("," ( ").replace(")"," ) ").replace("not ","~"));
    } 
   
    for(int i=0;i<names.length;i++){
        regulatorsList=new HashSet<String>();
        for(int j=0;j<regulators[i].length;j++){
            //This create the name of the composite node and creates a new composite node if it doesnt exist

            boolDummy3=true; //If the composite node contains a node and its negations, in which case its irrelevant
            indexList=new HashSet<Integer>(); //We use a set to store the indexes in case the elements in a composite node
                                                //get repeated

            if(regulators[i][j].length>1){
                for(int k=0;k<regulators[i][j].length;k++){
                    if(!regulators[i][j][k].startsWith("~")){
                        index=Arrays.asList(names).indexOf(regulators[i][j][k]);
                        indexList.add(index);
                        if(indexList.contains(-index-1) ){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                        

                    }
                    else if(regulators[i][j][k].startsWith("~")){
                        index=Arrays.asList(negations).indexOf(regulators[i][j][k]);
                        indexList.add(-index-1);
                        if(indexList.contains(index)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                    }

                    else{
                        System.out.println("Couldn't find node "+regulators[i][j][k]+" in list of names");
                        System.exit(0);
                    }
                }

                if(boolDummy3){ //In case the composite node contains a node and its negations then we skip everything
                    dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
                    indexArray=new int[dummyInt.length]; 
                    for(int k=0;k<dummyInt.length;k++){indexArray[k]=dummyInt[k].intValue();}

                    indexArray=OtherMethods.orderArraybyOrder(indexArray, 0, indexArray.length-1,OtherMethods.orderHighesttoLowest(indexArray, 0, indexArray.length-1));
                    //We are ordering the input nodes of the composite node for higher to lower to have a unique name for each combination
                    index=indexArray[0];
                    if(index>=0){
                        newNode=namesExpanded[index];
                        }
                    else{
                        newNode=negations[-index-1];
                    }
                    for(int k=1;k<regulators[i][j].length;k++){
                        index=indexArray[k];
                        if(index>=0){
                            newNode=newNode+"_"+namesExpanded[index];
                        }
                        else{
                            newNode=newNode+"_"+negations[-index-1];
                        }
                    }
                    if(!namesList.contains(newNode)){
                        //if it hasn't been added before we add it to the list of node names
                        namesList.add(newNode);
                        complementary=Arrays.copyOf(complementary,complementary.length+1);
                        complementary[complementary.length-1]=newNode;
                        namesExpanded=Arrays.copyOf(namesExpanded,namesExpanded.length+1);
                        namesExpanded[namesExpanded.length-1]=newNode;
                        regulatorsExpanded=Arrays.copyOf(regulatorsExpanded,regulatorsExpanded.length+1);
                        regulatorsExpanded[regulatorsExpanded.length-1]=Arrays.copyOf(regulators[i][j], regulators[i][j].length);
                    }
                    regulatorsList.add(newNode);
                }

            }
            //This is for the case the input node is not a composite node, but may still be a negative node
            else{
                if(!regulators[i][j][0].startsWith("~")){

                }
                else if(regulators[i][j][0].startsWith("~")){

                }
                else{
                        System.out.println("Couldn't find node "+regulators[i][j][0]+" in list of names");
                        System.exit(0);
                }
                regulatorsList.add(regulators[i][j][0]);

            }

        }

        //We now add all the regulators of this node to its corresponding array
        regulatorsExpanded[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);

    }

    //In this part we look for the negation of all nodes. The idea is that something like
    //A*= (B and not C) or (D and not E) is transformed to
    //not A= (not B and not D) or (not B and E) or (C and not D) or (C and E)
    //What the following code does is transform the A* into the not A* function
    for(int i=0;i<negations.length;i++){
        regulatorsList=new HashSet<String>();
        regulatorsArrayList=new HashSet<String[][]>(); //We will store all the composite nodes affecting the negative nodes
                                                    //here since we don't actually now how many nodes will each composite node
                                                    //have, nor how many composite nodes per negation node will there be. The first row
                                                    //contains the elements, the second the name of the composite node
        countArray=new int[regulators[i].length];

        boolDummy1=true;
        indexNegations=0;
        while(boolDummy1){
            compositeList=new HashSet<String>();
            indexList=new HashSet<Integer>();
            boolDummy3=true;
            for(int j=0;j<regulators[i].length;j++){

                if(regulators[i][j][countArray[j]].startsWith("~")){
                    compositeList.add(regulators[i][j][countArray[j]].substring(1));
                    //with this we only add the name of the negative regulator, which is what we want since we are taking the negation of the function
                    index=Arrays.asList(negations).indexOf(regulators[i][j][countArray[j]]);
                    indexList.add(index);
                    if(indexList.contains(-index-1)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                }
                else{
                    compositeList.add("~"+regulators[i][j][countArray[j]]);
                    //with this we add the negation of the regulator, which is what we want since we are taking the negation of the function
                    index=Arrays.asList(names).indexOf(regulators[i][j][countArray[j]]);
                    indexList.add(-index-1);
                    //since the regulator is negative, we need to distinguish it for the positive node
                    if(indexList.contains(index)){
                            boolDummy3=false;
                            //If the composite node contains a node and its negations then its irrelevant
                        }
                }

            }

            index=0;
            boolDummy2=true;          
            while(boolDummy2){
                countArray[index]++;
                if(countArray[index]>=regulators[i][index].length){
                    //If it reaches the maximum allowed values of the index, it will now try to increase the next entry in the array
                    //, similar to when 09 transforms into 10 when you sum 01 to 09.
                    countArray[index]=0;
                    index++;
                }
                else{
                    boolDummy2=false; //When a valid index for the countArray index is obtained it stops searching. The numbers in
                    //countArray will be used in then next cycle controlled by boolDummy1 to create the following composite node
                }

                if(index==regulators[i].length){
                    boolDummy1=false;  //This means that we have explored all possible composite nodes for the negations of the nodes
                    boolDummy2=false;
                }

            }

            if(boolDummy3){ //In case the composite node contains a node and its negations then we skip everything


                    dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
                    indexArray=new int[dummyInt.length];
                    //regulatorsArrayList.add((String[])compositeList.toArray(new String[compositeList.size()]));

                    for(int k=0;k<dummyInt.length;k++){indexArray[k]=dummyInt[k].intValue();}

                    indexArray=OtherMethods.orderArraybyOrder(indexArray, 0, indexArray.length-1,OtherMethods.orderHighesttoLowest(indexArray, 0, indexArray.length-1));
                    //We are ordering the input nodes of the composite node for higher to lower to have a unique name for each combination
                    index=indexArray[0];
                    if(index>=0){
                        newNode=namesExpanded[index];
                        }
                    else{
                        newNode=negations[-index-1];
                    }
                    for(int k=1;k<indexArray.length;k++){
                        index=indexArray[k];
                        if(index>=0){
                            newNode=newNode+"_"+namesExpanded[index];
                        }
                        else{
                            newNode=newNode+"_"+negations[-index-1];
                        }
                    }
                    if(!namesList.contains(newNode)){
                        //if it hasn't been added before we add it to the list of node names                            
                        namesList.add(newNode);
                        complementary=Arrays.copyOf(complementary,complementary.length+1);
                        complementary[complementary.length-1]=newNode;
                        namesExpanded=Arrays.copyOf(namesExpanded,namesExpanded.length+1);
                        namesExpanded[namesExpanded.length-1]=newNode;
                        regulatorsExpanded=Arrays.copyOf(regulatorsExpanded,regulatorsExpanded.length+1);
                        regulatorsExpanded[regulatorsExpanded.length-1]=Arrays.copyOf((String[])compositeList.toArray(new String[compositeList.size()]), compositeList.size());
                    }
                    if(!regulatorsList.contains(newNode)){
                        regulatorsList.add(newNode);
                        stringdoubleDummy=new String[2][];
                        stringdoubleDummy[0]=(String[])compositeList.toArray(new String[compositeList.size()]);
                        stringdoubleDummy[1]=new String[1];stringdoubleDummy[1][0]=newNode;
                        regulatorsArrayList.add(stringdoubleDummy);
                    }
                    indexNegations++;
            }


        }
        regulatorsNegationsExpanded[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
        //We now add all the regulators of this node to its corresponding array. The composite nodes in this array are just a single name

        regulatorsNegations[i]=new String[regulatorsArrayList.size()][];
        Iterator it=regulatorsArrayList.iterator();
        while(it.hasNext()){
            stringdoubleDummy=(String[][]) it.next();
            index=Arrays.asList(regulatorsNegationsExpanded[i]).indexOf(stringdoubleDummy[1][0]);
            regulatorsNegations[i][index]= Arrays.copyOf(stringdoubleDummy[0], stringdoubleDummy[0].length);
        }




    }

    //We need to remove the composite input nodes that are irrelevant in the negations. For example if we have
    //~A*= B or (~C and B) , this is actually equivalent to ~A*=B , but because of the way we go the function of the
    //negation we would have both
       
    for(int i=0;i<negations.length;i++){

            indexArray=new int[regulatorsNegationsExpanded[i].length];
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){ //We do this for every input node

                    for(int l=0;l<regulatorsNegations[i].length;l++){ //We check every other input node to see if this node is irrelevant

                        if(regulatorsNegations[i][j].length < regulatorsNegations[i][l].length && indexArray[l]==0 && indexArray[j]==0){
                            //For the node ij no te be irrelevant it must be contained in the il node. For it to be irrelevant we check if every node in
                            //ij is in il
                            boolDummy2=true;
                            index=0;
                            while(index<regulatorsNegations[i][j].length && boolDummy2){
                                //if it finds the nodes ij index inside il, then ij could be contained in il
                                boolDummy2=Arrays.asList(regulatorsNegations[i][l]).contains(regulatorsNegations[i][j][index]);
                                index++;
                            }
                            //if it found the node ijindex for all nodes, then ij is cointained in il. Then il is irrelevant
                            if(boolDummy2){
                                indexArray[l]=1;
                            }
                        }

                    }

            }
            index=0; //We get the number of relevant regulators
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){if(indexArray[j]==0){index++;}}
            stringDummy=new String[index];
            stringdoubleDummy=new String[index][];
            index=0;
            for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){
                if(indexArray[j]==0){
                    stringDummy[index]=regulatorsNegationsExpanded[i][j];
                    stringdoubleDummy[index]=Arrays.copyOf(regulatorsNegations[i][j], regulatorsNegations[i][j].length);
                    index++;
                }
            }
            
            regulatorsNegationsExpanded[i]=Arrays.copyOf(stringDummy,stringDummy.length);            
            regulatorsNegations[i]=Arrays.copyOf(stringdoubleDummy,stringdoubleDummy.length);
            if(regulatorsExpanded[i][0].equals("UnstableOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="UnstableOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="UnstableOscillation";
            }
            if(regulatorsExpanded[i][0].equals("IncompleteOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="IncompleteOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="IncompleteOscillation";
            }
            if(regulatorsExpanded[i][0].equals("FullOscillation")){
                regulatorsNegationsExpanded[i]=new String[1];regulatorsNegationsExpanded[i][0]="FullOscillation";
                regulatorsNegations[i]=new String[1][1];regulatorsNegations[i][0][0]="FullOscillation";
            }
            

    }


    N=namesExpanded.length+negations.length; //This the total number of nodes in the expanded network
    networkMatrix=new int[N][N]; //This matrix will contain all the connectivity information of the network
    for(int i=0;i<names.length;i++){
        for(int j=0;j<regulatorsExpanded[i].length;j++){           
            if(!regulatorsExpanded[i][j].equals("UnstableOscillation")&&!regulatorsExpanded[i][j].equals("FullOscillation")&&!regulatorsExpanded[i][j].equals("IncompleteOscillation")){
                //We first check if the regulator is a positive or composite
                index=Arrays.asList(namesExpanded).indexOf(regulatorsExpanded[i][j]);
                if(index==-1 ){
                    index=-1*Arrays.asList(negations).indexOf(regulatorsExpanded[i][j])-1;
                    //If the node is negative we need to search in the negative list
                }

                if(index>=names.length){
                    index=index+names.length; //We will put the composite nodes after the normal and negative ones
                }
                else if(index<0){
                    index=-1*index-1; //We will put the negative nodes before the positive ones
                }
                else{
                    index=index+names.length;
                }
                networkMatrix[i+names.length][index]=1; //We will put the negative nodes before the positive ones
            }
        }
    }

    for(int i=0;i<negations.length;i++){
        for(int j=0;j<regulatorsNegationsExpanded[i].length;j++){
            if(!regulatorsNegationsExpanded[i][j].equals("UnstableOscillation")&&!regulatorsNegationsExpanded[i][j].equals("FullOscillation")&&!regulatorsNegationsExpanded[i][j].equals("IncompleteOscillation")){
                //We first check if the regulator is a positive or composite
                index=Arrays.asList(namesExpanded).indexOf(regulatorsNegationsExpanded[i][j]);
                if(index==-1){
                    index=-1*Arrays.asList(negations).indexOf(regulatorsNegationsExpanded[i][j])-1;
                }
                if(index>=names.length){
                    index=index+names.length; //We will put the composite nodes after the normal and negative ones
                }
                else if(index<0){
                    index=-1*index-1; //We will put the negative nodes before the positive ones
                }
                else{
                    index=index+names.length;
                }
                networkMatrix[i][index]=1; //We will put the negative nodes before the positive ones
            }
        }

    }

    for(int i=names.length;i<namesExpanded.length;i++){
        for(int j=0;j<regulatorsExpanded[i].length;j++){
            //We first check if the regulator is a positive or composite
            index=Arrays.asList(namesExpanded).indexOf(regulatorsExpanded[i][j]);
            if(index==-1){
                index=-1*Arrays.asList(negations).indexOf(regulatorsExpanded[i][j])-1;
            }
            if(index>=names.length){
                index=index+names.length; //We will put the composite nodes after the normal and negative ones
            }
            else if(index<0){
                index=-1*index-1; //We will put the negative nodes before the positive ones
            }
            else{
                index=index+names.length;
            }
            networkMatrix[i+names.length][index]=1; //The composite nodes go after the normal and composite ones
        }

    }
    
    this.Nexpanded=N;
    relevantComposites=new int[N];
    indexi=0;
    for(int i=0;i<names.length;i++){
        relevantComposites[i]=0;
        if(targetAttractor[i]<0){
            relevantComposites[i]=1;
        }
        if(relevantComposites[i]==0){this.Nexpanded--;}
        //else{System.out.println(indexi+" "+negations[i]);indexi++;}
    }
    for(int i=names.length;i<2*names.length;i++){
        relevantComposites[i]=0;
        if(targetAttractor[i-names.length]>0){
            relevantComposites[i]=1;
        }
        if(relevantComposites[i]==0){this.Nexpanded--;}
        //else{System.out.println(indexi+" "+names[i-names.length]);indexi++;}
    }
    for(int i=2*names.length;i<N;i++){
        index=0;
        boolDummy1=true;
        while(boolDummy1 && index<2*names.length){
            if(networkMatrix[i][index]==1){
                if(relevantComposites[index]==0)
                {boolDummy1=false;} //if the composite node has at least an output, then it is relevant
            }
            index++;
        }
        index=0;
        boolDummy=false;
        while(!boolDummy && index<2*names.length){
            if(networkMatrix[index][i]==1){
                boolDummy=true; //if the composite node has at least an output, then it is relevant
            }
            index++;
        }
        if(boolDummy1&&boolDummy){
            relevantComposites[i]=1;
            //System.out.println(indexi+" "+namesExpanded[i-names.length]);indexi++;
        }
        else{
            relevantComposites[i]=0;
            this.Nexpanded--;
        }


    }
    
    this.adjacencyList=new int[Nexpanded][];
    //This will be the output, and will contain the adjacency List of the expanded network
    //The adjacency list is an array which contains the outputs of every nodes. The first
    //dimension in the array represents the same node as in the namesExpandedFinal array
    //, the second dimension represents the indices of those nodes,
    //that are direct successors of the node.
    this.nodeInputs=new int[Nexpanded][];
    this.namesExpandedNetwork=new String[Nexpanded];
    this.numberedNames=new String[Nexpanded];
    
    indexi=0;
    for(int i=0;i<N;i++){
        if(relevantComposites[i]==1){
            if(i<names.length){this.namesExpandedNetwork[indexi]=negations[i];}
            else if (i<2*names.length){this.namesExpandedNetwork[indexi]=names[i-names.length];}
            else{this.namesExpandedNetwork[indexi]=namesExpanded[i-names.length];}
            
            indexj=0;
            indexList=new HashSet<Integer>();
            for(int j=0;j<N;j++){
                if(relevantComposites[j]==1){
                    if(networkMatrix[j][i]==1){indexList.add(indexj);}
                    indexj++;
                }
            }           
            dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
            adjacencyList[indexi]=new int[dummyInt.length];
            for(int k=0;k<dummyInt.length;k++){adjacencyList[indexi][k]=dummyInt[k].intValue();}
            indexi++;
        }
        
    }
    this.namesExpandedNetworkList=new ArrayList(Arrays.asList(namesExpandedNetwork));
    for(int i=0;i<namesExpandedNetwork.length;i++){
            //newNode=namesExpandedNetwork[i];
            //System.out.println(newNode);
    }
    
    indexi=0;
    for(int i=0;i<N;i++){
        if(relevantComposites[i]==1){
            indexj=0;
            indexList=new HashSet<Integer>();
            for(int j=0;j<N;j++){
                if(relevantComposites[j]==1){
                    if(networkMatrix[i][j]==1){indexList.add(indexj);}
                    indexj++;
                }
            }
            dummyInt=indexList.toArray(new Integer[indexList.size()]); //For some reason Java cant convert directly an Int to int
            nodeInputs[indexi]=new int[dummyInt.length];
            for(int k=0;k<dummyInt.length;k++){nodeInputs[indexi][k]=dummyInt[k].intValue();}
            indexi++;
        }
        
    }
    
    indexi=0;
    for(int i=0;i<N;i++){
        if(i<names.length && relevantComposites[i]==1){
            numberedNames[indexi]=""+(-i-1);
            indexi++;
        }
        else if (i<2*names.length && relevantComposites[i]==1){
            numberedNames[indexi]=""+(i-names.length+1);
            indexi++;
        }
        else if (relevantComposites[i]==1){
            numberedNames[indexi]=""+(i-2*names.length+1)+".5";
            indexi++;
        }
        
    }
    this.numberedNamesList=new ArrayList(Arrays.asList(numberedNames));
    this.dictionary = new Hashtable();
    for(int i=0;i<numberedNames.length;i++){
            dictionary.put(numberedNames[i],i);
    }
    compositeNodes=new ArrayList<String>();
    for(int i=0;i<numberedNames.length;i++){
            if(Double.parseDouble(numberedNames[i])!=Math.rint(Double.parseDouble(numberedNames[i]))){
                compositeNodes.add(numberedNames[i]);
            }
   }
            
        
    for(int i=0;i<N;i++){
        indexi=0;
        if(relevantComposites[i]==1){
            for(int j=0;j<N;j++){
                indexj=0;
                if(relevantComposites[j]==1){
                    if(networkMatrix[i][j]==1){                      
                        if(j<names.length){index=numberedNamesList.indexOf(""+(-j-1));}
                        else if (j<2*names.length){index=numberedNamesList.indexOf(""+(j-names.length+1));}
                        else{index=numberedNamesList.indexOf(""+(j-2*names.length+1)+".5");}
                        newNode=index+"";
                        //newNode=namesExpandedNetwork[index];
                        if(i<names.length){index=numberedNamesList.indexOf(""+(-i-1));}
                        else if (i<2*names.length){index=numberedNamesList.indexOf(""+(i-names.length+1));}
                        else{index=numberedNamesList.indexOf(""+(i-2*names.length+1)+".5");}
                        //newNode=newNode+" pp "+namesExpandedNetwork[index];
                        newNode=newNode+" "+index;
                        //System.out.println(newNode);
                    }
                    indexj++;
                }
            }
            indexi++;
        }
    }
        
    }

    
    public String getNameFromNumberedName(String name){
        int index;
        index=numberedNamesList.indexOf(name);
        return namesExpandedNetwork[index];
    }
    
    public void findCycles(){
        
        ElementaryCyclesSearchforStableSCC ecs=new ElementaryCyclesSearchforStableSCC(this.adjacencyList,this.numberedNames);
        this.cycles=ecs.getElementaryCycles(); 
    }

    
    public ArrayList<ArrayList<String>> getCycles(){        
        return cycles; 
    }

    public int[][] getAdjacencyList(){        
        return this.adjacencyList; 
    }
    
    public String[] getNumberedNames(){        
        return this.numberedNames; 
    }
    
    public int[][] getNodeInputs(){        
        return this.nodeInputs; 
    }
    
    public void findStronglyConnectedComponets(){
        //Note that all nodes are classified a SCC. If they have no outputs or no inputs they form their own class
        //of SCC.
        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class); 
        for(int i=0;i<namesExpandedNetwork.length;i++){
            g.addVertex(namesExpandedNetwork[i]);
        }
        for(int i=0;i<this.adjacencyList.length;i++){
            for(int j=0;j<this.adjacencyList[i].length;j++){
                g.addEdge(namesExpandedNetwork[i],namesExpandedNetwork[adjacencyList[i][j]]);
            }
        }
        int index;
        HashSet<String> dummyset;
        Iterator itr;
        StrongConnectivityInspector sci=new StrongConnectivityInspector(g);
        
        List<Object> list = new ArrayList<Object>(sci.stronglyConnectedSets());
        sci.stronglyConnectedSets();
        Object [] objects = list.toArray();
        stronglyConnectedComponents=new String[objects.length][];
        for(int i=0;i<objects.length;i++){
            dummyset=(HashSet<String>) objects[i];
            
            index=0;
            stronglyConnectedComponents[i]=new String[dummyset.size()];
            itr=dummyset.iterator();
            while(itr.hasNext()){  
                stronglyConnectedComponents[i][index]=(String) itr.next();
                index++; 
            }
        }
        
    }

    
    public String[][] getStronglyConnectedComponets(){
        return Arrays.copyOf(stronglyConnectedComponents, stronglyConnectedComponents.length);
        
    }
    
    public boolean signCoherent(ArrayList<String> cycle){
        boolean signCoherent=true;
        double nodeName;
        for(int i=0;i<cycle.size();i++){
            nodeName=Double.parseDouble(cycle.get(i));
            if(nodeName>0){
                if(cycle.contains("-"+cycle.get(i))){
                    signCoherent=false;
                    break;
                }
                
            }
            else{
                if(cycle.contains(cycle.get(i).split("-")[1])){
                    signCoherent=false;
                    break;
                }
            }
            
        }
        return signCoherent;
    }
    
    public boolean stableMotif(ArrayList<String> cycle){
        boolean coherent=true;
        double nodeName;
        for(int i=0;i<cycle.size();i++){
            nodeName=Double.parseDouble(cycle.get(i));
            if(nodeName>0 && Math.rint(nodeName)==nodeName){
                if(cycle.contains("-"+cycle.get(i))){
                    coherent=false;
                    break;
                }
                
            }
            else if (nodeName<0 && Math.rint(nodeName)==nodeName){
                if(cycle.contains(cycle.get(i).split("-")[1])){
                    coherent=false;
                    break;
                }
            }
            
        }
        
        boolean composite=true;
        int index;
        for(int j=0;j<cycle.size();j++){
                nodeName=Double.parseDouble(cycle.get(j));
                if(nodeName>0 && Math.rint(nodeName)!=nodeName){
                    index=(Integer) dictionary.get(cycle.get(j));
                    for(int k=0;k<nodeInputs[index].length;k++){
                        if(!cycle.contains(numberedNames[nodeInputs[index][k]])){
                            composite=false;
                            break;
                        }
                    }
                }
            }

        
        
        return composite&&coherent;
    }
    
    public ArrayList<ArrayList<String>> mergeCompatibleComponents(ArrayList<ArrayList<String>> sSCC){
        
        ArrayList<HashSet<String>> setSCC=new ArrayList<HashSet<String>>();
        ArrayList<ArrayList<String>> result= new ArrayList<ArrayList<String>>();
        Set<String> diference;
        boolean bool;
        String node;
        ArrayList<String> remove;
        HashSet<String> temporarySet;
        for(int i=0;i<sSCC.size();i++){
            remove=new ArrayList<String>();
            for(int j=0;j<sSCC.get(i).size();j++){
                node=sSCC.get(i).get(j);
                if(Double.parseDouble(node)!=Math.rint(Double.parseDouble(node))){
                    remove.add(node);
                }
            }
            for(int j=0;j<remove.size();j++){
                node=remove.get(j);
                sSCC.get(i).remove(node);
            }
            temporarySet=new HashSet<String>(sSCC.get(i));
            setSCC.add(temporarySet);
        }
        
        
        for(int i=0;i<setSCC.size();i++){
            bool=true;
            for(int j=0;j<setSCC.size();j++){
                if(j!=i){
                    diference = new HashSet<String>(setSCC.get(j));
                    diference.removeAll(setSCC.get(i));
                    if(diference.isEmpty()&&(setSCC.get(i).size()!=setSCC.get(j).size())){
                        bool=false;
                        break;
                    } 
                }
                if(i<j){
                    if(setSCC.get(i).size()==setSCC.get(j).size()){
                        if(setSCC.get(i).containsAll(setSCC.get(j))&&setSCC.get(j).containsAll(setSCC.get(i))){
                            bool=false;
                            break;
                        }
                    }
                }
            }
            if(bool){
                result.add(sSCC.get(i));
            }
        
        }
        
        return result;
    }
            
    
    public boolean coherentComposite(ArrayList<String> cycle){
        boolean coherentComposite=true;
        double nodeName;
        int index;
        for(int j=0;j<cycle.size();j++){
                nodeName=Double.parseDouble(cycle.get(j));
                if(nodeName>0 && Math.rint(nodeName)!=nodeName){
                    index=(Integer) dictionary.get(cycle.get(j));
                    for(int k=0;k<nodeInputs[index].length;k++){
                        if(Double.parseDouble(numberedNames[nodeInputs[index][k]])>0){
                            if(cycle.contains("-"+numberedNames[nodeInputs[index][k]])){
                                coherentComposite=false;
                                break;
                            }
                                
                            
                        }
                        else{
                            if(cycle.contains(numberedNames[nodeInputs[index][k]].split("-")[1])){
                                coherentComposite=false;
                                break;
                            }
                        }
                    }
                }
            }
        return coherentComposite;
    }    
    
    public boolean completableComposite(String nodeNameString,ArrayList<String> nodes){
        boolean completableComposite=true;
        double nodeName;
        int index;

            nodeName=Double.parseDouble(nodeNameString);
            if(nodeName!=Math.rint(nodeName)){
                index=(Integer) dictionary.get(nodeNameString);
                for(int k=0;k<nodeInputs[index].length;k++){
                    if(!nodes.contains(numberedNames[nodeInputs[index][k]])){
                        completableComposite=false;
                        break;
                    }
                }
            }
        
        
        return completableComposite;
    }
    
    
    public void findStableStronglyConnectedComponets(){
        
        ElementaryCyclesSearchforStableSCC ecs=new ElementaryCyclesSearchforStableSCC(this.adjacencyList,this.numberedNames);
        ArrayList<ArrayList<String>> cycles=ecs.getElementaryCycles();
        ArrayList<ArrayList<String>> cyclesSimplified=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> cyclesDummy;
        ArrayList<String> cycle;
        ArrayList<String> dummyArrayList;
        int Lmax,Lmin,dictionarySize,L;
        HashSet<Integer> SetNext,SetNextIntersection;
        HashSet<Integer> SetCurrent;
        HashSet<Integer> remove;
        HashSet<String> dummySet;
        String[] dummyStringArray;
        ArrayList<HashSet<Integer>> compositeCyclesSet;
        ArrayList<ArrayList<ArrayList<String>>> compositeCyclesList;
        Iterator itr,itr2;
        Map<Integer,ArrayList<String>> cycleDictionary;
        Map<Integer,ArrayList<Integer>> cyclePairs;
        
        Dictionary checkedComposites = new Hashtable();
        boolean boolDummy1,boolDummy2,boolDummy3;
        double nodeName;
        int index,index2;
        
        
        for(int i=0;i<numberedNames.length;i++){
            if(Double.parseDouble(numberedNames[i])!=Math.rint(Double.parseDouble(numberedNames[i]))){
                checkedComposites.put(numberedNames[i],true);
            }
        }
        
        //For each cycle with a composite node it checks if the complimentary node of each of its inputs
        //is in the cycle. If that is the case then that composite node cannot form part of a stable SCC
        //and it is removed
        //System.out.println("Simplifying cycles...");        
        for(int i=0;i<cycles.size();i++){
            boolDummy1=coherentComposite(cycles.get(i));
            
            if(boolDummy1){
                cyclesSimplified.add(cycles.get(i));
            }
        }
        
        cyclesDummy=new ArrayList<ArrayList<String>>();

        
        //For each cycle with more than a composite node it checks if the inputs
        //necessary for one of the composites is the opposite of the ones necessary
        //for another one in that cycle. If that is the case then this cycle can never
        //be part of a stable SCC and it is removed
        
        for(int i=0;i<cyclesSimplified.size();i++){
            cyclesDummy.add(cyclesSimplified.get(i));
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.get(i).size();j++){
                nodeName=Double.parseDouble(cyclesSimplified.get(i).get(j));
                if(nodeName>0){
                    if(nodeName!=Math.rint(nodeName)){
                        index=(Integer) dictionary.get(cyclesSimplified.get(i).get(j));
                        for(int k=0;k<nodeInputs[index].length;k++){
                            cycle.add(numberedNames[nodeInputs[index][k]]);
                        }
                    }
                }
            }
            if(!signCoherent(cycle)){
                cyclesDummy.remove(cyclesSimplified.get(i));
            }
        }
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cyclesDummy=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesSimplified.size();i++){cyclesDummy.add(cyclesSimplified.get(i));}
        
        
        //This checks if joining all the cycles containing a composite node has all the inputs
        //of said composite node. If this is not the case then this composite node can never
        //be part of a stable SCC and so we remove those cycles
        for(int i=0;i<compositeNodes.size();i++){
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.size();j++){
                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                            cycle.add(cyclesSimplified.get(j).get(k));
                        }
                    }
                }
            }
            if(!completableComposite(compositeNodes.get(i),cycle)){
                for(int j=0;j<cyclesSimplified.size();j++){                   
                    if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                        cyclesDummy.remove(cyclesSimplified.get(j));
                    }
                }
            }
        }

        
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cycleDictionary=new HashMap<Integer,ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){cycleDictionary.put(i, cyclesSimplified.get(i));}
        //Uncomment to output the cycle numbers and the nodes they contain
//        for(int i=0;i<cyclesSimplified.size();i++){
            //System.out.print("Cycle "+i+" \n");
//            if(cyclesSimplified.get(i).size()<=10){
//                for(int j=0;j<cyclesSimplified.get(i).size();j++){
//                    System.out.print(getNameFromNumberedName(cyclesSimplified.get(i).get(j))+"\t");
//                }
//                System.out.print("\n");
//            }
//            
//        }
        dictionarySize=cyclesSimplified.size();
        boolDummy1=true;
        Lmax=10;
        while(boolDummy1){
            Lmin=Lmax;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    compositeCyclesSet=new ArrayList<HashSet<Integer>>();
                    //In this object will save a array with the cycle numbers that have
                    //at least one of the inputs of the ith composite node. Each element
                    //in the array will be one of those inputs.
                    compositeCyclesList=new ArrayList<ArrayList<ArrayList<String>>>();
                    //In this object we will have the actual cycles (with their numbered names)
                    //of the previous array                 
                    index=this.numberedNamesList.indexOf(compositeNodes.get(i));
                    for(int j=0;j<nodeInputs[index].length;j++){
                        compositeCyclesSet.add(new HashSet<Integer>());
                        compositeCyclesList.add(new ArrayList<ArrayList<String>>());
                    }
                    for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {                     
                        if(entry.getValue().contains(compositeNodes.get(i))){
                            for(int k=0;k<nodeInputs[index].length;k++){
                                if(entry.getValue().contains(numberedNames[nodeInputs[index][k]])){
                                    compositeCyclesSet.get(k).add(entry.getKey());
                                }
                            }                            
                        }                        
                    }
                    for(int j=0;j<compositeCyclesSet.size();j++){
                        itr=compositeCyclesSet.get(j).iterator();
                        while(itr.hasNext()){
                            index=(Integer) itr.next();
                            compositeCyclesList.get(j).add(cycleDictionary.get(index));
                        }
                    }
                    L=0;
                    
                    //We will first try the sets with the smallest number of cycles, so that we don't run out of memory
                    //for networks with an enormous number of networks
                    for(int j=0;j<compositeCyclesList.size()-1;j++){L=L+compositeCyclesList.get(j).size()+compositeCyclesList.get(j+1).size();}
                    boolDummy2=true;
                    if(L<Lmax){
                        
                        for(int j=0;j<compositeCyclesSet.size()-1;j++){
                            //Uncomment to output the cycle numbers on each j
                            //itr=compositeCyclesSet.get(j).iterator();
                            //System.out.print("Input "+j+": \t");
                            //while(itr.hasNext()){
                            //    System.out.print((Integer) itr.next()+" ");
                            //}
                            //System.out.print("\n");
                            //System.out.print("Input "+(j+1)+": \t");
                            //itr=compositeCyclesSet.get(j+1).iterator();
                            //while(itr.hasNext()){
                            //    System.out.print((Integer) itr.next()+" ");
                            //}    
                            //System.out.print("\n");
                            
                            boolDummy3=true; //If boolDummy3=false, that means that there are will be no combination of cycles
                                            //for inputs j and j+1 since SetCurrent.isEmpty() or SetNext.isEmpty()
                            cyclePairs=new HashMap<Integer,ArrayList<Integer>>();
                            //Here we will have all the new cycles that are formed by taking the union of one cycle containing regulator j (but not j+1)
                            //with all cycle containing regulator j+1 (but not regulator j). The idea is that we want to substitute in all arrays
                            //the new cycles formed with the original one, which we will remove.
                            if(compositeCyclesSet.get(j).isEmpty()||compositeCyclesSet.get(j+1).isEmpty()){boolDummy2=false;break;}
                                SetNext=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j+1 (but
                                //not regulator j)
                                SetNext.removeAll(compositeCyclesSet.get(j));
                                SetNextIntersection=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the cycles that contain both regulators
                                SetNextIntersection.removeAll(SetNext);
                                SetCurrent=new HashSet<Integer>(compositeCyclesSet.get(j));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j (but
                                //not regulator j+1)
                                SetCurrent.removeAll(compositeCyclesSet.get(j+1));
                                if(SetCurrent.isEmpty()){
                                    boolDummy3=false;
                                }
                                
                                itr=SetCurrent.iterator();
                                if(boolDummy3){
                                    while(itr.hasNext()){
                                        index=(Integer) itr.next();
                                        cyclePairs.put(index,new ArrayList<Integer>());
                                        if(!SetNext.isEmpty()){
                                            itr2=SetNext.iterator();
                                            while(itr2.hasNext()){
                                                index2=(Integer) itr2.next();
                                                if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                dummySet=new HashSet<String>();
                                                //We are gonna put the union of cycle index with cycle index2 in this set
                                                for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                Arrays.sort(dummyStringArray);
                                                dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                if(signCoherent(dummyArrayList)){
                                                    if(coherentComposite(dummyArrayList)){
                                                        if(!cyclesSimplified.contains(dummyArrayList)){                                            
                                                                cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                cyclesSimplified.add(dummyArrayList);
                                                                //This will later substitute cycles index and index2 with the new cycles
                                                                cyclePairs.get(index).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                //Uncomment to output new cycles
                                                                //System.out.print("Cycle Next "+dictionarySize+" \n");
                                                                //for(int j1=0;j1<dummyArrayList.size();j1++){
                                                                //    System.out.print(getNameFromNumberedName(dummyArrayList.get(j1))+"\t");
                                                                //}
                                                                //System.out.print("\n");
                                                                dictionarySize++;

                                                        }
                                                    }
                                                }   
                                            }
                                        }
                                        if(!SetNextIntersection.isEmpty()){
                                            itr2=SetNextIntersection.iterator();
                                            while(itr2.hasNext()){
                                                index2=(Integer) itr2.next();
                                                if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                dummySet=new HashSet<String>();
                                                //We are gonna put the union of cycle index with cycle index2 in this set
                                                for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                Arrays.sort(dummyStringArray);
                                                dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                if(signCoherent(dummyArrayList)){
                                                    if(coherentComposite(dummyArrayList)){
                                                        if(!cyclesSimplified.contains(dummyArrayList)){                                            
                                                                cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                cyclesSimplified.add(dummyArrayList);
                                                                //This will later substitute cycles index and index2 with the new cycles
                                                                cyclePairs.get(index).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                //We add this because we do not want to remove the cycles with
                                                                //that have both inputs j and j+1 because they may be needed
                                                                //in their precombined form later on
                                                                cyclePairs.get(index2).add((Integer) index2);                                                               
                                                                //Uncomment to output new cycles
                                                                //System.out.print("Cycle Inters "+dictionarySize+" \n");
                                                                //for(int j1=0;j1<dummyArrayList.size();j1++){
                                                                //    System.out.print(getNameFromNumberedName(dummyArrayList.get(j1))+"\t");
                                                                //}
                                                                //System.out.print("\n");
                                                                dictionarySize++;

                                                        }
                                                    }
                                                }   
                                            }
                                        }
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index); 
                                    }
                                }
                                //Cycle pairs gives the combined cycle number that will substitute
                                //the precombined cycle. Given a precombined cycle number it gives you
                                //the combined cycle number
                                if(boolDummy3){
                                    for (Map.Entry<Integer,ArrayList<Integer>> entry : cyclePairs.entrySet()) {
                                        index=entry.getKey();
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){
                                            //index is a precombined cycle number. This checks if the sets
                                            //with the cycle numbers per composite node have the precombined
                                            //cycle number. If they do, it puts the combined cycles instead
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                if(!SetNextIntersection.contains((Integer) index)){compositeCyclesSet.get(k).remove((Integer) index);}
                                                for(int k1=0;k1<cyclePairs.get(index).size();k1++){
                                                    index2=cyclePairs.get(index).get(k1);
                                                    compositeCyclesSet.get(k).add((Integer) index2);
                                                }
                                            }
                                        }
                                    }
                                    //Since the elements which have only input j+1 (but not j) have already been combined
                                    //with the ones that have j, the former can no longer form part of a stable SCC, since                            
                                    //all of those which could already contain both j and j+1, we remove them
                                    itr=SetNext.iterator();
                                    while(itr.hasNext()){
                                        index=(Integer) itr.next();
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index);
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                compositeCyclesSet.get(k).remove((Integer) index);
                                            }    
                                        }

                                    }
                                }
                                    
                                }
                                
                        
                        checkedComposites.remove(compositeNodes.get(i));
                        checkedComposites.put(compositeNodes.get(i), false);
                        //In case no nodes with a given input are found we set boolDumm2=false and
                        //remove all cycles containing that composite node from the arrays
                        if(!boolDummy2){
                            remove=new HashSet<Integer>();
                            for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                if(entry.getValue().contains(compositeNodes.get(i))){
                                    cyclesSimplified.remove(entry.getValue());
                                    remove.add(entry.getKey());
                                }
                            }
                            itr=remove.iterator();
                            while(itr.hasNext()){
                                cycleDictionary.remove((Integer) itr.next());
                            }
                        }
                        else{
                        //We check if the new cycles can be simplified
                            cycle=new ArrayList();
                            for(int j=0;j<cyclesSimplified.size();j++){
                                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                                            cycle.add(cyclesSimplified.get(j).get(k));
                                        }
                                    }
                                }
                            }
                            if(!completableComposite(compositeNodes.get(i),cycle)){
                                remove=new HashSet<Integer>();
                                for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                    if(entry.getValue().contains(compositeNodes.get(i))){
                                        cyclesSimplified.remove(entry.getValue());
                                        remove.add(entry.getKey());
                                    }
                                }
                                itr=remove.iterator();
                                while(itr.hasNext()){
                                    cycleDictionary.remove((Integer) itr.next());
                                }
                            }
                            
                        }
                        
                    }
                    else{
                        if(L<Lmin || Lmin==Lmax){Lmin=L+1;}
                    }
                    
                }
            }
            
            index=0;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    index++;
                }  
            }
            if(index==0){
                boolDummy1=false;
            }
            else{Lmax=Lmin;}
        
        }
        
        this.stableSCC=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){
            if(stableMotif(cyclesSimplified.get(i))){
                this.stableSCC.add(cyclesSimplified.get(i));
            }
        }
        
//        for(int i=0;i<this.stableSCC.size();i++){
//           cycle=(ArrayList<String>) stableSCC.get(i);
//           System.out.print(cycle.size()+"\t");
//           for(int j=0;j<cycle.size();j++){
//               System.out.print(cycle.get(j) +"\t");
//           }
//           System.out.print("\n");
//        }
        this.stableSCCresult=mergeCompatibleComponents(this.stableSCC);
        
//        System.out.print("Final simplified cycles \n");
//        for(int i=0;i<this.stableSCCresult.size();i++){
//           cycle=(ArrayList<String>) stableSCCresult.get(i);
//           System.out.print(cycle.size()+"\t");
//           for(int j=0;j<cycle.size();j++){
//               System.out.print(cycle.get(j) +"\t");
//           }
//           System.out.print("\n");
//        }
           }

    
    public void findStableStronglyConnectedComponets(int maxCycleSize, int maxMotifSize){
        
        ElementaryCyclesSearchforStableSCC ecs=new ElementaryCyclesSearchforStableSCC(this.adjacencyList,this.numberedNames);
        ArrayList<ArrayList<String>> cycles=ecs.getElementaryCycles(maxCycleSize);
        ArrayList<ArrayList<String>> cyclesSimplified=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> longCycles=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> cyclesDummy;
        ArrayList<String> cycle;
        ArrayList<String> dummyArrayList;
        int Lmax,Lmin,dictionarySize,L;
        HashSet<Integer> SetNext;
        HashSet<Integer> SetNextIntersection;
        HashSet<Integer> SetCurrent;
        HashSet<Integer> remove;
        HashSet<String> dummySet;
        String[] dummyStringArray;
        ArrayList<HashSet<Integer>> compositeCyclesSet;
        ArrayList<ArrayList<ArrayList<String>>> compositeCyclesList;
        Iterator itr,itr2;
        Map<Integer,ArrayList<String>> cycleDictionary;
        Map<Integer,ArrayList<Integer>> cyclePairs;
        
        Dictionary checkedComposites = new Hashtable();
        boolean boolDummy1,boolDummy2,boolDummy3;
        double nodeName;
        int index,index2;
        
        
        for(int i=0;i<numberedNames.length;i++){
            if(Double.parseDouble(numberedNames[i])!=Math.rint(Double.parseDouble(numberedNames[i]))){
                checkedComposites.put(numberedNames[i],true);
            }
        }
        
        //For each cycle with a composite node it checks if the complimentary node of each of its inputs
        //is in the cycle. If that is the case then that composite node cannot form part of a stable SCC
        //and it is removed
        
        for(int i=0;i<cycles.size();i++){
            boolDummy1=coherentComposite(cycles.get(i));
            if(boolDummy1){
                cyclesSimplified.add(cycles.get(i));
            }
        }
        
        cyclesDummy=new ArrayList<ArrayList<String>>();

        
        //For each cycle with more than a composite node it checks if the inputs
        //necessary for one of the composites is the opposite of the ones necessary
        //for another one in that cycle. If that is the case then this cycle can never
        //be part of a stable SCC and it is removed
        
        for(int i=0;i<cyclesSimplified.size();i++){
            cyclesDummy.add(cyclesSimplified.get(i));
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.get(i).size();j++){
                nodeName=Double.parseDouble(cyclesSimplified.get(i).get(j));
                if(nodeName>0){
                    if(nodeName!=Math.rint(nodeName)){
                        index=(Integer) dictionary.get(cyclesSimplified.get(i).get(j));
                        for(int k=0;k<nodeInputs[index].length;k++){
                            cycle.add(numberedNames[nodeInputs[index][k]]);
                        }
                    }
                }
            }
            if(!signCoherent(cycle)){
                cyclesDummy.remove(cyclesSimplified.get(i));
            }
        }
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cyclesDummy=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesSimplified.size();i++){cyclesDummy.add(cyclesSimplified.get(i));}
        
        
        //This checks if joining all the cycles containing a composite node has all the inputs
        //of said composite node. If this is not the case then this composite node can never
        //be part of a stable SCC and so we remove those cycles
        for(int i=0;i<compositeNodes.size();i++){
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.size();j++){
                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                            cycle.add(cyclesSimplified.get(j).get(k));
                        }
                    }
                }
            }
            if(!completableComposite(compositeNodes.get(i),cycle)){
                for(int j=0;j<cyclesSimplified.size();j++){
                    if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                        cyclesDummy.remove(cyclesSimplified.get(j));
                    }
                }
            }
        }

        
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cycleDictionary=new HashMap<Integer,ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){cycleDictionary.put(i, cyclesSimplified.get(i));}
        dictionarySize=cyclesSimplified.size();
        boolDummy1=true;
        Lmax=10;
        
        
        while(boolDummy1){
            Lmin=Lmax;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    compositeCyclesSet=new ArrayList<HashSet<Integer>>();
                    //In this object will save a array with the cycle numbers that have
                    //at least one of the inputs of the ith composite node. Each element
                    //in the array will be one of those inputs.
                    compositeCyclesList=new ArrayList<ArrayList<ArrayList<String>>>();
                    //In this object we will have the actual cycles (with their numbered names)
                    //of the previous array                 
                    index=this.numberedNamesList.indexOf(compositeNodes.get(i));
                    for(int j=0;j<nodeInputs[index].length;j++){
                        compositeCyclesSet.add(new HashSet<Integer>());
                        compositeCyclesList.add(new ArrayList<ArrayList<String>>());
                    }
                    for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                        if(entry.getValue().contains(compositeNodes.get(i))){
                            for(int k=0;k<nodeInputs[index].length;k++){
                                if(entry.getValue().contains(numberedNames[nodeInputs[index][k]])){
                                    compositeCyclesSet.get(k).add(entry.getKey());
                                }
                            }                            
                        }                        
                    }
                    
                    for(int j=0;j<compositeCyclesSet.size();j++){
                        itr=compositeCyclesSet.get(j).iterator();
                        while(itr.hasNext()){
                            index=(Integer) itr.next();
                            compositeCyclesList.get(j).add(cycleDictionary.get(index));
                        }
                    }
                    L=0;
                    
                    //We will first try the sets with the smallest number of cycles, so that we don't run out of memory
                    //for networks with an enormous number of networks
                    for(int j=0;j<compositeCyclesList.size()-1;j++){L=L+compositeCyclesList.get(j).size()+compositeCyclesList.get(j+1).size();}
                    if(L<Lmax){
                        boolDummy2=true;
   
                        for(int j=0;j<compositeCyclesSet.size()-1;j++){
                            boolDummy3=true; //If boolDummy3=false, that means that there are will be no combination of cycles
                                            //for inputs j and j+1 since SetCurrent.isEmpty() or SetNext.isEmpty()
                            cyclePairs=new HashMap<Integer,ArrayList<Integer>>();
                            //Here we will have all the new cycles that are formed by taking the union of one cycle containing regulator j (but not j+1)
                            //with all cycle containing regulator j+1 (but not regulator j). The idea is that we want to substitute in all arrays
                            //the new cycles formed with the original one, which we will remove.
                            if(compositeCyclesSet.get(j).isEmpty()||compositeCyclesSet.get(j+1).isEmpty()){boolDummy2=false;break;}
                                SetNext=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j+1 (but
                                //not regulator j)
                                SetNext.removeAll(compositeCyclesSet.get(j));
                                SetNextIntersection=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the cycles that contain both regulators
                                SetNextIntersection.removeAll(SetNext);
                                SetCurrent=new HashSet<Integer>(compositeCyclesSet.get(j));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j (but
                                //not regulator j+1)
                                SetCurrent.removeAll(compositeCyclesSet.get(j+1));
                                if(SetCurrent.isEmpty()){boolDummy3=false;}
                                itr=SetCurrent.iterator();
                                if(boolDummy3){
                                    while(itr.hasNext()){  
                                        index=(Integer) itr.next();
                                        cyclePairs.put(index,new ArrayList<Integer>());
                                        if(!SetNext.isEmpty()){
                                                itr2=SetNext.iterator();
                                                while(itr2.hasNext()){
                                                        index2=(Integer) itr2.next();
                                                        if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                        dummySet=new HashSet<String>();
                                                        //We are gonna put the union of cycle index with cycle index2 in this set
                                                        for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                        for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                        dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                        Arrays.sort(dummyStringArray);
                                                        dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                        if(signCoherent(dummyArrayList)){
                                                                if(coherentComposite(dummyArrayList)){
                                                                        if(!cyclesSimplified.contains(dummyArrayList)){
                                                                                //We add this so that the super large motifs, which are likely to not be stable motifs
                                                                                //don't make the program never finish
                                                                                if(dummyArrayList.size()<maxMotifSize){
                                                                                        cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                                    cyclesSimplified.add(dummyArrayList);
                                                                                    //This will later substitute cycles index and index2 with the new cycles
                                                                                    cyclePairs.get(index).add((Integer)dictionarySize);
                                                                                    cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                                    //Uncomment to output new cycles
                                                                                    //System.out.print("Cycle Next "+dictionarySize+" \n");
                                                                                    //for(int j1=0;j1<dummyArrayList.size();j1++){
                                                                                    //    System.out.print(getNameFromNumberedName(dummyArrayList.get(j1))+"\t");
                                                                                    //}
                                                                                    //System.out.print("\n");
                                                                                    dictionarySize++;
                                                                                    if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);} 
                                                                                }
                                                                                else{
                                                                                        if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);}       
                                                                                }
                                                                        }
                                                                }
                                                        }   
                                                }
                                        }
                                        if(!SetNextIntersection.isEmpty()){
                                            itr2=SetNextIntersection.iterator();
                                            while(itr2.hasNext()){
                                                index2=(Integer) itr2.next();
                                                if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                dummySet=new HashSet<String>();
                                                //We are gonna put the union of cycle index with cycle index2 in this set
                                                for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                Arrays.sort(dummyStringArray);
                                                dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                if(signCoherent(dummyArrayList)){
                                                    if(coherentComposite(dummyArrayList)){
                                                        if(!cyclesSimplified.contains(dummyArrayList)){
                                                            //We add this so that the super large motifs, which are likely to not be stable motifs
                                                            //don't make the program never finish
                                                            if(dummyArrayList.size()<maxMotifSize){
                                                                cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                cyclesSimplified.add(dummyArrayList);
                                                                cyclePairs.get(index).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)index2);
                                                                //We add this because we do not want to remove the cycles with
                                                                //that have both inputs j and j+1 because they may be needed
                                                                //in their precombined form later on
                                                                dictionarySize++;
                                                                if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);} 
                                                            }
                                                            else{
                                                                if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);}       
                                                            }
                                                        }
                                                    }
                                                }   
                                            }
                                        }
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index);
                                        //Not removing these for the case
                                        //when one puts a cutoff in the mas motif size may make finding some motifs easier
                                    }
                                }
                                //Cycle pairs gives the combined cycle number that will substitute
                                //the precombined cycle. Given a precombined cycle number it gives you
                                //the combined cycle number
                                if(boolDummy3){
                                    for (Map.Entry<Integer,ArrayList<Integer>> entry : cyclePairs.entrySet()) {
                                        index=entry.getKey();
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){                                       
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                if(!SetNextIntersection.contains((Integer) index)){compositeCyclesSet.get(k).remove((Integer) index);}
                                                for(int k1=0;k1<cyclePairs.get(index).size();k1++){
                                                    index2=cyclePairs.get(index).get(k1);
                                                    compositeCyclesSet.get(k).add((Integer) index2);
                                                }
                                            }
                                        }
                                    }
                                    //Since the elements which have only input j+1 (but not j) have already been combined
                                    //with the ones that have j, the formers can no longer form part of a stable SCC, since                            
                                    //all of those which could already contain both j and j+1, we remove them
                                    itr=SetNext.iterator();
                                    while(itr.hasNext()){
                                        index=(Integer) itr.next();
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index);
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                compositeCyclesSet.get(k).remove((Integer) index);
                                            }    
                                        }

                                    }
                                }
                        }

                        checkedComposites.remove(compositeNodes.get(i));
                        checkedComposites.put(compositeNodes.get(i), false);
                        //In case no nodes with a given input are found we set boolDumm2=false and
                        //remove all cycles containing that composite node from the arrays
                        if(!boolDummy2){
                            remove=new HashSet<Integer>();
                            for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                if(entry.getValue().contains(compositeNodes.get(i))){
                                    cyclesSimplified.remove(entry.getValue());
                                    remove.add(entry.getKey());
                                }
                            }
                            itr=remove.iterator();
                            while(itr.hasNext()){
                                cycleDictionary.remove((Integer) itr.next());
                            }
                        }
                        else{
                        //We check if the new cycles can be simplified
                            cycle=new ArrayList();
                            for(int j=0;j<cyclesSimplified.size();j++){
                                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                                            cycle.add(cyclesSimplified.get(j).get(k));
                                        }
                                    }
                                }
                            }
                            if(!completableComposite(compositeNodes.get(i),cycle)){
                                remove=new HashSet<Integer>();
                                for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                    if(entry.getValue().contains(compositeNodes.get(i))){
                                        cyclesSimplified.remove(entry.getValue());
                                        remove.add(entry.getKey());
                                    }
                                }
                                itr=remove.iterator();
                                while(itr.hasNext()){
                                    cycleDictionary.remove((Integer) itr.next());
                                }
                            }
                            
                        }
                        
                    }
                    else{
                        if(L<Lmin || Lmin==Lmax){Lmin=L+1;}
                    }
                    
                }
            }
            
            index=0;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    index++;
                }  
            }
            if(index==0){
                boolDummy1=false;
            }
            else{Lmax=Lmin;}
        
        }
        
        this.stableSCC=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){
            if(stableMotif(cyclesSimplified.get(i))){
                this.stableSCC.add(cyclesSimplified.get(i));
            }
        }
        for(int i=0;i<longCycles.size();i++){
                this.stableSCC.add(longCycles.get(i));
        }
        
        //for(int i=0;i<this.stableSCC.size();i++){
           //cycle=(ArrayList<String>) stableSCC.get(i);
           //System.out.print(cycle.size()+"\t");
           //for(int j=0;j<cycle.size();j++){
               //System.out.print(cycle.get(j) +"\t");
           //}
           //System.out.print("\n");
        //}
        this.stableSCCresult=mergeCompatibleComponents(this.stableSCC);
        
        //System.out.print("Final simplified cycles \n");
        //for(int i=0;i<this.stableSCCresult.size();i++){
           //cycle=(ArrayList<String>) stableSCCresult.get(i);
           //System.out.print(cycle.size()+"\t");
           //for(int j=0;j<cycle.size();j++){
               //System.out.print(cycle.get(j) +"\t");
           //}
           //System.out.print("\n");
        //}
    }
   
    public void findStableStronglyConnectedComponets(int maxCycleSize, int maxMotifSize, int attractor){
        
        ElementaryCyclesSearchforStableSCC ecs=new ElementaryCyclesSearchforStableSCC(this.adjacencyList,this.numberedNames,attractor);
        ArrayList<ArrayList<String>> cycles=ecs.getElementaryCycles(maxCycleSize);
        ArrayList<ArrayList<String>> cyclesSimplified=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> longCycles=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> cyclesDummy;
        ArrayList<String> cycle;
        ArrayList<String> dummyArrayList;
        int Lmax,Lmin,dictionarySize,L;
        HashSet<Integer> SetNext;
        HashSet<Integer> SetNextIntersection;
        HashSet<Integer> SetCurrent;
        HashSet<Integer> remove;
        HashSet<String> dummySet;
        String[] dummyStringArray;
        ArrayList<HashSet<Integer>> compositeCyclesSet;
        ArrayList<ArrayList<ArrayList<String>>> compositeCyclesList;
        Iterator itr,itr2;
        Map<Integer,ArrayList<String>> cycleDictionary;
        Map<Integer,ArrayList<Integer>> cyclePairs;
        
        Dictionary checkedComposites = new Hashtable();
        boolean boolDummy1,boolDummy2,boolDummy3;
        double nodeName;
        int index,index2;
        
        
        for(int i=0;i<numberedNames.length;i++){
            if(Double.parseDouble(numberedNames[i])!=Math.rint(Double.parseDouble(numberedNames[i]))){
                checkedComposites.put(numberedNames[i],true);
            }
        }
        
        //For each cycle with a composite node it checks if the complimentary node of each of its inputs
        //is in the cycle. If that is the case then that composite node cannot form part of a stable SCC
        //and it is removed
        
        for(int i=0;i<cycles.size();i++){
            boolDummy1=coherentComposite(cycles.get(i));
            if(boolDummy1){
                cyclesSimplified.add(cycles.get(i));
            }
        }
        
        cyclesDummy=new ArrayList<ArrayList<String>>();

        
        //For each cycle with more than a composite node it checks if the inputs
        //necessary for one of the composites is the opposite of the ones necessary
        //for another one in that cycle. If that is the case then this cycle can never
        //be part of a stable SCC and it is removed
        
        for(int i=0;i<cyclesSimplified.size();i++){
            cyclesDummy.add(cyclesSimplified.get(i));
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.get(i).size();j++){
                nodeName=Double.parseDouble(cyclesSimplified.get(i).get(j));
                if(nodeName>0){
                    if(nodeName!=Math.rint(nodeName)){
                        index=(Integer) dictionary.get(cyclesSimplified.get(i).get(j));
                        for(int k=0;k<nodeInputs[index].length;k++){
                            cycle.add(numberedNames[nodeInputs[index][k]]);
                        }
                    }
                }
            }
            if(!signCoherent(cycle)){
                cyclesDummy.remove(cyclesSimplified.get(i));
            }
        }
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cyclesDummy=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesSimplified.size();i++){cyclesDummy.add(cyclesSimplified.get(i));}
        
        
        //This checks if joining all the cycles containing a composite node has all the inputs
        //of said composite node. If this is not the case then this composite node can never
        //be part of a stable SCC and so we remove those cycles
        for(int i=0;i<compositeNodes.size();i++){
            cycle=new ArrayList();
            for(int j=0;j<cyclesSimplified.size();j++){
                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                            cycle.add(cyclesSimplified.get(j).get(k));
                        }
                    }
                }
            }
            if(!completableComposite(compositeNodes.get(i),cycle)){
                for(int j=0;j<cyclesSimplified.size();j++){
                    if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                        cyclesDummy.remove(cyclesSimplified.get(j));
                    }
                }
            }
        }

        
        
        cyclesSimplified=new ArrayList<ArrayList<String>>();     
        for(int i=0;i<cyclesDummy.size();i++){cyclesSimplified.add(cyclesDummy.get(i));}
        cycleDictionary=new HashMap<Integer,ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){cycleDictionary.put(i, cyclesSimplified.get(i));}
        dictionarySize=cyclesSimplified.size();
        boolDummy1=true;
        Lmax=10;
        
        
        while(boolDummy1){
            Lmin=Lmax;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    compositeCyclesSet=new ArrayList<HashSet<Integer>>();
                    //In this object will save a array with the cycle numbers that have
                    //at least one of the inputs of the ith composite node. Each element
                    //in the array will be one of those inputs.
                    compositeCyclesList=new ArrayList<ArrayList<ArrayList<String>>>();
                    //In this object we will have the actual cycles (with their numbered names)
                    //of the previous array                 
                    index=this.numberedNamesList.indexOf(compositeNodes.get(i));
                    for(int j=0;j<nodeInputs[index].length;j++){
                        compositeCyclesSet.add(new HashSet<Integer>());
                        compositeCyclesList.add(new ArrayList<ArrayList<String>>());
                    }
                    for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                        if(entry.getValue().contains(compositeNodes.get(i))){
                            for(int k=0;k<nodeInputs[index].length;k++){
                                if(entry.getValue().contains(numberedNames[nodeInputs[index][k]])){
                                    compositeCyclesSet.get(k).add(entry.getKey());
                                }
                            }                            
                        }                        
                    }
                    
                    for(int j=0;j<compositeCyclesSet.size();j++){
                        itr=compositeCyclesSet.get(j).iterator();
                        while(itr.hasNext()){
                            index=(Integer) itr.next();
                            compositeCyclesList.get(j).add(cycleDictionary.get(index));
                        }
                    }
                    L=0;
                    
                    //We will first try the sets with the smallest number of cycles, so that we don't run out of memory
                    //for networks with an enormous number of networks
                    for(int j=0;j<compositeCyclesList.size()-1;j++){L=L+compositeCyclesList.get(j).size()+compositeCyclesList.get(j+1).size();}
                    if(L<Lmax){
                        boolDummy2=true;
   
                        for(int j=0;j<compositeCyclesSet.size()-1;j++){
                            boolDummy3=true; //If boolDummy3=false, that means that there are will be no combination of cycles
                                            //for inputs j and j+1 since SetCurrent.isEmpty() or SetNext.isEmpty()
                            cyclePairs=new HashMap<Integer,ArrayList<Integer>>();
                            //Here we will have all the new cycles that are formed by taking the union of one cycle containing regulator j (but not j+1)
                            //with all cycle containing regulator j+1 (but not regulator j). The idea is that we want to substitute in all arrays
                            //the new cycles formed with the original one, which we will remove.
                            if(compositeCyclesSet.get(j).isEmpty()||compositeCyclesSet.get(j+1).isEmpty()){boolDummy2=false;break;}
                                SetNext=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j+1 (but
                                //not regulator j)
                                SetNext.removeAll(compositeCyclesSet.get(j));
                                SetNextIntersection=new HashSet<Integer>(compositeCyclesSet.get(j+1));
                                //Here we will have the cycle numbers of the cycles that contain both regulators
                                SetNextIntersection.removeAll(SetNext);
                                SetCurrent=new HashSet<Integer>(compositeCyclesSet.get(j));
                                //Here we will have the cycle numbers of the the cycle that only contain regulator j (but
                                //not regulator j+1)
                                SetCurrent.removeAll(compositeCyclesSet.get(j+1));
                                if(SetCurrent.isEmpty()){boolDummy3=false;}
                                itr=SetCurrent.iterator();
                                if(boolDummy3){
                                    while(itr.hasNext()){  
                                        index=(Integer) itr.next();
                                        cyclePairs.put(index,new ArrayList<Integer>());
                                        if(!SetNext.isEmpty()){
                                                itr2=SetNext.iterator();
                                                while(itr2.hasNext()){
                                                        index2=(Integer) itr2.next();
                                                        if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                        dummySet=new HashSet<String>();
                                                        //We are gonna put the union of cycle index with cycle index2 in this set
                                                        for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                        for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                        dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                        Arrays.sort(dummyStringArray);
                                                        dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                        if(signCoherent(dummyArrayList)){
                                                                if(coherentComposite(dummyArrayList)){
                                                                        if(!cyclesSimplified.contains(dummyArrayList)){
                                                                                //We add this so that the super large motifs, which are likely to not be stable motifs
                                                                                //don't make the program never finish
                                                                                if(dummyArrayList.size()<maxMotifSize){
                                                                                        cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                                    cyclesSimplified.add(dummyArrayList);
                                                                                    //This will later substitute cycles index and index2 with the new cycles
                                                                                    cyclePairs.get(index).add((Integer)dictionarySize);
                                                                                    cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                                    //Uncomment to output new cycles
                                                                                    //System.out.print("Cycle Next "+dictionarySize+" \n");
                                                                                    //for(int j1=0;j1<dummyArrayList.size();j1++){
                                                                                    //    System.out.print(getNameFromNumberedName(dummyArrayList.get(j1))+"\t");
                                                                                    //}
                                                                                    //System.out.print("\n");
                                                                                    dictionarySize++;
                                                                                    if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);} 
                                                                                }
                                                                                else{
                                                                                        if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);}       
                                                                                }
                                                                        }
                                                                }
                                                        }   
                                                }
                                        }
                                        if(!SetNextIntersection.isEmpty()){
                                            itr2=SetNextIntersection.iterator();
                                            while(itr2.hasNext()){
                                                index2=(Integer) itr2.next();
                                                if(!cyclePairs.containsKey(index2)){cyclePairs.put(index2,new ArrayList<Integer>());}
                                                dummySet=new HashSet<String>();
                                                //We are gonna put the union of cycle index with cycle index2 in this set
                                                for(int k=0;k<cycleDictionary.get(index).size();k++){dummySet.add(cycleDictionary.get(index).get(k));}
                                                for(int k=0;k<cycleDictionary.get(index2).size();k++){dummySet.add(cycleDictionary.get(index2).get(k));}

                                                dummyStringArray=dummySet.toArray(new String[dummySet.size()]);
                                                Arrays.sort(dummyStringArray);
                                                dummyArrayList=new ArrayList<String>(Arrays.asList(dummyStringArray));
                                                if(signCoherent(dummyArrayList)){
                                                    if(coherentComposite(dummyArrayList)){
                                                        if(!cyclesSimplified.contains(dummyArrayList)){
                                                            //We add this so that the super large motifs, which are likely to not be stable motifs
                                                            //don't make the program never finish
                                                            if(dummyArrayList.size()<maxMotifSize){
                                                                cycleDictionary.put(dictionarySize, dummyArrayList);
                                                                cyclesSimplified.add(dummyArrayList);
                                                                cyclePairs.get(index).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)dictionarySize);
                                                                cyclePairs.get(index2).add((Integer)index2);
                                                                //We add this because we do not want to remove the cycles with
                                                                //that have both inputs j and j+1 because they may be needed
                                                                //in their precombined form later on
                                                                dictionarySize++;
                                                                if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);} 
                                                            }
                                                            else{
                                                                if(stableMotif(dummyArrayList)){longCycles.add(dummyArrayList);}       
                                                            }
                                                        }
                                                    }
                                                }   
                                            }
                                        }
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index);
                                        //Not removing these for the case
                                        //when one puts a cutoff in the mas motif size may make finding some motifs easier
                                    }
                                }
                                //Cycle pairs gives the combined cycle number that will substitute
                                //the precombined cycle. Given a precombined cycle number it gives you
                                //the combined cycle number
                                if(boolDummy3){
                                    for (Map.Entry<Integer,ArrayList<Integer>> entry : cyclePairs.entrySet()) {
                                        index=entry.getKey();
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){
                                            //index is a precombined cycle number. This checks if the sets
                                            //with the cycle numbers per composite node have the precombined
                                            //cycle number. If they do, it puts the combined cycles instead
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                if(!SetNextIntersection.contains((Integer) index)){compositeCyclesSet.get(k).remove((Integer) index);}
                                                for(int k1=0;k1<cyclePairs.get(index).size();k1++){
                                                    index2=cyclePairs.get(index).get(k1);
                                                    compositeCyclesSet.get(k).add((Integer) index2);
                                                }
                                            }
                                        }
                                    }
                                    //Since the elements which have only input j+1 (but not j) have already been combined
                                    //with the ones that have j, the formers can no longer form part of a stable SCC, since                            
                                    //all of those which could already contain both j and j+1, we remove them
                                    itr=SetNext.iterator();
                                    while(itr.hasNext()){
                                        index=(Integer) itr.next();
                                        cyclesSimplified.remove(cycleDictionary.get(index));
                                        cycleDictionary.remove(index);
                                        for(int k=j+1;k<compositeCyclesSet.size();k++){
                                            if(compositeCyclesSet.get(k).contains((Integer) index)){
                                                compositeCyclesSet.get(k).remove((Integer) index);
                                            }    
                                        }

                                    }
                                }
                        }

                        checkedComposites.remove(compositeNodes.get(i));
                        checkedComposites.put(compositeNodes.get(i), false);
                        //In case no nodes with a given input are found we set boolDumm2=false and
                        //remove all cycles containing that composite node from the arrays
                        if(!boolDummy2){
                            remove=new HashSet<Integer>();
                            for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                if(entry.getValue().contains(compositeNodes.get(i))){
                                    cyclesSimplified.remove(entry.getValue());
                                    remove.add(entry.getKey());
                                }
                            }
                            itr=remove.iterator();
                            while(itr.hasNext()){
                                cycleDictionary.remove((Integer) itr.next());
                            }
                        }
                        else{
                        //We check if the new cycles can be simplified
                            cycle=new ArrayList();
                            for(int j=0;j<cyclesSimplified.size();j++){
                                if(cyclesSimplified.get(j).contains(compositeNodes.get(i))){
                                    for(int k=0;k<cyclesSimplified.get(j).size();k++){
                                        if(!cycle.contains(cyclesSimplified.get(j).get(k))){
                                            cycle.add(cyclesSimplified.get(j).get(k));
                                        }
                                    }
                                }
                            }
                            if(!completableComposite(compositeNodes.get(i),cycle)){
                                remove=new HashSet<Integer>();
                                for (Map.Entry<Integer,ArrayList<String>> entry : cycleDictionary.entrySet()) {
                                    if(entry.getValue().contains(compositeNodes.get(i))){
                                        cyclesSimplified.remove(entry.getValue());
                                        remove.add(entry.getKey());
                                    }
                                }
                                itr=remove.iterator();
                                while(itr.hasNext()){
                                    cycleDictionary.remove((Integer) itr.next());
                                }
                            }
                            
                        }
                        
                    }
                    else{
                        if(L<Lmin || Lmin==Lmax){Lmin=L+1;}
                    }
                    
                }
            }
            
            index=0;
            for(int i=0;i<compositeNodes.size();i++){
                if((Boolean) checkedComposites.get(compositeNodes.get(i))){
                    index++;
                }  
            }
            if(index==0){
                boolDummy1=false;
            }
            else{Lmax=Lmin;}
        
        }
        
        this.stableSCC=new ArrayList<ArrayList<String>>();
        for(int i=0;i<cyclesSimplified.size();i++){
            if(stableMotif(cyclesSimplified.get(i))){
                this.stableSCC.add(cyclesSimplified.get(i));
            }
        }
        for(int i=0;i<longCycles.size();i++){
                this.stableSCC.add(longCycles.get(i));
        }
        
        //for(int i=0;i<this.stableSCC.size();i++){
           //cycle=(ArrayList<String>) stableSCC.get(i);
           //System.out.print(cycle.size()+"\t");
           //for(int j=0;j<cycle.size();j++){
               //System.out.print(cycle.get(j) +"\t");
           //}
           //System.out.print("\n");
        //}
        this.stableSCCresult=mergeCompatibleComponents(this.stableSCC);
        
        //System.out.print("Final simplified cycles \n");
        //for(int i=0;i<this.stableSCCresult.size();i++){
           //cycle=(ArrayList<String>) stableSCCresult.get(i);
           //System.out.print(cycle.size()+"\t");
           //for(int j=0;j<cycle.size();j++){
               //System.out.print(cycle.get(j) +"\t");
           //}
           //System.out.print("\n");
        //}
    }
     
    
    
    public boolean getStableStronglyConnectedComponents(){
        stableStates=new ArrayList<ArrayList<String>>();
        stableValues=new ArrayList<ArrayList<String>>();
        stableType=new ArrayList<Boolean>();
        String nodeName;
        double doubleName;
        int index;
        boolean bool,stable;
        //If the stable motif contains a composite node it marks its stableType as false
        for(int i=0;i<stableSCCresult.size();i++){
            stableStates.add(new ArrayList<String>());
            stableValues.add(new ArrayList<String>());
            stable=true;
            for(int j=0;j<stableSCCresult.get(i).size();j++){
                nodeName=stableSCCresult.get(i).get(j);
                doubleName=Double.parseDouble(nodeName);
                if(doubleName==Math.rint(doubleName)){
                    if(doubleName>0){
                        stableStates.get(i).add(this.originalNames[Integer.parseInt(nodeName)-1]);
                        stableValues.get(i).add("1");
                    }
                    else{
                        stableStates.get(i).add(this.originalNames[-Integer.parseInt(nodeName)-1]);
                        stableValues.get(i).add("0");
                    }
                    
                }
                else{
                    stable=false;
                }
            }
            stableType.add(stable);
            //System.out.println(stable+" "+stableStates.get(i));
        }
        
        newFunctions=new String[stableStates.size()][];
        for(int i=0;i<stableStates.size();i++){
            //System.out.println("sSCC "+i);
            newFunctions[i]=Arrays.copyOf(this.originalFunctions, this.originalFunctions.length);
             for(int j=0;j<stableStates.get(i).size();j++){
                 index=namesList.indexOf(stableStates.get(i).get(j));
                 newFunctions[i][index]=stableValues.get(i).get(j);
                 //System.out.println(originalNames[index]+" *= "+newFunctions[i][index]);
             }
        }
        
        if(newFunctions.length==0){bool=false;}
        else{bool=true;}
        return bool;
        
    }

    public void getArbitrarySizeStableMotifs(){
        int index;
        ArrayList<ArrayList<String>> stableStatesDummy=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> stableValuesDummy=new ArrayList<ArrayList<String>>();
        String nodeName;
        double doubleName;
        for(int i=0;i<stableSCC.size();i++){
            stableStatesDummy.add(new ArrayList<String>());
            stableValuesDummy.add(new ArrayList<String>());
            for(int j=0;j<stableSCC.get(i).size();j++){
                nodeName=stableSCC.get(i).get(j);
                doubleName=Double.parseDouble(nodeName);
                if(doubleName==Math.rint(doubleName)){
                    if(doubleName>0){
                        stableStatesDummy.get(i).add(this.originalNames[Integer.parseInt(nodeName)-1]);
                        stableValuesDummy.get(i).add("1");
                    }
                    else{
                        stableStatesDummy.get(i).add(this.originalNames[-Integer.parseInt(nodeName)-1]);
                        stableValuesDummy.get(i).add("0");
                    }
                    
                }
            }
        }
        
        this.ArbitrarySizeMotifsFunctions=new String[stableStatesDummy.size()][];
        for(int i=0;i<stableStatesDummy.size();i++){
            //System.out.println("sSCC "+i);
            ArbitrarySizeMotifsFunctions[i]=Arrays.copyOf(this.originalFunctions, this.originalFunctions.length);
             for(int j=0;j<stableStatesDummy.get(i).size();j++){
                 index=namesList.indexOf(stableStatesDummy.get(i).get(j));
                 ArbitrarySizeMotifsFunctions[i][index]=stableValuesDummy.get(i).get(j);
                 //System.out.println(originalNames[index]+" *= "+newFunctions[i][index]);
             }
        }       
        
    }


    
    public int getNumberOfStableMotifs(){
        return stableSCCresult.size();
    }
    
    
    public void findOscillations(){
        String nodeName;
        double numberedName;
        boolean bool,bool2;
        int index,indexi;
        this.oscillatingSCC=new String[this.stronglyConnectedComponents.length];
        indexi=0;
        
        this.numberedStronglyConnectedComponents=new ArrayList<ArrayList<String>>();
        for(int i=0;i<this.stronglyConnectedComponents.length;i++){
            if(this.stronglyConnectedComponents[i].length>1){
                this.numberedStronglyConnectedComponents.add(new ArrayList<String>());
                for(int j=0;j<this.stronglyConnectedComponents[i].length;j++){
                    nodeName=this.stronglyConnectedComponents[i][j];
                    index=this.namesExpandedNetworkList.indexOf(nodeName);  
                    this.numberedStronglyConnectedComponents.get(indexi).add(this.numberedNames[index]);
                }
                indexi++;
            }
        }
        
        for(int i=0;i<this.numberedStronglyConnectedComponents.size();i++){
            bool=true;
            bool2=false;
            //First it checks if the SCC has both a node and its negations, and all the inputs
            //of the composite nodes. If that is the case, then bool=true
            for(int j=0;j<this.numberedStronglyConnectedComponents.get(i).size();j++){
                numberedName=Double.parseDouble(this.numberedStronglyConnectedComponents.get(i).get(j));
                if(numberedName==Math.rint(numberedName)){
                    if(numberedName>0){nodeName="-"+this.numberedStronglyConnectedComponents.get(i).get(j);}
                    else{nodeName=this.numberedStronglyConnectedComponents.get(i).get(j).split("-")[1];}
                    if(!this.numberedStronglyConnectedComponents.get(i).contains(nodeName)){
                        bool=false;                        
                        break;
                    }
                }
                else{
                    index=this.numberedNamesList.indexOf(this.numberedStronglyConnectedComponents.get(i).get(j));
                    for(int k=0;k<this.nodeInputs[index].length;k++){
                        if(!this.numberedStronglyConnectedComponents.get(i).contains(numberedNames[nodeInputs[index][k]])){
                            bool=false;
                            break;                            
                        }
                    }
                    if(!bool){break;}
                }
            }
            
            //This checks if the oscillating component has any stable motifs with no composite nodes in it, if so
            //then bool=false
            if(bool){
                for(int j=0;j<this.stableType.size();j++){
                    if(stableType.get(j)){
                        for(int k=0;k<this.stableStates.get(j).size();k++){
                            nodeName=stableStates.get(j).get(k);
                            index=this.namesExpandedNetworkList.indexOf(nodeName);
                            if(this.numberedStronglyConnectedComponents.get(i).contains(this.numberedNames[index])){
                                bool=false;
                                break;
                            }
                        }
                    }
                    if(!bool){break;}
                }
            }
            
            //This checks if the oscillating component has any stable motifs with composite nodes in it, if so
            //then bool2=true
            if(bool){
                for(int j=0;j<this.stableType.size();j++){
                    if(!stableType.get(j)){
                        for(int k=0;k<this.stableStates.get(j).size();k++){
                            nodeName=stableStates.get(j).get(k);
                            index=this.namesExpandedNetworkList.indexOf(nodeName);
                            if(this.numberedStronglyConnectedComponents.get(i).contains(this.numberedNames[index])){
                                bool2=true;
                                break;
                            }
                        }
                    }
                    if(bool2){break;}
                }
            }
            
            //If the oscillating component has a stable motif on it, it checks if the oscillating component has
            //composite nodes. If it does, the oscillations are referred too as unstable; else, they are said
            //to be stable.
            if(bool){
                
                bool=false;
                for(int j=0;j<this.numberedStronglyConnectedComponents.get(i).size();j++){
                    numberedName=Double.parseDouble(this.numberedStronglyConnectedComponents.get(i).get(j));
                    if(numberedName!=Math.rint(numberedName)){
                        bool=true;
                        break;        
                    }
                }
                if(bool){
                    if(bool2){oscillatingSCC[i]="UnstableOscillation";}
                    else{oscillatingSCC[i]="IncompleteOscillation";}      
                }
                else{oscillatingSCC[i]="FullOscillation";}
                //System.out.println(oscillatingSCC[i]);
                //System.out.println(numberedStronglyConnectedComponents.get(i));
            }
            else{
                oscillatingSCC[i]="No";
            }
            
            
        }
        
    }
    
    public void getOscillationsGA(){
        
        this.oscillatingStates=new ArrayList<ArrayList<String>>();
        oscillationType=new ArrayList<ArrayList<String>>();
        String nodeName;
        double doubleName;
        int index,count;
        boolean bool4=false,bool5=false;
        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        ConnectivityInspector ci=new ConnectivityInspector(g);
        ArrayList<String> numberList=new ArrayList<String>();
        
        //This gets which of the nodes will oscillate from oscillatingSCC and saves it in the
        //oscillatingStates arraylist with its type in the oscillationType arraylist
        index=0;
        for(int i=0;i<numberedStronglyConnectedComponents.size();i++){
            if(!oscillatingSCC[i].equals("No")){
                oscillatingStates.add(new ArrayList<String>());
                oscillationType.add(new ArrayList<String>());
                for(int j=0;j<numberedStronglyConnectedComponents.get(i).size();j++){
                    nodeName=numberedStronglyConnectedComponents.get(i).get(j);
                    doubleName=Double.parseDouble(nodeName);
                    if(doubleName==Math.rint(doubleName)){
                        if(doubleName>0){
                            oscillatingStates.get(index).add(this.originalNames[Integer.parseInt(nodeName)-1]);
                            oscillationType.get(index).add(oscillatingSCC[i]);
                        }

                    }
                }
                
                index++;
            }
        }
  
        for(int i=0;i<namesExpandedNetwork.length;i++){
            g.addVertex(namesExpandedNetwork[i]);
        }
        for(int i=0;i<this.adjacencyList.length;i++){
            for(int j=0;j<this.adjacencyList[i].length;j++){
                g.addEdge(namesExpandedNetwork[i],namesExpandedNetwork[adjacencyList[i][j]]);
            }
        }

        bool4=false;
        //bool4=true means that the oscilatting motif has downstream at least one stable motif with no composite nodes
        for(int k=0;k<oscillatingStates.size();k++){
            nodeName=oscillatingStates.get(k).get(0);
            if("FullOscillation".equals(oscillationType.get(k).get(0))&&oscillatingStates.get(k).size()==1){    
                for(int j=0;j<this.stableType.size();j++){
                    if(stableType.get(j)){
                        if(ci.pathExists(nodeName,stableStates.get(j).get(0))){
                            bool4=true;
                            break;
                        }
                    }
                    if(bool4){break;}
                }
            }
            if(bool4){break;}
        }
        
        bool5=false;
        //bool5=true means that at least a stable motifs (that isn't itself downstream of oscillating component k) 
        //has as a downstream one of the stable motifs (with no comp nodes)           
        //that are downstream of the oscillating componen k
        if(!bool4){
            //numberList contains all stable motifs with a composite nodes downstream of an oscillating component with one node            
            for(int j=0;j<this.stableType.size();j++){
                if(!stableType.get(j)){
                    for(int k=0;k<oscillatingStates.size();k++){
                        nodeName=oscillatingStates.get(k).get(0);
                        if("FullOscillation".equals(oscillationType.get(k).get(0))&&oscillatingStates.get(k).size()==1){
                            if(ci.pathExists(nodeName,stableStates.get(j).get(0))){
                                numberList.add(j+"");
                                break;
                            }
                        }
                    }  
                }
            }
            
            //If there is a stable motif upstream of the stable motifs in numberList that is itself not in numberlist, then bool5 is true
            for(int j=0;j<this.stableType.size();j++){
                if(numberList.contains(j+"")){
                    for(int i=0;i<this.stableType.size();i++){
                        if(!numberList.contains(i+"")){
                            if(ci.pathExists(stableStates.get(i).get(0), stableStates.get(j).get(0))){
                                bool5=true;
                            }
                        }
                        if(bool5){break;}
                    }
                }
                if(bool5){break;}
            }
        }
        
        //If there are no new motifs then there is no need to create new networks
        if(newFunctions.length>0){
            //If there are no downstream stable motifs with no composite nodes (bool4=false), and there is at least a downstream stable motif
            //with a composite node (numberList.size()>0), and there is no upstream stable motif of these stable motifs (bool5=false)
            //then we create a new network
            if(!bool4 && !bool5 && numberList.size()>0){
                newFunctions=Arrays.copyOf(newFunctions, newFunctions.length+1);
                newFunctions[newFunctions.length-1]=Arrays.copyOf(this.originalFunctions, this.originalFunctions.length);
                for(int k=0;k<oscillatingStates.size();k++){
                    nodeName=oscillatingStates.get(k).get(0);
                    if("FullOscillation".equals(oscillationType.get(k).get(0)) && oscillatingStates.get(k).size()==1){    
                        for(int j=0;j<oscillatingStates.get(k).size();j++){
                            index=namesList.indexOf(oscillatingStates.get(k).get(j));
                            newFunctions[newFunctions.length-1][index]=oscillationType.get(k).get(j);
                        }
                        for(int j=0;j<this.stableType.size();j++){
                            if(!stableType.get(j)){
                                if(ci.pathExists(nodeName,stableStates.get(j).get(0))){
                                    for(int i=0;i<stableStates.get(j).size();i++){
                                        index=namesList.indexOf(stableStates.get(j).get(i));
                                        newFunctions[newFunctions.length-1][index]="UnstableOscillation";
                                    }
                                }
                            }
                        }
                    }
                }
            }
  
            for(int k=0;k<oscillatingStates.size();k++){
                nodeName=oscillatingStates.get(k).get(0);
                if(!"FullOscillation".equals(oscillationType.get(k).get(0)) || oscillatingStates.get(k).size()>1){    
                    newFunctions=Arrays.copyOf(newFunctions, newFunctions.length+1);
                    newFunctions[newFunctions.length-1]=Arrays.copyOf(this.originalFunctions, this.originalFunctions.length);
                    for(int j=0;j<oscillatingStates.get(k).size();j++){
                        index=namesList.indexOf(oscillatingStates.get(k).get(j));
                        newFunctions[newFunctions.length-1][index]=oscillationType.get(k).get(j);
                    }
                    for(int j=0;j<this.stableType.size();j++){
                        if(!stableType.get(j)){
                            if(ci.pathExists(nodeName,stableStates.get(j).get(0))){
                                for(int i=0;i<stableStates.get(j).size();i++){
                                    index=namesList.indexOf(stableStates.get(j).get(i));
                                    newFunctions[newFunctions.length-1][index]="UnstableOscillation";
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
                newFunctions=Arrays.copyOf(newFunctions, newFunctions.length+1);
                newFunctions[newFunctions.length-1]=Arrays.copyOf(this.originalFunctions, this.originalFunctions.length);
                for(int k=0;k<oscillatingStates.size();k++){
                    for(int j=0;j<oscillatingStates.get(k).size();j++){
                        index=namesList.indexOf(oscillatingStates.get(k).get(j));
                        newFunctions[newFunctions.length-1][index]=oscillationType.get(k).get(j);
                    }
                }
        }
           

    }


    public static String[][] createRegulatorsArray(String functions){
        
        Scanner scanner;
        MatchResult result;
        String[][] regulators;
        String[] splitted,splittedAnd;
        Pattern patternOr,patternAnd,patternOne;
        patternOr = Pattern.compile("\\s+(or)+\\s+"); //this will separate the part separated by ors
        patternAnd = Pattern.compile("\\s+(and)+\\s+"); //this will separate the part separated by ands
        patternOne = Pattern.compile("\\s+"); //this is for the case when there is something like A*=(B and C)
        
        splitted=patternOr.split(functions);
        regulators=new String[splitted.length][];
        if(patternOne.split(functions).length==1){
            splitted=patternOne.split(functions);
        }        
        for(int j=0;j<splitted.length;j++){
            if(splitted[j].equals("")){
                System.out.println("Formatting error in the following line:\n"+functions);
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
                    System.out.println("Formatting error in the following line:\n"+functions);
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
                        System.out.println("Formatting error in the following line:\n"+functions);
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
                        System.out.println("Formatting error in the following line:\n"+functions);
                        System.exit(0);
                    }
                }
            }
            regulators[j]=Arrays.copyOf(splittedAnd, splittedAnd.length);
            //The result is that regulators[j] has a String[] array with the nodes making up its regulator
            //if its a composite node then it consists only on one String, otherwise it consists of many        
        }
        
        return regulators;
}
    

    
    
   public String[][] getNewFunctions(){
        
        return Arrays.copyOf(newFunctions, newFunctions.length);
        
}
    
   public String[][] getArbitrarySizeStableMotifsFunctions(){
        
        return Arrays.copyOf(this.ArbitrarySizeMotifsFunctions, this.ArbitrarySizeMotifsFunctions.length);
        
}
    
     
     
    
}
