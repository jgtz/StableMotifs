/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stablemotifs;

import fileOperations.FileToRead;
import java.util.Arrays;
import java.util.StringTokenizer;

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

public class Node {
    
    private String name;    //This is the name of the node
    private int[] inputNodes;   //This array contains the node number of the nodes regulating this node
    private int[] outputNodes;   //This array contains the node number of the nodes regulating this node.
                                //The output nodes are only necessary for the use of some evolution methods
                                //and the autonomous updating scheme
    private int ki; //Number of input nodes, ki=inputNodes.length()
    private int ko; //Number of output nodes, ko=outputNodes.length()
    private int[] regulatoryFunctions;  //This array contains the map from each discrete state to the next one.
                                        //In the case of a Boolean network this just corresponds to the truth
                                        //table for the current node.
    private int type;   //This indicates the type of states the node can have. The Boolean case corresponds to type==1.
                        //In the future this can be changed to generalize to any number of states.
    private int stateDiscrete;  //This stores the discrete state of the current node. If it is Boolean then it can only take
                                //the values 0 and 1.
    private int initialStateDiscrete;

    /**
     * @param name - String This is the name of the node
     * @param inputNodes - int[] This array contains the node number of the
     * nodes regulating this node
     * @param type - int This indicates the type of states the node can have. 
     * The Boolean case corresponds to type==1. 
     * 
     */
    public Node(String name, int[] inputNodes, int type) {
        this.name=name;
        this.inputNodes=Arrays.copyOf(inputNodes,inputNodes.length);
        this.type=type;   
    }

    public Node(Node otherNode) {
        this.name=otherNode.getName();
        this.inputNodes=otherNode.getInputNodes();
        this.ki=otherNode.getKi();
        this.outputNodes=otherNode.getOutputNodes();
        this.ko=otherNode.getKo();
        this.regulatoryFunctions=otherNode.getRegulatoryFunctions();
        this.stateDiscrete=otherNode.getStateDiscrete();     
        this.type=otherNode.getType();
        this.initialStateDiscrete=otherNode.getInitialStateDiscrete();
    }
    
    public Node(String name, String[] dictionary, String directoryName){
        int i,m;
        this.name = name;
        String fileName = directoryName + "/"+ name + ".txt";
	FileToRead fr = new FileToRead(fileName);
        this.initialStateDiscrete = fr.nextInt();
        this.stateDiscrete=this.initialStateDiscrete;
	this.ki=fr.nextInt();
	String[] regNom = new String[ki];
	this.inputNodes = new int[ki];
	String line;
        fr.nextLine();
	line = fr.nextLine();
	StringTokenizer t = new StringTokenizer(line);
	for(int n = 0; n < ki; ++n){
            regNom[n] = t.nextToken();
            i = OtherMethods.searchIndex(regNom[n],dictionary);
            if(i != -1){
                    inputNodes[n] = i;
            }
            else{
                    System.out.println("Couldn't find the name in the list for regulator " + n + ": " + regNom[n]);
                    System.out.println("this corresponds to the node " + name);
                    System.exit(0);
            }
	}
        int configurationNumber = (int)(Math.pow(2, ki)+0.1);
        this.regulatoryFunctions = new int[configurationNumber];

        int[] booleanIndex = new int[ki];

        while(fr.hasNextInt()){
                for(int n = 0; n < ki; ++n){
                    booleanIndex[n] = fr.nextInt();
                }
                m = OtherMethods.binaryToInt(booleanIndex);
                regulatoryFunctions[m] = fr.nextInt();
        }
        fr.close();

        this.type = 1;
     
	}
    
    public int[] getInputNodes() {
        return  Arrays.copyOf(inputNodes,inputNodes.length);
    }
    
    public int getInputNode(int n) {
        return  inputNodes[n];
    }
    public int[] getOutputNodes() {
        return  Arrays.copyOf(outputNodes,outputNodes.length);
    }
    
    public int getOutputNode(int n) {
        return  outputNodes[n];
    }     

    public void setInputNodes(int[] inputNodes) {
        this.inputNodes = Arrays.copyOf(inputNodes,inputNodes.length);
    }
    
    public void setOutputNodes(int[] outputNodes) {
        this.outputNodes = Arrays.copyOf(outputNodes,outputNodes.length);
    }    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getRegulatoryFunctions() {
        return Arrays.copyOf(regulatoryFunctions,regulatoryFunctions.length);
    }

    public int getRegulatoryFunction(int stateNumber) {
        return regulatoryFunctions[stateNumber];
    }

    public void setRegulatoryFunctions(int[] regulatoryFunctions) {
        this.regulatoryFunctions = Arrays.copyOf(regulatoryFunctions,regulatoryFunctions.length);
    }
    
    public void setRegulatoryFunction(int stateNumber, int newValue) {
        this.regulatoryFunctions[stateNumber]=newValue;
    }    

    public int getStateDiscrete() {
        return stateDiscrete;
    }

    public void setStateDiscrete(int stateDiscrete) {
        this.stateDiscrete = stateDiscrete;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public int getKi() {
        return ki;
    }
    
    public int getKo() {
        return ko;
    }    

    public void setKi(int ki) {
        this.ki = ki;
    }

    public void setKo(int ko) {
        this.ko = ko;
    }    

    public int getInitialStateDiscrete() {
        return initialStateDiscrete;
    }

    public void setInitialStateDiscrete(int initialStateDiscrete) {
        this.initialStateDiscrete = initialStateDiscrete;
    }    

    public void resetInitialStateDiscrete() {
        this.stateDiscrete = initialStateDiscrete;
    }      
    
    
}
