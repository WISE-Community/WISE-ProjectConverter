package wise2.converter.converters;

import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 Sensemaker step into a Wise 4 MatchSequence step
 * @author geoffreykwan
 */
public class SensemakerConverter extends Converter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//get the prompt
		String prompt = stepNode.selectSingleNode("parameters/instructions").getText();
		
		JSONObject assessmentItem = new JSONObject();
		try {
			String identifier = "MatchSequence";
		
			//set the attributes for the interaction
			JSONObject interaction = new JSONObject();
			interaction.put("choices", new JSONArray());
			interaction.put("fields", new JSONArray());
			interaction.put("hasInlineFeedback", true);
			interaction.put("ordered", false);
			interaction.put("prompt", prompt);
			interaction.put("responseIdentifier", identifier);
			interaction.put("shuffle", true);
			
			//set the attributes for the response
			JSONObject responseDeclaration = new JSONObject();
			responseDeclaration.put("correctResponses", new JSONArray());
			responseDeclaration.put("identifier", identifier);
			
			//set the attributes for the assessment item
			assessmentItem.put("adaptive", false);
			assessmentItem.put("identifier", identifier);
			assessmentItem.put("interaction", interaction);
			assessmentItem.put("responseDeclaration", responseDeclaration);
			assessmentItem.put("timeDependent", false);
			
			//set the attributes for the step
			stepNodeJSONObject.put("assessmentItem", assessmentItem);
			stepNodeJSONObject.put("type", getType());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return stepNodeJSONObject;
	}
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "matchsequence";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "MatchSequenceNode";
	}

	/**
	 * Get the step file name
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".ms";
	}

	/**
	 * Get the step type
	 */
	protected String getType() {
		return "MatchSequence";
	}

}
