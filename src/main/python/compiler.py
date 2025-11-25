import sys
import dis
import ast
import json

def dict_to_ast(node):
    if isinstance(node, dict) and 'type' in node:
        node_type = getattr(ast, node['type'])
        fields = {}
        for k, v in node.get('fields', {}).items():
            fields[k] = dict_to_ast(v)
        return node_type(**fields)
    elif isinstance(node, list):
        return [dict_to_ast(n) for n in node]
    else:
        return node

if __name__ == "__main__": # compiler.py input.json output.py
    if len(sys.argv) != 3:
        print("Usage: python compiler.py <input_file.json> <output_file.py>")
        sys.exit(1)

    with open(sys.argv[1], "r") as f:
        ast_dict = json.load(f)

    tree = dict_to_ast(ast_dict)
    ast.fix_missing_locations(tree)
    compiled_code = ast.unparse(tree)
    #compiled_code = compile(tree, filename="ast", mode="exec")
    #dis.dis(compiled_code)

    with open(sys.argv[2], "w") as f:
        f.write(compiled_code)

    print("Python code written to", sys.argv[2])
    sys.exit(0)
