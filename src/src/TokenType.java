import java.util.HashMap;

public enum TokenType {
	// Single alpha character tokens.
	ASSIGN('A'), BOOLEAN('B'), CHARACTER('C'), DOUBLE('D'), ESCAPE('E'), FUNCTION('F'), FLOAT('G'), H_UNUSED('H'), IF('I'), J_UNUSED('J'),
	K_UNUSED('K'), LONG('L'), MONOLOGUE('M'), INTEGER('N'), OBJECT('O'), PRINT('P'), QUIT('Q'), RETURN('R'), STRING('S'), TRUE('T'), UNTRUE('U'), 
	VALUE('V'), WHILE('W'), EXECUTE('X'), YET('Y'), ZENITH('Z'), 
	
	// Symbol tokens
	NOT('!'), AND('&'), OR('|'), MODULUS('%'), ADD('+'), SUB('-'), MULTIPLY('*'), DIVIDE('/'), EQUALITY('='), LESS_THAN('<'), GREATER_THAN('>'),
	
	// Special
	IDENTIFIER, EOF;
	
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
		for(char c = '0'; c <= '9'; c++) {
			charMap.put(c, IDENTIFIER);
		}
	}
	
	
	public static TokenType getTokenType(char c) {
		return charMap.getOrDefault(c, null);
	}
}