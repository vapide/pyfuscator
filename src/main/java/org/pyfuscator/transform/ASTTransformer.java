package org.pyfuscator.transform;

import org.pyfuscator.ast.Node;

import java.util.ArrayList;
import java.util.List;

// basic template for AST transformation
public class ASTTransformer {
	private final List<TransformationPass> passes = new ArrayList<>();

	public void addPass(TransformationPass pass) {
		passes.add(pass);
	}

	public Node transform(Node rootNode) {
		Node currentNode = rootNode;
		for (TransformationPass pass : passes) {
			currentNode = pass.apply(currentNode);
		}
		return currentNode;
	}

	public List<TransformationPass> getPasses() {
		return passes;
	}

}
