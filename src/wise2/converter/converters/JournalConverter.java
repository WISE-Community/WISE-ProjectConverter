package wise2.converter.converters;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 Journal step into a Wise 4 AssessmentList step  
 * @author geoffreykwan
 */
public class JournalConverter extends Converter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		
		JSONObject stepNodeJSONObject = new JSONObject();
		JSONArray assessments = new JSONArray();
		
		//get all the prompts
		List selectNodes = stepNode.selectNodes("parameters/prompt");
		
		//loop through all the prompts
		for(int x=0; x<selectNodes.size(); x++) {
			//get a prompt
			Element element = (Element) selectNodes.get(x);
			String prompt = element.getText();
			
			JSONObject assessment = new JSONObject();
			JSONObject starter = new JSONObject();
			
			try {
				//set the starter sentence attributes
				starter.put("display", 1);
				starter.put("text", "");
				
				//set the assessment attributes
				assessment.put("id", "assessment" + x);
				assessment.put("type", "text");
				assessment.put("prompt", prompt);
				assessment.put("isRichTextEditorAllowed", false);
				assessment.put("starter", starter);
				
				assessments.put(assessment);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		try {
			//set the step attributes
			stepNodeJSONObject.put("assessments", assessments);
			stepNodeJSONObject.put("displayAnswerAfterSubmit", true);
			stepNodeJSONObject.put("isLockAfterSubmit", false);
			stepNodeJSONObject.put("isMustCompleteAllPartsBeforeExit", true);
			stepNodeJSONObject.put("prompt", "");
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
