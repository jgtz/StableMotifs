========================
README
========================
------------
I)	THE METHOD
------------

The method implemented in this Java library is the Stable Motif Control Algorithm, which is described in the following article: 

Jorge G. T. Zañudo and Réka Albert (2015).
Cell fate reprogramming by control of intracellular network dynamics.
PLoS Comput Biol 11(4): e1004193.

This algorithm is based on the concept of stable motifs and its related algorithm to find the attractors of a logical model, which is described in the following article: 

Jorge G. T. Zañudo and Réka Albert (2013).
An effective network reduction approach to find the dynamical repertoire of discrete dynamic networks. 
Chaos 23 (2), 025111. Focus Issue: Quantitative Approaches to Genetic Networks.

------------
II)	INSTRUCTIONS
------------

To run the project, go to the command line, navigate to the folder where the "StableMotifs.jar" file and the "lib" folder are located. Once there type the command:

java -jar StableMotifs.jar RulesFile.txt

where "RulesFile.txt" is the name of the TXT file with the Boolean functions of the network. The "RulesFile.txt" file should have the following format:

"
#BOOLEAN RULES
Node1 *= Node2 or Node3
Node2 *= Node1 and Node2
Node3 *= ((not Node3 or Node4) and not Node1)
Node4 *= 1
Node5 *= 0
...
NodeN *= not Node1 or (Node1 and Node2)
"

In the above, the text before the "*=" symbol is the node name, while the text after the "*=" symbol is the Boolean function of the node.

As an optional input, one can include a cutoff for the maximum cycle length and the maximum stable motif size during the network reduction. In this case the command would be

java -jar StableMotifs.jar RulesFile.txt mcl msm

where "mcl" would be a number corresponding to the maximum cycle length, and "msm" would be a number corresponding to the maximum stable motif size.

NODE NAMES

For the node n*ames use only alphanumeric characters (A-Z,a-z), numbers (0-9) and "_". The reserved words for the program, which shouldn't be used for node names, are: "True", "False", "true", "false", "0", "1", "and", "or", and "not".

BOOLEAN FUNCTIONS

For the Boolean functions use only the node names, the logical operators "and", "or", "not", and the parentheses symbols ")" and "(". In case the Boolean function is constant, use "0" or "1", depending on the constant state of the function. The logical function does not need to be written in a disjunctive normal form; the program will take the logical form in the TXT file and transform it into its disjunctive normal form using the Quine–McCluskey algorithm.

------------
III)	OUTPUT
------------

The program will produce the following:

•	2 tab separated TXT files with the quasi-attractors.
•	A folder with the reduced networks for the first tab separated TXT file.
•	A folder with the reduced networks for the second tab separated TXT file.
•	A tab separated TXT file with the stable motifs found during network reduction.
•	2 tab separated TXT file with the sequences of stable motifs composing the stable motif succession diagram, and each transition in the diagram.
•	A TXT file with the stable motif control sets.

The names of these files and folder will depend on the the name if the input TXT file. For example, for an input TXT file named "RulesFile.txt", the two tab separated output files with the quasi-attractors will be "RulesFile-QuasiAttractors.txt" and "RulesFile-PutativeQuasiAttractors.txt". For the case of the folders with the reduced networks, the folder names will be "QA-RulesFile" and "PQA-RulesFile", respectively. The TXT file with the stable motifs will be named "RulesFile-StableMotifs.txt". Finally, the TXT file with the sequences of stable motifs, the transitions of the succession diagram, and the stable motif control sets will be named "RulesFile-DiagramSequencesAttractors.txt", "Diagram-RulesFileModified.txt", and "RulesFile-StableMotifControlSets.txt", respectively.

Each of the TXT files with the quasi-attractors contains one line with the node names and another line for every quasi-attractor, with the states of the nodes in the quasi-attractors. If the state of the node stabilizes in a quasi-attractor, then its corresponding state will be either "0"or "1"; if it does not then it will marked with an "X". The program will also produce a TXT file with the Boolean functions for the reduced network corresponding to each quasi-attractor; this reduced network will only contains the nodes whose state in the quasi-attractor does not stabilize (i.e., the ones marked with an "X"). The TXT files with the reduced networks for the quasi-attractors in the "RulesFile-QuasiAttractors.txt" file will be in the folder "QA-RulesFile" and have the names "QA-ReducedNetworkZ.txt", where "Z" will be a number corresponding to the order of the quasi-attractor in the "RulesFile-QuasiAttractors.txt" file. Similarly, the TXT file for the quasi-attractors in the "RulesFile-PutativeQuasiAttractors.txt" file will be in the folder "PQA-RulesFile" and have the names "PQA-ReducedNetworkZ.txt".

The quasi-attractors in the file ending with "QuasiAttractors.txt" will have at least one corresponding attractor in the asynchronous Boolean network that has the same stabilized states as the quasi-attractor. For example, if Node1=0, Node2=1 and Node3=X in a quasi-attractor, then there will be at least an attractor with Node1=0 and Node2=1, while Node3 could oscillate or take the state 0 or 1 (in most cases Node3 will oscillate; for more details see the article referenced in the "THE METHOD" section).

The quasi-attractors in the file ending with "PutativeQuasiAttractors.txt" may or may not have a corresponding attractor in the asynchronous Boolean network. As explained in more detail in the article referenced in the "THE METHOD" section, these quasi-attractors need to be considered to make sure the method finds all attractors. These quasi-attractors arise because a group of nodes in a strongly connected component may be able display both oscillatory and fixed state behavior in the attractors. Our results with random Boolean networks suggest that in only about 2% of the networks will one or more of the putative quasi-attractors correspond to an attractor of the asynchronous Boolean network. It is worth noting that these putative quasi-attractors are an artifact of the asynchronous updating scheme being Markovian (i.e. having no memory).

------------
IV)	EXAMPLE
------------

As an example we include the TXT file "TLGLNetwork.txt", which contains the Boolean functions for the T-LGL leukemia Boolean network with the  input signals IL15=1 and Stimuli=1 (for more details, see the article referenced in the "THE METHOD" section). To run the program, go to the folder where the "StableMotifs.jar" file and the "lib" folder are located and type the command:

java -jar StableMotifs.jar TLGLNetwork.txt

If everything is working properly, the following should appear in the console:

$ java -jar StableMotifs.jar TLGLNetwork.txt

Filename: TLGLNetwork.txt
Creating Boolean table directory: TLGLNetwork
Boolean table directory created.
Creating functions and names files.
Functions and names files created.
Performing network reduction...
Finding stable motifs in this network...
There are 4 stable motifs in this network:
1/4     PDGFR=0 S1P=0   SPHK1=0
2/4     P2=1
3/4     TBET=1
4/4     Ceramide=0      PDGFR=1 S1P=1   SPHK1=1
There is 1 oscillating motif in this network that could display unstable or incomplete oscillations.
Performing network reduction using motif 1/5...
Performing network reduction using motif 2/5...
Performing network reduction using motif 3/5...
Performing network reduction using motif 4/5...
Performing network reduction using motif 5/5...
Network reduction complete.
Removing duplicate quasi-attractors.
Total number of quasi-attractors: 3
Number of putative quasi-attractors: 0
Total time for finding quasi-attractors: 212.713955303 s
Writing TXT files with quasi-attractors and stable motifs.
Starting analyis of stable motif succession diagram.
Identifying quasi-attractors corresponding to stable motif sequences.
Shortening stable motif sequences.
Finding control sets for each stable motif...
Creating control sets for each stable motif sequence.
Removing duplicates control sets.
Total time for finding stable motif control sets: 54.818246606 s
Writing TXT files with stable motif control sets.
Done!

For this case there are no putative quasi-attractors. 3 of the 5 quasi-attractors correspond to the apoptosis attractor (Apoptosis=1). The remaining 2 quasi-attractors correspond to the T-LGL leukemia attractor (with either P2=0 or P2=1).

------------
V)	SOFTWARE USED AND LICENSES
------------

JohnsonCycleAlgorithm

A modified version of the Java implementation “JohnsonCycleAlgorithm” by Frank Meyer (http://www.normalisiert.de/) is used to search for cycles in the network. The code for the Java implementation is available under the BSD-2 license.

JGraphT

Several functions from the JGraphT java class library by Barak Naveh and Contributors are used (https://github.com/jgrapht/jgrapht). JGraphT is available under GNU LESSER GENERAL PUBLIC LICENSE Version 2.1.

Quine-McCluskey_algorithm

An implementation of the Quine-McCluskey_algorithm in the “Term.java” and “Formula.java” classes were retrieved in 2013 from http://en.literateprograms.org/Quine-McCluskey_algorithm_(Java)?action=history&offset=20110925122251. The “Term.java” and “Formula.java” classes are available under the MIT License.

------------
VI)	COPYRIGHT
------------

The MIT License (MIT)

Copyright (c) 2013-2015 Jorge G. T. Zañudo and Réka Albert.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
