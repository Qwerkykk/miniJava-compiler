import myVisitors.*;
import myTypes.*;
import syntaxtree.*;
import java.io.*;


class Main {
    public static void main(String[] args)  {

        for(int i = 0;  i < args.length; i++) {
            System.err.println("----------------------------------------------------------------------------\n" + args[i]);
            FileInputStream fis = null;
            try {
                IR_Producer.openFile(args[i].substring(0,args[i].length()-5)+".ll");


                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);
                SymbolTable symbolTable = new SymbolTable();
                SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
                Goal root = parser.Goal();
                root.accept(symbolTableVisitor, symbolTable);
                IR_Producer.initClassFuncDetailsList();
                IR_Producer.writeVTables(symbolTable);
                IR_Producer.writeBoilerplateCode();

                IR_Visitor ir_visitor = new IR_Visitor();
                root.accept(ir_visitor,symbolTable);

                if(!symbolTable.parseErrorFound())
                    System.err.println("Program parsed successfully.");
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (IR_Producer.fileWriter != null) IR_Producer.closeFile();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }

        }

    }
}
