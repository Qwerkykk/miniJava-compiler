package myVisitors;
import myTypes.*;
import javafx.util.Pair;
import visitor.GJDepthFirst;
import syntaxtree.*;

import java.io.IOException;


public class IR_Visitor extends GJDepthFirst<String, SymbolTable> {

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, SymbolTable argu) {
        String _ret = null;
        IR_Producer.initClassAllocations();
        IR_Producer.initParameterList();
        IR_Producer.resetVarCounter();
        IR_Producer.resetAndCounter();
        IR_Producer.resetBranchCounter();
        IR_Producer.resetLoopCounter();
        argu.setParameterCounter(1);
        n.f0.accept(this, argu);
        String type = n.f1.accept(this, argu);
        String id = n.f2.accept(this, argu);
        try {
            if (type.equals("int"))
                IR_Producer.fileWriter.write("define i32 @" + argu.getScope().getKey() + "." + id + "(i8* %this ");
            else if (type.equals("boolean"))
                IR_Producer.fileWriter.write("define i1 @" + argu.getScope().getKey() + "." + id + "(i8* %this ");
            else if (type.equals("boolean[]"))
                IR_Producer.fileWriter.write("define i8* @" + argu.getScope().getKey() + "." + id + "(i8* %this ");
            else if (type.equals("int[]"))
                IR_Producer.fileWriter.write("define i32* @" + argu.getScope().getKey() + "." + id + "(i8* %this ");
            else
                IR_Producer.fileWriter.write("define i8* @" + argu.getScope().getKey() + "." + id + "(i8* %this ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        argu.enter(id, argu.getScope().getKey(), false);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        try {
            IR_Producer.fileWriter.write(") {\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        IR_Producer.allocateParameterList();
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        _ret = n.f10.accept(this, argu);
        try {
            String returnVar = "";
            if (type.equals("boolean") && _ret.startsWith("%"))
                IR_Producer.fileWriter.write("ret i1 " + _ret + " \n}\n\n");
            else if (type.equals("boolean") && _ret.matches("[0-9]+"))
                IR_Producer.fileWriter.write("ret i1 " + _ret + " \n}\n\n");
            else if (_ret.matches("[0-9]+"))
                IR_Producer.fileWriter.write("ret i32 " + _ret + " \n}\n\n");
            else if (type.equals("boolean[]") && _ret.startsWith("%"))
                IR_Producer.fileWriter.write("ret i8* " + _ret + " \n}\n\n");
            else if (type.equals("int[]") && _ret.startsWith("%"))
                IR_Producer.fileWriter.write("ret i32* " + _ret + " \n}\n\n");
            else if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(_ret) && type.equals("int")) {
                String elementPtr, bitcast;

                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (argu.getVarOffset(_ret) + 8) + "\n\t");

                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32*\n\t");

                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i32, i32* " + bitcast + "\n\t");
                IR_Producer.fileWriter.write("ret i32 " + returnVar + "\n}\n\n");
            } else if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(_ret) && type.equals("boolean")) {
                String elementPtr, bitcast;

                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (argu.getVarOffset(_ret) + 8) + "\n\t");

                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");

                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i1, i1* " + bitcast + "\n\t");
                IR_Producer.fileWriter.write("ret i1 " + returnVar + "\n}\n\n");
            } else if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(_ret)) {
                String elementPtr, bitcast;

                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (argu.getVarOffset(_ret) + 8) + "\n\t");

                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");

                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i8*, i8** " + bitcast + "\n\t");
                IR_Producer.fileWriter.write("ret i8* " + returnVar + "\n}\n\n");
            } else if (type.equals("int")) {
                returnVar = _ret;
                if (!_ret.startsWith("%")) {
                    returnVar = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(returnVar + " = load i32, i32* %" + _ret + "\n\t");
                }
                IR_Producer.fileWriter.write("ret i32 " + returnVar + "\n}\n\n");
            } else if (type.equals("boolean")) {
                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i1, i1* %" + _ret + "\n\t");
                IR_Producer.fileWriter.write("ret i1 " + returnVar + "\n}\n\n");
            } else if (type.equals("int[]")) {
                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i32*, i32** %" + _ret + "\n\t");
                IR_Producer.fileWriter.write("ret i32* " + returnVar + "\n}\n\n");
            } else {
                returnVar = IR_Producer.createVar();
                IR_Producer.fileWriter.write(returnVar + " = load i8*, i8** %" + _ret + "\n\t");
                IR_Producer.fileWriter.write("ret i8* " + returnVar + "\n}\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        argu.exit();
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "int[]";
    }


    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, SymbolTable argu) {
        String _ret = null, size, errorLabel, okLabel, compare, allocPtr, bitcast;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        size = n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        try {
            String var = size;
            if (size.matches("[0-9]+") || size.startsWith("%"))
                var = size;
            else if (!size.startsWith("%") && argu.isVarInVtable(size)) {
                var = IR_Producer.loadIntFromVtable(size, argu);
            } else {
                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i32, i32* %" + size + "\n\t");
            }

            IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = add i32 1, " + var + "\n\t");
            size = IR_Producer.createVar();


            okLabel = "nsz_ok_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();
            errorLabel = "nsz_error_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();

            compare = IR_Producer.createVar();

            IR_Producer.fileWriter.write(compare + " = icmp sge i32 " + size + " , 1\n\t");
            IR_Producer.fileWriter.write("br i1 " + compare + ", label %" + okLabel + ", label %" + errorLabel + "\n\t");

            IR_Producer.fileWriter.write("\n" + errorLabel + ":\n\t");
            IR_Producer.fileWriter.write("call void @throw_nsz()\n\t");
            IR_Producer.fileWriter.write("br label %" + okLabel + "\n\t");
            IR_Producer.fileWriter.write("\n" + okLabel + ":\n\t");

            allocPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(allocPtr + " = call i8* @calloc(i32 " + size + ", i32 4)\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + allocPtr + " to i32*\n\t");

            IR_Producer.fileWriter.write("store i32 " + var + ", i32* " + bitcast + "\n\t");
            IR_Producer.lastTypeReturned = "int[]";

            return bitcast;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _ret;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, SymbolTable argu) {
        String _ret = null, id, lookupExpr, assignAddr;
        id = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        lookupExpr = n.f2.accept(this, argu);
        n.f3.accept(this, argu);


        try {
            _ret = IR_Producer.createVar();
            if (IR_Producer.lastTypeReturned != null && IR_Producer.lastTypeReturned.equals("int[]") && id.startsWith("%")) {
                assignAddr = IR_Producer.writeIntArrayLookup(id, lookupExpr, argu);
                IR_Producer.fileWriter.write(_ret + " = load i32, i32* " + assignAddr + "\n\t");
            } else if (IR_Producer.lastTypeReturned != null && IR_Producer.lastTypeReturned.equals("boolean[]") && id.startsWith("%")) {
                String var = IR_Producer.createVar();
                assignAddr = IR_Producer.writeBooleanArrayLookup(id, lookupExpr, argu);
                IR_Producer.fileWriter.write(var + " = load i8, i8* " + assignAddr + "\n\t");
                IR_Producer.fileWriter.write(_ret + " = trunc i8 " + var + " to i1\n\t");
            } else if (argu.lookupVariable(id).equals("int[]")) {
                assignAddr = IR_Producer.writeIntArrayLookup(id, lookupExpr, argu);
                IR_Producer.fileWriter.write(_ret + " = load i32, i32* " + assignAddr + "\n\t");
            } else if (argu.lookupVariable(id).equals("boolean[]")) {
                String var = IR_Producer.createVar();
                assignAddr = IR_Producer.writeBooleanArrayLookup(id, lookupExpr, argu);
                IR_Producer.fileWriter.write(var + " = load i8, i8* " + assignAddr + "\n\t");
                IR_Producer.fileWriter.write(_ret + " = trunc i8 " + var + " to i1\n\t");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }


    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(BooleanArrayAllocationExpression n, SymbolTable argu) {
        String _ret = null, size, errorLabel, okLabel, compare, allocPtr, bitcast;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        size = n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        try {
            String var = size;
            if (size.matches("[0-9]+") || size.startsWith("%"))
                var = size;
            else if (argu.isVarInVtable(size)) {
                var = IR_Producer.loadIntFromVtable(size, argu);
            } else {
                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i32, i32* %" + size + "\n\t");
            }

            IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = add i32 4, " + var + "\n\t");
            size = IR_Producer.createVar();


            okLabel = "nsz_ok_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();
            errorLabel = "nsz_error_" + IR_Producer.varCounter;
            IR_Producer.varCounterUp();

            compare = IR_Producer.createVar();

            IR_Producer.fileWriter.write(compare + " = icmp sge i32 " + size + " , 4\n\t");
            IR_Producer.fileWriter.write("br i1 " + compare + ", label %" + okLabel + ", label %" + errorLabel + "\n\t");

            IR_Producer.fileWriter.write("\n" + errorLabel + ":\n\t");
            IR_Producer.fileWriter.write("call void @throw_nsz()\n\t");
            IR_Producer.fileWriter.write("br label %" + okLabel + "\n\t");
            IR_Producer.fileWriter.write("\n" + okLabel + ":\n\t");


            allocPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(allocPtr + " = call i8* @calloc(i32 1, i32 " + size + ")\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + allocPtr + " to i32*\n\t");

            IR_Producer.fileWriter.write("store i32 " + var + ", i32* " + bitcast + "\n\t");

            return allocPtr;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, SymbolTable argu) {
        String _ret = null, id, lookupExpr, assignAddr = "", value, loadedVar;
        id = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        lookupExpr = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        value = n.f5.accept(this, argu);
        n.f6.accept(this, argu);


        try {
            if (argu.lookupVariable(id).equals("int[]")) {
                assignAddr = IR_Producer.writeIntArrayLookup(id, lookupExpr, argu);

                if (value.matches("[0-9]+") || value.startsWith("%"))
                    IR_Producer.fileWriter.write("store i32 " + value + ", i32* " + assignAddr + "\n\t");
                else if (argu.isVarInVtable(value)) {
                    loadedVar = IR_Producer.loadIntFromVtable(value, argu);
                    IR_Producer.fileWriter.write("store i32 " + loadedVar + ", i32* " + assignAddr + "\n\t");
                } else {
                    String var = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(var + " = load i32, i32* %" + value + "\n\t");
                    IR_Producer.fileWriter.write("store i32 " + var + ", i32* " + assignAddr + "\n\t");
                }
            } else if (argu.lookupVariable(id).equals("boolean[]")) {

                assignAddr = IR_Producer.writeBooleanArrayLookup(id, lookupExpr, argu);

                if (IR_Producer.lastTypeReturned != null && IR_Producer.lastTypeReturned.equals("boolean[]") && value.startsWith("%")) {
                    String zextedValue = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(zextedValue + " = zext i1 " + value + " to i8\n\t");
                    IR_Producer.fileWriter.write("store i8 " + zextedValue + ", i8* " + assignAddr + "\n\t");
                } else if (value.startsWith("%"))
                    IR_Producer.fileWriter.write("store i8 " + value + ", i8* " + assignAddr + "\n\t");
                else if (argu.isVarInVtable(value)) {
                    loadedVar = IR_Producer.loadBooleanFromVtable(value, argu);
                    IR_Producer.fileWriter.write("store i8 " + loadedVar + ", i8* " + assignAddr + "\n\t");
                } else {
                    String zextedValue = IR_Producer.createVar();

                    if (!value.matches("[0-9]+")) {
                        IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = load i1, i1* %" + value + "\n\t");
                        value = IR_Producer.createVar();
                    }
                    IR_Producer.fileWriter.write(zextedValue + " = zext i1 " + value + " to i8\n\t");
                    IR_Producer.fileWriter.write("store i8 " + zextedValue + ", i8* " + assignAddr + "\n\t");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _ret;
    }


    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, SymbolTable argu) {
        String _ret = null;
        String id = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expr = n.f2.accept(this, argu);

        String type = argu.lookupVariable(id);
        if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(id)) {
            try {


                String elementPtr, bitcast;

                if (!argu.isVarInVtable(expr) && !expr.startsWith("%") && !expr.matches("[0-9]+")) {

                    if (type.equals("boolean")) {
                        IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = load i1, i1* %" + expr + "\n\t");
                        expr = IR_Producer.createVar();
                    } else if (type.equals("int")) {
                        IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = load i32, i32* %" + expr + "\n\t");
                        expr = IR_Producer.createVar();
                    } else {
                        IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = load i8*, i8** %" + expr + "\n\t");
                        expr = IR_Producer.createVar();
                    }
                }


                elementPtr = IR_Producer.createVar();
                bitcast = IR_Producer.createVar();

                if (type.equals("int")) {
                    IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(id) + 8) + "\n\t");
                    IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i32*\n\t");
                    IR_Producer.fileWriter.write("store i32 " + expr + ", i32* " + bitcast + "\n\t");
                } else if (type.equals("boolean")) {
                    IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(id) + 8) + "\n\t");
                    IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");
                    IR_Producer.fileWriter.write("store i1 " + expr + ", i1* " + bitcast + "\n\t");
                } else if (type.equals("int[]")) {
                    id = IR_Producer.loadIntArrayPointerFromVtable(id, argu);
                    IR_Producer.fileWriter.write("store i32* " + expr + ", i32** " + id + "\n\t");
                } else if (type.equals("boolean[]")) {
                    id = IR_Producer.loadBooleanArrayPointerFromVtable(id, argu);
                    IR_Producer.fileWriter.write("store i8* " + expr + ", i8** " + id + "\n\t");
                } else {
                    IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(id) + 8) + "\n\t");
                    IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");
                    IR_Producer.fileWriter.write("store i8* " + expr + ", i8** " + bitcast + "\n\t");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {


            try {
                if (type.equals("boolean")) {
                    if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(expr)) {
                        String elementPtr, bitcast;

                        elementPtr = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");


                        bitcast = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");

                        expr = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(expr + " = load i1, i1* " + bitcast + "\n\t");

                    }
                    IR_Producer.fileWriter.write("store i1 " + expr + ", i1* %" + id + "\n\t");
                } else if (type.equals("int[]")) {
                    if (expr.startsWith("%"))
                        IR_Producer.fileWriter.write("store i32* " + expr + ", i32** %" + id + "\n\t");
                    else if (argu.isVarInVtable(expr)) {
                        expr = IR_Producer.loadIntArrayPointerFromVtable(expr, argu);
                        String var = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(var + " = load i32* , i32** " + expr + "\n\t");
                        IR_Producer.fileWriter.write("store i32* " + var + ", i32** %" + id + "\n\t");
                    } else {
                        String var = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(var + " = load i32* , i32** %" + expr + "\n\t");
                        IR_Producer.fileWriter.write("store i32* " + var + ", i32** %" + id + "\n\t");
                    }
                } else if (type.equals("boolean[]"))
                    IR_Producer.fileWriter.write("store i8* " + expr + ", i8** %" + id + "\n\t");
                else if (expr.matches("[0-9]+"))
                    IR_Producer.fileWriter.write("store i32 " + expr + ", i32* %" + id + "\n\t");
                else if (type.equals("int")) {
                    if (argu.isVarInVtable(expr))
                        expr = IR_Producer.loadIntFromVtable(expr, argu);
                    else if (!expr.startsWith("%")) {
                        IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = load i32, i32* %" + expr + "\n\t");
                        expr = IR_Producer.createVar();
                    }
                    IR_Producer.fileWriter.write("store i32 " + expr + ", i32* %" + id + "\n\t");
                } else if (expr.equals("this"))
                    IR_Producer.fileWriter.write("store i8* %" + expr + ", i8** %" + id + "\n\t");
                else if (!expr.startsWith("%")) {
                    String var = IR_Producer.createVar();

                    if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(expr)) {
                        String elementPtr, bitcast;

                        elementPtr = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");


                        bitcast = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");

                        var = IR_Producer.createVar();

                        IR_Producer.fileWriter.write(var + " =load i8*, i8**  " + bitcast + "\n\t");
                    } else
                        IR_Producer.fileWriter.write(var + " = load i8*, i8** %" + expr + "\n\t");

                    IR_Producer.fileWriter.write("store i8* " + var + ", i8** %" + id + "\n\t");
                } else
                    IR_Producer.fileWriter.write("store i8* " + expr + ", i8** %" + id + "\n\t");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        IR_Producer.saveAllocationCredentials = true;
        _ret = n.f1.accept(this, argu);
        IR_Producer.saveAllocationCredentials = false;
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, SymbolTable argu) {
        String _ret = null;
        String primExpr, id, vTablePtr = "", bitcastVar, elementPtr, functionPtr, functionSignature = "";
        IR_Producer.saveAllocationCredentials = true;
        String name = n.f0.accept(this, argu);
        primExpr = name;
        if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(primExpr)) {
            String bitcast, var = "";
            try {
                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (argu.getVarOffset(primExpr) + 8) + "\n\t");

                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i8**\n\t");

                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i8*, i8** " + bitcast + "\n\t");
            } catch (IOException e) {
                e.printStackTrace();
            }
            primExpr = var;
        } else if (!primExpr.equals("this") && !primExpr.startsWith("%")) {
            String var = IR_Producer.createVar();

            try {
                IR_Producer.fileWriter.write(var + " = load i8*, i8** %" + primExpr + "\n\t");
                primExpr = var;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (!primExpr.startsWith("%"))
            primExpr = "%" + primExpr;

        try {
            bitcastVar = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcastVar + " = bitcast i8* " + primExpr + " to i8***\n\t");

            vTablePtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(vTablePtr + " = load i8**, i8*** " + bitcastVar + "\n\t");


        } catch (IOException e) {
            e.printStackTrace();
        }


        n.f1.accept(this, argu);
        id = n.f2.accept(this, argu);
        String type;
        if (name.startsWith("%")) {
            type = IR_Producer.getIRVarType(name);
            IR_Producer.classAllocationsList.remove(new Pair<String, String>(name, type));
        } else if (name.equals("this"))
            type = argu.getScope().getValue();
        else
            type = argu.lookupVariable(name);
        try {
            elementPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8*, i8** " + vTablePtr + ", i32 " + argu.getFuncOffset(id) / 8 + "\n\t");

            functionPtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(functionPtr + " = load i8*, i8** " + elementPtr + "\n\t");

            functionSignature = IR_Producer.createVar();
            IR_Producer.fileWriter.write(functionSignature + " = bitcast i8* " + functionPtr + " to ");
            IR_Producer.writeFunctionSignature(id, type, argu);
            IR_Producer.fileWriter.write("\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);


        String returnVal = IR_Producer.writeFunctionCall(id, type, primExpr, functionSignature, argu);

        if (IR_Producer.saveAllocationCredentials)
            IR_Producer.classAllocationsList.add(new Pair<String, String>(returnVal, type));

        return returnVal;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, SymbolTable argu) {
        String _ret = null, id, size = "", arrayPointer, bitcast, loadedPtr;
        id = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        try {
            if (id.startsWith("%")) {
                size = IR_Producer.createVar();
                IR_Producer.fileWriter.write(size + " = load i32, i32* " + id + "\n\t-");
            } else {
                if (argu.isVarInVtable(id)) {
                    if (argu.lookupVariable(id).equals("int[]")) {
                        arrayPointer = IR_Producer.loadIntArrayPointerFromVtable(id, argu);
                        size = IR_Producer.createVar();
                        loadedPtr = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(loadedPtr + " = load i32*, i32**" + arrayPointer + "\n\t");
                        IR_Producer.fileWriter.write(size + " = load i32, i32* " + loadedPtr + "\n\t");
                    } else {
                        size = IR_Producer.createVar();
                        arrayPointer = IR_Producer.loadBooleanArrayPointerFromVtable(id, argu);
                        loadedPtr = IR_Producer.createVar();
                        bitcast = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(loadedPtr + " = load i8*, i8**" + arrayPointer + "\n\t");
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + loadedPtr + " to i32*\n\t");
                        IR_Producer.fileWriter.write(size + " = load i32, i32* " + bitcast + "\n\t");
                    }
                } else {
                    if (argu.lookupVariable(id).equals("int[]")) {
                        size = IR_Producer.createVar();
                        loadedPtr = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(loadedPtr + " = load i32*, i32** %" + id + "\n\t");
                        IR_Producer.fileWriter.write(size + " = load i32, i32* " + loadedPtr + "\n\t");
                    } else {
                        size = IR_Producer.createVar();
                        loadedPtr = IR_Producer.createVar();
                        bitcast = IR_Producer.createVar();
                        IR_Producer.fileWriter.write(loadedPtr + " = load i8*, i8** %" + id + "\n\t");
                        IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + loadedPtr + " to i32*\n\t");
                        IR_Producer.fileWriter.write(size + " = load i32, i32* " + bitcast + "\n\t");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, SymbolTable argu) {
        String _ret = null;
        String expr, var = "";
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr = n.f2.accept(this, argu);
        var = expr;

        try {
            if (!expr.matches("[0-9]+") && !expr.startsWith("%")) {

                if (argu.isVarInVtable(expr)) {
                    var = IR_Producer.loadIntFromVtable(expr, argu);
                } else {
                    var = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(var + " = load i32, i32* %" + expr + "\n\t");
                }
            }

            IR_Producer.fileWriter.write("call void (i32) @print_int(i32 " + var + ")\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, SymbolTable argu) {
        String expr, _ret = IR_Producer.createVar();
        n.f0.accept(this, argu);
        expr = n.f1.accept(this, argu);


        try {
            if (!argu.getScope().getKey().equals("main") && argu.isVarInVtable(expr)) {
                String elementPtr, bitcast;

                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8, i8* %this, i32 " + (argu.getVarOffset(expr) + 8) + "\n\t");


                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");

                expr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(expr + " = load i1, i1* " + bitcast + "\n\t");
                IR_Producer.fileWriter.write(_ret + " = xor i1 1, " + expr + "\n\t");
            } else if (expr.matches("[0-9]+") || expr.startsWith("%"))
                IR_Producer.fileWriter.write(_ret + " = xor i1 1, " + expr + "\n\t");
            else {
                String var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i1, i1* %" + expr + "\n\t");
                IR_Producer.fileWriter.write(_ret + " = xor i1 1, " + var + "\n\t");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, SymbolTable argu) {
        String _ret = null, expr, loop1, loop2, loop3;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);

        try {

            loop1 = "loop" + IR_Producer.loopCounter;
            IR_Producer.loopCounterUp();

            loop2 = "loop" + IR_Producer.loopCounter;
            IR_Producer.loopCounterUp();

            loop3 = "loop" + IR_Producer.loopCounter;
            IR_Producer.loopCounterUp();

            IR_Producer.fileWriter.write("br label %" + loop1 + "\n\n" + loop1 + ":\n\t");

            expr = n.f2.accept(this, argu);
            String var = expr;
            if ( argu.isVarInVtable(expr)){
                var = IR_Producer.loadBooleanFromVtable(expr,argu);
                IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = trunc i8 " + var + " to i1\n\t");
                var = IR_Producer.createVar();
            }
            else if (!expr.startsWith("%_")) {
                var = IR_Producer.createVar();

                IR_Producer.fileWriter.write(var + " = load i1, i1* %" + expr + "\n\t");
            }


            IR_Producer.fileWriter.write("br i1 " + var + ", label %" + loop2 + ", label %" + loop3 + "\n\n");

            IR_Producer.fileWriter.write(loop2 + ":\n\t");

            n.f3.accept(this, argu);
            n.f4.accept(this, argu);


            IR_Producer.fileWriter.write("br label %" + loop1 + "\n\n");
            IR_Producer.fileWriter.write(loop3 + ":\n\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }


        return _ret;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, SymbolTable argu) {
        String _ret = null;
        String expr1, expr2, var1, var2;
        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);
        var1 = expr1;
        var2 = expr2;
        try {
            if (!expr1.startsWith("%") && !expr1.matches("[0-9]+") && !argu.isVarInVtable(expr1)) {
                var1 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var1 + " = load i32, i32* %" + expr1 + "\n\t");
            } else if (argu.isVarInVtable(expr1))
                var1 = IR_Producer.loadIntFromVtable(expr1, argu);

            if (!expr2.startsWith("%") && !expr2.matches("[0-9]+") && !argu.isVarInVtable(expr2)) {
                var2 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var2 + " = load i32, i32* %" + expr2 + "\n\t");
            } else if (argu.isVarInVtable(expr2))
                var2 = IR_Producer.loadIntFromVtable(expr2, argu);


            _ret = IR_Producer.createVar();


            IR_Producer.fileWriter.write(_ret + " = add i32 " + var1 + ", " + var2 + "\n\t");
            return _ret;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, SymbolTable argu) {
        String _ret = null;
        String expr1, expr2, var1, var2;
        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);
        var1 = expr1;
        var2 = expr2;
        try {
            if (expr1.matches("[0-9]+") || expr1.startsWith("%"))
                var1 = expr1;
            else if (argu.isVarInVtable(expr1)) {
                var1 = IR_Producer.loadIntFromVtable(expr1, argu);
            } else {
                var1 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var1 + " = load i32, i32* %" + expr1 + "\n\t");
            }

            if (expr2.matches("[0-9]+") || expr2.startsWith("%"))
                var2 = expr2;
            else if (argu.isVarInVtable(expr2)) {
                var2 = IR_Producer.loadIntFromVtable(expr2, argu);
            } else {
                var2 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var2 + " = load i32, i32* %" + expr2 + "\n\t");
            }

            _ret = IR_Producer.createVar();

            IR_Producer.fileWriter.write(_ret + " = sub i32 " + var1 + ", " + var2 + "\n\t");
            return _ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, SymbolTable argu) {
        String _ret = null;
        String expr = n.f0.accept(this, argu);
        IR_Producer.addExpression(expr);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        String expr = n.f1.accept(this, argu);
        IR_Producer.addExpression(expr);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, SymbolTable argu) {
        String _ret = null;
        String expr1, expr2;
        expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        expr2 = n.f2.accept(this, argu);


        try {
            String var1, var2;
            var1 = expr1;
            var2 = expr2;
            if (!expr1.startsWith("%_") && !expr1.matches("[0-9]+")) {
                var1 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var1 + " = load i32, i32* %" + expr1 + "\n\t");
            }
            if (!expr2.startsWith("%_") && !expr2.matches("[0-9]+")) {
                var2 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var2 + " = load i32, i32* %" + expr2 + "\n\t");
            }

            _ret = IR_Producer.createVar();

            IR_Producer.fileWriter.write(_ret + " = mul i32 " + var1 + ", " + var2 + "\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }


    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return " ";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, SymbolTable argu) {
        String _ret = null;
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, argu);
        IR_Producer.addParameter(new Pair<String, String>(id, type));
        try {
            if (type.equals("int"))
                IR_Producer.fileWriter.write(",i32 %." + id);
            else if (type.equals("boolean"))
                IR_Producer.fileWriter.write(",i1 %." + id);
            else if (type.equals("boolean[]"))
                IR_Producer.fileWriter.write(",i8* %." + id);
            else if (type.equals("int[]"))
                IR_Producer.fileWriter.write(",i32* %." + id);
            else
                IR_Producer.fileWriter.write(",i8* %." + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        argu.setParameterCounter(argu.getParameterCounter() + 1);
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, SymbolTable argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, SymbolTable argu) {
        String _ret = null;
        String id, allocation = "", bitcast, vTablePtr;
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        int classSize = argu.getClassSize(id) + 8;
        try {
            allocation = IR_Producer.createVar();
            IR_Producer.fileWriter.write(allocation + " = call i8* @calloc(i32 1, i32 " + classSize + ")\n\t");

            bitcast = IR_Producer.createVar();
            IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + allocation + " to i8***\n\t");

            vTablePtr = IR_Producer.createVar();
            IR_Producer.fileWriter.write(vTablePtr + " = getelementptr [ " + IR_Producer.getCountFuncOfClass(id) + " x i8*], [ " + IR_Producer.getCountFuncOfClass(id) + " x i8*]* @." + id + "_vtable, i32 0, i32 0\n\t");
            IR_Producer.fileWriter.write("store i8** " + vTablePtr + ", i8*** " + bitcast + "\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (IR_Producer.saveAllocationCredentials)
            IR_Producer.classAllocationsList.add(new Pair<String, String>(allocation, id));
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return allocation;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, SymbolTable argu) {
        String _ret = null;
        String id, parentId;
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        parentId = n.f3.accept(this, argu);
        argu.enter(id, parentId, false);
        n.f4.accept(this, argu);
        //n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, SymbolTable argu) {

        String _ret = null;
        String id;
        IR_Producer.resetAndCounter();
        IR_Producer.resetLoopCounter();
        IR_Producer.resetBranchCounter();
        IR_Producer.resetVarCounter();
        IR_Producer.initExpressionList();
        IR_Producer.initClassAllocations();
        try {
            IR_Producer.fileWriter.write("define i32 @main() {\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        argu.enter(id, "", false);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        id = n.f6.accept(this, argu);
        argu.enter(id, argu.getScope().getKey(), false);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        argu.exit();

        try {
            IR_Producer.fileWriter.write("ret i32 0\n}\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _ret;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, SymbolTable argu) {
        String _ret = null;
        String id;
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        argu.enter(id, "", false);
        n.f2.accept(this, argu);
        //n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, SymbolTable argu) {
        String _ret = null;
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, argu);

        try {
            if (type.equals("int"))
                IR_Producer.fileWriter.write("%" + id + " = alloca i32\n\t");
            else if (type.equals("boolean"))
                IR_Producer.fileWriter.write("%" + id + " = alloca i1\n\t");
            else if (type.equals("int[]"))
                IR_Producer.fileWriter.write("%" + id + " = alloca i32*\n\t");
            else if (type.equals("boolean[]"))
                IR_Producer.fileWriter.write("%" + id + " = alloca i8*\n\t");
            else
                IR_Producer.fileWriter.write("%" + id + " = alloca i8*\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }

        n.f2.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, SymbolTable argu) {
        String _ret = null, clause1, clause2, and1, and2, and3, and4, phi = "";

        and1 = "andclause" + IR_Producer.andCounter;
        IR_Producer.andCounterUp();
        and2 = "andclause" + IR_Producer.andCounter;
        IR_Producer.andCounterUp();
        and3 = "andclause" + IR_Producer.andCounter;
        IR_Producer.andCounterUp();
        and4 = "andclause" + IR_Producer.andCounter;
        IR_Producer.andCounterUp();


        clause1 = n.f0.accept(this, argu);
        String var1 = clause1;
        try {
            if (!clause1.startsWith("%") && !clause1.equals("0") && !clause1.equals("1")) {
                var1 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var1 + " = load i1, i1* %" + clause1 + "\n\t");
            }
            IR_Producer.fileWriter.write("br label %" + and1 + "\n\n");
            IR_Producer.fileWriter.write(and1 + ":\n\t");
            IR_Producer.fileWriter.write("br i1 " + var1 + ", label %" + and2 + ", label %" + and4 + "\n\n" + and2 + ":\n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f1.accept(this, argu);
        clause2 = n.f2.accept(this, argu);
        String var2 = clause2;
        try {
            if (!clause2.startsWith("%") && !clause2.equals("0") && !clause2.equals("1")) {
                var2 = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var2 + " = load i1, i1* %" + clause2 + "\n\t");
            }

            IR_Producer.fileWriter.write("br label %" + and3 + "\n\n");
            IR_Producer.fileWriter.write(and3 + ":\n\t");
            IR_Producer.fileWriter.write("br label %" + and4 + "\n" + and4 + ":\n\t");

            phi = IR_Producer.createVar();


            IR_Producer.fileWriter.write(phi + " = phi i1 [ 0, %" + and1 + " ], [ " + var2 + ", %" + and3 + " ] \n\t");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return phi;
    }


    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, SymbolTable argu) {
        String _ret = null;
        String evalVar, var, branch1, branch2, branch3;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        evalVar = n.f2.accept(this, argu);
        var = evalVar;
        branch1 = "if" + IR_Producer.branchCounter;
        IR_Producer.branchCounterUp();

        branch2 = "if" + IR_Producer.branchCounter;
        IR_Producer.branchCounterUp();

        branch3 = "if" + IR_Producer.branchCounter;
        IR_Producer.branchCounterUp();


        try {
            if (argu.isVarInVtable(evalVar)) {
                String elementPtr, bitcast;

                elementPtr = IR_Producer.createVar();
                IR_Producer.fileWriter.write(elementPtr + " = getelementptr i8 , i8* %this , i32 " + (argu.getVarOffset(evalVar) + 8) + "\n\t");

                bitcast = IR_Producer.createVar();
                IR_Producer.fileWriter.write(bitcast + " = bitcast i8* " + elementPtr + " to i1*\n\t");

                var = IR_Producer.createVar();
                IR_Producer.fileWriter.write(var + " = load i1, i1* " + bitcast + "\n\t");
            } else if (!evalVar.equals("1") && !evalVar.equals("0") && !evalVar.startsWith("%_")) {
                var = IR_Producer.createVar();

                IR_Producer.fileWriter.write(var + " = load i1, i1* %" + evalVar + "\n\t");
            }

            IR_Producer.fileWriter.write("br i1 " + var + ", label %" + branch1 + ", label %" + branch2 + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f3.accept(this, argu);
        try {
            IR_Producer.fileWriter.write(branch1 + ":\n\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f4.accept(this, argu);
        try {
            IR_Producer.fileWriter.write("br label %" + branch3 + "\n\n" + branch2 + ":\n\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        try {
            IR_Producer.fileWriter.write("br label %" + branch3 + "\n\n" + branch3 + ":\n\n\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return _ret;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, SymbolTable argu) {
        String _ret = null;
        String expr1 = n.f0.accept(this, argu);

        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);


        try {
            String var1 = expr1, var2 = expr2;


            if (!expr1.matches("[0-9]+") && !expr1.startsWith("%")) {

                if (argu.isVarInVtable(expr1)) {
                    var1 = IR_Producer.loadIntFromVtable(expr1, argu);
                } else {
                    var1 = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(var1 + " = load i32, i32* %" + expr1 + "\n\t");
                }
            }

            if (!expr2.matches("[0-9]+") && !expr2.startsWith("%")) {

                if (argu.isVarInVtable(expr2)) {
                    var2 = IR_Producer.loadIntFromVtable(expr2, argu);
                } else {
                    var2 = IR_Producer.createVar();
                    IR_Producer.fileWriter.write(var2 + " = load i32, i32* %" + expr2 + "\n\t");
                }
            }

            IR_Producer.fileWriter.write("%_" + IR_Producer.varCounter + " = icmp slt i32 " + var1 + ", " + var2 + "\n\t");
            _ret = IR_Producer.createVar();
            return _ret;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return _ret;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, SymbolTable argu) {

        n.f0.accept(this, argu);
        return "1";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, SymbolTable argu) {
        n.f0.accept(this, argu);
        return "0";
    }


    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | BracketExpression()
     */
    public String visit(PrimaryExpression n, SymbolTable argu) {
        String _ret = n.f0.accept(this, argu);
        return _ret;
    }

    public String visit(NodeToken n, SymbolTable argu) {
        return n.toString();
    }


}