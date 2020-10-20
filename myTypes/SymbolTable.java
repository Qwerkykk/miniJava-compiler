package myTypes;
import myVisitors.*;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    // Map < Pair<Child,Parent> , Μap < Variable, Type>>
    private HashMap<Pair<String, String>, HashMap<String, String>> variablesTable;
    // Map<Pair<Child,Parent>, Μap < Function Name, Type>>
    //Map<Pair<Function Name, Class>, Map < Parameter, Type>>
    private HashMap<Pair<String, String>, HashMap<String, String>> functionsTable;

    private Pair<String, String> scope;
    private Pair<String, String> expressionListScope;

    private int parameterCounter;
    private int prevFuncDeclParamCount;
    private boolean parseErrorFlag;

    private boolean varInClass;

    List<Pair<String, Pair<String, Integer>>> variablesOffset;
    List<Pair<String, Pair<String, Integer>>> functionsOffset;


    public Pair<String, String> getScope() {
        return scope;
    }

    public int getPrevFuncDeclParamCount() {
        return prevFuncDeclParamCount;
    }

    public int getFuncOffset(String functionName) {
        for (Pair<String, Pair<String, Integer>> function : functionsOffset) {
            if (function.getValue().getKey().equals(functionName))
                return function.getValue().getValue();
        }
        return -1;
    }

    public int getVarOffset(String varName) {
        for (Pair<String, Pair<String, Integer>> var : variablesOffset) {
            if (var.getValue().getKey().equals(varName))
                return var.getValue().getValue();
        }
        return -1;
    }

    public void setPrevFuncDeclParamCount(int prevFuncDeclParamCount) {
        this.prevFuncDeclParamCount = prevFuncDeclParamCount;
    }

    public HashMap<Pair<String, String>, HashMap<String, String>> getFunctionsTable() {
        return functionsTable;
    }

    public List<Pair<String, Pair<String, Integer>>> getFunctionsOffset() {
        return functionsOffset;
    }


    public Pair<String, String> getExpressionListScope() {
        return expressionListScope;
    }

    public void setExpressionListScope(Pair<String, String> expressionListScope) {
        this.expressionListScope = expressionListScope;
    }

    public int getClassSize(String name) {
        if (variablesOffset.size() == 0)
            return 0;

        Pair<String, String> currentscope = new Pair<>("", "");

        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
            if (name.equals(entry.getKey().getKey())) {
                currentscope = entry.getKey();
                break;
            }
        }


        while (!currentscope.getValue().equals("")) {
            for (int i = variablesOffset.size() - 1; i >= 0; i--) {
                if (variablesOffset.get(i).getKey().equals(name)) {
                    Pair<String, String> tempScope = scope;
                    scope = currentscope;

                    String type = lookupVariable(variablesOffset.get(i).getValue().getKey());

                    scope = tempScope;

                    if (type.equals("boolean")) {
                        return 1 + variablesOffset.get(i).getValue().getValue();
                    } else if (type.equals("int")) {
                        return 4 + variablesOffset.get(i).getValue().getValue();
                    } else {
                        return 8 + variablesOffset.get(i).getValue().getValue();
                    }
                }

            }

            for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
                if (currentscope.getValue().equals(entry.getKey().getKey())) {
                    currentscope = entry.getKey();
                    name = currentscope.getKey();
                    break;
                }
            }

        }

        for (int i = variablesOffset.size() - 1; i >= 0; i--) {
            if (variablesOffset.get(i).getKey().equals(name)) {
                Pair<String, String> tempScope = scope;
                scope = currentscope;

                String type = lookupVariable(variablesOffset.get(i).getValue().getKey());

                scope = tempScope;

                if (type.equals("boolean")) {
                    return 1 + variablesOffset.get(i).getValue().getValue();
                } else if (type.equals("int")) {
                    return 4 + variablesOffset.get(i).getValue().getValue();
                } else {
                    return 8 + variablesOffset.get(i).getValue().getValue();
                }
            }
        }

        return 0;
    }

    public int getNumOfFunctions(String className) {
        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
            if (entry.getKey().getKey().equals(className))
                return entry.getValue().size();
        }

        return -1;
    }

    public void checkDoubleClassDeclaration(String id) {
        if (parseErrorFound())
            return;
        if (variablesTable.get(new Pair<String, String>(id, "")) != null) {
            System.err.println("Double declaration of class " + id);
            parseErrorFlag = true;
            return;
        }
    }

    public void setParameterCounter(int parameterCounter) {
        this.parameterCounter = parameterCounter;
    }

    public void setScope(Pair<String, String> scope) {
        this.scope = scope;
    }

    public int getParameterCounter() {
        return parameterCounter;
    }

    public void enter(String childScope, String parentScope, boolean createTables) {
        if (createTables) {
            if (scope.getKey().equals("")) {
                variablesTable.put(new Pair<String, String>(childScope, ""), new HashMap<String, String>());
                functionsTable.put(new Pair<String, String>(childScope, ""), new HashMap<String, String>());
                this.scope = new Pair<String, String>(childScope, "");
            } else {
                HashMap<String, String> varMap = new HashMap<String, String>();
                HashMap<String, String> funcMap = new HashMap<String, String>();

                variablesTable.put(new Pair<String, String>(childScope, parentScope), varMap);
                functionsTable.put(new Pair<String, String>(childScope, parentScope), funcMap);
                this.scope = new Pair<String, String>(childScope, parentScope);
            }
        } else {
            if (scope.getKey().equals("")) {
                this.scope = new Pair<String, String>(childScope, "");
            } else {
                this.scope = new Pair<String, String>(childScope, parentScope);
            }

        }

    }


    public void insertVar(String id, String type) {
        if (parseErrorFound())
            return;


        if (varInClass()) {
            String prevVarId;
            int prevVarSize;

            if (variablesOffset.size() == 0 || (!variablesOffset.get(variablesOffset.size() - 1).getKey().equals(scope.getKey()) && scope.getValue().equals("")))
                variablesOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, 0)));
            else if (!variablesOffset.get(variablesOffset.size() - 1).getKey().equals(scope.getValue()) && !variablesOffset.get(variablesOffset.size() - 1).getKey().equals(scope.getKey()))
                variablesOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, 0)));
            else {
                prevVarId = variablesOffset.get(variablesOffset.size() - 1).getValue().getKey();

                if (lookupVariable(prevVarId).equals("int"))
                    prevVarSize = 4;
                else if (lookupVariable(prevVarId).equals("boolean"))
                    prevVarSize = 1;
                else
                    prevVarSize = 8;
                variablesOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, prevVarSize + variablesOffset.get(variablesOffset.size() - 1).getValue().getValue())));
            }
        }
        if (variablesTable.get(scope).containsKey(id)) {
            System.err.println("Double declaration of variable " + id);
            parseErrorFlag = true;
            return;
        }
        variablesTable.get(scope).put(id, type);
    }


    public void insertFunc(String id, String type) {
        if (parseErrorFound())
            return;

        if (functionOverride(id, type)) {
            functionsTable.get(scope).put(id, type);
            return;
        }

        functionsTable.get(scope).put(id, type);

        if (id.equals("main"))
            return;

        if ((functionsOffset.size() == 0) || (!functionsOffset.get(functionsOffset.size() - 1).getKey().equals(scope.getKey()) && scope.getValue().equals("")))
            functionsOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, 0)));
        else if (!functionsOffset.get(functionsOffset.size() - 1).getKey().equals(scope.getValue()) && !functionsOffset.get(functionsOffset.size() - 1).getKey().equals(scope.getKey()))
            functionsOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, 0)));
        else
            functionsOffset.add(new Pair<String, Pair<String, Integer>>(scope.getKey(), new Pair<String, Integer>(id, functionsOffset.get(functionsOffset.size() - 1).getValue().getValue() + 8)));


    }

    public void printFunctionOffsetsForClass(String id) {
        System.err.println("---Methods---");
        for (int i = 0; i < functionsOffset.size(); i++) {
            if (functionsOffset.get(i).getKey().equals(id)) {
                System.err.println(id + "." + functionsOffset.get(i).getValue().getKey() + " : " + functionsOffset.get(i).getValue().getValue());
            }
        }
    }

    public void printVarOffsetsForClass(String id) {
        System.err.println("--Variables--");
        for (int i = 0; i < variablesOffset.size(); i++) {
            if (variablesOffset.get(i).getKey().equals(id)) {
                System.err.println(id + "." + variablesOffset.get(i).getValue().getKey() + " : " + variablesOffset.get(i).getValue().getValue());
            }
        }
    }

    public void insertMainClassArgs(String id) {
        variablesTable.get(scope).put(id, "String[]");
    }

    public void checkPrevParameterListLength(String id, String type) {
        if (parseErrorFound())
            return;
        if (prevFuncDeclParamCount != -1)
            if (functionsTable.get(scope).size() != prevFuncDeclParamCount) {
                System.err.println("Function <" + id + "> declared with different parameters from the last declaration");
                parseErrorFlag = true;
            }
    }

    public boolean functionOverride(String id, String type) {
        if (parseErrorFound())
            return false;
        Pair<String, String> tempScope = scope;

        if (functionsTable.get(tempScope).containsKey(id)) {
            System.err.println("Double declaration of function " + id);
            parseErrorFlag = true;
            return false;
        }

        while (!tempScope.getValue().equals("")) {
            if (functionsTable.get(tempScope).containsKey(id)) {
                if (!functionsTable.get(tempScope).get(id).equals(type)) {
                    System.err.println("Overiding function has wrong return type");
                    parseErrorFlag = true;
                    return false;
                }
                prevFuncDeclParamCount = functionsTable.get(new Pair<String, String>(id, tempScope.getKey())).size();
                if (prevFuncDeclParamCount == -1)
                    prevFuncDeclParamCount = 0;
                return true;
            }
            for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
                if (entry.getKey().getKey().equals(tempScope.getValue())) {
                    tempScope = entry.getKey();
                    break;
                }
            }
        }

        if (functionsTable.get(tempScope).containsKey(id)) {
            if (!functionsTable.get(tempScope).get(id).equals(type)) {
                System.err.println("Overiding function has wrong return type");
                parseErrorFlag = true;
                return false;
            }
            prevFuncDeclParamCount = functionsTable.get(new Pair<String, String>(id, tempScope.getKey())).size();
            if (prevFuncDeclParamCount == -1)
                prevFuncDeclParamCount = 0;
            return true;
        }

        return false;
    }

    public void insertParameterList(String type) {
        functionsTable.get(scope).put(String.valueOf(parameterCounter), type);
        parameterCounter++;
    }

    public boolean isSubclassOf(String subclass, String parent) {
        Pair<String, String> p = new Pair<String, String>(subclass, parent);

        if (functionsTable.get(p) != null)
            return true;

        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
            if (p.getKey().equals(entry.getKey().getKey())) {
                p = entry.getKey();
                break;
            }
        }

        while (!p.getValue().equals("")) {
            if (p.getValue().equals(parent)) {
                return true;
            }
            for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
                if (p.getValue().equals(entry.getKey().getKey())) {
                    p = entry.getKey();
                    break;
                }
            }
        }

        return false;
    }

    public String lookupVariable(String id) {
        return lookup(id, variablesTable);
    }

    public String lookupFunction(String id) {
        return lookup(id, functionsTable);
    }

    public void checkParameterListLength() {
        if (parseErrorFound())
            return;
        if ((parameterCounter - 1) != functionsTable.get(expressionListScope).size()) {
            System.err.println("Wrong number of arguments on function " + expressionListScope.getKey());
            parseErrorFlag = true;
        }
    }

    public String functionParameterLookup() {
        if (parseErrorFound())
            return "";
        Pair<String, String> currentScope = expressionListScope;

        while (!currentScope.getValue().equals("")) {
            if (functionsTable.get(currentScope) != null) {
                if (functionsTable.get(currentScope).containsKey(String.valueOf(parameterCounter))) {

                    parameterCounter++;
                    return functionsTable.get(currentScope).get(String.valueOf(parameterCounter - 1));
                }
            }
            for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
                if (currentScope.getValue().equals(entry.getKey().getKey())) {
                    currentScope = entry.getKey();
                    break;
                }
            }
        }

        if (functionsTable.get(currentScope) != null) {
            if (functionsTable.get(currentScope).containsKey(String.valueOf(parameterCounter))) {

                parameterCounter++;
                return functionsTable.get(currentScope).get(String.valueOf(parameterCounter - 1));
            }
        }

        System.err.println("Wrong number of arguments on function " + scope.getKey());
        parseErrorFlag = true;
        return "";

    }

    public Pair<String, String> findScope(String id) {
        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
            if (entry.getKey().getKey().equals(id)) {
                return entry.getKey();
            }
        }

        System.err.println("Scope doesn't exist");
        parseErrorFlag = true;
        return new Pair<String, String>("", "");
    }

    private String lookup(String id, HashMap<Pair<String, String>, HashMap<String, String>> symbolTable) {
        if (parseErrorFound())
            return "";
        boolean scopeExists = true;
        String idType = null;
        Pair<String, String> currentScope = scope;

        if (symbolTable.get(scope) != null)
            idType = symbolTable.get(scope).get(id);

        if (idType != null)
            return idType;

        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : symbolTable.entrySet()) {
            if (entry.getKey().getKey().equals(scope.getValue())) {
                currentScope = entry.getKey();
                scopeExists = true;
                break;
            }
        }

        while (scopeExists) {
            scopeExists = false;

            if (symbolTable.get(currentScope) != null && symbolTable.get(currentScope).containsKey(id)) {
                return symbolTable.get(currentScope).get(id);
            }

            if (currentScope.getValue().equals("")) {
                if (isClass(id))
                    return "";
                System.err.println(id + " is not declared");
                parseErrorFlag = true;
                return "";
            }


            for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : symbolTable.entrySet()) {
                if (entry.getKey().getKey().equals(currentScope.getValue())) {
                    currentScope = entry.getKey();
                    scopeExists = true;
                    break;
                }
            }
        }
        if (isClass(id))
            return "";

        return "";
    }

    public boolean isVarInVtable(String id) {
        for (Pair<String, Pair<String, Integer>> entry : variablesOffset) {
            if (id.equals(entry.getValue().getKey()))
                return true;
        }
        return false;
    }

    public boolean parentClassExists(String id) {

        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : functionsTable.entrySet()) {
            if (id.equals(entry.getKey().getKey()))
                return true;

        }
        System.err.println("Parent Class <" + id + "> is not declared");
        parseErrorFlag = true;
        return false;
    }

    public void exit() {
        if (parseErrorFound())
            return;
        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : variablesTable.entrySet()) {
            if (scope.getValue().equals(entry.getKey().getKey())) {
                scope = new Pair<String, String>(entry.getKey().getKey(), entry.getKey().getValue());
                return;
            }
        }

        System.err.println("Parse Error on Exit()");
        parseErrorFlag = true;

    }

    public boolean isClass(String id) {
        if (parseErrorFlag)
            return false;
        for (Map.Entry<Pair<String, String>, HashMap<String, String>> entry : variablesTable.entrySet()) {
            if (id.equals(entry.getKey().getKey())) {
                return true;
            }
        }

        return false;
    }

    public void setParseErrorFlag(boolean parseErrorFlag) {
        this.parseErrorFlag = parseErrorFlag;
    }

    public boolean parseErrorFound() {
        return parseErrorFlag;
    }

    public boolean varInClass() {
        return varInClass;
    }

    public void setVarInClass(boolean varInClass) {
        this.varInClass = varInClass;
    }

    public SymbolTable() {
        this.variablesTable = new HashMap<Pair<String, String>, HashMap<String, String>>();
        this.functionsTable = new HashMap<Pair<String, String>, HashMap<String, String>>();
        variablesOffset = new LinkedList<Pair<String, Pair<String, Integer>>>();
        functionsOffset = new LinkedList<Pair<String, Pair<String, Integer>>>();
        this.scope = new Pair<String, String>("", "");
        parameterCounter = 1;
        prevFuncDeclParamCount = -1;
        parseErrorFlag = false;
        varInClass = false;
    }


}
