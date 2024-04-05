import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	private ArrayList<Token> tokenList = null;
	private int idx = 0;
	private int lineNum = 1;
	private final String codeStr;
	
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
			char c = codeStr.charAt(idx);
			
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
				throwError("UNEXPECTED");
			}
			
			switch(tType) {
			case NUMBER:
				getNumber();
				break;
			case TRUE:
				tokenList.add(new Token(TokenType.BOOLEAN, true, lineNum));
				break;
			case UNTRUE:
				tokenList.add(new Token(TokenType.BOOLEAN, false, lineNum));
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
			case MONOLOGUE:
				removeComment();
				break;
			case LESS_THAN:
			case GREATER_THAN:
			case NOT:
				// Handle 2 character "less/greater than or equal to" or "not equal to" tokens
				if(codeStr.length() > idx+1 && codeStr.charAt(idx+1) == '=') {
					idx++;
					tType = (tType == TokenType.LESS_THAN) ? TokenType.LT_EQ : (tType == TokenType.GREATER_THAN ? TokenType.GT_EQ : TokenType.NOT_EQ);
				}
				tokenList.add(new Token(tType, lineNum));
				break;
			default:
				tokenList.add(new Token(tType, lineNum));
				break;
			}
			idx++;
		}
		
		tokenList.add(new Token(TokenType.EOF, lineNum));
		return tokenList;
	}
	
	// Removes a comment that is in the form M# COMMENT #M
	private void removeComment() {
		Pattern pattern = Pattern.compile("[Mm]#.*?#[Mm]", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			idx += matcher.end() - 1;
			lineNum += matcher.group().lines().count()-1;
		}
		else {
			throwError("COMMENT");
		}
	}
	
	// Finds and creates a token from a string of lowercase letters, underscores, and numbers
	// Cannot start with a number
	private void getIdentifier() {
		Pattern pattern = Pattern.compile("[a-z_][a-z0-9_]*");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip single quotes from character before adding it
			tokenList.add(new Token(TokenType.IDENTIFIER, matcher.group(), lineNum));
			idx += matcher.end() - 1;
		}
		else {
			throwError("IDENTIFIER");
		}
	}
	
	// Finds a character enclosed in single quotes and adds it as a token to the list
	private void getCharacter() {
		Pattern pattern = Pattern.compile("'\\\\?.'");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip single quotes from character before adding it
			tokenList.add(new Token(TokenType.CHARACTER, matcher.group().substring(1, matcher.group().length()-1).translateEscapes().charAt(0), lineNum));
			idx += matcher.end() - 1;
		}
		else {
			throwError("CHARACTER");
		}
	}

	// Finds a string enclosed in double quotes and adds it as a token to the list
	private void getString() {
		Pattern pattern = Pattern.compile("\"([^\"]|\\\\\")*\"");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			// Remember to strip double quotes from string before adding it
			tokenList.add(new Token(TokenType.STRING, matcher.group().substring(1, matcher.group().length()-1).translateEscapes(), lineNum));
			idx += matcher.end() - 1;
		}
		else {
			throwError("STRING");
		}
	}
	
	// Finds a number of type int, long, float, or double and adds the corresponding token to the list
	private void getNumber() {
		if(!getFloat() && !getInteger()) {
			throwError("NUMBER");
		}
	}
	
	// Finds and converts an integer value from a string, puts it in a token, and adds the token to the list
	private boolean getInteger() {
		Pattern pattern = Pattern.compile("\\d+[NL]?");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			try {
				//Default to int if not specified as a long using 'L'
				if(!matcher.group().endsWith("L")) {
					tokenList.add(new Token(TokenType.INTEGER, Integer.parseInt(matcher.group()), lineNum));
				}
				else {
					tokenList.add(new Token(TokenType.LONG, Long.parseLong(matcher.group()), lineNum));
				}
			} catch(NumberFormatException e) {
				throwError("INTEGER");
			}
			idx += matcher.end() - 1;
			return true;
		}
		return false;
	}
	
	// Finds a float/double following the letter G or D
	// Converts the value from a string, puts it in a token, and adds the token to the list
	// Returns whether or not it succeeds
	private boolean getFloat() {
		Pattern pattern = Pattern.compile("(\\d+\\.\\d+([Ee]\\d+)?)[DG]?");
		Matcher matcher = pattern.matcher(codeStr.substring(idx));
		boolean matchFound = matcher.find();
		
		if(matchFound) {
			try {
				//Default to double unless specified with an 'F' for float
				if(matcher.group().endsWith("F")) {
					tokenList.add(new Token(TokenType.FLOAT, Float.parseFloat(matcher.group()), lineNum));
				}
				else {
					tokenList.add(new Token(TokenType.DOUBLE, Double.parseDouble(matcher.group()), lineNum));
				}
			} catch(NumberFormatException e) {
				throwError("FLOAT");
			}
			idx += matcher.end() - 1;
			return true;
		}
		return false;
	}
	
	private void throwError(String errName) {
		String errString = "Error on line "+lineNum+": ";
		
		switch(errName) {
		case "EOF":
			errString += "Unexpected end of file character.";
			break;
		case "NUMBER":
			errString += "Invalid number sequence.";
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
		case "COMMENT":
			errString += "Unclosed comment.";
			break;
		case "UNEXPECTED":
			errString += "Unexpected character '"+codeStr.charAt(idx)+"'.";
			break;
		default:
			errString += "Unknown error.";
			break;
		}
		System.out.println(errString);
		System.out.println(codeStr.substring(idx));
		for(Token t : tokenList) {
			System.out.println(t);
		}
		System.exit(1);
	}
}
