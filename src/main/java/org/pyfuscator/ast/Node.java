package org.pyfuscator.ast;

import java.util.*;
import java.util.function.Consumer;

public class Node {
	private final Map<String, Object> fields = new LinkedHashMap<>();
	private final List<Node> children = new ArrayList<>();
	private String type;
	private Node parent;

	// basic constructo rthen getters and setters

	public Node(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void addField(String key, Object value) {
		fields.put(key, value);
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void addChild(Node child) {
		children.add(child);
		child.parent = this;
	}

	// walk using depth first traversal with enter and exit consumers
	// used exit consumer so that we can go back up the tree if needed
	// https://www.baeldung.com/java-depth-first-search
	// used dequestack because stack class is old
	public void walk(Consumer<Node> enter, Consumer<Node> exit) {
		class Frame {
			final Node node;
			final boolean childrenProcessed;

			Frame(Node n, boolean processed) {
				node = n;
				childrenProcessed = processed;
			}
		}

		Deque<Frame> stack = new ArrayDeque<>();
		stack.push(new Frame(this, false));

		while (!stack.isEmpty()) {
			Frame f = stack.pop();
			Node cur = f.node;

			if (!f.childrenProcessed) {
				enter.accept(cur);
				stack.push(new Frame(cur, true));
				List<Node> kids = cur.getChildren();
				for (int i = kids.size() - 1; i >= 0; i--) {
					Node child = kids.get(i);
					if (child != null) {
						stack.push(new Frame(child, false));
					}
				}
			} else {
				exit.accept(cur);
			}
		}
	}

	@Override
	public String toString() {
		return "Node(" + type + ", fields=" + fields + ", children=" + children.size() + ")";
	}

}
