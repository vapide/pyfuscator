package org.pyfuscator.ast;

public class ASTVisitor {
	// default visit method
	public void visit(Node node) {
	}

	// basic recursive method to visit children
	public void visitChildren(Node node) {
		for (Node child : node.getChildren()) {
			visit(child);
		}
	}

	public void preVisit(Node node) {
	}

	public void postVisit(Node node) {
	}
}
