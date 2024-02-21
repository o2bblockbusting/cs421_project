import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	private ArrayList<Token> tokenList = null;
	private int idx = 0;
	private int lineNum = 1;
	private final String codeStr;
	
	public static void main(String[] args) {
		StringBuilder code = new StringBuilder();
		File f = new File("C:\\Users\\pc\\vscode-workspace\\CS421\\cs421_project\\example\\primefinder.suf");
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
		tokenList.forEach((token) -> System.out.println(token));
	}
	
	public Lexer(String codeStr) {
		this.codeStr = codeStr;
	}
	
	public ArrayList<Token> lex() {
		if(tokenList == null) {
			tokenList = new ArrayList<>();
		}
		else {
			return tokenList;
		}
		
		Pattern whitespacePattern = Pattern.compile("\\s");
		
		while(idx < codeStr.length()) {
			char c = Character.toUpperCase(codeStr.charAt(idx));
			
			// Ignore and skip over whitespace
			Matcher matcher = whitespacePattern.matcher(codeStr.substring(idx, idx+1));
			boolean isWhitespace = matcher.find();
			if(isWhitespace) {
				if(c == '\n') {
					lineNum++;
				}
				idx++;
				continue;
			}
			
			// Find token that matches the given character or throw an error
			TokenType tType = TokenType.getTokenType(c);
			if(tType == null) {
				System.out.println("Error on line "+lineNum+": Unrecognized character '"+c+"'");
				System.exit(1);
			}
			
			switch(tType) {
			case INTEGER:
			case LONG:
				getInteger(tType);
				break;
			case DOUBLE:
			case FLOAT:
				getFloat(tType);
				break;
			case BOOLEAN:
				getBoolean(tType);
				break;
			case STRING:
				getString();
				break;
			case CHARACTER:
				getCharacter();
				break;
			case IDENTIFIER:
				getIdentifier();
				break;
			default:
				tokenList.add(new Token(tType));
				break;
			}
			idx++;
		}
		
		return tokenList;
	}
	
	// Finds and creates a token from a string of digits that act as an identifier
	// Yes, this language is cursed
	private void getIdentifier() {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip single quotes from character before adding it
			tokenList.add(new Token(TokenType.IDENTIFIER, matcher.group()));
			idx += matcher.end() - 1;
		}
		else {
			throwError("IDENTIFIER");
		}
	}
	
	// Finds a character enclosed in single quotes and adds it as a token to the list
	private void getCharacter() {
		Pattern pattern = Pattern.compile("'\\\\?.'");
		Matcher matcher = pattern.matcher(codeStr.substring(++idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip single quotes from character before adding it
			tokenList.add(new Token(TokenType.CHARACTER, matcher.group().substring(1, matcher.group().length()-1).translateEscapes().charAt(0)));
			idx += matcher.end() - 1;
		}
		else {
			throwError("CHARACTER");
		}
	}

	// Finds a string enclosed in double quotes and adds it as a token to the list
	private void getString() {
		Pattern pattern = Pattern.compile("\"([^\"]|\\\")+\"");
		Matcher matcher = pattern.matcher(codeStr.substring(++idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip double quotes from string before adding it
			tokenList.add(new Token(TokenType.STRING, matcher.group().substring(1, matcher.group().length()-1).translateEscapes()));
			idx += matcher.end() - 1;
		}
		else {
			throwError("STRING");
		}
	}
	
	// Finds a boolean (T or U) following the letter B
	// Adds the correct token to the list
	private void getBoolean(TokenType type) {
		idx++;
		if(idx < codeStr.length()) {
			switch(codeStr.charAt(idx)) {
			case 'T':
			case 't':
				tokenList.add(new Token(type, true));
				break;
			case 'U':
			case 'u':
				tokenList.add(new Token(type, false));
				break;
			default:
				throwError("BOOLEAN");
				break;
			}
		}
		else {
			throwError("EOF");
		}
	}
	
	// Finds an integer following the letter N or L
	// Converts the value from a string, puts it in a token, and adds the token to the list
	private void getInteger(TokenType type) {
		Pattern pattern = Pattern.compile("[+-]?\\d+");
		Matcher matcher = pattern.matcher(codeStr.substring(++idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			try {
				if(type == TokenType.INTEGER) {
					tokenList.add(new Token(type, Integer.parseInt(matcher.group())));
				}
				else {
					tokenList.add(new Token(type, Long.parseLong(matcher.group())));
				}
			} catch(NumberFormatException e) {
				throwError("INTEGER");
			}
			idx += matcher.end() - 1;
		}
		else {
			throwError("INTEGER");
		}
		
		/*int i = ++idx;
		
		// Move past + or - if present
		char c = codeStr.charAt(i);
		if(i < codeStr.length() && (c == '-' || c == '+')) {
			i++;
		}
		
		// Find end of digit sequence
		while(i < codeStr.length() && isDigit(codeStr.charAt(i))) {
			i++;
		}
		
		// Convert from string to number and stop if an error occurs
		String intStr = codeStr.substring(idx, i);
		Token t = null;
		
		try {
			if(type == TokenType.INTEGER) {
				t = new Token(type, Integer.parseInt(intStr));
			}
			else {
				t = new Token(type, Long.parseLong(intStr));
			}
		} catch(NumberFormatException e) {
			System.out.println("Error on line "+lineNum+": Invalid integer value.");
			System.exit(1);
		}
		return t;*/
	}
	
	// Finds a float/double following the letter G or D
	// Converts the value from a string, puts it in a token, and adds the token to the list
	private void getFloat(TokenType type) {
		Pattern pattern = Pattern.compile("[+-]?(\\d+\\.\\d+([Ee]\\d+)?)");
		Matcher matcher = pattern.matcher(codeStr.substring(++idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			try {
				if(type == TokenType.FLOAT) {
					tokenList.add(new Token(type, Float.parseFloat(matcher.group())));
				}
				else {
					tokenList.add(new Token(type, Double.parseDouble(matcher.group())));
				}
			} catch(NumberFormatException e) {
				throwError("FLOAT");
			}
			idx += matcher.end() - 1;
		}
		else {
			throwError("FLOAT");
		}
		/*
		int i = ++idx;
		boolean dotFound = false;
		
		// Move past + or - if present
		char c = codeStr.charAt(i);
		if(i < codeStr.length() && (c == '-' || c == '+')) {
			i++;
		}
		
		// Find end of digit sequence
		while(i < codeStr.length()) {
			char c = codeStr.charAt(i);
			if(c == '.') {
				if(dotFound) {
					System.out.println("Error on line "+lineNum+": Invalid floating point value. Float cannot have two decimals.");
					System.exit(1);
				}
				dotFound = true;
			}
			else if(!isDigit(c)) {
				break;
			}
			i++;
		}
		
		// Convert from string to number and stop if an error occurs
		String numStr = codeStr.substring(idx, i);
		Token t = null;
		
		try {
			if(type == TokenType.FLOAT) {
				t = new Token(type, Float.parseFloat(numStr));
			}
			else {
				t = new Token(type, Double.parseDouble(numStr));
			}
		} catch(NumberFormatException e) {
			System.out.println("Error on line "+lineNum+": Invalid floating point value.");
			System.exit(1);
		}
		return t;*/
	}
	
	private void throwError(String errName) {
		String errString = "Error on line "+lineNum+": ";
		
		switch(errName) {
		case "EOF":
			errString += "Unexpected end of file character.";
			break;
		case "FLOAT":
			errString += "Invalid floating point value.";
			break;
		case "INTEGER":
			errString += "Invalid integer value.";
			break;
		case "BOOLEAN":
			errString += "Invalid boolean value. Valid options are 'T' or 'U'.";
			break;
		case "STRING":
			errString += "Invalid string sequence.";
			break;
		case "CHARACTER":
			errString += "Invalid character value.";
			break;
		case "IDENTIFIER":
			errString += "Invalid identifier.";
			break;
		default:
			errString += "Unknown error.";
			break;
		}
		System.out.println(errString);
		System.exit(1);
	}
}
