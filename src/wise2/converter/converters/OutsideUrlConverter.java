package wise2.converter.converters;

import org.dom4j.Node;
import org.json.JSONException;
import org.json.JSONObject;

public class OutsideUrlConverter extends Converter {

	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "www";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "OutsideUrlNode";
	}

	/**
	 * Get the step file name
	 * @param stepCounter the global step counter
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".ou";
	}

	/**
	 * Get the step type
	 */
	protected String getType() {
		return "OutsideUrl";
	}

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//get the url
		String url = stepNode.selectSingleNode("parameters/url").getText();
		
		try {
			//set the attributes fo the step
			stepNodeJSONObject.put("type", getType());
			stepNodeJSONObject.put("url", url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return stepNodeJSONObject;
	}

}
