

public class Token {

	public final TokenType type;
	public final Object value;
	public final int line;
	
	public Token(TokenType type, int line) {
		this.type = type;
		this.line = line;
		this.value = null;
	}
	public Token(TokenType type, Object value, int line) {
		// create token with the corresponding type and and value
		this.type = type;
		this.value = value;
		this.line = line;
	}
	
	
	public String toString() {
		String token_str = "";

		token_str += type.toString();
		if(value != null) {
			token_str += ':' + value.toString();
		}

		return token_str;
	}
}
