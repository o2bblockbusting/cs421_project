package Sulfur;

/*
 * This class would not need to exist if Java was dynamically typed, but it is not.
 * This class handles all arithmetic/operators used on the various data types.
 * It first uses the 'getResultType' method to ensure the operands are compatible and then chooses the highest order data type as the output.
 * Then, each method casts both operands to the expected output type before performing the operation.
 * This results in a very lengthy class, and I really wish there was a better way to do this.
 */

public class OperationPerformer {
	
	// Ensures that the two objects have compatible types for the operation
	// Returns the tokentype for the expected resulting data type from this operation
	public static TokenType getResultType(Object left, Object right, int lineNum) {
		TokenType lt = getObjType(left, lineNum);
		TokenType rt = getObjType(right, lineNum);
		TokenType[] typePriority = {TokenType.STRING_T, TokenType.DOUBLE_T, TokenType.FLOAT_T, TokenType.LONG_T, TokenType.INTEGER_T, TokenType.CHARACTER_T};
		
		if(lt == rt) {
			return lt;
		}
		else if(rt == TokenType.BOOLEAN_T && lt != TokenType.STRING) {
			Interpreter.error("Cannot perform operation with boolean and "+lt, lineNum);
		}
		else if(lt == TokenType.BOOLEAN_T && rt != TokenType.STRING) {
			Interpreter.error("Cannot perform operation with boolean and "+rt, lineNum);
		}
		else {
			for(TokenType t : typePriority) {
				if(rt == t || lt == t) {
					return t;
				}
			}
		}
		Interpreter.error("Failed to find result type between "+lt+" and "+rt, lineNum);
		return null;
	}
	
	public static TokenType getObjType(Object obj, int lineNum) {
		if(obj instanceof Integer) {
			return TokenType.INTEGER_T;
		} else if(obj instanceof Long) {
			return TokenType.LONG_T;
		} else if(obj instanceof Float) {
			return TokenType.FLOAT_T;
		} else if(obj instanceof Double) {
			return TokenType.DOUBLE_T;
		} else if(obj instanceof Character) {
			return TokenType.CHARACTER_T;
		} else if(obj instanceof String) {
			return TokenType.STRING_T;
		} else if(obj instanceof Boolean) {
			return TokenType.BOOLEAN_T;
		} else {
			Interpreter.error("Unexpected type "+obj.getClass().getSimpleName(), lineNum);
			return null;
		}
	}
	
	public static Object add(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case STRING_T:
			return (String) left + (String) right;
		case DOUBLE_T:
			return toDouble(left) + toDouble(right);
		case FLOAT_T:
			return toFloat(left) + toFloat(right);
		case LONG_T:
			return toLong(left) + toLong(right);
		case INTEGER_T:
			return toInteger(left) + toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) + toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in addition expression", lineNum);
			return null;
		}
	}
	
	public static Object sub(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) - toDouble(right);
		case FLOAT_T:
			return toFloat(left) - toFloat(right);
		case LONG_T:
			return toLong(left) - toLong(right);
		case INTEGER_T:
			return toInteger(left) - toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) - toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in subtraction expression", lineNum);
			return null;
		}
	}
	
	public static Object multiply(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) * toDouble(right);
		case FLOAT_T:
			return toFloat(left) * toFloat(right);
		case LONG_T:
			return toLong(left) * toLong(right);
		case INTEGER_T:
			return toInteger(left) * toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) * toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in multiplication expression", lineNum);
			return null;
		}
	}
	
	public static Object divide(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		if(Math.abs(toDouble(right)) == 0.0) {
			Interpreter.error("Cannot divide by zero", lineNum);
		}
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) / toDouble(right);
		case FLOAT_T:
			return toFloat(left) / toFloat(right);
		case LONG_T:
			return toLong(left) / toLong(right);
		case INTEGER_T:
			return toInteger(left) / toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) / toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in division expression", lineNum);
			return null;
		}
	}
	
	public static Object modulus(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
				
		if(Math.abs(toDouble(right)) == 0.0) {
			Interpreter.error("Cannot divide by zero", lineNum);
		}
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) % toDouble(right);
		case FLOAT_T:
			return toFloat(left) % toFloat(right);
		case LONG_T:
			return toLong(left) % toLong(right);
		case INTEGER_T:
			return toInteger(left) % toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) % toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in modulus expression", lineNum);
			return null;
		}
	}
	
	public static Object and(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case BOOLEAN_T:
			return (Boolean) left && (Boolean) right;
		default:
			Interpreter.error("Cannot use type "+t+" in and expression", lineNum);
			return null;
		}
	}
	
	public static Object or(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case BOOLEAN_T:
			return (Boolean) left || (Boolean) right;
		default:
			Interpreter.error("Cannot use type "+t+" in or expression", lineNum);
			return null;
		}
	}
	
	public static Object equality(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case STRING_T:
			return (left.toString()).equals(right.toString());
		case DOUBLE_T:
			return toDouble(left) == toDouble(right);
		case FLOAT_T:
			return toFloat(left) == toFloat(right);
		case LONG_T:
			return toLong(left) == toLong(right);
		case INTEGER_T:
			return toInteger(left) == toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) == toCharacter(right);
		case BOOLEAN_T:
			return ((Boolean) left).equals((Boolean) right);
		default:
			Interpreter.error("Cannot use type "+t+" in equality expression", lineNum);
			return null;
		}
	}
	
	public static Object less_than(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) < toDouble(right);
		case FLOAT_T:
			return toFloat(left) < toFloat(right);
		case LONG_T:
			return toLong(left) < toLong(right);
		case INTEGER_T:
			return toInteger(left) < toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) < toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in less_than expression", lineNum);
			return null;
		}
	}
	
	public static Object greater_than(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) > toDouble(right);
		case FLOAT_T:
			return toFloat(left) > toFloat(right);
		case LONG_T:
			return toLong(left) > toLong(right);
		case INTEGER_T:
			return toInteger(left) > toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) > toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in greater_than expression", lineNum);
			return null;
		}
	}
	
	public static Object not_eq(Object left, Object right, int lineNum) {
		return !(Boolean) equality(left, right, lineNum);
	}
	
	public static Object lt_eq(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) <= toDouble(right);
		case FLOAT_T:
			return toFloat(left) <= toFloat(right);
		case LONG_T:
			return toLong(left) <= toLong(right);
		case INTEGER_T:
			return toInteger(left) <= toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) <= toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in less_than_or_equal_to expression", lineNum);
			return null;
		}
	}
	
	public static Object gt_eq(Object left, Object right, int lineNum) {
		TokenType t = getResultType(left, right, lineNum);
		
		switch(t) {
		case DOUBLE_T:
			return toDouble(left) >= toDouble(right);
		case FLOAT_T:
			return toFloat(left) >= toFloat(right);
		case LONG_T:
			return toLong(left) >= toLong(right);
		case INTEGER_T:
			return toInteger(left) >= toInteger(right);
		case CHARACTER_T:
			return toCharacter(left) >= toCharacter(right);
		default:
			Interpreter.error("Cannot use type "+t+" in greater_than_or_equal_to expression", lineNum);
			return null;
		}
	}
	
	public static double toDouble(Object obj) {
		if(obj instanceof Double) {
			return (double) obj;
		}
		else if(obj instanceof Float) {
			return ((Float) obj).doubleValue();
		}
		else if(obj instanceof Long) {
			return ((Long) obj).doubleValue();
		}
		else if(obj instanceof Integer) {
			return ((Integer) obj).doubleValue();
		}
		else if(obj instanceof Character) {
			return ((Integer)((Character) obj + 0)).doubleValue();
		}
		else {
			throw new RuntimeException("Failed to convert "+obj.getClass().getSimpleName()+" to a double");
		}
	}
	
	public static float toFloat(Object obj) {
		if(obj instanceof Double) {
			return ((Double) obj).floatValue();
		}
		else if(obj instanceof Float) {
			return (float) obj;
		}
		else if(obj instanceof Long) {
			return ((Long) obj).floatValue();
		}
		else if(obj instanceof Integer) {
			return ((Integer) obj).floatValue();
		}
		else if(obj instanceof Character) {
			return ((Integer)((Character) obj + 0)).floatValue();
		}
		else {
			throw new RuntimeException("Failed to convert "+obj.getClass().getSimpleName()+" to a float");
		}
	}
	
	public static long toLong(Object obj) {
		if(obj instanceof Double) {
			return ((Double) obj).longValue();
		}
		else if(obj instanceof Float) {
			return ((Float) obj).longValue();
		}
		else if(obj instanceof Long) {
			return (long) obj;
		}
		else if(obj instanceof Integer) {
			return ((Integer) obj).longValue();
		}
		else if(obj instanceof Character) {
			return (Character) obj + 0L;
		}
		else {
			throw new RuntimeException("Failed to convert "+obj.getClass().getSimpleName()+" to a long");
		}
	}
	
	public static int toInteger(Object obj) {
		if(obj instanceof Double) {
			return ((Double) obj).intValue();
		}
		else if(obj instanceof Float) {
			return ((Float) obj).intValue();
		}
		else if(obj instanceof Long) {
			return ((Long) obj).intValue();
		}
		else if(obj instanceof Integer) {
			return (int) obj;
		}
		else if(obj instanceof Character) {
			return ((Character) obj) + 0;
		}
		else {
			throw new RuntimeException("Failed to convert "+obj.getClass().getSimpleName()+" to an integer");
		}
	}
	
	public static char toCharacter(Object obj) {
		return (char) toInteger(obj);
	}
}
