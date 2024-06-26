# cs421_project
This project is a lexer, parser, and interpreter for a new language called Sulfur. The lexer can convert sulfur source code into a list of tokens, which are converted into an expression tree by the parser, which can then be executed by the interpreter. Sulfur's main selling point is that most common programming symbols are now expressed using a single capital letter. See 'tokens.txt' in the docs folder to get an idea of what each letter stands for. Variables are made up of lowercase letters, underscores, and numbers as long as they do not start with a number. Sulfur code is meant to sacrifice some readability for compactness, while also abandoning all meaning that whitespace has. Check out the examples in the examples folder to see some Sulfur code, or check the wiki to start learning.

# Example Code
Sulfur language is quite similar to Java as it is run through an interpreter written in Java. However unlike Java, uppercase letter represent keywords while lowercase letters are used for variables. Shown below is an example program with a factorial function. For more detailed information, please check the wiki. To run this code, download all the code in the src folder and run Main.java. If you provide a file name on the command line, it will attempt to run that file but otherwise will try to run one of the examples whose file paths have been hardcoded in Main.java.
```
M# Factorial function is recursive, takes an int as an argument and returns a long #M
AfacFLV(Nx)Y
Ix=0|x=1Y
R1
ZEY
Rx*fac(x-1)
Z
Z

P("Factorial of 7: ")
P(fac(7))
P('\n')
```

# Tools Used
The lexer is written in Java and instead of using a lexical analyzer generator, it relies on the Java regular expressions library to have more direct control over how the language is tokenized. Also, a lexical analyzer generator adds an extra layer of complexity that is not necessary in an already complex project. The parser and interpreter are also written in Java without using external tools to limit the complexity and dependencies of the project.
