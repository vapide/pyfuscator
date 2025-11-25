package org.pyfuscator.scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// basic class, but very important for scoping and binding variables
public class Scope {
	private final Map<String, String> map = new HashMap<>();
	private final Set<String> globals = new HashSet<>();
	private final Set<String> nonlocals = new HashSet<>();

	// bind is just adding a mapping from original to obfuscated
	public void bind(String original, String obfuscated) {
		map.put(original, obfuscated);
	}

	public String getBinding(String original, String defaultValue) {
		return map.getOrDefault(original, defaultValue);
	}

	public boolean contains(String original) {
		return map.containsKey(original);
	}

	public void markGlobal(String name) {
		globals.add(name);
	}

	public void markNonlocal(String name) {
		nonlocals.add(name);
	}

	public boolean isGlobal(String name) {
		return globals.contains(name);
	}

	public boolean isNonlocal(String name) {
		return nonlocals.contains(name);
	}

}
