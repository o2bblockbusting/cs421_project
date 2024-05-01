package Sulfur;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;


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
		if(current < tokens.size() && peek().type != TokenType.EOF) {
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
				if(peek().type == TokenType.PROPERTY_ACCESSOR)
					statements.add(arrayFunctionCall());
				else
					statements.add(funcCall());
			} else if(match(TokenType.RETURN)) {
				statements.add(new Expr.ReturnStmt(comparison1()));
			} else if(match(TokenType.JUMP_OUT, TokenType.KONTINUE, TokenType.QUIT)) {
				statements.add(new Expr.FlowControlStmt(previous()));
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
		int arrayDegree = 0;
		
		if(match(TokenType.FUNCTION)) {
			return functionDef(idTok);
		}
		
		if(!match(TokenType.dataTypeTokens)) {
			error(peek(),"Expected variable data type");
		}
		Token dataType = previous();
		
		while(match(TokenType.LEFT_BRACKET)) {
			consume(TokenType.RIGHT_BRACKET);
			arrayDegree++;
		}
		
		consume(TokenType.VALUE);
		Expr value = comparison1();
		
		return new Expr.Assign(idTok, dataType, value, arrayDegree);
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
			int arrayDegree = 0;
			while(match(TokenType.LEFT_BRACKET)) {
				consume(TokenType.RIGHT_BRACKET);
				arrayDegree++;
			}
			
			consume(TokenType.IDENTIFIER);
			params.add(new Expr.Parameter(dataType, previous(), arrayDegree));
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
		int lineNum = consume(TokenType.YET).line;
		
		Expr.StatementBlock innerBlock = block();
		
		consume(TokenType.ZENITH);
		
		return new Expr.WhileStatement(condition, innerBlock, lineNum);
	}
	
	//Detect if statement in the following forms 
	// IF <condition> YET <block> ZENITH
	// IF <condition> YET <block> ZENITH ELSE YET <block> ZENITH
	// IF <condition> YET <block> ZENITH ELSE IF <condition> YET <block> ZENITH ELSE YET <block> ZENITH
	private Expr.IfStatement ifStmt() {
		// List of all conditions (ifs and else ifs) and their corresponding code blocks
		ArrayList<Expr.ConditionalBlock> conditionalBlocks = new ArrayList<>();
		
		// Add first if to list
		Expr condition = comparison1();
		int lineNum = consume(TokenType.YET).line;
		
		Expr.StatementBlock ifBlock = block();
		conditionalBlocks.add(new Expr.ConditionalBlock(condition, ifBlock, lineNum));
		
		consume(TokenType.ZENITH);
		
		Expr.StatementBlock elseBlock = null;
		
		while(match(TokenType.ELSE)) {
			//Add else-if to conditionals list
			if(match(TokenType.IF)) {
				condition = comparison1();
				lineNum = consume(TokenType.YET).line;
				
				ifBlock = block();
				conditionalBlocks.add(new Expr.ConditionalBlock(condition, ifBlock, lineNum));
				
				consume(TokenType.ZENITH);
			}
			//Match final else block
			else {
				consume(TokenType.YET);
				elseBlock = block();
				consume(TokenType.ZENITH);
			}
		}
		
		return new Expr.IfStatement(conditionalBlocks, elseBlock);
	}
	// <identifier> (<arg1>, <arg2>, <arg3>...)
	private Expr funcCall() {
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
		
		if(funcID.type == TokenType.PRINT)
			return new Expr.PrintStmt(arguments);
		
		return new Expr.FunctionCall(funcID, arguments);
	}
	
	//<arr-id>~<func-name>(<arg1>, <arg2>, <arg3>...)
	//EX: arr~length()
	//    arr~get(0)
	//    arr~set(0)
	private Expr arrayFunctionCall() {		
		ArrayList<Expr> arguments = new ArrayList<Expr>();
		Token arrIDTok = previous();
		arguments.add(new Expr.VariableAccess(arrIDTok));
		
		consume(TokenType.PROPERTY_ACCESSOR);
		Token funcTok = consume(TokenType.IDENTIFIER);
		
		//Switch function name to include ArrayList so it doesn't conflict with similar user created functions
		//User functions also cannot have capital letters so there should be no conflicts
		Token arrayFuncTok = new Token(funcTok.type, "ArrayList_"+((String) funcTok.value), funcTok.line);
		consume(TokenType.LEFT_PAREN);
		
		boolean firstParam = true;
		while(!match(TokenType.RIGHT_PAREN)) {
			if(!firstParam) {
				consume(TokenType.SEPARATOR);
			}
			
			arguments.add(comparison1());
			
			firstParam = false;
		}
		
		return new Expr.FunctionCall(arrayFuncTok, arguments);
	}
	
	// Rule: comparison1 → comparison2 ( "|" comparison2 )*
	private Expr comparison1() {
		Expr left = comparison2();
		
		while (match(TokenType.OR)) {
			Token operator = previous();
			Expr right = comparison2();
			left = new Expr.BinaryOp(left, operator, right);
		}
		return left;
	}
	
	// Rule: comparison2 → comparison3 ( "&" comparison3 )*
	private Expr comparison2() {
		Expr left = comparison3();

		while (match(TokenType.AND)) {
			Token operator = previous();
			Expr right = comparison3();
			left = new Expr.BinaryOp(left, operator, right);
		}

		return left;
	}
	
	// Rule: comparison3 → term ( ( "<" | ">" | "<=" | ">="  | "=" | "!=" ) term )*
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
		
		if(match(TokenType.LEFT_BRACE)) {
			ArrayList<Expr> list = new ArrayList<Expr>();
			boolean first = true;
			
			while(!match(TokenType.RIGHT_BRACE)) {
				if(first) {
					first = false;
				}
				else {
					consume(TokenType.SEPARATOR);
				}
				list.add(comparison1());
			}
			
			return new Expr.ValueArray(list);
		}
		
		if (match(TokenType.IDENTIFIER)) {
			if(peek().type == TokenType.LEFT_PAREN) {
				return funcCall();
			}
			else if(peek().type == TokenType.PROPERTY_ACCESSOR) {
				return arrayFunctionCall();
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
