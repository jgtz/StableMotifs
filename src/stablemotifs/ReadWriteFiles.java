package stablemotifs;

import fileOperations.DeleteDirectoryfiles;
import fileOperations.FileToRead;
import fileOperations.FileToWrite;
import java.io.File;
import java.util.*;
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
 
public class ReadWriteFiles {
 
    public static void createTablesFromBooleanRules(String tablesDirectory, String filename) throws ScriptException{
        
        DeleteDirectoryfiles.Deldir(new File(tablesDirectory));
        new File(tablesDirectory).mkdir();
        FileToRead fr=new FileToRead(filename);
        String line,option="";
        Scanner scanner;
        Set<String> namesList=new HashSet<String>();
        Set<String> regulatorsList=new HashSet<String>();
        String [] names,namesUnordered;
        String[][] regulators;
        String[] functions,functionsSimplified;
        int initialConditions[];
        int index,Ninput;
        int [][] statesON,simplifiedON;
        FileToWrite fwFunctions,fwNames;
        
        //First we search the Network.txt file for all the names
        //of the nodes.
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"
        //The File must contain the following
        //format or there will be errors:
        //#BOOLEAN RULES
        //Node1* = Node2 and Node 3
        //..
        //
        while (fr.hasNext()){
            line=fr.nextLine();
            if("#BOOLEAN RULES".equals(line)){
                option=line;
            }
            if("#BOOLEAN RULES".equals(option) && !"#BOOLEAN RULES".equals(line)){
                scanner = new Scanner(line);
                MatchResult result;
                //Only accepts inputs of the form:
                //Node1*= Node2 and Node 3. It will ignore
                //spaces at the beginning, at the end
                //and after/before the *= sign.
                
                if(null!=scanner.findInLine("(\\S+)\\s*\\*\\s*=\\s*(.+)\\s*"))
                {
                    result = scanner.match();
                    namesList.add(result.group(1)); //Here we only want the name of the nodes
                }
                else{
                    if("".equals(line)){
                    }
                    else{
                        System.out.println("Formatting error in the following line:\n"+line);
                        System.exit(0);
                    }
                }                                    
            }                
        }

        if(!"#BOOLEAN RULES".equals(option)){
            System.out.println("Found no #BOOLEAN RULES line");
            System.exit(0);
            //If no option was found it sends out an error
        }
        
        namesUnordered = (String[])namesList.toArray(new String[namesList.size()]); //this contais the unordered names of the nodes stored
        //in the  nameList list. I will order them by largest to smallest so when we substitute the names we do not have problems with longer
        //names containing part of the shorter names.
        names=OtherMethods.ArrayByLength(namesUnordered);
        initialConditions= new int [names.length]; //we will store the initial conditions specified here. If they are not
        //specified they will be set to 0.
        functions=new String[names.length]; //the string corresponding to the function of each node is saved
        functionsSimplified=new String[names.length]; 
        regulators=new String[names.length][]; //the regulators of every node are saved here
        
        for(int i=0;i<names.length;i++){
            initialConditions[i]=0; //If the initial conditions are not specified they will be set to 0.
        }
        
        
        fr.close();
        fr=new FileToRead(filename);
        while (fr.hasNext()){
            line=fr.nextLine();
            if("#BOOLEAN RULES".equals(line)){
                option=line;
            }
            if("#BOOLEAN RULES".equals(option)&& !"#BOOLEAN RULES".equals(line)){
            //In this part we look for the inputs of each node, so we only look at the parts after the
            //equal sign. For that we parse out every character that is not reserved, that is, everything but:
            //and, or, not, (, )    
                
                scanner = new Scanner(line);
                MatchResult result;
                //Only accepts inputs of the form:
                //Node1*= Node2 and Node 3. It will ignore
                //spaces at the beginning, at the end
                //and after/before the *= sign.
                if(null!=scanner.findInLine("(\\S+)\\s*\\*\\s*=\\s*(.+)\\s*"))
                {
                    result = scanner.match();
                    index=Arrays.<String>asList(names).indexOf(result.group(1));
                    functions[index]=result.group(2); //Here we only want the update function of the nodes
                    functions[index]=functions[index].replace("("," ( ").replace(")"," ) "); //this is to facilitate the substitution of the values
                }
                else{
                    if("".equals(line)){
                    }
                    else{
                        System.out.println("Formatting error in the following line:\n"+line);
                        System.exit(0);
                    }
                }

            }
                
        }
        
        //In case one of the nodes has no regulators, gives back a warning
        //In such a case the function assigned to that node will be the identity
        //function
        
        fwNames=new FileToWrite("Names-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){  
            if(functions[i]==null){
                System.out.println("Found no function for node "+names[i]+". I will set the identity as the function");
                regulators[i]=new String[1];
                regulators[i][0]=names[i];
                functions[i]=names[i];
            }           
            fwNames.writeLine(names[i]);
        }       
        fwNames.close();
        
        //Gets the regulators from each of the rules
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"
        String[] splitted;
        Pattern pattern = Pattern.compile("[()\\s]+");
        for(int i=0;i<names.length;i++){
            splitted=pattern.split(functions[i]);
            for(int j=0;j<splitted.length;j++){
                if(!splitted[j].equals("") && !splitted[j].equals("and") && !splitted[j].equals("or") && !splitted[j].equals("not") && !splitted[j].equals("0") && !splitted[j].equals("False") && !splitted[j].equals("1") && !splitted[j].equals("True") && !splitted[j].equals("Random")){
                    regulatorsList.add(splitted[j]);
                }
            }
            regulators[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
            regulatorsList=new HashSet<String>();
        }

        

        //Now we will write the Boolean tables into the directory given by tablesDirectory 
        FileToWrite fw;
        String functionEvaluate;
        int[] booleanConfiguration;
        boolean[] booleanState;
        boolean booleanRandom=false;
        String[] namesNumbered=new String[names.length];
        String[][] regulatorsInteger = new String[names.length][];
        String rule;
        Formula f;
        int randomInt,countZeros;
        for(int i=0;i<names.length;i++){
            //Attaches number to name to avoid using dictionary
            if(i<10){namesNumbered[i]="000"+i+"_"+names[i];}
            else if (i<100){namesNumbered[i]="00"+i+"_"+names[i];}
            else if (i<1000){namesNumbered[i]="0"+i+"_"+names[i];}
            else {namesNumbered[i]=i+"_"+names[i];}
            regulatorsInteger[i]=new String[regulators[i].length];
        }
        for(int i=0;i<names.length;i++){
            for(int j=0;j<regulatorsInteger[i].length;j++){
                for(int k=0;k<regulatorsInteger.length;k++){
                    if(regulators[i][j].equals(names[k])){regulatorsInteger[i][j]=namesNumbered[k];}
                    //susbtitutes the numbered version of the names for the regulators
                }
            }
        }

        for(int i=0;i<names.length;i++){
            booleanConfiguration=new int[regulators[i].length];
            booleanState=new boolean[regulators[i].length];
            fw=new FileToWrite(tablesDirectory+"/"+namesNumbered[i]+".txt"); //file were the table is saved
            fw.writeLine(""+initialConditions[i]); //it write the initial condition
            fw.writeLine(""+regulatorsInteger[i].length);  //then the number of regulators
            for(int j=0;j<regulatorsInteger[i].length;j++){
                if(j<regulatorsInteger[i].length-1){
                    fw.writeString(regulatorsInteger[i][j]+"\t");
                    //then the names of the regulators
                }
                else{
                    fw.writeString(regulatorsInteger[i][regulatorsInteger[i].length-1]);
                    //in the last step it has to write a line jump
                }
            }
            fw.writeString("\n");
            
            statesON=new int[0][0];
            countZeros=0;
            Ninput=(int)(Math.pow(2,regulatorsInteger[i].length)+0.1);
            for(int j=0;j<Ninput;j++){
                functionEvaluate=" "+functions[i]+" ";
                OtherMethods.intToBinary(j,booleanConfiguration);
                OtherMethods.intToBoolean(booleanConfiguration,booleanState);                
                for(int k=0;k<regulators[i].length;k++){
                    fw.writeString(booleanConfiguration[k]+"\t");
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
                fw.writeString(""+randomInt+"\n");
                if(randomInt==1){
                    countZeros++;
                    statesON=Arrays.copyOf(statesON, statesON.length+1);
                    statesON[statesON.length-1]=Arrays.copyOf(booleanConfiguration, booleanConfiguration.length);
                }
                
            }
            
            fw.close();
            if(countZeros==0){functionsSimplified[i]=" 0";}
            else if(countZeros==Ninput){functionsSimplified[i]=" 1";}
            else{
                f = Formula.readintArray(statesON);
                f.reduceToPrimeImplicants();
                simplifiedON=f.toArray();
                functionsSimplified[i]=OtherMethods.wildcardToFunction(simplifiedON,regulators[i]);
            }
            functionsSimplified[i]=functionsSimplified[i].replace("~", "not ");
            
        }
        
        fwFunctions=new FileToWrite("Functions-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){           
            fwFunctions.writeLine(functionsSimplified[i].replace("("," ( ").replace(")"," ) ").replace("not ","~").replace(" True "," 1 ").replace(" False "," 0 "));
        }
        fwFunctions.close();

        
        
        
            
        }
    

    public static void createTablesFromBooleanRulesDisjunctive(String tablesDirectory, String filename) throws ScriptException{
        
        DeleteDirectoryfiles.Deldir(new File(tablesDirectory));
        new File(tablesDirectory).mkdir();
        FileToRead fr=new FileToRead(filename);
        String line,option="";
        Scanner scanner;
        Set<String> namesList=new HashSet<String>();
        Set<String> regulatorsList=new HashSet<String>();
        String [] names,namesUnordered;
        String[][] regulators;
        String[] functions,functionsSimplified;
        int initialConditions[];
        int index,Ninput;
        int [][] statesON,simplifiedON;
        FileToWrite fwFunctions,fwNames;
        
        //First we search the Network.txt file for all the names
        //of the nodes.
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"
        //The File must contain the following
        //format or there will be errors:
        //#BOOLEAN RULES
        //Node1* = Node2 and Node 3
        //..
        //
        while (fr.hasNext()){
            line=fr.nextLine();
            if("#BOOLEAN RULES".equals(line)){
                option=line;
            }
            if("#BOOLEAN RULES".equals(option) && !"#BOOLEAN RULES".equals(line)){
                scanner = new Scanner(line);
                MatchResult result;
                //Only accepts inputs of the form:
                //Node1*= Node2 and Node 3. It will ignore
                //spaces at the beginning, at the end
                //and after/before the *= sign.
                
                if(null!=scanner.findInLine("(\\S+)\\s*\\*\\s*=\\s*(.+)\\s*"))
                {
                    result = scanner.match();
                    namesList.add(result.group(1)); //Here we only want the name of the nodes
                }
                else{
                    if("".equals(line)){
                    }
                    else{
                        System.out.println("Formatting error in the following line:\n"+line);
                        System.exit(0);
                    }
                }                                    
            }                
        }

        if(!"#BOOLEAN RULES".equals(option)){
            System.out.println("Found no #BOOLEAN RULES line");
            System.exit(0);
            //If no option was found it sends out an error
        }
        
        namesUnordered = (String[])namesList.toArray(new String[namesList.size()]); //this contais the unordered names of the nodes stored
        //in the  nameList list. I will order them by largest to smallest so when we substitute the names we do not have problems with longer
        //names containing part of the shorter names.
        names=OtherMethods.ArrayByLength(namesUnordered);
        initialConditions= new int [names.length]; //we will store the initial conditions specified here. If they are not
        //specified they will be set to 0.
        functions=new String[names.length]; //the string corresponding to the function of each node is saved
        functionsSimplified=new String[names.length]; 
        regulators=new String[names.length][]; //the regulators of every node are saved here
        
        for(int i=0;i<names.length;i++){
            initialConditions[i]=0; //If the initial conditions are not specified they will be set to 0.
        }
        
        
        fr.close();
        fr=new FileToRead(filename);
        while (fr.hasNext()){
            line=fr.nextLine();
            if("#BOOLEAN RULES".equals(line)){
                option=line;
            }
            if("#BOOLEAN RULES".equals(option)&& !"#BOOLEAN RULES".equals(line)){
            //In this part we look for the inputs of each node, so we only look at the parts after the
            //equal sign. For that we parse out every character that is not reserved, that is, everything but:
            //and, or, not, (, )    
                
                scanner = new Scanner(line);
                MatchResult result;
                //Only accepts inputs of the form:
                //Node1*= Node2 and Node 3. It will ignore
                //spaces at the beginning, at the end
                //and after/before the *= sign.
                if(null!=scanner.findInLine("(\\S+)\\s*\\*\\s*=\\s*(.+)\\s*"))
                {
                    result = scanner.match();
                    index=Arrays.<String>asList(names).indexOf(result.group(1));
                    functions[index]=result.group(2); //Here we only want the update function of the nodes
                    functions[index]=functions[index].replace("("," ( ").replace(")"," ) "); //this is to facilitate the substitution of the values
                }
                else{
                    if("".equals(line)){
                    }
                    else{
                        System.out.println("Formatting error in the following line:\n"+line);
                        System.exit(0);
                    }
                }

            }
                
        }
        
        //In case one of the nodes has no regulators, gives back a warning
        //In such a case the function assigned to that node will be the identity
        //function
        
        fwNames=new FileToWrite("Names-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){  
            if(functions[i]==null){
                System.out.println("Found no function for node "+names[i]+". I will set the identity as the function");
                regulators[i]=new String[1];
                regulators[i][0]=names[i];
                functions[i]=names[i];
            }           
            fwNames.writeLine(names[i]);
        }       
        fwNames.close();
        
        //Gets the regulators from each of the rules
        //The reserved keywords are "or", "not", "and", "(", ")", "True", "False", "Random", "0" and "1"
        String[] splitted;
        Pattern pattern = Pattern.compile("[()\\s]+");
        for(int i=0;i<names.length;i++){
            splitted=pattern.split(functions[i]);
            for(int j=0;j<splitted.length;j++){
                if(!splitted[j].equals("") && !splitted[j].equals("and") && !splitted[j].equals("or") && !splitted[j].equals("not") && !splitted[j].equals("0") && !splitted[j].equals("False") && !splitted[j].equals("1") && !splitted[j].equals("True") && !splitted[j].equals("Random")){
                    regulatorsList.add(splitted[j]);
                }
            }
            regulators[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
            regulatorsList=new HashSet<String>();
        }

        

        //Now we will write the Boolean tables into the directory given by tablesDirectory 
        FileToWrite fw;
        String functionEvaluate;
        int[] booleanConfiguration;
        boolean[] booleanState;
        boolean booleanRandom=false;
        String[] namesNumbered=new String[names.length];
        String[][] regulatorsInteger = new String[names.length][];
        String rule;
        Formula f;
        int randomInt,countZeros;
        for(int i=0;i<names.length;i++){
            //Attaches number to name to avoid using dictionary
            if(i<10){namesNumbered[i]="000"+i+"_"+names[i];}
            else if (i<100){namesNumbered[i]="00"+i+"_"+names[i];}
            else if (i<1000){namesNumbered[i]="0"+i+"_"+names[i];}
            else {namesNumbered[i]=i+"_"+names[i];}
            regulatorsInteger[i]=new String[regulators[i].length];
        }
        for(int i=0;i<names.length;i++){
            for(int j=0;j<regulatorsInteger[i].length;j++){
                for(int k=0;k<regulatorsInteger.length;k++){
                    if(regulators[i][j].equals(names[k])){regulatorsInteger[i][j]=namesNumbered[k];}
                    //susbtitutes the numbered version of the names for the regulators
                }
            }
        }

        for(int i=0;i<names.length;i++){
            booleanConfiguration=new int[regulators[i].length];
            booleanState=new boolean[regulators[i].length];
            fw=new FileToWrite(tablesDirectory+"/"+namesNumbered[i]+".txt"); //file were the table is saved
            fw.writeLine(""+initialConditions[i]); //it write the initial condition
            fw.writeLine(""+regulatorsInteger[i].length);  //then the number of regulators
            for(int j=0;j<regulatorsInteger[i].length;j++){
                if(j<regulatorsInteger[i].length-1){
                    fw.writeString(regulatorsInteger[i][j]+"\t");
                    //then the names of the regulators
                }
                else{
                    fw.writeString(regulatorsInteger[i][regulatorsInteger[i].length-1]);
                    //in the last step it has to write a line jump
                }
            }
            fw.writeString("\n");
            
            statesON=new int[0][0];
            countZeros=0;
            Ninput=(int)(Math.pow(2,regulatorsInteger[i].length)+0.1);
            for(int j=0;j<Ninput;j++){
                functionEvaluate=" "+functions[i]+" ";
                OtherMethods.intToBinary(j,booleanConfiguration);
                OtherMethods.intToBoolean(booleanConfiguration,booleanState);                
                for(int k=0;k<regulators[i].length;k++){
                    fw.writeString(booleanConfiguration[k]+"\t");
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
                fw.writeString(""+randomInt+"\n");
                if(randomInt==1){
                    countZeros++;
                    statesON=Arrays.copyOf(statesON, statesON.length+1);
                    statesON[statesON.length-1]=Arrays.copyOf(booleanConfiguration, booleanConfiguration.length);
                }
                
            }
            
            fw.close();          
        }
        
        functionsSimplified=NetworkReduction.createAndSimplifyRuleDNF(names,functions);
        fwFunctions=new FileToWrite("Functions-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){           
            fwFunctions.writeLine(functionsSimplified[i].replace("("," ( ").replace(")"," ) ").replace("not ","~").replace(" True "," 1 ").replace(" False "," 0 "));
        }
        fwFunctions.close();

        
        
        
            
        }
    
    public static void createFromTables(String tablesDirectory) {
        
        Set<String> namesList=new HashSet<String>();
        Set<String> regulatorsList=new HashSet<String>();
        String [] names,namesUnordered;
        String[][] regulators;
        String[] functions,functionsSimplified;
        int initialConditions[];
        int index,Ninput;
        int [][] statesON,simplifiedON;
        FileToWrite fwFunctions,fwNames;
        FileToRead fr;
        File folder = new File(tablesDirectory);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                namesList.add(file.getName().split(".txt")[0].split("_")[0]);
                
            }
        }

        
        namesUnordered = (String[])namesList.toArray(new String[namesList.size()]); //this contais the unordered names of the nodes stored
        //in the  nameList list. I will order them by largest to smallest so when we substitute the names we do not have problems with longer
        //names containing part of the shorter names.
        names=OtherMethods.ArrayByLength(namesUnordered);
        initialConditions= new int [names.length]; //we will store the initial conditions specified here. If they are not
        //specified they will be set to 0.
        functions=new String[names.length]; //the string corresponding to the function of each node is saved
        functionsSimplified=new String[names.length]; 
        regulators=new String[names.length][]; //the regulators of every node are saved here
        
        for(int i=0;i<names.length;i++){
            initialConditions[i]=0; //If the initial conditions are not specified they will be set to 0.
        }
        
        
        //In case one of the nodes has no regulators, gives back a warning
        //In such a case the function assigned to that node will be the identity
        //function
        
        fwNames=new FileToWrite("Names-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){         
            fwNames.writeLine(names[i]);    
        }       
        fwNames.close();
        
        String[] splitted;
        for(int i=0;i<names.length;i++){
            fr=new FileToRead(tablesDirectory+"/"+names[i]+"_"+names[i]+".txt");
            fr.nextLine();fr.nextLine();
            splitted=fr.nextLine().split("\t");
            regulatorsList=new HashSet<String>();
            for(int j=0;j<splitted.length;j++){regulatorsList.add(splitted[j]);}
            regulators[i] = Arrays.copyOf(splitted, splitted.length);
            //regulators[i] = (String[])regulatorsList.toArray(new String[regulatorsList.size()]);
            fr.close();
        }
               

        //Now we will write the Boolean tables into the directory given by tablesDirectory 
        FileToWrite fw;
        String functionEvaluate;
        int[] booleanConfiguration;
        boolean[] booleanState;
        boolean booleanRandom=false;
        String[] namesNumbered=new String[names.length];
        String[][] regulatorsInteger = new String[names.length][];
        String rule;
        Formula f;
        int randomInt,countZeros;
        for(int i=0;i<names.length;i++){
            regulatorsInteger[i]=new String[regulators[i].length];
        }
        for(int i=0;i<names.length;i++){
            namesNumbered[i]=names[i];
            for(int j=0;j<regulatorsInteger[i].length;j++){
                for(int k=0;k<regulatorsInteger.length;k++){
                    if(regulators[i][j].equals(namesNumbered[k])){regulatorsInteger[i][j]=namesNumbered[k];}
                }
            }
        }

        for(int i=0;i<names.length;i++){
            booleanConfiguration=new int[regulators[i].length];
            booleanState=new boolean[regulators[i].length];
            fr=new FileToRead(tablesDirectory+"/"+names[i]+"_"+names[i]+".txt"); //file were the table is saved
            fr.nextLine();fr.nextLine();fr.nextLine();
            
            statesON=new int[0][0];
            countZeros=0;
            Ninput=(int)(Math.pow(2,regulatorsInteger[i].length));
            for(int j=0;j<Ninput;j++){
                functionEvaluate=" "+functions[i]+" ";
                OtherMethods.intToBinary(j,booleanConfiguration);
                OtherMethods.intToBoolean(booleanConfiguration,booleanState);                
                splitted=fr.nextLine().split("\t");
                randomInt=Integer.parseInt(splitted[regulators[i].length]);
                if(randomInt==1){
                    countZeros++;
                    statesON=Arrays.copyOf(statesON, statesON.length+1);
                    statesON[statesON.length-1]=Arrays.copyOf(booleanConfiguration, booleanConfiguration.length);
                }               
            } 
            fr.close();
            if(countZeros==0){functionsSimplified[i]=" 0";}
            else if(countZeros==Ninput){functionsSimplified[i]=" 1";}
            else{
                f = Formula.readintArray(statesON);
                f.reduceToPrimeImplicants();
                simplifiedON=f.toArray();
                functionsSimplified[i]=OtherMethods.wildcardToFunction(simplifiedON,regulators[i]);
            }
            functionsSimplified[i]=functionsSimplified[i].replace("~", "not ");
            
        }
        
        fwFunctions=new FileToWrite("Functions-"+tablesDirectory+".txt");
        for(int i=0;i<names.length;i++){           
            fwFunctions.writeLine(functionsSimplified[i].replace("("," ( ").replace(")"," ) ").replace("not ","~").replace(" True "," 1 ").replace(" False "," 0 "));
        }
        fwFunctions.close();

        
        
        
            
        }
    
   public static ArrayList<String[]> getStableMotifsFromFileAndRemoveDuplicates(String fileName){
       
        String motif;
        boolean bool;
        String[] resultDummy;
        ArrayList<String[]> stableMotifs;
        FileToRead motifs;
        motifs=new FileToRead(fileName);
        stableMotifs=new ArrayList<String[]>();
        while(motifs.hasNext()){
            motif=motifs.nextLine();
            if(motif.split("\t").length>0){
                if(!motif.split("\t")[0].equals("")){
                    resultDummy=Arrays.copyOf(motif.split("\t"),motif.split("\t").length);
                    Arrays.sort(resultDummy);
                    bool=true;
                    for(int i=0;i<stableMotifs.size();i++){
                        if(Arrays.equals(stableMotifs.get(i), resultDummy)){
                            bool=false;
                            break;
                        }
                    }
                    if(bool){
                        stableMotifs.add(resultDummy);
                    }
                }
            }
        }
        motifs.close();
        return stableMotifs;
   }
    
    public static void writeStableMotifsAndQuasiAttractorFiles(String networkName, String[] names, ArrayList<String[]> stableMotifs, ArrayList<String[]> finalAttractorsReduction, ArrayList<String[]> unstableAttractorsFinal){
        
        String function;
        String[] resultDummy;
        FileToWrite attrsred;
        
        attrsred=new FileToWrite(networkName+"-StableMotifs.txt");
        for(int i=0;i<stableMotifs.size();i++){          
            function="";
            resultDummy=stableMotifs.get(i);
            for(int j=0;j<resultDummy.length;j++){                    
                function=function+resultDummy[j]+"\t";
            }
            attrsred.writeLine(function);      
        }
        attrsred.close();
           
        DeleteDirectoryfiles.Deldir(new File("QA-"+networkName));
        new File("QA-"+networkName).mkdir();
        DeleteDirectoryfiles.Deldir(new File("PQA-"+networkName));
        new File("PQA-"+networkName).mkdir();
        attrsred=new FileToWrite(networkName+"-QuasiAttractors"+".txt");
        function="";
        for(int i=0;i<names.length;i++){                 
            function=function+names[i]+"\t";                  
        }
        attrsred.writeLine(function);
        for(int i=0;i<finalAttractorsReduction.size();i++){
            function="";
            resultDummy=finalAttractorsReduction.get(i);
            for(int j=0;j<resultDummy.length;j++){                    
                function=function+resultDummy[j]+"\t";
            }
            attrsred.writeLine(function);      
        }
        attrsred.close();
        attrsred=new FileToWrite(networkName+"-PutativeQuasiAttractors"+".txt");
        function="";
        for(int i=0;i<names.length;i++){                 
            function=function+names[i]+"\t";                  
        }
        attrsred.writeLine(function);
        for(int i=0;i<unstableAttractorsFinal.size();i++){
            function="";
            resultDummy=unstableAttractorsFinal.get(i);
            for(int j=0;j<resultDummy.length;j++){                    
                function=function+resultDummy[j]+"\t";
            }
            attrsred.writeLine(function);      
        }
        attrsred.close();
    
    }    

    public static void writeAttractorsCorrespondingToMotifSequence(ArrayList<String[]> motifsAttractorsDiagram, String networkName){
        
        FileToWrite fw=new FileToWrite(networkName+"-DiagramSequencesAttractors.txt");
        String[] motifAttractor;
        for(int i=0;i<motifsAttractorsDiagram.size();i++){
            motifAttractor=motifsAttractorsDiagram.get(i);
            fw.writeLine(motifAttractor[0]+"\tAttractor"+motifAttractor[1]);
        }
        fw.close();
        

}

}
