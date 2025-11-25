package org.pyfuscator.ast;

// enum for all node types, sourced from Python AST documentation
public enum NodeType {
	MODULE,
	INTERACTIVE,
	EXPRESSION,
	FUNCTION_DEF,
	ASYNC_FUNCTION_DEF,
	CLASS_DEF,
	RETURN,
	DELETE,
	ASSIGN,
	AUG_ASSIGN,
	ANN_ASSIGN,
	FOR,
	ASYNC_FOR,
	WHILE,
	IF,
	WITH,
	ASYNC_WITH,
	MATCH,
	MATCH_CASE,
	TRY,
	EXCEPT_HANDLER,
	FINALLY,
	RAISE,
	ASSERT,
	IMPORT,
	IMPORT_FROM,
	GLOBAL,
	NONLOCAL,
	PASS,
	BREAK,
	CONTINUE,
	NAME,
	CONSTANT,
	ATTRIBUTE,
	SUBSCRIPT,
	TUPLE,
	LIST,
	DICT,
	SET,
	CALL,
	KEYWORD,
	BIN_OP,
	UNARY_OP,
	BOOL_OP,
	COMPARE,
	LAMBDA,
	ARG,
	ARGUMENTS,
	KEYWORD_ARGUMENT,
	COMPREHENSION,
	LIST_COMP,
	SET_COMP,
	DICT_COMP,
	GENERATOR_EXP,
	SLICE,
	FORMATTED_VALUE,
	JOINED_STR,
	UNKNOWN;

	public static NodeType fromString(String s) {
		if (s == null) {
			return UNKNOWN; // return unknown for null input
		}
		try {
			return NodeType.valueOf(s.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN; // or for any invalid input
		}
	}


}
