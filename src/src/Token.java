

public class Token {

	public final TokenType type;
	public final Object value;
	
	public Token(TokenType type, Object value) {
		// create token with the corresponding type and and value
		this.type = type;
		this.value = value;
	}
	public Token(TokenType type) {
		this.type = type;
		this.value = null;
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
