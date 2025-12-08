import sys
import dis
import ast
import json

def dict_to_ast(node):
    if isinstance(node, dict) and 'type' in node:
        node_type_name = node['type']
        node_type = getattr(ast, node_type_name)
        fields = {}
        for k, v in node.get('fields', {}).items():
            fields[k] = dict_to_ast(v)

        # force single AST nodes into lists for common list fields
        # Lambda.body and arguments.args should NOT be converted
        LIST_FIELDS = {'body', 'orelse', 'finalbody', 'decorator_list', 'handlers',
                       'targets', 'keywords', 'elts', 'names'}

        # fields that should NOT be lists for certain node types
        SINGLE_VALUE_FIELDS = {
            'Lambda': {'body'},  # Lambda.body is a single expression
            'arguments': {'args'}  # arguments.args is going to be handled separately
        }

        # fields that should ALWAYS be lists (even if they are single elements in the JSON object)
        ALWAYS_LIST_FIELDS = {
            'Dict': {'keys', 'values'},  # Dict.keys and Dict.values must be lists
            'Tuple': {'elts'},  # Tuple.elts must be a list
            'List': {'elts'},   # List.elts must be a list
            'Set': {'elts'}      # Set.elts must be a list
        }

        for k in list(fields.keys()):
            should_always_be_list = (node_type_name in ALWAYS_LIST_FIELDS and
                                     k in ALWAYS_LIST_FIELDS[node_type_name])

            # check if the field is in list_fields or shuold always be a listlist
            if (k in LIST_FIELDS or should_always_be_list) and not isinstance(fields[k], list):
                #  skip if it is an exception (unless it should always be a list)
                if not should_always_be_list and node_type_name in SINGLE_VALUE_FIELDS and k in SINGLE_VALUE_FIELDS[node_type_name]:
                    continue

                # if value is None it shuold be replaced with an empty list
                if fields[k] is None:
                    fields[k] = []
                else:
                    fields[k] = [fields[k]]

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
