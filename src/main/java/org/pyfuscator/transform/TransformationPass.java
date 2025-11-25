package org.pyfuscator.transform;

import org.pyfuscator.ast.Node;

// template for passes
public abstract class TransformationPass {
	public abstract Node apply(Node node);
}
