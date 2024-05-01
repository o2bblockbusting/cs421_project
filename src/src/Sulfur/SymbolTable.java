package Sulfur;
import java.util.HashMap;
import java.util.Map.Entry;

public class SymbolTable {
	private final HashMap<String, Object> vars;
	private final SymbolTable parent;
	
	public SymbolTable(SymbolTable parent) {
		this.parent = parent;
		this.vars = new HashMap<String, Object>();
	}
	
	// Retrieves a variable's value from the current table or the parent's table
	public Object getValue(Token varTok) {
		String varName = (String) varTok.value;
		if(vars.containsKey(varName)) {
			return vars.get(varName);
		}
		else if(parent != null) {
			return parent.getValue(varTok);
		}
		Interpreter.error("Variable "+varName+" not defined", varTok.line);
		return null;
	}
	
	// If the value is already defined in the current table or one of it's parents, it reassigns that variable
	// Otherwise, it adds a new entry to the current symbol table
	public void setValue(String varName, Object value) {
		if(!updateValue(varName, value)) {
			vars.put(varName, value);
		}
	}
	
	// Checks the current table and its parents to see if a variable is already defined, and if so, updates the value
	private boolean updateValue(String varName, Object value) {
		if(vars.containsKey(varName)) {
			if(value.getClass() != vars.get(varName).getClass()) {
				// Type checking disabled, for now
			}
			vars.replace(varName, value);
			return true;
		}
		else if(parent != null && parent.updateValue(varName, value)) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for(Entry<String, Object> ent : vars.entrySet()) {
			s.append(ent.getKey() + " = " + ent.getValue() + ", ");
		}
		return s.toString();
	}
}
