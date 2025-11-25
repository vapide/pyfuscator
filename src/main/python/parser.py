import ast
import json
import sys

class ASTEncoder(ast.NodeVisitor):
    def visit(self, node):
        if isinstance(node,ast.AST):
            fields = {}
            for field in node._fields or []:
                fields[field] = self.visit(getattr(node, field))
            return {
                "type": node.__class__.__name__,
                "fields": fields
            }
        elif isinstance(node, list):
            return [self.visit(n) for n in node]
        else:
            return node

def ast_to_json(source_code: str) -> str:
    tree = ast.parse(source_code)
    encoder = ASTEncoder()
    ast_dict = encoder.visit(tree)
    return json.dumps(ast_dict, indent=2)

if __name__ == "__main__": # parser.py input.py output.json
    if len(sys.argv) != 3:
        print("Usage: python parser.py <input_file.py> <output_file.json>")
        sys.exit(-1)

    with open(sys.argv[1], "r") as f:
        source = f.read()

    ast_json = ast_to_json(source)

    with open(sys.argv[2], "w") as f:
        f.write(ast_json)

    print("AST JSON written to ", sys.argv[2])
    
    sys.exit(0)
