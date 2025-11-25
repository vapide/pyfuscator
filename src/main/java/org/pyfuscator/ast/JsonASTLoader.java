package org.pyfuscator.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonASTLoader {
	private final ObjectMapper mapper = new ObjectMapper();

	// exception if json is invalid
	public Node loadFromString(String jsonString) throws IOException {
		// sanity check from earlier testing
		if (jsonString == null || jsonString.trim().isEmpty()) {
			return null;
		}
		JsonNode rootElement = mapper.readTree(jsonString);
		return convertNode(rootElement);
	}

	// exception if file not found or json is invalid
	public Node loadFromFile(File file) throws IOException {
		if (file == null || !file.exists()) {
			throw new IOException("Input file does not exist or is null");
		}
		JsonNode rootElement = mapper.readTree(file);
		return convertNode(rootElement);
	}

	// convert the node from json to the node class
	private Node convertNode(JsonNode astNode) {
		// early exit for null nodes
		if (astNode == null || astNode.isNull()) return null;

		String nodeType = astNode.get("type").asText();
		Node resultNode = new Node(nodeType);

		// process all fields in the AST node
		JsonNode fieldsContainer = astNode.get("fields");
		if (fieldsContainer != null && fieldsContainer.isObject()) {
			fieldsContainer.fields().forEachRemaining(fieldEntry -> {
				String fieldName = fieldEntry.getKey();
				JsonNode fieldValue = fieldEntry.getValue();

				if (fieldValue.isArray()) {
					// handle array fields like function body and arguments
					if (fieldValue.isEmpty()) {
						resultNode.addField("_empty_array_" + fieldName, true);
					} else {
						fieldValue.elements().forEachRemaining(rawChild -> {
							Node childNode = convertNode(rawChild);
							if (childNode != null) {
								resultNode.addChild(childNode);
								childNode.addField("_parent_field", fieldName);
							}
						});
					}
				} else if (fieldValue.isObject() && fieldValue.has("type")) {
					// ctx (context) nodes get flattened to avoid extra nesting
					if ("ctx".equals(fieldName)) {
						fieldValue.fields().forEachRemaining(ctxEntry -> {
							String flatKey = fieldName + "." + ctxEntry.getKey();
							JsonNode ctxValue = ctxEntry.getValue();
							if (ctxValue.isTextual()) {
								resultNode.addField(flatKey, ctxValue.asText());
							} else if (ctxValue.isNumber()) {
								resultNode.addField(flatKey, ctxValue.toString());
							} else if (ctxValue.isNull()) {
								resultNode.addField(flatKey, null);
							} else if (ctxValue.isObject() && ctxValue.isEmpty()) {
								// skip empty ctx objects
							} else {
								resultNode.addField(flatKey, ctxValue.toString());
							}
						});
					} else {
						// regular child nodes
						Node childNode = convertNode(fieldValue);
						if (childNode != null) {
							resultNode.addChild(childNode);
							childNode.addField("_parent_field", fieldName);
							resultNode.addField(fieldName + "_node_type", childNode.getType());
						}
					}
				} else if (fieldValue.isObject()) {
					// flatten non AST objects into dotted field names
					fieldValue.fields().forEachRemaining(objEntry -> {
						String flatKey = fieldName + "." + objEntry.getKey();
						JsonNode objValue = objEntry.getValue();
						if (objValue.isTextual()) {
							resultNode.addField(flatKey, objValue.asText());
						} else if (objValue.isNumber()) {
							resultNode.addField(flatKey, objValue.toString());
						} else if (objValue.isNull()) {
							resultNode.addField(flatKey, null);
						} else {
							resultNode.addField(flatKey, objValue.toString());
						}
					});
				} else {
					// primitive field values
					if (fieldValue.isTextual()) {
						resultNode.addField(fieldName, fieldValue.asText());
					} else if (fieldValue.isNumber()) {
						resultNode.addField(fieldName, fieldValue.toString());
					} else if (fieldValue.isBoolean()) {
						resultNode.addField(fieldName, String.valueOf(fieldValue.asBoolean()));
					} else if (fieldValue.isNull()) {
						resultNode.addField(fieldName, null);
					} else {
						// for unknown types
						resultNode.addField(fieldName, fieldValue.toString());
					}
				}
			});
		}

		return resultNode;
	} // end convertNode
}

/* example json format
{
  "type": "Module",
  "fields": {
    "body": [
      {
        "type": "Assign",
        "fields": {
          "targets": [
            {
              "type": "Name",
              "fields": {
                "id": "x",
                "ctx": {
                  "type": "Store",
                  "fields": {}
                }
              }
            }
          ],
          "value": {
            "type": "Constant",
            "fields": {
              "value": 2,
              "kind": null
            }
          },
          "type_comment": null
        }
      },
      {
        "type": "Assign",
        "fields": {
          "targets": [
            {
              "type": "Name",
              "fields": {
                "id": "y",
                "ctx": {
                  "type": "Store",
                  "fields": {}
                }
              }
            }
          ],
          "value": {
            "type": "Constant",
            "fields": {
              "value": 3,
              "kind": null
            }
          },
          "type_comment": null
        }
      },
      {
        "type": "Assign",
        "fields": {
          "targets": [
            {
              "type": "Name",
              "fields": {
                "id": "result",
                "ctx": {
                  "type": "Store",
                  "fields": {}
                }
              }
            }
          ],
          "value": {
            "type": "BinOp",
            "fields": {
              "left": {
                "type": "Name",
                "fields": {
                  "id": "x",
                  "ctx": {
                    "type": "Load",
                    "fields": {}
                  }
                }
              },
              "op": {
                "type": "Add",
                "fields": {}
              },
              "right": {
                "type": "Name",
                "fields": {
                  "id": "y",
                  "ctx": {
                    "type": "Load",
                    "fields": {}
                  }
                }
              }
            }
          },
          "type_comment": null
        }
      },
      {
        "type": "Expr",
        "fields": {
          "value": {
            "type": "Call",
            "fields": {
              "func": {
                "type": "Name",
                "fields": {
                  "id": "print",
                  "ctx": {
                    "type": "Load",
                    "fields": {}
                  }
                }
              },
              "args": [
                {
                  "type": "Name",
                  "fields": {
                    "id": "result",
                    "ctx": {
                      "type": "Load",
                      "fields": {}
                    }
                  }
                }
              ],
              "keywords": []
            }
          }
        }
      }
    ],
    "type_ignores": []
  }
}
*/