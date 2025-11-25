package org.pyfuscator.utils;

import java.util.HashSet;
import java.util.Set;

public class VariableTracker {

	//builtins
	private static final Set<String> BUILT_IN_FUNCTIONS = Set.of(
			"abs", "all", "any", "ascii", "bin", "bool", "breakpoint", "bytearray", "bytes",
			"callable", "chr", "classmethod", "compile", "complex", "delattr", "dict", "dir",
			"divmod", "enumerate", "eval", "exec", "filter", "float", "format", "frozenset",
			"getattr", "globals", "hasattr", "hash", "help", "hex", "id", "input", "int",
			"isinstance", "issubclass", "iter", "len", "list", "locals", "map", "max",
			"memoryview", "min", "next", "object", "oct", "open", "ord", "pow", "print",
			"property", "range", "repr", "reversed", "round", "set", "setattr", "slice",
			"sorted", "staticmethod", "str", "sum", "super", "tuple", "type", "vars", "zip"
	);

	//keywrods
	private static final Set<String> PYTHON_KEYWORDS = Set.of(
			"False", "None", "True", "and", "as", "assert", "async", "await", "break",
			"class", "continue", "def", "del", "elif", "else", "except", "finally",
			"for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
			"not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
	);

	//attributes
	private static final Set<String> SPECIAL_ATTRIBUTES = Set.of(
			"__name__", "__doc__", "__file__", "__module__", "__class__",
			"__dict__", "__weakref__", "__bases__", "__mro__"
	);

	//special idenftifiers
	private static final Set<String> SPECIAL_IDENTIFIERS = Set.of(
			"self", "cls", "Ellipsis", "NotImplemented"
	);

	// common imports
	private static final Set<String> COMMON_MODULES = Set.of(
			"os", "sys", "re", "json", "math", "random", "datetime", "collections",
			"itertools", "functools", "operator", "pathlib", "urllib", "http", "time"
	);
	private final ObfuscationConfig config;
	private final Set<String> importedModules = new HashSet<>();

	public VariableTracker(ObfuscationConfig config) {
		this.config = config != null ? config : ObfuscationConfig.createDefault();
	}

	public VariableTracker() {
		this.config = ObfuscationConfig.createDefault();
	}

	public boolean isCommonModule(String varName) {
		return COMMON_MODULES.contains(varName);
	}

	public void trackImport(String moduleName) {
		if (moduleName != null) {
			importedModules.add(moduleName);
		}
	}

	public boolean isImported(String varName) {
		return importedModules.contains(varName);
	}

	// checks for nulls, builtins, keywords, special attributes, imports, and __ prefix
	public boolean shouldRenameVariable(String varName) {
		if (varName == null || varName.isEmpty()) return false;
		if (varName.startsWith("__") || isSpecialAttribute(varName)) return false;
		if (SPECIAL_IDENTIFIERS.contains(varName)) return false;
		if (isPythonKeyword(varName)) return false;
		if (varName.equals("_")) return false;
		if (config.isPreserveBuiltins() && isBuiltinFunction(varName)) return false;
		if (config.isPreserveImports() && isImported(varName)) return false;
		return !config.isPreserveImports() || !isCommonModule(varName);
	}


	public boolean isBuiltinFunction(String varName) {
		return BUILT_IN_FUNCTIONS.contains(varName);
	}

	public boolean isImportStatement(String varName) {
		return "import".equals(varName) || "from".equals(varName);
	}

	public boolean isPythonKeyword(String varName) {
		return PYTHON_KEYWORDS.contains(varName);
	}

	public boolean isSpecialMethod(String varName) {
		return varName.startsWith("__") && varName.endsWith("__");
	}

	public boolean isSpecialAttribute(String varName) {
		return SPECIAL_ATTRIBUTES.contains(varName);
	}


}
