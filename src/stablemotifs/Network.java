package stablemotifs;

import java.io.File;
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

public class Network {

    private int N;  //This is the number of nodes
    private Node[] nodes;   //This array contains the nodes of the network
    private String[] functions; //This array contains the Boolean rules of the network
    private String[] names; //This array contains the names of the nodes of the network
        
    /**
     * @param N - int Network size (number of nodes)
     */
    
    public Network (int N, int type) {
        this.N=N;
        nodes=new Node[N];
        functions=new String[N];
    }   
    
    public Network (Network otherNetwork) {
        this.N=otherNetwork.getN();
        this.names=otherNetwork.getNames();
        this.nodes=otherNetwork.copyNodes();
    }
    
    public Network(String directoryName){
        File directory = new File(directoryName);
        String[] fileNames = directory.list();
	this.N = fileNames.length;
        this.names = new String[N];
        this.nodes = new Node[N];
        StringTokenizer st;
        Arrays.sort(fileNames);
        for(int n = 0; n < N; ++n){
            st = new StringTokenizer(fileNames[n],".");
            this.names[n] =  st.nextToken();
        }
	for(int n = 0; n < N; ++n){
            nodes[n] = new Node(names[n],names,directoryName);
	}
	
    }
    
    
    public void setNetworkState(int[] nodeStates) {
        for(int i=0;i<N;i++){
            nodes[i].setStateDiscrete(nodeStates[i]);
        }
    }
    
    public void findNodeOutputs(){
		int[][] connectionMatrix=new int[N][N];
                int[] outputNumber=new int[N];
                int[] outputNodes;
		for(int n=0;n<N;n++){
                    outputNumber[n]=0;
                }
                for(int n=0;n<N;n++)
                {
                    for(int i=0;i<nodes[n].getKi();i++)
                    {
                        connectionMatrix[nodes[n].getInputNode(i)][outputNumber[nodes[n].getInputNode(i)]]=n;    
                        outputNumber[nodes[n].getInputNode(i)]+=1;
                    }
                }
                
                for(int n=0;n<N;n++){
                    outputNodes=new int[outputNumber[n]];
                    System.arraycopy(connectionMatrix[n], 0, outputNodes, 0, outputNodes.length);
                    nodes[n].setOutputNodes(outputNodes);
                    nodes[n].setKo(outputNumber[n]);
                }
                
        }    
    
    public Node[] getNodes() {
        return nodes;
    }
    
    public Node[] copyNodes() {
        Node[] copiedNodes=new Node[N];
        for(int i=0;i<N;i++){
            copiedNodes[i]=new Node(nodes[i]);
        }
        return copiedNodes;
    }    
    
    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }
    
    public Node getNode(int nodeNumber) {
        return nodes[nodeNumber];
    }
    
    public void setFunctions(String[] functions) {
        this.functions = Arrays.copyOf(functions, functions.length);
    }
    
    public String[] getFunctions() {
        return Arrays.copyOf(this.functions, this.functions.length);
    }
    
    public void setNames(String[] names) {
        this.names = Arrays.copyOf(names, names.length);
    }
    
    public String[] getNames() {
        return Arrays.copyOf(this.names, this.names.length);
    }
    
    public void setNode(Node node,int nodeNumber) {
        this.nodes[nodeNumber] = node;
    }  
    
    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;
    }

    public void resetInitalState() {
        for(int i=0;i<N;i++){
            nodes[i].resetInitialStateDiscrete();
        }
    }
    
    public int [] getState() {
        int [] state=new int[N];
        for(int i=0;i<N;i++){
            state[i]=nodes[i].getStateDiscrete();
        }
        return state;
    }

    public void printState() {
        for(int i=0;i<N;i++){
            System.out.println(nodes[i].getName()+" "+nodes[i].getStateDiscrete());
        }
    }
    

    

    
    
}
