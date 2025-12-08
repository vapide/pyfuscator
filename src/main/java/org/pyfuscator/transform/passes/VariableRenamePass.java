package org.pyfuscator.transform.passes;

import org.pyfuscator.ast.Node;
import org.pyfuscator.scope.ScopeManager;
import org.pyfuscator.transform.TransformationPass;
import org.pyfuscator.utils.NameGenerator;
import org.pyfuscator.utils.ObfuscationConfig;
import org.pyfuscator.utils.VariableTracker;

import java.util.ArrayList;
import java.util.List;

// a pass that renames local variables and optionally functions/classes
// it tracks imports and scopes and then goes ahead and generates new names
public class VariableRenamePass extends TransformationPass {
    private final VariableTracker variableTracker;
    private final ScopeManager scopeManager;
    private final NameGenerator nameGenerator;
    private final ObfuscationConfig config;

    // creates the pass with required helpers and config
    public VariableRenamePass(ScopeManager scopeManager, NameGenerator nameGenerator, ObfuscationConfig config) {
        this.scopeManager = scopeManager;
        this.nameGenerator = nameGenerator;
        this.config = config != null ? config : ObfuscationConfig.createDefault();
        this.variableTracker = new VariableTracker(config);
    }

    @Override
    public Node apply(Node rootNode) {
        rootNode.walk(this::processEnterNode, this::processExitNode);
        return rootNode;
    }

    // handles node when entering it
    private void processEnterNode(Node node) {
        // note down the names from imports so they are not renamed
        if ("Import".equals(node.getType()) || "ImportFrom".equals(node.getType())) {
            for (Node child : node.getChildren()) {
                if ("alias".equals(child.getType())) {
                    Object nameField = child.getFields().get("name");
                    if (nameField != null) {
                        String moduleName = nameField.toString();
                        if (moduleName.contains(".")) {
                            moduleName = moduleName.split("\\.")[0];
                        }
                        variableTracker.trackImport(moduleName);
                    }
                    Object asnameField = child.getFields().get("asname");
                    if (asnameField != null) {
                        String asname = asnameField.toString();
                        variableTracker.trackImport(asname);
                    }
                }
            }
        }

        // handle global and nonlocal statements
        if ("Global".equals(node.getType())) {
            processGlobalNode(node);
        } else if ("Nonlocal".equals(node.getType())) {
            processNonlocalNode(node);
        }

        // handle function definitions and async definitions so that we can rename and bind
        if ("FunctionDef".equals(node.getType()) || "AsyncFunctionDef".equals(node.getType())) {
            Object nameField = node.getFields().get("name");
            if (nameField != null) {
                String funcName = nameField.toString();
                if (config.isRenameFunctions() && variableTracker.shouldRenameVariable(funcName)) {
                    String newName = nameGenerator.generate();
                    scopeManager.bindLocal(funcName, newName);
                    scopeManager.registerFunctionRename(funcName, newName); // Register globally
                    node.addField("name", newName);
                } else {
                    scopeManager.bindLocal(funcName, funcName);
                }
            }
        } else if ("ClassDef".equals(node.getType())) {
            // same with classes
            Object nameField = node.getFields().get("name");
            if (nameField != null) {
                String className = nameField.toString();
                if (config.isRenameClasses() && variableTracker.shouldRenameVariable(className)) {
                    String newName = nameGenerator.generate();
                    scopeManager.bindLocal(className, newName);
                    node.addField("name", newName);
                } else {
                    scopeManager.bindLocal(className, className);
                }
            }
        }

        // enter a new scope for functions slash classes slash lambdas
        if (isScopeCreatingNode(node)) {
            scopeManager.enterScope();
            bindFunctionParameters(node);
        }

        // process the identifier and the parameter nodes
        if ("Name".equals(node.getType())) {
            processNameNode(node);
        } else if ("arg".equals(node.getType())) {
            processArgNode(node);
        } else if ("Attribute".equals(node.getType())) {
            processAttributeNode(node);
        }
    }

    private void processAttributeNode(Node node) {
        Object attributeField = node.getFields().get("attr");
        if (attributeField == null) return;

        String attributeName = attributeField.toString();

        // skip special methods and attributes
        if (variableTracker.isSpecialMethod(attributeName) || variableTracker.isSpecialAttribute(attributeName)) {
            return;
        }

        // check if this is self.attribute by looking at children
        boolean isSelfAttribute = false;
        for (Node child : node.getChildren()) {
            if ("Name".equals(child.getType())) {
                Object idField = child.getFields().get("id");
                if ("self".equals(idField)) {
                    isSelfAttribute = true;
                    break;
                }
            }
        }

        if (!variableTracker.shouldRenameVariable(attributeName)) {
            return;
        }

        // handle self.attribute - always rename
        if (isSelfAttribute) {
            if (scopeManager.isFunctionRegistered(attributeName)) {
                node.addField("attr", scopeManager.resolveFunctionGlobally(attributeName));
            } else {
                String newName = nameGenerator.generate();
                scopeManager.registerFunctionRename(attributeName, newName);
                node.addField("attr", newName);
            }
            return;
        }

        // handle obj.method - only if config allows
        if (config.isRenameFunctions() && scopeManager.isFunctionRegistered(attributeName)) {
            node.addField("attr", scopeManager.resolveFunctionGlobally(attributeName));
        }
    }

    // exit node or pop if its a creating node (function/class/lambda)
    private void processExitNode(Node node) {

        // new if statement to update global and nonlocal nodes/statements on exit
        if ("Global".equals(node.getType()) || "Nonlocal".equals(node.getType())) {
            updateStatementNames(node);
        }
        if (isScopeCreatingNode(node)) {
            scopeManager.exitScope();
        }
    }

    private void updateStatementNames(Node node) {
        // if turned into seperate method this would be start
        Object namesField = node.getFields().get("names"); // names not name
        if (namesField instanceof List) {
            List<Object> namesList = (List<Object>) namesField;
            List<String> renamedNames = new ArrayList<>();

            for (Object nameObject : namesList) {
                String varName = nameObject.toString();
                String renamedVar = scopeManager.resolve(varName); // resolve new name
                renamedNames.add(renamedVar);
            }

            node.addField("names", renamedNames);
        }
    }
    // end of method


    private void processNameNode(Node node) {
        Object idField = node.getFields().get("id");
        if (idField == null) return;

        String varName = idField.toString();

        if (variableTracker.shouldRenameVariable(varName)) {
            String currentBinding = scopeManager.resolve(varName);
            boolean hasBinding = scopeManager.isBound(varName);

            if (!hasBinding) {
                // first time seeing this variable so then generate a new name and bind it
                String newName = nameGenerator.generate();
                scopeManager.bindLocal(varName, newName);
                node.addField("id", newName);
            } else {
                // otherwise use the new name which was already generated
                node.addField("id", currentBinding);
            }
        }
    }

    private void processArgNode(Node node) { // processes argument node
        Object argField = node.getFields().get("arg");
        if (argField == null) return;

        String paramName = argField.toString();
        if ("self".equals(paramName) || "cls".equals(paramName)) return;

        if (variableTracker.shouldRenameVariable(paramName)) {
            String currentBinding = scopeManager.resolve(paramName);
            node.addField("arg", currentBinding);
        }
    }

    // bind parameters within a function or lambda expression
    private void bindFunctionParameters(Node functionNode) {
        String nodeType = functionNode.getType();
        if (!("FunctionDef".equals(nodeType) || "AsyncFunctionDef".equals(nodeType) || "Lambda".equals(nodeType))) {
            return;
        }

        for (Node child : functionNode.getChildren()) {
            if ("arguments".equals(child.getType())) {
                for (Node argNode : child.getChildren()) {
                    if ("arg".equals(argNode.getType())) {
                        Object argField = argNode.getFields().get("arg");
                        if (argField != null) {
                            String paramName = argField.toString();
                            if ("self".equals(paramName) || "cls".equals(paramName)) continue;

                            if (variableTracker.shouldRenameVariable(paramName)) {
                                String newName = nameGenerator.generate();
                                scopeManager.bindLocal(paramName, newName);
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private boolean isScopeCreatingNode(Node node) {
        String type = node.getType();
        return "FunctionDef".equals(type) || "AsyncFunctionDef".equals(type) ||
                "ClassDef".equals(type) || "Lambda".equals(type);
    }

    private void processGlobalNode(Node node) {
        Object namesField = node.getFields().get("names");
        if (namesField instanceof List) {
            List<Object> namesList = (List<Object>) namesField; // cast to object list

            for (Object nameObject : namesList) {
                String varName = nameObject.toString();
                scopeManager.markGlobal(varName); // mark global
            }
        }
    }

    // copy paste of processGlobalNode
    private void processNonlocalNode(Node node) {
        Object namesField = node.getFields().get("names");
        if (namesField instanceof List) {
            List<Object> namesList = (List<Object>) namesField; // cast to object list

            for (Object nameObject : namesList) {
                String varName = nameObject.toString();
                scopeManager.markNonlocal(varName); // mark global
            }
        }
    }
}
