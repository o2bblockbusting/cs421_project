import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		StringBuilder code = new StringBuilder();
		File f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\function.suf");
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
		
		Lexer lexr = new Lexer(code.toString());
		ArrayList<Token> tokenList = lexr.lex();
		System.out.print("/////////// TOKENS ///////////");
		int line = 0;
		for(Token t : tokenList) {
			while(line < t.line) {
				line++;
				System.out.print("\n"+line + ": ");
			}
			System.out.print(t + " ");
		}
		System.out.println("\n/////////// PARSE TREE ///////////");
		Parser parser = new Parser(tokenList);
		Expr res = parser.parse();
		System.out.println(res);
	}

}
