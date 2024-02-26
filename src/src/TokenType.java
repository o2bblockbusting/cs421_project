import java.util.HashMap;

public enum TokenType {
	// Single alpha character tokens.
	ASSIGN('A'), BOOLEAN_T('B'), CHARACTER_T('C'), DOUBLE_T('D'), ELSE('E'), FUNCTION('F'), FLOAT_T('G'), H_UNUSED('H'), IF('I'), JUMP_OUT('J'),
	KONTINUE('K'), LONG_T('L'), MONOLOGUE('M'), INTEGER_T('N'), OBJECT_T('O'), PRINT('P'), QUIT('Q'), RETURN('R'), STRING_T('S'), TRUE('T'), UNTRUE('U'), 
	VALUE('V'), WHILE('W'), EXECUTE('X'), YET('Y'), ZENITH('Z'), 
	
	// Symbol tokens
	NOT('!'), AND('&'), OR('|'), MODULUS('%'), ADD('+'), SUB('-'), MULTIPLY('*'), DIVIDE('/'), EQUALITY('='), LESS_THAN('<'), GREATER_THAN('>'),
	LEFT_PAREN('('), RIGHT_PAREN(')'), LEFT_BRACKET('['), RIGHT_BRACKET(']'), LEFT_BRACE('{'), RIGHT_BRACE('}'),
	SEPARATOR(','), PROPERTY_ACCESSOR('~'), 
	
	// Data Types
	BOOLEAN, CHARACTER('\''), DOUBLE, FLOAT, LONG, INTEGER, STRING('"'),
	
	// Special
	NUMBER, IDENTIFIER, EOF;
	
	private char tokenChar;
	private static final HashMap<Character, TokenType> charMap = new HashMap<>();
	
	private TokenType(char tokenChar) {
		this.tokenChar = tokenChar;
	}
	
	private TokenType() {
		this.tokenChar = (char) 127;
	}
	
	// Hashmap initialization
	static {
		for(TokenType t : TokenType.values()) {
			if(t.tokenChar != (char) 127)
				charMap.put(t.tokenChar, t);
		}
		for(char c = 'a'; c <= 'z'; c++) {
			charMap.put(c, IDENTIFIER);
		}
		charMap.put('_', IDENTIFIER);
		
		for(char c = '0'; c <= '9'; c++) {
			charMap.put(c, NUMBER);
		}
	}
	
	
	public static TokenType getTokenType(char c) {
		return charMap.getOrDefault(c, null);
	}
}