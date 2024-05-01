package Sulfur;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		for(String arg : args) {
			System.out.println(arg);
		}
		
		StringBuilder code = new StringBuilder();
		File f;
		
		// If the user supplies a file name, run it
		if(args.length > 0) {
			f =  new File(args[0]);
		}
		//Otherwise run one of the test files
		else {
			//f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\helloworld.suf");
			//f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\primefinder.suf");
			//f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\test.suf");
			f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\array.suf");
			//f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\function.suf");
			//f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\comment.suf");
		}
		
		//Scan in whole code file
		Scanner scan;
		try {
			scan = new Scanner(f);
		} catch (FileNotFoundException e) {
			System.out.println("Error: File not found.");
			System.exit(1);
			scan = null;
		}
		
		while(scan.hasNextLine()) {
			code.append(scan.nextLine());
			code.append('\n');
		}
		scan.close();
		//System.out.println(code.toString());
		
		// Run lexer
		Lexer lexr = new Lexer(code.toString());
		ArrayList<Token> tokenList = lexr.lex();
		//printTokens(tokenList);
		
		// Run Parser
		//System.out.println("\n/////////// PARSE TREE ///////////");
		Parser parser = new Parser(tokenList);
		Expr res = parser.parse();
		//System.out.println(res);
		
		System.out.println("\n/////////// PROGRAM OUTPUT ///////////");
		
		// Run interpreter
		Interpreter interpreter = new Interpreter(res);
		interpreter.run();
	}
	
	public static void printTokens(ArrayList<Token> tokenList) {
		System.out.print("/////////// TOKENS ///////////");
		int line = 0;
		for(Token t : tokenList) {
			while(line < t.line) {
				line++;
				System.out.print("\n"+line + ": ");
			}
			System.out.print(t + " ");
		}
	}

}
