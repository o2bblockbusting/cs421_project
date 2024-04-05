import java.util.List;

public abstract class Expr {
	
	public static class StatementBlock extends Expr {
		final List<Expr> statements;
		
		StatementBlock(List<Expr> statements) {
			this.statements = statements;
		}
		
		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for(Expr e : statements) {
				s.append(e);
				s.append('\n');
			}
			return s.toString();
		}
	}
	
	public static class FunctionCall extends Expr {
		final Token funcIdTok;
		final List<Expr> arguments;
		
		FunctionCall(Token funcIdTok, List<Expr> arguments) {
			this.funcIdTok = funcIdTok;
			this.arguments = arguments;
		}
		@Override
		public String toString() {
			String s;
			if(funcIdTok.type == TokenType.PRINT) {
				s = "PRINT (";
			} else {
				s = "CALL "+funcIdTok.value+" WITH ARGS (";
			}
			
			boolean first = true;
			for(Expr e : arguments) {
				if(!first) s += ", ";
				s += e;
				first = false;
			}
			return s + ')';
		}
	}
	
	public static class FunctionDef extends Expr {
		final Token funcIdTok;
		final Token returnType;
		final List<Parameter> parameters;
		final StatementBlock funcBlock;
		
		FunctionDef(Token funcIdTok, Token returnType, List<Parameter> parameters, StatementBlock funcBlock) {
			this.funcIdTok = funcIdTok;
			this.returnType = returnType;
			this.parameters = parameters;
			this.funcBlock = funcBlock;
		}
		
		@Override
		public String toString() {
			String retType = returnType != null ? returnType.type.name() : "NULL";
			String s = "DEFINE " + retType + " FUNC " + funcIdTok.value + " WITH PARAMS (";
			boolean first = true;
			for(Expr e : parameters) {
				if(!first) s += ", ";
				s += e;
				first = false;
			}
			s += ") {\n" + funcBlock + "}\n";
			return s;
		}
	}
	
	public static class ReturnStmt extends Expr {
		final Expr returnExp;
		
		ReturnStmt(Expr returnExp) {
			this.returnExp = returnExp;
		}
		@Override
		public String toString() {
			return "RETURN (" + returnExp + ")";
		}
	}

	public static class WhileStatement extends Expr {
		final Expr condition;
		final StatementBlock block;
		
		WhileStatement(Expr condition, StatementBlock block) {
			this.condition = condition;
			this.block = block;
		}
		
		@Override
		public String toString() {
			return "WHILE (" + condition + ") {\n" + block + "}\n";
		}
	}
	
	// For break, continue, and quit
	public static class FlowControlStmt extends Expr {
		final Token ctrlTok;
		FlowControlStmt(Token ctrlTok) {
			this.ctrlTok = ctrlTok;
		}
		
		@Override
		public String toString() {
			return "FLOW_CTRL-"+ctrlTok.type.name();
		}
	}
	
	public static class IfStatement extends Expr {
		final Expr condition;
		final StatementBlock block;
		final StatementBlock elseBlock;
		
		IfStatement(Expr condition, StatementBlock block, StatementBlock elseBlock) {
			this.condition = condition;
			this.block = block;
			this.elseBlock = elseBlock;
		}
		
		@Override
		public String toString() {
			String s = "IF (" + condition + ") {\n" + block + "}\n";
			if(elseBlock != null) {
				s += "ELSE {\n" + elseBlock + "}\n";
			}
			return s;
		}
	}
	
	public static class BinaryOp extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;

		BinaryOp(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
		
		@Override
		public String toString() {
			return "BINOP ("+left+", "+operator.type+", "+right+")";
		}
	}
	
	static class UnaryOp extends Expr {
		final Token operator;
		final Expr right;

		UnaryOp(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}
		
		@Override
		public String toString() {
			return "UNOP ("+operator.type+", "+right+")";
		}
	}
	
	public static class Literal extends Expr {
		final Object value;

		Literal(Object value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return "LITERAL ("+value.getClass().getSimpleName()+":"+value+")";
		}
	}
	public static class VariableAccess extends Expr {
		final Token varIdTok;
	    
		VariableAccess(Token varIdTok) {
	      this.varIdTok = varIdTok;
	    }
		@Override
		public String toString() {
			return "GET ("+varIdTok.value+")";
		}
	}
	public static class Parameter extends Expr {
		final Token varType;
		final Token varIdTok;
		
		Parameter(Token varType, Token varIdTok) {
	      this.varIdTok = varIdTok;
	      this.varType = varType;
	    }
		@Override
		public String toString() {
			return varType.type + " " + varIdTok.value;
		}
	}
	
	public static class Assign extends Expr {
		final Token varIdTok;
		final Token dataTypeTok;
		final Expr value;
		
	    Assign(Token varIdTok, Token dataTypeTok, Expr value) {
	      this.varIdTok = varIdTok;
	      this.value = value;
	      this.dataTypeTok = dataTypeTok;
	    }
	   
	    @Override
		public String toString() {
			return "ASSIGN " + varIdTok.value + " "+ dataTypeTok.type + " VALUE " + value;
		}
	}
	public static class Grouping extends Expr {
		final Expr expression;

		Grouping(Expr expression) {
			this.expression = expression;
		}
		@Override
		public String toString() {
			return "(( " + expression + " ))";
		}
	}
}
