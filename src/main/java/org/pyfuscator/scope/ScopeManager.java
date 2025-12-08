package org.pyfuscator.scope;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScopeManager {
	//deque used as stack for scopes
	private final Deque<Scope> stack = new ArrayDeque<>();
	// global registry to track all function renamings across all scopes
	private final Map<String, String> globalFunctionRegistry = new HashMap<>();

	public ScopeManager() {
		stack.push(new Scope());
	}

	public void enterScope() {
		stack.push(new Scope());
	}

	public void exitScope() {
		stack.pop();
	}

	public void bindLocal(String original, String obfuscated) {
		stack.peek().bind(original, obfuscated);
	}

	public void markGlobal(String name) {
		stack.peek().markGlobal(name);
	}

	public void markNonlocal(String name) {
		stack.peek().markNonlocal(name);
	}

	// resolve is looking for the binding of the original name
	// follows python's LEGB rule
	public String resolve(String original) {
		Iterator<Scope> scopeIterator = stack.iterator();
		while (scopeIterator.hasNext()) {
			Scope currentScope = scopeIterator.next();

			if (currentScope.isGlobal(original)) { // if its global, we go to the global scope
				Scope global = stack.getLast();
				String globalBinding = global.getBinding(original, original);
				return globalBinding;
			}

			if (currentScope.isNonlocal(original)) { // if its nonlocal, we skip this scope
				continue;
			}

			String localBinding = currentScope.getBinding(original, null); // check for local binding
			if (localBinding != null) {
				return localBinding;
			}
		}
		return original;
	}

	// checks if the name is already renamed in any scope based off ofthe LEGB rule
	public boolean isBound(String original) {
		Iterator<Scope> scopeIterator = stack.iterator();
		while (scopeIterator.hasNext()) {
			Scope currentScope = scopeIterator.next();
			if (currentScope.isGlobal(original)) {
				return stack.getLast().contains(original);
			}
			if (currentScope.isNonlocal(original)) continue;
			if (currentScope.contains(original)) return true;
		}
		return false;
	}

	public Scope currentScope() {
		return stack.peek();
	}

	public int depth() {
		return stack.size();
	}

	// register a function rename globally (to access attributes across scopes)
	public void registerFunctionRename(String original, String obfuscated) {
		globalFunctionRegistry.put(original, obfuscated);
	}

	// look up the function for a rename from the global registry
	public String resolveFunctionGlobally(String original) {
		return globalFunctionRegistry.getOrDefault(original, original);
	}

	// check if the function is registered/exists
	public boolean isFunctionRegistered(String original) {
		return globalFunctionRegistry.containsKey(original);
	}

}
