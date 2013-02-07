package wise2.converter.converters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 ChallengeQuestion step into a Wise 4 MultipleChoice step
 * @author geoffreykwan
 */
public class ChallengeQuestionConverter extends AssessmentConverter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		//the JSONObject that will contain the step
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//parse the interactions and responses
		parseInteractionsAndResponses(stepNode);
		
		JSONObject interaction = new JSONObject();
		JSONObject responseDeclaration = new JSONObject();
		
		if(interactions.size() == 1) {
			//there is only one interaction in challenge question
			interaction = interactions.get(0);
		}
		
		if(responses.size() == 1) {
			//there is only one response in challenge question
			responseDeclaration = responses.get(0);
		}
		
		JSONObject assessmentItem = new JSONObject();
		try {
			String identifier = interaction.getString("responseIdentifier");
			
			//set the attributes into the assessment item
			assessmentItem.put("adaptive", false);
			assessmentItem.put("identifier", identifier);
			assessmentItem.put("interaction", interaction);
			assessmentItem.put("responseDeclaration", responseDeclaration);
			assessmentItem.put("timeDependent", false);
			
			//set the attributes into the JSONObject step
			stepNodeJSONObject.put("assessmentItem", assessmentItem);
			stepNodeJSONObject.put("type", getType());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return stepNodeJSONObject;
	}
	
	/**
	 * Parse the choice interaction item body xml node
	 * @param itemBodyChild the xml node of the item body
	 * @return the choice interaction JSONObject
	 */
	protected JSONObject parseChoiceInteraction(Element itemBodyChild) {
		JSONObject interaction = new JSONObject();
		
		//get the response identifier
		Node responseIdentifierNode = itemBodyChild.selectSingleNode("@responseIdentifier");
		String responseIdentifier = responseIdentifierNode.getText();
		
		//get the shuffle value
		Node shuffleNode = itemBodyChild.selectSingleNode("@shuffle");
		String shuffle = shuffleNode.getText();
		
		//get the max choices value
		Node maxChoicesNode = itemBodyChild.selectSingleNode("@maxChoices");
		String maxChoices = maxChoicesNode.getText();
		
		//get the prompt
		String prompt = getPrompt(itemBodyChild);
		
		/*
		 * download all the images and change all the references to point
		 * to the image in the assets folder
		 */
		prompt = downloadImagesAndReplaceReferences(getProjectFolder(), prompt);
		
		JSONArray choices = getChoices(itemBodyChild);
		
		try {
			//set the attributes in the interaction
			interaction.put("choices", choices);
			interaction.put("hasInlineFeedback", true);
			interaction.put("maxChoices", maxChoices);
			interaction.put("prompt", prompt);
			interaction.put("responseIdentifier", responseIdentifier);
			interaction.put("shuffle", shuffle);
			
			//create the attempts object for the challenge question
			JSONObject attempts = new JSONObject();
			attempts.put("navigateTo", "");
			attempts.put("scores", new JSONObject());
			
			//add the attempts object to the interaction
			interaction.put("attempts", attempts);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return interaction;
	}
	
	/**
	 * Get the choices
	 * @param the interaction xml node
	 * @return the JSONArray that contains all the choice objects
	 */
	protected JSONArray getChoices(Element interaction) {
		JSONArray choices = new JSONArray();
		
		//get the simple choices
		ArrayList<Element> simpleChoices = getChildElementsWithName(interaction, "simpleChoice");

		//get an iterator for the simple choices
		Iterator<Element> simpleChoicesIter = simpleChoices.iterator();
		
		//loop through all the simple choices
		while(simpleChoicesIter.hasNext()) {
			//get a simple choice
			Element simpleChoice = simpleChoicesIter.next();
			
			//get the identifier
			Node identifierNode = simpleChoice.selectSingleNode("@identifier");
			String identifier = identifierNode.getText();
			
			//get the choice text
			String interactionChildText = simpleChoice.getText();
			String choiceText = interactionChildText;
			
			//get the feedback
			Element feedbackElement = getChildElementWithName(simpleChoice, "feedbackInline");
			String feedback = feedbackElement.getText();
			
			JSONObject choice = new JSONObject();
			try {
				//set the attributes of the choice JSONObject
				choice.put("feedback", feedback);
				choice.put("fixed", true);
				choice.put("identifier", identifier);
				choice.put("text", choiceText);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			/*
			 * check if there is any text in the choice and only add the 
			 * choice object if there is non-white space in the text
			 */
			if(!choiceText.trim().equals("")) {
				choices.put(choice);						
			}
		}
		
		return choices;
	}
	
	/**
	 * Get the responses
	 * @param a list of assessment item xml nodes
	 * @return a list of response JSONObjects
	 */
	protected ArrayList<JSONObject> parseResponses(List<Node> assessmentItemNodes) {
		//get an iterator of all the assessment item xml nodes
		Iterator<Node> assessmentItemNodesIterator = assessmentItemNodes.iterator();
		
		//the list that will contain the responses we will return
		ArrayList<JSONObject> responses = new ArrayList<JSONObject>();
		
		//loop through all the assessment item xml nodes
		while(assessmentItemNodesIterator.hasNext()) {
			//get an assessment item xml node
			Node assessmentItemNode = assessmentItemNodesIterator.next();
			
			if(assessmentItemNode instanceof DefaultElement) {
				DefaultElement assessmentItemElement = (DefaultElement) assessmentItemNode;
				
				//get the response declarations
				ArrayList<Element> responseDeclarations = getChildElementsWithName(assessmentItemElement, "responseDeclaration");
				Iterator<Element> responseDeclarationsIter = responseDeclarations.iterator();
				
				//loop through all the response declarations
				while(responseDeclarationsIter.hasNext()) {
					//get a response declaration
					Element responseDeclaration = responseDeclarationsIter.next();
					
					JSONObject response = new JSONObject();
					
					//get the identifier
					Node identifierNode = responseDeclaration.selectSingleNode("@identifier");
					String identifier = identifierNode.getText();
					
					//get the correct response
					Element correctResponseElement = getChildElementWithName(responseDeclaration, "correctResponse");
					Element correctResponseValueElement = getChildElementWithName(correctResponseElement, "value");
					String correctResponseValue = correctResponseValueElement.getText();
					
					try {
						//correct responses is an array because there may be multiple correct answers
						JSONArray correctResponse = new JSONArray();
						correctResponse.put(correctResponseValue);
						
						//set the values of the response
						response.put("identifier", identifier);
						response.put("correctResponse", correctResponse);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					//add the response to our array of responses
					responses.add(response);
				}
			}
		}
		
		return responses;
	}
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "multiplechoice";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "ChallengeNode";
	}

	/**
	 * Get the name of the file we will write the JSON to
	 * @param stepCounter the global step counter
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".ch";
	}

	/**
	 * Get the type that will be an attribute in the step JSON
	 */
	protected String getType() {
		return "Challenge";
	}

}
