package wise2.converter.converters;

import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 Discussion step into a Wise 4 Brainstorm step
 * @author geoffreykwan
 */
public class DiscussionConverter extends Converter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		String prompt = "";
		
		//get the prompt
		Node promptNode = stepNode.selectSingleNode("parameters/prompt");
		if(promptNode != null) {
			prompt = stepNode.selectSingleNode("parameters/prompt").getText();			
		}
		
		JSONObject stepNodeJSONObject = new JSONObject();
		JSONObject interaction = new JSONObject();
		
		try {
			//set the attributes of the interaction
			interaction.put("expectedLines", "0");
			interaction.put("prompt", prompt);
			interaction.put("responseIdentifier", "Brainstorm");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONObject assessmentItem = new JSONObject();
		try {
			//set the attributes of the assessment item
			assessmentItem.put("adaptive", false);
			assessmentItem.put("identifier", "Brainstorm");
			assessmentItem.put("interaction", interaction);
			assessmentItem.put("timeDependent", false);
			
			/*
			 * set the starter sentence to empty string because wise 2 does not have 
			 * starter sentences for discussion
			 */
			JSONObject starterSentence = new JSONObject();
			starterSentence.put("display", "0");
			starterSentence.put("sentence", "");
			
			//set the attributes of the step
			stepNodeJSONObject.put("assessmentItem", assessmentItem);
			stepNodeJSONObject.put("cannedResponses", new JSONArray());
			stepNodeJSONObject.put("displayName", "0");
			stepNodeJSONObject.put("isGated", true);
			stepNodeJSONObject.put("isInstantPollActive", false);
			stepNodeJSONObject.put("isPollEnded", false);
			stepNodeJSONObject.put("isRichTextEditorAllowed", false);
			stepNodeJSONObject.put("starterSentence", starterSentence);
			stepNodeJSONObject.put("title", "");
			stepNodeJSONObject.put("type", getType());
			stepNodeJSONObject.put("useServer", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return stepNodeJSONObject;
	}

	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "brainstorm";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "BrainstormNode";
	}

	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".bs";
	}

	protected String getType() {
		return "Brainstorm";
	}
}
