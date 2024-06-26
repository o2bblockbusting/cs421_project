package Sulfur;

import java.util.ArrayList;

public class Interpreter {
	Expr rootExpr;
	SymbolTable globalSymTable;
	
	public Interpreter(Expr exp) {
		this.rootExpr = exp;
		this.globalSymTable = new SymbolTable(null);
	}
	
	public static void error(String msg, int lineNum) throws RuntimeException {
		throw new RuntimeException("[Line "+lineNum+"]: "+msg);
	}
	
	public void run() {
		Object res = evaluate(rootExpr, globalSymTable);
		checkForInvalidUsage(res, false);
	}
	
	// Figures out what type of expression it is using an inefficient chain of if/elif statements
	// Calls the corresponding method to run the method and returns the result
	private Object evaluate(Expr expr, SymbolTable symTable) {
		//System.out.println("EXPRESSION "+expr);
		//System.out.println("SYMTABLE: "+symTable);
		if(expr instanceof Expr.StatementBlock) {
			return evaluate_StatementBlock((Expr.StatementBlock) expr, symTable);
		}
		else if(expr instanceof Expr.FunctionCall) {
			return evaluate_FunctionCall((Expr.FunctionCall) expr, symTable);
		}
		else if(expr instanceof Expr.PrintStmt) {
			return evaluate_PrintStmt((Expr.PrintStmt) expr, symTable);
		}
		else if(expr instanceof Expr.FunctionDef) {
			return evaluate_FunctionDef((Expr.FunctionDef) expr, symTable);
		}
		else if(expr instanceof Expr.ReturnStmt) {
			return evaluate_ReturnStmt((Expr.ReturnStmt) expr, symTable);
		}
		else if(expr instanceof Expr.WhileStatement) {
			return evaluate_WhileStatement((Expr.WhileStatement) expr, symTable);
		}
		else if(expr instanceof Expr.FlowControlStmt) {
			return evaluate_FlowControlStmt((Expr.FlowControlStmt) expr, symTable);
		}
		else if(expr instanceof Expr.ConditionalBlock) {
			throw new RuntimeException("Attempted to execute a conditional block directly, which should not happen");
		}
		else if(expr instanceof Expr.IfStatement) {
			return evaluate_IfStatement((Expr.IfStatement) expr, symTable);
		}
		else if(expr instanceof Expr.BinaryOp) {
			return evaluate_BinaryOp((Expr.BinaryOp) expr, symTable);
		}
		else if(expr instanceof Expr.UnaryOp) {
			return evaluate_UnaryOp((Expr.UnaryOp) expr, symTable);
		}
		else if(expr instanceof Expr.Literal) {
			return ((Expr.Literal) expr).value;
		}
		else if(expr instanceof Expr.ValueArray) {
			return evaluate_ValueArray((Expr.ValueArray) expr, symTable);
		}
		else if(expr instanceof Expr.VariableAccess) {
			return evaluate_VariableAccess((Expr.VariableAccess) expr, symTable);
		}
		else if(expr instanceof Expr.Parameter) {
			throw new RuntimeException("Attempted to execute a parameter, which should not happen");
		}
		else if(expr instanceof Expr.Assign) {
			return evaluate_Assign((Expr.Assign) expr, symTable);
		}
		else if(expr instanceof Expr.Grouping) {
			return evaluate_Grouping((Expr.Grouping) expr, symTable);
		}
		else {
			throw new RuntimeException("Unrecognized expression type: "+expr.getClass().getSimpleName());
		}
	}
	
	private Object evaluate_StatementBlock(Expr.StatementBlock expr, SymbolTable symTable) {
		for(Expr e : expr.statements) {
			Object retVal = evaluate(e, symTable);
			if(!(e instanceof Expr.FunctionCall) && retVal != null) {
				return retVal;
			}
		}
		return null;
	}
	
	private Object evaluate_FunctionCall(Expr.FunctionCall expr, SymbolTable symTable) {
		// Get name and check if it is an ArrayList function
		String funcName = expr.funcIdTok.value.toString();
		if(funcName.startsWith("ArrayList")) {
			return evaluate_ArrayFunctionCall(expr, symTable);
		}
		
		// Find function in symbol table		
		Object func = symTable.getValue(expr.funcIdTok);
		if(!(func instanceof Expr.FunctionDef)) {
			error("Attempted to run function "+funcName+" but found a variable of type "+func.getClass().getSimpleName()+" instead", expr.funcIdTok.line);
		}
		
		Expr.FunctionDef funcExpr = (Expr.FunctionDef) func;
		SymbolTable funcSymTable = new SymbolTable(globalSymTable);
		
		//Check arguments length
		if(expr.arguments.size() != funcExpr.parameters.size()) {
			error("Expected "+funcExpr.parameters.size()+"arguments for function "+funcName+" but got "+expr.arguments.size()+" arguments instead", expr.funcIdTok.line);
		}
		
		//Check argument types against parameter types
		for(int i=0; i<expr.arguments.size(); i++) {
			//Get each argument and its matching parameter
			Object arg = evaluate(expr.arguments.get(i), symTable);
			Expr.Parameter param = funcExpr.parameters.get(i);
			TokenType paramType = param.varType.type;
			
			//After double checking it is the right type, add it to the symbol table
			Object checkedArg = arg;
			if(param.arrayDegree == 0) {
				checkedArg = getTypeCheckedObj(arg, paramType, expr.funcIdTok.line);
			}
			else if(!arg.getClass().getName().equals(ArrayList.class.getName())) {
				error("Cannot assign non-array value to array parameter",param.varIdTok.line);
			}
				
			funcSymTable.setValue((String) param.varIdTok.value, checkedArg);
		}
		
		//Run function body and check return type
		Object res = evaluate(funcExpr.funcBlock, funcSymTable);
		checkForInvalidUsage(res, true);
		if(funcExpr.returnType == null)
			return null;
		
		return getTypeCheckedObj(res, funcExpr.returnType.type, expr.funcIdTok.line);
	}
	
	// Array function calls are saved in a Expr.FunctionCall expression at parsing but are different because the method name always starts with ArrayList_
	// These calls happen every time the property accessor (~) is used on an array
	// EX:
	// A arr N[]V {61}
	// arr~add(42)
	// arr~get(0)
	// 
	// These function calls all directly correspond to valid ArrayList methods
	private Object evaluate_ArrayFunctionCall(Expr.FunctionCall expr, SymbolTable symTable) {
		ArrayList<Object> args = new ArrayList<Object>();
		for(Expr e : expr.arguments) {
			args.add(evaluate(e, symTable));
		}
		
		// First argument should always be the array
		if(!args.get(0).getClass().getName().equals(ArrayList.class.getName())) {
			error("Expected array", expr.funcIdTok.line);
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object> list = (ArrayList<Object>) args.get(0);
		
		// Argument count doesn't include array
		int numExtraArgs = args.size() - 1;
		int line = expr.funcIdTok.line;
		
		switch((String) expr.funcIdTok.value) {
		case "ArrayList_size":
		case "ArrayList_length":
			checkArgLength(numExtraArgs, 0, line);
			return list.size();
			
		case "ArrayList_get":
			checkArgLength(numExtraArgs, 1, line);
			Integer index = (Integer) getTypeCheckedObj(args.get(1), TokenType.INTEGER_T, line);
			return list.get(index);
		case "ArrayList_set":
			checkArgLength(numExtraArgs, 1, line);
			Integer idx = (Integer) getTypeCheckedObj(args.get(1), TokenType.INTEGER_T, line);
			return list.set(idx, args.get(2));
		case "ArrayList_clone":
			checkArgLength(numExtraArgs, 0, line);
			return list.clone();
		case "ArrayList_indexof":	
			checkArgLength(numExtraArgs, 1, line);
			return list.indexOf(args.get(1));
		case "ArrayList_contains":	
			checkArgLength(numExtraArgs, 1, line);
			return list.contains(args.get(1));
		case "ArrayList_remove":
			checkArgLength(numExtraArgs, 1, line);
			Integer idx2 = (Integer) getTypeCheckedObj(args.get(1), TokenType.INTEGER_T, line);
			return list.remove(idx2);
		case "ArrayList_add":
			if(numExtraArgs == 2) {
				Integer insertIdx = (Integer) getTypeCheckedObj(args.get(1), TokenType.INTEGER_T, line);
				list.add(insertIdx, args.get(2));
				return true;
			}
			checkArgLength(numExtraArgs, 1, line);
			return list.add(args.get(1));
		case "ArrayList_clear":
			checkArgLength(numExtraArgs, 0, line);
			list.clear();
			break;
		default:
			error("Unrecognized array method "+expr.funcIdTok.value, line);
		}
		return null;
	}
	
	//Throws an error if expected argument length is not matched
	private void checkArgLength(int numArgs, int expectedNum, int lineNum) {
		if(numArgs != expectedNum) {
			error("Expected "+expectedNum+" arguments but received "+numArgs, lineNum);
		}
	}
	
	private Object evaluate_PrintStmt(Expr.PrintStmt expr, SymbolTable symTable) {
		for(Expr e : expr.arguments) {
			Object res = evaluate(e, symTable);
			System.out.print(res);
		}
		return null;
	}
	
	private Object evaluate_FunctionDef(Expr.FunctionDef expr, SymbolTable symTable) {
		symTable.setValue(expr.funcIdTok.value.toString(), expr);
		return null;
	}
	
	private Object evaluate_ReturnStmt(Expr.ReturnStmt expr, SymbolTable symTable) {
		return evaluate(expr.returnExp, symTable);
	}
	
	// While loop that continues as long as the condition results in a true boolean
	private Object evaluate_WhileStatement(Expr.WhileStatement expr, SymbolTable symTable) {
		SymbolTable loopSymTable = new SymbolTable(symTable);
		
		while(true) {
			Object conditionalRes = evaluate(expr.condition, loopSymTable);
			if(!(Boolean) getTypeCheckedObj(conditionalRes, TokenType.BOOLEAN_T, expr.lineNum)) {
				break;
			}
			
			// Return result if a return statement is run
			Object returnedRes = evaluate(expr.block, loopSymTable);
			if(returnedRes == TokenType.JUMP_OUT) {
				break;
			}
			else if(returnedRes == TokenType.KONTINUE) {
				continue;
			}
			else if(returnedRes != null) {
				return returnedRes;
			}
		}
		return null;
	}
	
	private Object evaluate_FlowControlStmt(Expr.FlowControlStmt expr, SymbolTable symTable) {
		if(expr.ctrlTok.type == TokenType.QUIT) {
			System.exit(0);
		}
		return expr.ctrlTok.type;
	}
	
	private Object evaluate_IfStatement(Expr.IfStatement expr, SymbolTable symTable) {
		SymbolTable ifSymTable = new SymbolTable(symTable);
		
		for(Expr.ConditionalBlock ifBlock : expr.conditionalBlocks) {
			Object conditionalRes = evaluate(ifBlock.condition, ifSymTable);
			if(!(Boolean) getTypeCheckedObj(conditionalRes, TokenType.BOOLEAN_T, ifBlock.lineNum)) {
				continue;
			}
			
			Object returnedRes = evaluate(ifBlock.body, ifSymTable);
			return returnedRes;
		}
		return expr.elseBlock != null ? evaluate(expr.elseBlock, symTable) : null;
	}
	
	private Object evaluate_BinaryOp(Expr.BinaryOp expr, SymbolTable symTable) {
		Object left = evaluate(expr.left, symTable);
		Object right = evaluate(expr.right, symTable);
		int lineNum = expr.operator.line;
		
		//System.out.println("PERFORMING "+left.toString()+" "+expr.operator.type+" "+right.toString());
		// Handles the following operations
		// AND('&'), OR('|'), MODULUS('%'), ADD('+'), SUB('-'), MULTIPLY('*'), DIVIDE('/'), EQUALITY('='), LESS_THAN('<'), GREATER_THAN('>'),
		//LT_EQ, GT_EQ, NOT_EQ,

		switch(expr.operator.type) {
		case ADD:
			return OperationPerformer.add(left, right, lineNum);
		case SUB:
			return OperationPerformer.sub(left, right, lineNum);
		case MULTIPLY:
			return OperationPerformer.multiply(left, right, lineNum);
		case DIVIDE:
			return OperationPerformer.divide(left, right, lineNum);
		case MODULUS:
			return OperationPerformer.modulus(left, right, lineNum);
		case AND:
			return OperationPerformer.and(left, right, lineNum);
		case OR:
			return OperationPerformer.or(left, right, lineNum);
		case EQUALITY:
			return OperationPerformer.equality(left, right, lineNum);
		case LESS_THAN:
			return OperationPerformer.less_than(left, right, lineNum);
		case GREATER_THAN:
			return OperationPerformer.greater_than(left, right, lineNum);
		case NOT_EQ:
			return OperationPerformer.not_eq(left, right, lineNum);
		case LT_EQ:
			return OperationPerformer.lt_eq(left, right, lineNum);
		case GT_EQ:
			return OperationPerformer.gt_eq(left, right, lineNum);
		default:
			error("Unknown operator type "+expr.operator.type, expr.operator.line);
		}
		return null;
	}
	
	private Object evaluate_UnaryOp(Expr.UnaryOp expr, SymbolTable symTable) {
		TokenType t = expr.operator.type;
		Object o = evaluate(expr.right, symTable);
		
		switch(t) {
		case ADD:
			//Ensure plus is not used on boolean or string
			getTypeCheckedObj(o, TokenType.DOUBLE_T, expr.operator.line);
			return o;
		case SUB:
			if(o instanceof Double) return -(Double) o;
			if(o instanceof Float) return -(Float) o;
			if(o instanceof Long) return -(Long) o;
			if(o instanceof Integer) return -(Integer) o;
			if(o instanceof Character) return -(Character) o;
			error("Cannot use negation on object of type "+o.getClass().getSimpleName(), expr.operator.line);
		case NOT:
			if(o instanceof Boolean) return !(Boolean) o;
			error("Cannot use not operator on object of type "+o.getClass().getSimpleName(), expr.operator.line);
		// HANDLE CASTING
		case DOUBLE_T:
			return OperationPerformer.toDouble(o);
		case FLOAT_T:
			return OperationPerformer.toFloat(o);
		case LONG_T:
			return OperationPerformer.toLong(o);
		case INTEGER_T:
			return OperationPerformer.toInteger(o);
		case CHARACTER_T:
			return OperationPerformer.toCharacter(o);
		case STRING_T:
			return o.toString();
		case BOOLEAN_T:
			if(o instanceof String) {
				return ((String) o).length() > 0;
			}
			else if(o instanceof Boolean) {
				return o;
			}
			else if(o.getClass().isArray()) {
				error("Cannot cast array to boolean", expr.operator.line);
			}
			else {
				//Any non-zero number is true
				return ((Integer) OperationPerformer.toInteger(o)).intValue() != 0;
			}
		default:
			error("Unrecognized unary operator type "+t, expr.operator.line);
		}
		return null;
	}
	
	private Object evaluate_VariableAccess(Expr.VariableAccess expr, SymbolTable symTable) {
		return symTable.getValue(expr.varIdTok);
	}
	
	private Object evaluate_Assign(Expr.Assign expr, SymbolTable symTable) {
		Object value = evaluate(expr.value, symTable);
		//Don't bother checking the type of arrays, its too much of a hassle
		if(expr.arrayDegree == 0)
			value = getTypeCheckedObj(value, expr.dataTypeTok.type, expr.varIdTok.line);
		else if(!value.getClass().getName().equals(ArrayList.class.getName())) {
			error("Cannot assign non-array value to array variable",expr.varIdTok.line);
		}
		
		symTable.setValue((String) expr.varIdTok.value, value);
		return null;
	}
	
	private Object evaluate_Grouping(Expr.Grouping expr, SymbolTable symTable) {
		return evaluate(expr.expression, symTable);
	}
	
	private Object evaluate_ValueArray(Expr.ValueArray expr, SymbolTable symTable) {
		ArrayList<Object> resultArray = new ArrayList<Object>();
		
		for(Expr e : expr.value) {
			resultArray.add(evaluate(e, symTable));
		}
		return resultArray;
	}
	
	//Checks to see if an object matches the expected type or can automatically be casted to the correct type
	//Returns a new object, casted to match the expected type
	//Throws an error if the type does not match and cannot be casted
	private Object getTypeCheckedObj(Object o, TokenType type, int lineNum) {
		switch(type) {
		case DOUBLE_T:
			if(o instanceof Double) return (Double) o;
		case FLOAT_T:
			if(o instanceof Float) return (Float) o;
		case LONG_T:
			if(o instanceof Long) return (Long) o;
		case INTEGER_T:
			if(o instanceof Integer) return (Integer) o;
		case CHARACTER_T:
			if(o instanceof Character) return (Character) o;
			break;
		case STRING_T:
			if(o instanceof String) return (String) o;
			break;
		case BOOLEAN_T:
			if(o instanceof Boolean) return (Boolean) o;
			break;
		case FUNCTION:
			if(o instanceof Expr.FunctionDef) return o;
		default:
			break;
		}
		error("Type mismatch: Expected "+type+" but got "+o.getClass().getSimpleName()+" instead", lineNum);
		return null;
	}
	
	// Called when a function returns and when a program finishes
	// Ensures that jump_out, kontinue, and return are not used in the wrong place
	private void checkForInvalidUsage(Object retObject, boolean funcCall) {
		if(retObject == TokenType.JUMP_OUT) {
			throw new RuntimeException("Attempted to use a jump out statement outside of a while loop");
		}
		else if(retObject == TokenType.KONTINUE) {
			throw new RuntimeException("Attempted to use a kontinue statement outside of a while loop");
		}
		else if(!funcCall && retObject != null) {
			throw new RuntimeException("Attempted to use a return statement outside of a function");
		}
	}
}
