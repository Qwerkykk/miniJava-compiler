package myTypes;
import myTypes.*;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IR_Producer {
    public static FileWriter fileWriter;
    public static String lastTypeReturned;
    public static List<Pair<String, String>> parameterList;
    public static List<String> expressionList;
    public static List<Pair<String, String>> classAllocationsList;
    public static List<ClassFuncDetails> classFuncDetailsList;


    public static int getCountFuncOfClass(String name) {
        int i = 0;

        for (ClassFuncDetails classFuncDetails : classFuncDetailsList) {
            if (classFuncDetails.className.equals(name))
                i++;
        }

        return i;
    }

    public static int varCounter;
    public static int branchCounter;
    public static int loopCounter;
    public static int andCounter;

    public static boolean saveAllocationCredentials;

    public static void resetAndCounter() {
        andCounter = 0;
    }

    public static void andCounterUp() {
        andCounter++;
    }

    public static void initClassFuncDetailsList() {
        classFuncDetailsList = new ArrayList<ClassFuncDetails>();
    }

    public static void resetLoopCounter() {
        loopCounter = 0;
    }

    public static void loopCounterUp() {
        loopCounter++;
    }

    public static void resetVarCounter() {
        varCounter = 0;
    }

    public static void varCounterUp() {
        varCounter++;
    }

    public static String createVar() {
        String var = "%_" + varCounter;
        varCounterUp();
        return var;
    }

    public static void resetBranchCounter() {
        branchCounter = 0;
    }

    public static void branchCounterUp() {
        branchCounter++;
    }

    public static void addParameter(Pair<String, String> param) {
        parameterList.add(param);
    }

    public static void initClassAllocations() {
        classAllocationsList = new LinkedList<Pair<String, String>>();
    }

    public static void initParameterList() {
        parameterList = new LinkedList<Pair<String, String>>();
    }

    public static void initExpressionList() {
        expressionList = new LinkedList<String>();
    }

    public static void addExpression(String expr) {
        expressionList.add(expr);
    }

    public static void openFile(String fileName) throws IOException {
        fileWriter = new FileWriter(fileName);
    }

    public static String getIRVarType(String name) {
        for (Pair<String, String> entry : classAllocationsList) {
            if (name.equals(entry.getKey()))
                return entry.getValue();
        }
        return "";
    }

    public static String loadIntFromVtable(String expr, SymbolTable argu) {
        String elementPtr, bitcast, var = "";

        try {
            elementPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32*\n\t");

            var = IR_Producer.createVar();
            IR_Producer.fileWriter.write(var + " = load i32, i32* " + bitcast + "\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return var;
    }

    public static String loadBooleanFromVtable(String expr, SymbolTable argu) {
        String elementPtr, bitcast, var = "";

        try {
            elementPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8*\n\t");

            var = IR_Producer.createVar();
            IR_Producer.fileWriter.write(var + " = load i8, i8* " + bitcast + "\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return var;
    }


    public static String loadIntArrayPointerFromVtable(String expr, SymbolTable argu) {
        String elementPtr, bitcast = "";

        try {
            elementPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32**\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitcast;
    }

    public static String loadBooleanArrayPointerFromVtable(String expr, SymbolTable argu) {
        String elementPtr, bitcast = "";

        try {
            elementPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitcast;
    }

    public static String writeBooleanArrayLookup(String id, String lookupExpr, SymbolTable argu) {
        String arrayAddr, sizeArray, compare1, compare2, validation, okLabel, errorLabel;
        String position, castedAddr, assignAddr = "";
        try {
            arrayAddr = id;
            castedAddr = IR_Producer.createVar();
            sizeArray = IR_Producer.createVar();
            compare1 = IR_Producer.createVar();
            compare2 = IR_Producer.createVar();
            validation = IR_Producer.createVar();
            okLabel = "oob_ok_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();
            errorLabel = "oob_error_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();

            if(!id.startsWith("%")){
                arrayAddr = IR_Producer.createVar();

                if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(id))
                    id = loadBooleanArrayPointerFromVtable(id, argu);

                if (!id.startsWith("%"))
                    id = "%" + id;
                IR_Producer.fileWriter.write(arrayAddr + " = load i8*, i8** " + id + "\n\t");

            }

            IR_Producer.fileWriter.write(castedAddr + "= bitcast i8* " + arrayAddr + " to i32*\n\t");
            IR_Producer.fileWriter.write(sizeArray + " = load i32, i32* " + castedAddr + "\n\t");

            String var;
            if (lookupExpr.matches("[0-9]+") || lookupExpr.startsWith("%"))
                var = lookupExpr;
            else if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(lookupExpr)) {
                var = IR_Producer.loadIntFromVtable(lookupExpr, argu);
            } else {
                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i32, i32* %" + lookupExpr + "\n\t");
            }

            IR_Producer.fileWriter.write(compare1 + " = icmp sge i32 " + var + ", 0\n\t");
            IR_Producer.fileWriter.write(compare2 + " = icmp slt i32 " + var + ", " + sizeArray + "\n\t");
            IR_Producer.fileWriter.write(validation + " = and i1 " + compare1 + ", " + compare2 + "\n\t");
            IR_Producer.fileWriter.write("br i1 " + validation + ", label %" + okLabel + ", label %" + errorLabel + "\n\t");

            position = IR_Producer.createVar();
            assignAddr = IR_Producer.createVar();

            IR_Producer.fileWriter.write("\n" + errorLabel + ":\n\t");
            IR_Producer.fileWriter.write("call void @throw_oob()\n\t");
            IR_Producer.fileWriter.write("br label %" + okLabel + "\n\t");
            IR_Producer.fileWriter.write("\n" + okLabel + ":\n\t");

            IR_Producer.fileWriter.write(position + " = add i32 4, " + var + "\n\t");
            IR_Producer.fileWriter.write(assignAddr + " = getelementptr i8, i8* " + arrayAddr + ", i32 " + position + "\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return assignAddr;
    }

    public static String writeIntArrayLookup(String id, String lookupExpr, SymbolTable argu) {
        String arrayAddr, sizeArray, compare1, compare2, validation, okLabel, errorLabel;
        String position, assignAddr = "";
        try {
            arrayAddr = id;
            sizeArray = IR_Producer.createVar();
            compare1 = IR_Producer.createVar();
            compare2 = IR_Producer.createVar();
            validation = IR_Producer.createVar();
            okLabel = "oob_ok_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();
            errorLabel = "oob_error_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();

            if(!id.startsWith("%")) {
                arrayAddr = IR_Producer.createVar();
                if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(id))
                    id = loadIntArrayPointerFromVtable(id, argu);

                if (!id.startsWith("%"))
                    id = "%" + id;
                IR_Producer.fileWriter.write(arrayAddr + " = load i32*, i32** " + id + "\n\t");

            }
            IR_Producer.fileWriter.write(sizeArray + " = load i32, i32* " + arrayAddr + "\n\t");

            String var;
            if (lookupExpr.matches("[0-9]+") || lookupExpr.startsWith("%"))
                var = lookupExpr;
            else if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(lookupExpr)) {
                var = IR_Producer.loadIntFromVtable(lookupExpr, argu);
            } else {
                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i32, i32* %" + lookupExpr + "\n\t");
            }

            IR_Producer.fileWriter.write(compare1 + " = icmp sge i32 " + var + ", 0\n\t");
            IR_Producer.fileWriter.write(compare2 + " = icmp slt i32 " + var + ", " + sizeArray + "\n\t");
            IR_Producer.fileWriter.write(validation + " = and i1 " + compare1 + ", " + compare2 + "\n\t");
            IR_Producer.fileWriter.write("br i1 " + validation + ", label %" + okLabel + ", label %" + errorLabel + "\n\t");

            position = IR_Producer.createVar();
            assignAddr = IR_Producer.createVar();

            IR_Producer.fileWriter.write("\n" + errorLabel + ":\n\t");
            IR_Producer.fileWriter.write("call void @throw_oob()\n\t");
            IR_Producer.fileWriter.write("br label %" + okLabel + "\n\t");
            IR_Producer.fileWriter.write("\n" + okLabel + ":\n\t");
            IR_Producer.fileWriter.write(position + " = add i32 1, " + var + "\n\t");
            IR_Producer.fileWriter.write(assignAddr + " = getelementptr i32, i32* " + arrayAddr + ", i32 " + position + "\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return assignAddr;
    }

    public static void writeVTables(SymbolTable symbolTable) throws IOException {
        HashMap<Pair<String, String>, HashMap<String, String>> functionTable = symbolTable.getFunctionsTable();

        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionTable.entrySet()) {
            if ((entry.getValue().size() == 0 && entry.getKey().getValue().equals("")) || entry.getValue().size() == 1 && entry.getValue().get("main") != null)
                fileWriter.write("@." + entry.getKey().getKey() + "_vtable = global [0 x i8*] []\n\n");
            else if (entry.getValue().size() != 0 && entry.getValue().get("1") == null) {
                Pair<String, String> scope = entry.getKey();
                List<Pair<String, Pair<String, Integer>>> offsetList = symbolTable.functionsOffset;

                boolean foundScopeInList = false;
                while (true) {

                    for (int i = offsetList.size() - 1; i >= 0; i--) {
                        if (foundScopeInList && !offsetList.get(i).getKey().equals(scope.getKey())) {
                            foundScopeInList = false;
                            break;
                        }
                        if (offsetList.get(i).getKey().equals(scope.getKey()) && foundScopeInList != true)
                            foundScopeInList = true;

                        if (foundScopeInList) {
                            ClassFuncDetails classFuncDetails = new ClassFuncDetails();
                            Pair<String, String> tempScope = symbolTable.getScope();
                            symbolTable.setScope(scope);
                            classFuncDetails.className = entry.getKey().getKey();
                            classFuncDetails.funcName = offsetList.get(i).getValue().getKey();
                            classFuncDetails.functionOriginClass = offsetList.get(i).getKey();
                            classFuncDetails.returnType = symbolTable.lookupFunction(offsetList.get(i).getValue().getKey());
                            classFuncDetails.parameterList = functionTable.get(new Pair<String, String>(offsetList.get(i).getValue().getKey(), offsetList.get(i).getKey()));
                            classFuncDetailsList.add(classFuncDetails);
                            symbolTable.setScope(tempScope);
                        }
                    }

                    if (scope.getValue().equals(""))
                        break;

                    for (Map.Entry<Pair<String, String>, HashMap<String, String>> func : functionTable.entrySet()) {
                        if (func.getKey().getKey().equals(scope.getValue())) {
                            scope = func.getKey();
                            break;
                        }
                    }
                }

                scope = entry.getKey();


                if (IR_Producer.getCountFuncOfClass(scope.getKey()) == 0)
                    fileWriter.write("@." + entry.getKey().getKey() + "_vtable = global [0 x i8*] []\n\t");
                else {
                    fileWriter.write("@." + entry.getKey().getKey() + "_vtable = global [" + IR_Producer.getCountFuncOfClass(scope.getKey()) + " x i8*] [\n\t");

                    for (int i = classFuncDetailsList.size() - 1; i >= 0; i--) {
                        if (classFuncDetailsList.get(i).className.equals(scope.getKey())) {
                            IR_Producer.fileWriter.write("i8* bitcast (");
                            if (classFuncDetailsList.get(i).returnType.equals("int"))
                                IR_Producer.fileWriter.write("i32 (i8*");
                            else if (classFuncDetailsList.get(i).returnType.equals("boolean"))
                                IR_Producer.fileWriter.write("i1 (i8*");
                            else if (classFuncDetailsList.get(i).returnType.equals("boolean[]"))
                                IR_Producer.fileWriter.write("i8* (i8*");
                            else if (classFuncDetailsList.get(i).returnType.equals("int[]"))
                                IR_Producer.fileWriter.write("i32* (i8*");
                            else
                                IR_Producer.fileWriter.write("i8* (i8*");

                            for (int j = 1; j <= classFuncDetailsList.get(i).parameterList.size(); j++) {
                                if (classFuncDetailsList.get(i).parameterList.get(String.valueOf(j)).equals("int"))
                                    IR_Producer.fileWriter.write(", i32");
                                else if (classFuncDetailsList.get(i).parameterList.get(String.valueOf(j)).equals("boolean"))
                                    IR_Producer.fileWriter.write(", i1");
                                else if (classFuncDetailsList.get(i).parameterList.get(String.valueOf(j)).equals("boolean[]"))
                                    IR_Producer.fileWriter.write(", i8*");
                                else if (classFuncDetailsList.get(i).parameterList.get(String.valueOf(j)).equals("int[]"))
                                    IR_Producer.fileWriter.write(", i32*");
                                else
                                    IR_Producer.fileWriter.write(", i8*");
                            }
                            IR_Producer.fileWriter.write(")* @" + classFuncDetailsList.get(i).functionOriginClass + "." + classFuncDetailsList.get(i).funcName + " to i8*)");
                            if (i != 0 && classFuncDetailsList.get(i).className.equals(classFuncDetailsList.get(i - 1).className))
                                IR_Producer.fileWriter.write(",\n\t");

                        }
                    }
                }
                IR_Producer.fileWriter.write("\n]\n\n");

            }
        }
    }

    public static void writeBoilerplateCode() throws IOException {
        fileWriter.write("declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n" +
                "\n" +
                "define void @print_int(i32 %i) {\n" +
                "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_oob() {\n" +
                "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_nsz() {\n" +
                "    %_str = bitcast [15 x i8]* @_cNSZ to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}\n\n");
    }

    public static void allocateParameterList() {

        for (Pair<String, String> parameter : parameterList) {
            try {
                if (parameter.getValue().equals("int"))
                    fileWriter.write("%" + parameter.getKey() + " = alloca i32\n\tstore i32 %." + parameter.getKey() + ", i32* %" + parameter.getKey() + "\n\t");
                else if (parameter.getValue().equals("boolean"))
                    fileWriter.write("%" + parameter.getKey() + " = alloca i1\n\tstore i1 %." + parameter.getKey() + ", i1* %" + parameter.getKey() + "\n\t");
                else if (parameter.getValue().equals("boolean[]"))
                    fileWriter.write("%" + parameter.getKey() + " = alloca i8*\n\tstore i8* %." + parameter.getKey() + ", i8** %" + parameter.getKey() + "\n\t");
                else if (parameter.getValue().equals("int[]"))
                    fileWriter.write("%" + parameter.getKey() + " = alloca i32*\n\tstore i32* %." + parameter.getKey() + ", i32** %" + parameter.getKey() + "\n\t");
                else
                    fileWriter.write("%" + parameter.getKey() + " = alloca i8*\n\tstore i8* %." + parameter.getKey() + ", i8** %" + parameter.getKey() + "\n\t");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFunctionSignature(String funcName, String parent, SymbolTable symbolTable) {
        String returnType = "";
        HashMap<Pair<String, String>, HashMap<String, String>> functionTable = symbolTable.getFunctionsTable();
        Pair<String, String> currentScope = symbolTable.getScope();
        Pair<String, String> tempScope = new Pair<String, String>(funcName, parent);


        symbolTable.setScope(tempScope);
        returnType = symbolTable.lookupFunction(funcName);
        symbolTable.setScope(currentScope);

        if (symbolTable.getFunctionsTable().get(tempScope) == null) {
            HashMap<Pair<String, String>, HashMap<String, String>> funcTable = symbolTable.getFunctionsTable();

            while (true) {
                for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : funcTable.entrySet()) {
                    if (entry.getKey().getKey().equals(tempScope.getValue())) {
                        tempScope = entry.getKey();
                        break;
                    }
                }

                if (funcTable.get(tempScope).get(funcName) != null) {
                    tempScope = new Pair<String, String>(funcName, tempScope.getKey());
                    break;
                }
            }
        }

        try {


            if (returnType.equals("int"))
                IR_Producer.fileWriter.write("i32 (i8*");
            else if (returnType.equals("boolean"))
                IR_Producer.fileWriter.write("i1 (i8*");
            else if (returnType.equals("boolean[]"))
                IR_Producer.fileWriter.write("i8* (i8*");
            else if (returnType.equals("int[]"))
                IR_Producer.fileWriter.write("i32* (i8*");
            else
                IR_Producer.fileWriter.write("i8* (i8*");

            HashMap<String, String> parameterMap = functionTable.get(tempScope);


            for (int i = 1; i <= parameterMap.size(); i++) {
                if (parameterMap.get(String.valueOf(i)).equals("int"))
                    IR_Producer.fileWriter.write(", i32");
                else if (parameterMap.get(String.valueOf(i)).equals("boolean"))
                    IR_Producer.fileWriter.write(", i1");
                else if (parameterMap.get(String.valueOf(i)).equals("boolean[]"))
                    IR_Producer.fileWriter.write(", i8*");
                else if (parameterMap.get(String.valueOf(i)).equals("int[]"))
                    IR_Producer.fileWriter.write(", i32*");
                else
                    IR_Producer.fileWriter.write(", i8*");
            }


            fileWriter.write(")*");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static String writeFunctionCall(String funcName, String parent, String classPtr, String funcPtr, SymbolTable symbolTable) {
        String returnVar, funcType, paramType, varType;
        HashMap<Pair<String, String>, HashMap<String, String>> functionTable = symbolTable.getFunctionsTable();
        Pair<String, String> currentScope = symbolTable.getScope();
        Pair<String, String> tempScope = new Pair<String, String>(funcName, parent);


        returnVar = "%_" + IR_Producer.varCounter;
        IR_Producer.varCounterUp();


        symbolTable.setScope(tempScope);
        funcType = symbolTable.lookupFunction(funcName);
        symbolTable.setScope(currentScope);

        if (symbolTable.getFunctionsTable().get(tempScope) == null) {
            HashMap<Pair<String, String>, HashMap<String, String>> funcTable = symbolTable.getFunctionsTable();

            while (true) {
                for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : funcTable.entrySet()) {
                    if (entry.getKey().getKey().equals(tempScope.getValue())) {
                        tempScope = entry.getKey();
                        break;
                    }
                }

                if (funcTable.get(tempScope).get(funcName) != null) {
                    tempScope = new Pair<String, String>(funcName, tempScope.getKey());
                    break;
                }
            }
        }

        HashMap<String, String> paramMap = functionTable.get(tempScope);

        try {

            for (int i = 0; i < paramMap.size(); i++) {

                if (expressionList.get(expressionList.size() - paramMap.size() + i).matches("[0-9]+"))
                    continue;

                if (expressionList.get(expressionList.size() - paramMap.size() + i).equals("this")) {
                    expressionList.set(expressionList.size() - paramMap.size() + i, "%" + expressionList.get(expressionList.size() - paramMap.size() + i));
                    continue;
                }

                if (!symbolTable.isVarInVtable(expressionList.get(expressionList.size() - paramMap.size() + i))) {
                    if (!expressionList.get(expressionList.size() - paramMap.size() + i).startsWith("%")) {
                        String var = "%_" + IR_Producer.varCounter;
                        IR_Producer.varCounterUp();
                        varType = symbolTable.lookupVariable(expressionList.get(expressionList.size() - paramMap.size() + i));

                        if (varType.equals("int"))
                            IR_Producer.fileWriter.write(var + " = load i32, i32* %" + expressionList.get(expressionList.size() - paramMap.size() + i) + "\n\t");
                        else if (varType.equals("boolean"))
                            IR_Producer.fileWriter.write(var + " = load i1, i1* %" + expressionList.get(expressionList.size() - paramMap.size() + i) + "\n\t");
                        else if (varType.equals("boolean[]"))
                            IR_Producer.fileWriter.write(var + " = load i8*, i8** %" + expressionList.get(expressionList.size() - paramMap.size() + i) + "\n\t");
                        else if (varType.equals("int[]"))
                            IR_Producer.fileWriter.write(var + " = load i32*, i32** %" + expressionList.get(expressionList.size() - paramMap.size() + i) + "\n\t");
                        else
                            IR_Producer.fileWriter.write(var + " = load i8*, i8** %" + expressionList.get(expressionList.size() - paramMap.size() + i) + "\n\t");
                        expressionList.set(expressionList.size() - paramMap.size() + i, var);
                    }
                } else {
                    String elementPtr, var, bitcast;

                    elementPtr = "%_" + IR_Producer.varCounter;
                    IR_Producer.varCounterUp();
                    IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (symbolTable.getVarOffset(expressionList.get(expressionList.size() - paramMap.size() + i)) + 8) + "\n\t");

                    bitcast = "%_" + IR_Producer.varCounter;
                    IR_Producer.varCounterUp();

                    var = "%_" + IR_Producer.varCounter;
                    IR_Producer.varCounterUp();
                    varType = symbolTable.lookupVariable(expressionList.get(expressionList.size() - paramMap.size() + i));
                    if (varType.equals("int")) {
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32*\n\t");
                        IR_Producer.fileWriter.write(var + " = load i32, i32* " + bitcast + "\n\t");
                    } else if (varType.equals("boolean")) {
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");
                        IR_Producer.fileWriter.write(var + " = load i1, i1* " + bitcast + "\n\t");
                    } else if (varType.equals("boolean[]")) {
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");
                        IR_Producer.fileWriter.write(var + " = load i8*, i8** " + bitcast + "\n\t");
                    } else if (varType.equals("int[]")) {
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32**\n\t");
                        IR_Producer.fileWriter.write(var + " = load i32*, i32** " + bitcast + "\n\t");
                    } else {
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");
                        IR_Producer.fileWriter.write(var + " = load i8*, i8** " + bitcast + "\n\t");
                    }
                    expressionList.set(expressionList.size() - paramMap.size() + i, var);
                }
            }

            if (funcType.equals("int")) {
                IR_Producer.fileWriter.write(returnVar + " = call i32 " + funcPtr + "(i8* " + classPtr);
            } else if (funcType.equals("boolean")) {
                IR_Producer.fileWriter.write(returnVar + " = call i1 " + funcPtr + "(i8* " + classPtr);
            } else if (funcType.equals("int[]")) {
                IR_Producer.lastTypeReturned = "int[]";
                IR_Producer.fileWriter.write(returnVar + " = call i32* " + funcPtr + "(i8* " + classPtr);
            } else if (funcType.equals("boolean[]")) {
                IR_Producer.lastTypeReturned = "boolean[]";
                IR_Producer.fileWriter.write(returnVar + " = call i8* " + funcPtr + "(i8* " + classPtr);
            } else {
                IR_Producer.fileWriter.write(returnVar + " = call i8* " + funcPtr + "(i8* " + classPtr);
            }


            for (int i = 1; i <= paramMap.size(); i++) {
                paramType = paramMap.get(String.valueOf(i));


                if (paramType.equals("int")) {
                    IR_Producer.fileWriter.write(", i32 " + expressionList.get(expressionList.size() - paramMap.size() - 1 + i));
                } else if (paramType.equals("boolean")) {
                    IR_Producer.fileWriter.write(", i1 " + expressionList.get(expressionList.size() - paramMap.size() - 1 + i));
                } else if (paramType.equals("boolean[]")) {
                    IR_Producer.fileWriter.write(", i8* " + expressionList.get(expressionList.size() - paramMap.size() - 1 + i));
                } else if (paramType.equals("int[]")) {
                    IR_Producer.fileWriter.write(", i32* " + expressionList.get(expressionList.size() - paramMap.size() - 1 + i));
                } else {
                    IR_Producer.fileWriter.write(", i8* " + expressionList.get(expressionList.size() - paramMap.size() - 1 + i));
                }
            }

            for (int i = 1; i <= paramMap.size(); i++) {
                expressionList.remove(expressionList.size() - 1);
            }

            IR_Producer.fileWriter.write(")\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return returnVar;
    }

    public static void closeFile() throws IOException {
        fileWriter.close();
    }
}
