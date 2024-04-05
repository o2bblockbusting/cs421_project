import java.util.ArrayList;
import java.util.List;


public class Parser {
	private RuntimeException error(Token token, String message) {
		throw new RuntimeException("Error on token "+token.type+" at line "+token.line+": "+message);
	}

	// tokens
	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	public Expr parse() {
		Expr exp = block();
		if(current < tokens.size()) {
			error(peek(),"Invalid statement");
		}
		return exp;
	}
	
	private Expr.StatementBlock block() {
		ArrayList<Expr> statements = new ArrayList<Expr>();
		Expr.StatementBlock block = new Expr.StatementBlock(statements);
		
		while(current < tokens.size()) {
			if(match(TokenType.ASSIGN)) {
				statements.add(assignment());
			} else if(match(TokenType.WHILE)) {
				statements.add(whileStmt());
			} else if(match(TokenType.IF)) {
				statements.add(ifStmt());
			} else if(match(TokenType.IDENTIFIER, TokenType.PRINT)) {
				statements.add(funcCall());
			} else if(match(TokenType.RETURN)) {
				statements.add(new Expr.ReturnStmt(comparison1()));
			} else {
				break; 
			}
		}
		return block;
	}
	
	//Detect variable assignment or function assignment in the following forms
	// ASSIGN IDENTIFIER <type> VALUE <term>
	// ASSIGN IDENTIFIER FUNCTION <return_type>? VALUE LEFT_PAREN <arguments>? RIGHT_PAREN YET <block> ZENITH
	private Expr assignment() {
		Token idTok = consume(TokenType.IDENTIFIER);
		
		if(match(TokenType.FUNCTION)) {
			return functionDef(idTok);
		}
		
		if(!match(TokenType.dataTypeTokens)) {
			error(peek(),"Expected variable data type");
		}
		Token dataType = previous();
		
		consume(TokenType.VALUE);
		Expr value = comparison1();
		
		return new Expr.Assign(idTok, dataType, value);
	}
	
	// Called by the assignment() function and starts pointing to the <return_type>
	// ASSIGN IDENTIFIER FUNCTION <return_type>? VALUE LEFT_PAREN <arguments>? RIGHT_PAREN YET <block> ZENITH
	private Expr.FunctionDef functionDef(Token idTok) {
		Token returnType = peek();
		
		if(!match(TokenType.dataTypeTokens)) {
			// No return type means null
			returnType = null;
		}
		consume(TokenType.VALUE);
		consume(TokenType.LEFT_PAREN);
		
		// Input zero or more parameters with the format <type> IDENTIFIER (, <type> IDENTIFIER) ...
		ArrayList<Expr.Parameter> params = new ArrayList<Expr.Parameter>();
		while(!match(TokenType.RIGHT_PAREN)) {
			if(!params.isEmpty()) {
				consume(TokenType.SEPARATOR);
			}
			
			if(!match(TokenType.dataTypeTokens)) {
				error(peek(),"Expected data type");
			}
			Token dataType = previous();
			consume(TokenType.IDENTIFIER);
			params.add(new Expr.Parameter(dataType, previous()));
		}
		
		consume(TokenType.YET);
		Expr.StatementBlock funcBlock = block();
		consume(TokenType.ZENITH);
		
		return new Expr.FunctionDef(idTok, returnType, params, funcBlock);
	}
	//Detect while statement in the following form
	// WHILE <condition> YET <block> ZENITH
	private Expr.WhileStatement whileStmt() {
		Expr condition = comparison1();
		consume(TokenType.YET);
		
		Expr.StatementBlock innerBlock = block();
		
		consume(TokenType.ZENITH);
		
		return new Expr.WhileStatement(condition, innerBlock);
	}
	
	//Detect if statement in the following forms 
	// IF <condition> YET <block> ZENITH
	// IF <condition> YET <block> ZENITH ELSE YET <block> ZENITH
	// IF <condition> YET <block> ZENITH ELSE IF <condition> YET <block> ZENITH ELSE YET <block> ZENITH
	private Expr.IfStatement ifStmt() {
		Expr condition = comparison1();
		consume(TokenType.YET);
		
		Expr.StatementBlock ifBlock = block();
		
		consume(TokenType.ZENITH);
		
		Expr.StatementBlock elseBlock = null;
		if(match(TokenType.ELSE)) {
			if(peek().type == TokenType.IF) {
				//This will lead back to the ifStmt function eventually,
				//  but it has to be a block so it is done in a roundabout way
				elseBlock = block();
			}
			else {
				consume(TokenType.YET);
				elseBlock = block();
				consume(TokenType.ZENITH);
			}
		}
		
		return new Expr.IfStatement(condition, ifBlock, elseBlock);
	}
	private Expr.FunctionCall funcCall() {
		Token funcID = previous();
		ArrayList<Expr> arguments = new ArrayList<Expr>();
		consume(TokenType.LEFT_PAREN);
		
		boolean firstParam = true;
		while(!match(TokenType.RIGHT_PAREN)) {
			if(!firstParam) {
				consume(TokenType.SEPARATOR);
			}
			
			arguments.add(comparison1());
			
			firstParam = false;
		}
		return new Expr.FunctionCall(funcID, arguments);
	}
	
	// Rule: term → term ( "|" term )*
	private Expr comparison1() {
		Expr left = comparison2();

		while (match(TokenType.OR)) {
			Token operator = previous();
			Expr right = comparison2();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}
	
	// Rule: term → term ( "&" term )*
	private Expr comparison2() {
		Expr left = comparison3();

		while (match(TokenType.AND)) {
			Token operator = previous();
			Expr right = comparison3();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}
	
	// Rule: term → term ( ( "<" | ">" | "<=" | ">="  | "=" | "!=" ) term )*
	private Expr comparison3() {
		Expr left = term();

		while (match(TokenType.LESS_THAN, TokenType.GREATER_THAN, TokenType.LT_EQ, TokenType.GT_EQ, TokenType.EQUALITY, TokenType.NOT_EQ)) {
			Token operator = previous();
			Expr right = term();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}
	
	// Rule: term → factor ( ( "+" | "-" ) factor )*
	private Expr term() {
		Expr left = factor();

		while (match(TokenType.SUB, TokenType.ADD)) {
			Token operator = previous();
			Expr right = factor();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}

	// Rule: factor → unary ( ( "/" | "*" | "%") unary )* ;
	private Expr factor() {
		Expr left = unary();

		while (match(TokenType.DIVIDE, TokenType.MULTIPLY, TokenType.MODULUS)) {
			Token operator = previous();
			Expr right = unary();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}

	// Rule: unary → ("-" | "+" | "!") unary | primary ;
	private Expr unary() {
		if (match(TokenType.SUB, TokenType.ADD, TokenType.NOT)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.UnaryOp(operator, right);
		}

		return primary();

	}

	// Rule: primary → NUMBER | IDENTIFIER | "( cast_type )" | "(" expression ")" ;
	private Expr primary() {

		if (match(TokenType.dataTokens)) {
			return new Expr.Literal(previous().value);
		}
		
		if (match(TokenType.IDENTIFIER)) {
			if(peek().type == TokenType.LEFT_PAREN) {
				return funcCall();
			}
			return new Expr.VariableAccess(previous());
		}
		
		
		if (match(TokenType.LEFT_PAREN)) {
			if(match(TokenType.dataTypeTokens)) {
				Token castType = previous();
				consume(TokenType.RIGHT_PAREN, "Expect ')' after casting");
				return new Expr.UnaryOp(castType, primary());
			}
			Expr expr = comparison1();
			consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Expect expression.");

	}
	
	// > match token types
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}

		return false;
	}

	// > check
	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().type == type;
	}

	// > consume
	private Token consume(TokenType type) {
		return consume(type, null);
	}
	private Token consume(TokenType type, String message) {
		if (check(type))
			return advance();

		if(message == null)
			message = "Expected " + type.toString() + " token.";
		
		throw error(peek(), message);
	}

	// > advance
	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	// > utils
	private boolean isAtEnd() {
		return peek().type == TokenType.EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}
	private Token next() {
		return tokens.get(current + 1);
		
	}
	// < utils
}
