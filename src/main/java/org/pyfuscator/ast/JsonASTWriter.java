package org.pyfuscator.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonASTWriter {
	private final ObjectMapper mapper;

	// constructor to initialize the ObjectMapper
	public JsonASTWriter() {
		this.mapper = new ObjectMapper();
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // serializationfeature is just a pretty printing function
	}

	// exception if file cannot be written
	public void writeToFile(Node astRootNode, String filePath) throws IOException {
		// sanity check before writing
		if (astRootNode == null || filePath == null || filePath.isEmpty()) {
			throw new IOException("Invalid input: node or file path is null/empty");
		}
		JsonNode jsonOutput = convertNodeToJson(astRootNode);
		mapper.writeValue(new File(filePath), jsonOutput);
	}

	private JsonNode convertNodeToJson(Node astNode) {
		// early return for null nodes
		if (astNode == null) {
			return mapper.nullNode();
		}

		ObjectNode jsonResult = mapper.createObjectNode();
		jsonResult.put("type", astNode.getType());

		ObjectNode fieldsContainer = mapper.createObjectNode();

		// track empty arrays to preserve them during full circle conversion
		Set<String> preservedEmptyArrays = new HashSet<>();
		for (Map.Entry<String, Object> fieldEntry : astNode.getFields().entrySet()) {
			String fieldKey = fieldEntry.getKey();
			if (fieldKey.startsWith("_empty_array_")) {
				String arrayFieldName = fieldKey.substring("_empty_array_".length());
				preservedEmptyArrays.add(arrayFieldName);
			}
		}

		// process regular fields
		for (Map.Entry<String, Object> fieldEntry : astNode.getFields().entrySet()) {
			String fieldKey = fieldEntry.getKey();
			Object fieldVal = fieldEntry.getValue();

			// skip tracking fields
			if (fieldKey.endsWith("_node_type") || fieldKey.equals("_parent_field") || fieldKey.startsWith("_empty_array_")) {
				continue;
			}

			if (fieldKey.contains(".")) {
				handleDottedKey(fieldsContainer, fieldKey, fieldVal);
			} else {
				addFieldValue(fieldsContainer, fieldKey, fieldVal);
			}
		}

		// restore empty arrays
		for (String emptyArrayName : preservedEmptyArrays) {
			if (!fieldsContainer.has(emptyArrayName)) {
				fieldsContainer.set(emptyArrayName, mapper.createArrayNode());
			}
		}

		// group the child nodes based on their parent field name
		Map<String, List<Node>> childrenGroupedByField = new HashMap<>();
		for (Node childNode : astNode.getChildren()) {
			Object parentFieldTag = childNode.getFields().get("_parent_field");
			String parentFieldName = parentFieldTag != null ? parentFieldTag.toString() : "body";
			childrenGroupedByField.computeIfAbsent(parentFieldName, k -> new ArrayList<>()).add(childNode);
		}

		// convert child nodes to JSON (single element or array depending on field type)
		for (Map.Entry<String, List<Node>> childGroup : childrenGroupedByField.entrySet()) {
			String parentFieldName = childGroup.getKey();
			List<Node> childrenList = childGroup.getValue();

			boolean needsArray = isArrayField(parentFieldName, astNode.getType());

			if (childrenList.size() == 1 && !needsArray) {
				// shouldn't be in array
				fieldsContainer.set(parentFieldName, convertNodeToJson(childrenList.get(0)));
			} else {
				// must be an array
				ArrayNode childArray = mapper.createArrayNode();
				for (Node child : childrenList) {
					childArray.add(convertNodeToJson(child));
				}
				fieldsContainer.set(parentFieldName, childArray);
			}
		}

		jsonResult.set("fields", fieldsContainer);
		return jsonResult;
	} // end convertNodeToJson

	// determine if a field should be serialized as an array based on the Python AST structure
	private boolean isArrayField(String fieldName, String parentNodeType) {
		// special case: args field is NOT array for function definitions
		if ("args".equals(fieldName)) {
			return !"FunctionDef".equals(parentNodeType) &&
			       !"Lambda".equals(parentNodeType) &&
			       !"AsyncFunctionDef".equals(parentNodeType);
		}

		// body is single expr for lambda, array for everything else
		if ("body".equals(fieldName)) {
			return !"Lambda".equals(parentNodeType);
		}

		// common array fields in Python AST - collected from testing various node types
		return "targets".equals(fieldName) ||
				"keywords".equals(fieldName) ||
				"orelse".equals(fieldName) ||
				"finalbody".equals(fieldName) ||
				"decorator_list".equals(fieldName) ||
				"bases".equals(fieldName) ||
				"generators".equals(fieldName) ||
				"ifs".equals(fieldName) ||
				"posonlyargs".equals(fieldName) ||
				"kw_defaults".equals(fieldName) ||
				"defaults".equals(fieldName) ||
				"type_params".equals(fieldName) ||
				"ops".equals(fieldName) ||
				"comparators".equals(fieldName) ||
				"kwonlyargs".equals(fieldName) ||
				"names".equals(fieldName);
	}

	// dotted key is like ctx.lineno or ctx.col_offset (https://medium.com/@wshanshan/intro-to-python-ast-module-bbd22cd505f7)
	// example of a dotted key is in json format is
    /*
    {
        "type": "Name",
        "fields": {
            "id": "x",
            "ctx": {
                "type": "Load",
                "fields": {
                    "lineno": 1,
                    "col_offset": 0
                }
            }
        }
     */
	// recursively handle dotted keys like "ctx.lineno" by creating nested objects
	private void handleDottedKey(ObjectNode fieldsContainer, String dottedKey, Object value) {
		String[] keyParts = dottedKey.split("\\.", 2);
		String topLevelKey = keyParts[0];
		String nestedKey = keyParts[1];

		ObjectNode nestedObject;
		if (fieldsContainer.has(topLevelKey)) {
			JsonNode existingNode = fieldsContainer.get(topLevelKey);
			if (existingNode.isObject()) {
				nestedObject = (ObjectNode) existingNode;
			} else {
				// replace non object with new object
				nestedObject = mapper.createObjectNode();
				fieldsContainer.set(topLevelKey, nestedObject);
			}
		} else {
			nestedObject = mapper.createObjectNode();
			fieldsContainer.set(topLevelKey, nestedObject);
		}

		// go recursive if there are more dots to handle
		if (nestedKey.contains(".")) {
			handleDottedKey(nestedObject, nestedKey, value);
		} else {
			if ("type".equals(nestedKey)) {
				// type fields need a corresponding "fields" object
				nestedObject.put("type", value.toString());
				if (!nestedObject.has("fields")) {
					nestedObject.set("fields", mapper.createObjectNode());
				}
			} else {
				addFieldValue(nestedObject, nestedKey, value);
			}
		}
	} // end handleDottedKey

	// add field value to the parent node (could've used a switch statement probably)
	private void addFieldValue(ObjectNode parentObject, String fieldKey, Object fieldValue) {
		if (fieldValue == null) {
			parentObject.putNull(fieldKey);
		} else if (fieldValue instanceof Node) {
			// recursive casting
			parentObject.set(fieldKey, convertNodeToJson((Node) fieldValue));
		} else if (fieldValue instanceof List) {
			ArrayNode jsonArray = mapper.createArrayNode();
			for (Object listItem : (List<?>) fieldValue) {
				if (listItem instanceof Node) {
					jsonArray.add(convertNodeToJson((Node) listItem));
				} else if (listItem == null) {
					jsonArray.addNull();
				} else if (listItem instanceof Number) {
					jsonArray.add(((Number) listItem).doubleValue());
				} else if (listItem instanceof Boolean) {
					jsonArray.add((Boolean) listItem);
				} else {
					jsonArray.add(listItem.toString());
				}
			}
			parentObject.set(fieldKey, jsonArray);
		} else if (fieldValue instanceof Integer) {
			parentObject.put(fieldKey, (Integer) fieldValue);
		} else if (fieldValue instanceof Long) {
			parentObject.put(fieldKey, (Long) fieldValue);
		} else if (fieldValue instanceof Double) {
			parentObject.put(fieldKey, (Double) fieldValue);
		} else if (fieldValue instanceof Boolean) {
			parentObject.put(fieldKey, (Boolean) fieldValue);
		} else if (fieldValue instanceof String strValue)
		{
			try {
				if (strValue.contains(".")) {
					parentObject.put(fieldKey, Double.parseDouble(strValue));
				} else {
					parentObject.put(fieldKey, Integer.parseInt(strValue));
				}
			} catch (NumberFormatException e) {
				// keep as string
				parentObject.put(fieldKey, strValue);
			}
		} else {
			// for unknown types
			parentObject.put(fieldKey, fieldValue.toString());
		}
	} // end addFieldValue
}