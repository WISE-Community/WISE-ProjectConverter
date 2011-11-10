package wise2.converter.converters;

import org.dom4j.Node;
import org.json.JSONObject;

/**
 * Converts a Wise 2 SelfTest step into a Wise 4 AssessmentList step
 * @author geoffreykwan
 */
public class SelfTestConverter extends AssessmentConverter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//parse the interactions and responses
		parseInteractionsAndResponses(stepNode);
		
		//generate the assessment list step
		stepNodeJSONObject = generateAssessmentListJSON(qtiDocument, assessmentItemPath, interactions, responses);
		
		return stepNodeJSONObject;
	}
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "instantquiz";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "AssessmentListNode";
	}

	/**
	 * Get the step file name
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".al";
	}

	/**
	 * Get the step type
	 */
	protected String getType() {
		return "AssessmentList";
	}

}
