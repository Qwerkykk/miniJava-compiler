all: compile

compile:
	javac ./myTypes/SymbolTable.java ./myTypes/ClassFuncDetails.java  ./myTypes/IR_Producer.java  ./myVisitors/SymbolTableVisitor.java ./myVisitors/IR_Visitor.java  Main.java

clean:
	rm -f myTypes/*.class myVisitors/*.class *~
