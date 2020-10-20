package myVisitors;
import myTypes.*;
import javafx.util.Pair;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.HashMap;


public class SymbolTableVisitor extends GJDepthFirst<String, SymbolTable> {

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
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String id, type = "";
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        argu.enter(id, "", true);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        id = n.f6.accept(this, argu);
        argu.insertFunc(id, type);
        argu.enter(id, argu.getScope().getKey(), true);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        id = n.f11.accept(this, argu);
        argu.insertMainClassArgs(id);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        argu.exit();

        return _ret;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, SymbolTable argu) {
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String type, id;

        type = n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        argu.insertVar(id, type);
        n.f2.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, SymbolTable argu) {
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "boolean[]";
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, SymbolTable argu) {
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "int[]";
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
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String id;
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        System.err.println("-----------Class " + id + "-----------");
        argu.checkDoubleClassDeclaration(id);
        argu.enter(id, "", true);
        n.f2.accept(this, argu);
        argu.setVarInClass(true);
        n.f3.accept(this, argu);
        argu.printVarOffsetsForClass(id);
        argu.setVarInClass(false);
        n.f4.accept(this, argu);
        argu.printFunctionOffsetsForClass(id);
        n.f5.accept(this, argu);
        return _ret;
    }

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
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String id, type;
        n.f0.accept(this, argu);
        type = n.f1.accept(this, argu);
        id = n.f2.accept(this, argu);
        Pair<String,String> tempscope = argu.getScope();
        argu.insertFunc(id, type);
        argu.enter(id, argu.getScope().getKey(), true);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        argu.checkPrevParameterListLength(id, type);
        argu.setParameterCounter(1);
        argu.setPrevFuncDeclParamCount(-1);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
//        argu.exit();
        argu.setScope(tempscope);
        return _ret;
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
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String id, parentId;
        n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        System.err.println("-----------Class " + id + "-----------");
        argu.checkDoubleClassDeclaration(id);
        n.f2.accept(this, argu);
        parentId = n.f3.accept(this, argu);
        if (!argu.parentClassExists(parentId)) {
            argu.setParseErrorFlag(true);
            return "";
        }
        argu.enter(id, parentId, true);
        n.f4.accept(this, argu);
        argu.setVarInClass(true);
        n.f5.accept(this, argu);
        argu.printVarOffsetsForClass(id);
        argu.setVarInClass(false);
        n.f6.accept(this, argu);
        argu.printFunctionOffsetsForClass(id);
        n.f7.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, SymbolTable argu) {
        if (argu.parseErrorFound())
            return "";
        String _ret = null;
        String id, type;
        type = n.f0.accept(this, argu);
        id = n.f1.accept(this, argu);
        argu.insertVar(id, type);
//        argu.setPrevFuncDeclParamCount(argu.getPrevFuncDeclParamCount()+1);
        argu.insertParameterList(type);
        return _ret;
    }


    public String visit(NodeToken n, SymbolTable argu) {
        if (argu.parseErrorFound())
            return "";
        return n.toString();
    }

}
