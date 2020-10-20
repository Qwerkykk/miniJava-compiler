# About

LLVM IR generator for miniJava(http://cgi.di.uoa.gr/~thp06/project_files/minijava.html)

# Requirements
- Clang
- javaFX(java-openjdk)

# Compile and Run

- Compile Project: Just run "make"
- Generate LLVM IR: java Main [file1.java]...[fileN.java]
- Compile and Run LLVM IR: clang -o out file1.ll && ./out
