package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the parent for several steps that convert a Wise 2 Assessment step
 * into various Wise 4 steps such as MultipleChoice, Notes, OpenResponse, and
 * AssessmentList
 * @author geoffreykwan
 */
abstract class AssessmentConverter extends Converter {
	//the qti document for the step
	Document qtiDocument = null;
	
	//the path to the assessment item parent that contains all the assessment items
	XPath assessmentItemPath = null;
	
	//will contain all the interaction objects
	ArrayList<JSONObject> interactions = new ArrayList<JSONObject>();
	
	//will contain all the response objects
	ArrayList<JSONObject> responses = new ArrayList<JSONObject>();
	
	/**
	 * Parse all the interactions and responses
	 * @param stepNode the XML node for the step
	 */
	protected void parseInteractionsAndResponses(Node stepNode) {
		//get the qti document for the step
		qtiDocument = getQtiDocument(stepNode);
		
		//get the assessment item xml nodes from the xml document
		List<Node> assessmentItemNodes = getAssessmentItemNodes(qtiDocument);
		
		//get all the interactions
		interactions = parseInteractions(assessmentItemNodes);
		
		//get all the responses
		responses = parseResponses(assessmentItemNodes);
	}
	
	/**
	 * Get a Document object that contains the qti for the step
	 * @param stepNode the xml node for the step
	 * @return a Document containing the qti string
	 */
	protected Document getQtiDocument(Node stepNode) {
		//get the qti string from the step
		String qtiString = "";
		
		Node singleNode = stepNode.selectSingleNode("parameters/asQTI");
		
		if(singleNode != null) {
			qtiString = singleNode.getText();
		}
		
		SAXReader reader = new SAXReader();
		Reader in = new StringReader(qtiString);
		Document document = null;
		
		try {
			//write the qti string to the document
			document = reader.read(in);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	/**
	 * Get all the assessment item nodes in the qti document
	 * @param document the qti document
	 * @return a list of assessment item xml nodes
	 */
	protected List<Node> getAssessmentItemNodes(Document document) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("qti", "http://www.imsglobal.org/xsd/imsqti_v2p0");
		
		assessmentItemPath = DocumentHelper.createXPath("//qti:assessmentItem");
		assessmentItemPath.setNamespaceURIs(map);
		
		//get the qti:assessmentItem nodes from the qti document
		List<Node> assessmentItemNodes = assessmentItemPath.selectNodes(document);
		
		return assessmentItemNodes;
	}
	
	/**
	 * Get all the interaction objects as JSONObjects in a list
	 * @param assessmentItemNodes a list of assessment item xml nodes
	 * @return a list of interaction JSONObjects
	 */
	protected ArrayList<JSONObject> parseInteractions(List<Node> assessmentItemNodes) {
		//get an iterator for the assessmentItem xml nodes
		Iterator<Node> assessmentItemNodesIterator = assessmentItemNodes.iterator();
		
		//the list we will store all the interactions we find
		ArrayList<JSONObject> interactions = new ArrayList<JSONObject>();
		
		//loop through all the assessmentItem xml nodes 
		while(assessmentItemNodesIterator.hasNext()) {
			//get an assessmentItem xml node
			Node assessmentItemNode = assessmentItemNodesIterator.next();
			
			if(assessmentItemNode instanceof DefaultElement) {
				//get the children of the assessmentItem
				DefaultElement assessmentItemElement = (DefaultElement) assessmentItemNode;
				Iterator assessmentItemChildrenIterator = assessmentItemElement.nodeIterator();
				
				//loop through all the children of the assessmentItem
				while(assessmentItemChildrenIterator.hasNext()) {
					Object nextAssessmentItemChild = assessmentItemChildrenIterator.next();
					
					if(nextAssessmentItemChild instanceof Element) {
						//get an assessmentItem child
						Element assessmentItemChild = (Element) nextAssessmentItemChild;
						
						if(assessmentItemChild.getName().equals("itemBody")) {
							//the child is an itemBody
							
							//get the children of the itemBody
							Iterator itemBodyChildrenIterator = assessmentItemChild.nodeIterator();
							
							//loop through the children of the itemBody
							while(itemBodyChildrenIterator.hasNext()) {
								//get an itemBody child
								Object nextItemBodyChild = itemBodyChildrenIterator.next();
								
								if(nextItemBodyChild instanceof Element) {
									Element itemBodyChild = (Element) nextItemBodyChild;
									
									if(itemBodyChild.getName().equals("extendedTextInteraction")) {
										//the child is an extendedTextInteraction
										
										//parse the extendedTextInteraction into a JSONObject
										JSONObject interaction = parseExtendedTextInteraction(itemBodyChild);

										//add the interaction JSONObject to our list of interactions
										interactions.add(interaction);
									} else if(itemBodyChild.getName().equals("choiceInteraction")) {
										//the child is a choiceInteraction
										
										//parse the choiceInteraction into a JSONObject
										JSONObject interaction = parseChoiceInteraction(itemBodyChild);
										
										//add the interaction JSONObject to our list of interactions
										interactions.add(interaction);
									}
								}
							}
						}
					}
				}
			}
		}
		
		//return the array of interaction JSONObjects
		return interactions;
	}
	
	/**
	 * Parse an extended text interaction
	 * @param itemBodyChild a child element of an item body qti
	 * @return an interaction JSONObject
	 */
	protected JSONObject parseExtendedTextInteraction(Element itemBodyChild) {
		JSONObject interaction = new JSONObject();
		
		//get the responseIdentifier
		Node responseIdentifierNode = itemBodyChild.selectSingleNode("@responseIdentifier");
		String responseIdentifier = responseIdentifierNode.getText();
		
		//get the placeholderText
		Node placeholderTextNode = itemBodyChild.selectSingleNode("@placeholderText");
		String placeholderText = placeholderTextNode.getText();
		
		//get the expectedLines
		Node expectedLinesNode = itemBodyChild.selectSingleNode("@expectedLines");
		String expectedLines = "";
		if(expectedLinesNode != null) {
			expectedLines = expectedLinesNode.getText();											
		}
		
		//get the prompt
		String prompt = getPrompt(itemBodyChild);
		
		/*
		 * download all the images and change all the references to point
		 * to the image in the assets folder
		 */
		prompt = downloadImagesAndReplaceReferences(getProjectFolder(), prompt);
		
		try {
			//set the attributes into our interaction JSONObject
			interaction.put("expectedLines", expectedLines);
			interaction.put("hasInlineFeedback", false);
			interaction.put("placeholderText", placeholderText);
			interaction.put("prompt", prompt);
			interaction.put("responseIdentifier", responseIdentifier);
			interaction.put("type", "text");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return interaction;
	}
	
	/**
	 * Parse a choice interaction
	 * @param itemBodyChild a child element of an item body qti
	 * @return an interaction JSONObject
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
		
		//get the choices
		JSONArray choices = getChoices(itemBodyChild);
		
		try {
			//set the attributes into our interaction JSONObject
			interaction.put("prompt", prompt);
			interaction.put("responseIdentifier", responseIdentifier);
			interaction.put("choices", choices);
			interaction.put("type", "radio");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return interaction;
	}
	
	/**
	 * Get all the response objects as JSONObjects in a list
	 * @param assessmentItemNodes a list of assessment item xml nodes
	 * @return a list of response JSONObjects
	 */
	protected ArrayList<JSONObject> parseResponses(List<Node> assessmentItemNodes) {
		//get an iterator for the assessment items
		Iterator<Node> assessmentItemNodesIterator = assessmentItemNodes.iterator();
		
		//the list that will hold all the responses
		ArrayList<JSONObject> responses = new ArrayList<JSONObject>();
		
		//loop thorugh all the assessment items
		while(assessmentItemNodesIterator.hasNext()) {
			//get an assessment item
			Node assessmentItemNode = assessmentItemNodesIterator.next();
			
			if(assessmentItemNode instanceof DefaultElement) {
				DefaultElement assessmentItemElement = (DefaultElement) assessmentItemNode;
				
				//get all the children of this assessment item that are responseDeclarations 
				ArrayList<Element> responseDeclarations = getChildElementsWithName(assessmentItemElement, "responseDeclaration");
				
				//get an iterator for the responseDeclarations
				Iterator<Element> responseDeclarationsIter = responseDeclarations.iterator();
				
				//loop through all the responseDeclarations
				while(responseDeclarationsIter.hasNext()) {
					//get a responseDeclaration
					Element responseDeclaration = responseDeclarationsIter.next();
					
					//parse the responseDeclaration into a JSONObject
					JSONObject response = parseResponseDeclaration(responseDeclaration);
					
					//add the response JSONObject to our list
					responses.add(response);
				}
			}
		}
		
		//return the list of response JSONObjects
		return responses;
	}
	
	/**
	 * Parse a reponseDeclaration
	 * @param responseDeclaration a child element of an assessment item qti
	 * @return a responseDeclaration JSONObject
	 */
	protected JSONObject parseResponseDeclaration(Element responseDeclaration) {
		JSONObject response = new JSONObject();
		
		//get the identifier
		Node identifierNode = responseDeclaration.selectSingleNode("@identifier");
		String identifier = identifierNode.getText();
		
		//get the cardinality
		Node cardinalityNode = responseDeclaration.selectSingleNode("@cardinality");
		String cardinality = cardinalityNode.getText();
		
		//get the base type
		Node baseTypeNode = responseDeclaration.selectSingleNode("@baseType");
		String baseType = baseTypeNode.getText();

		//get the correct response
		String correctResponseText = null;
		Element correctResponse = getChildElementWithName(responseDeclaration, "correctResponse");
		if(correctResponse != null) {
			Element value = getChildElementWithName(correctResponse, "value");
			
			if(value != null) {
				//get the correct response text
				correctResponseText = value.getText();
			}
		}

		try {
			//set the attributes into the response JSONObject
			response.put("identifier", identifier);
			response.put("baseType", baseType);
			response.put("cardinality", cardinality);
			
			if(correctResponseText != null) {
				//set the correct response if there is one
				response.put("correctResponse", correctResponseText);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//return the response JSONObject
		return response;
	}
	
	/**
	 * Generate the JSONObject for an AssessmentList step
	 * @param document the qti document
	 * @param assessmentItemPath the xml path to the assessment item
	 * @param interactions the interaction JSONObjects
	 * @param responses the response JSONObjects
	 * @return the JSONObject for the assessment list step
	 */
	protected JSONObject generateAssessmentListJSON(Document document, XPath assessmentItemPath, ArrayList<JSONObject> interactions, ArrayList<JSONObject> responses) {
		//the JSON for the whole assessment list step
		JSONObject assessmentListJSON = new JSONObject();
		
		//the JSON array for the assessment parts
		JSONArray assessments = new JSONArray();
		
		//a map with key id to value response object
		HashMap<String, JSONObject> idToResponse = new HashMap<String, JSONObject>();
		
		//loop through all the responses
		for(int x=0; x<responses.size(); x++) {
			//get a response
			JSONObject response = responses.get(x);
			
			try {
				//get the identifier and put it into the map
				String identifier = response.getString("identifier");
				idToResponse.put(identifier, response);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		//get an iterator for the interactions
		Iterator<JSONObject> interactionsIter = interactions.iterator();
		
		//assessment counter used for generating ids
		int assessmentCounter = 0;
		
		//loop through all the interactions
		while(interactionsIter.hasNext()) {
			//get an iteraction
			JSONObject interaction = interactionsIter.next();
			
			try {
				//get the response identifier (this is not used anymore since it is not unique)
				//String responseIdentifier = interaction.getString("responseIdentifier");
				
				//make the assessment JSONObject
				JSONObject assessment = new JSONObject();
				
				//set the the attributes into the assessment JSONObject
				//assessment.put("id", interaction.getString("responseIdentifier"));
				assessment.put("id", "assessment" + assessmentCounter);
				assessment.put("type", interaction.getString("type"));
				assessment.put("prompt", interaction.get("prompt"));
				
				if(interaction.getString("type").equals("text")) {
					//interaction is a text input type
					
					//set this attribute to empty string because this does not exist in wise 2
					assessment.put("isRichTextEditorAllowed", "");
					
					//get the starter sentence text
					JSONObject starter = new JSONObject();
					String text = interaction.getString("placeholderText");
					
					if(text.trim().equals("")) {
						/*
						 * there was no starter sentence, display 0 is do not offer starter
						 * sentence
						 */
						starter.put("display", "0");
						starter.put("text", text);
					} else {
						//there is a starter sentence
						/*
						 * display 2 is always show starter sentence
						 * change the display back to "1" after "show starter sentence"
						 * button is implemented in assessmentlist items. 
						 */
						starter.put("display", "2");
						starter.put("text", text);						
					}
					
					//put the starter object into the assessment object
					assessment.put("starter", starter);
				} else if(interaction.getString("type").equals("radio")) {
					//interaction is a multiple choice radio button type
					
					//make an array to store the choices
					JSONArray choices = interaction.getJSONArray("choices");
					
					//put the choices into the assessment object
					assessment.put("choices", choices);
					
					//get the response for this interaction
					JSONObject response = responses.get(assessmentCounter);
					
					if(response.has("correctResponse")) {
						//there is a correct response field
						
						//get the correct response
						String correctResponse = response.getString("correctResponse");
						
						if(correctResponse.contains("|")) {
							/*
							 * correct response is comprised of multiple answers that are delimited by '|'
							 * if the correct responses are choice0, choice2, and choice3 it will look 
							 * like choice0|2|3
							 */
							
							//split the string at the '|' characters
							String[] split = correctResponse.split("\\|");
							
							JSONArray correctResponses = new JSONArray();
							
							//loop through each part of the split
							for(int x=0; x<split.length; x++) {
								//get a correct response
								String correctResponsePart = split[x];
							
								if(x == 0) {
									//this was the first split which will contain the word 'choice' in it
									correctResponses.put(correctResponsePart);
								} else {
									/*
									 * this was any split after the first which will only contain a number so
									 * we need to prepend 'choice' ourselves
									 */
									correctResponses.put("choice" + correctResponsePart);									
								}
							}
							
							//add the correct responses array to the assessment
							assessment.put("correctResponse", correctResponses);
						} else {
							//there is only one correct answer 
							
							//add the correct response string to the assessment
							assessment.put("correctResponse", correctResponse);							
						}
					}
				}
				
				//put the assessment into the assessments array
				assessments.put(assessment);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//increment the counter
			assessmentCounter++;
		}
		
		//wise 2 does not have prompts for assessments so we will set it to ""
		String prompt = "";
		
		try {
			//set the attributes into the assessment list step JSON
			assessmentListJSON.put("assessments", assessments);
			assessmentListJSON.put("displayAnswerAfterSubmit", true);
			assessmentListJSON.put("isLockAfterSubmit", false);
			assessmentListJSON.put("isMustCompleteAllPartsBeforeExit", false);
			assessmentListJSON.put("prompt", prompt);
			assessmentListJSON.put("type", getType());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//return the JSON for the assessment list step
		return assessmentListJSON;
	}
	
	/**
	 * Get the JSONObject for a notes step
	 * @param document the qti document
	 * @param assessmentItemPath the xml path to the assessment item
	 * @param interactions the interaction JSONObjects
	 * @param responses the response JSONObjects
	 * @return the JSONObject for the notes step
	 */
	protected JSONObject generateNotesJSON(Document document, XPath assessmentItemPath, ArrayList<JSONObject> interactions, ArrayList<JSONObject> responses) {
		//the JSON object for the whole notes step
		JSONObject notesJSON = new JSONObject();
		
		//get the identifier
		Node idNode = assessmentItemPath.selectSingleNode(document).selectSingleNode("@identifier");
		String assessmentItemIdentifier = idNode.getText();
		
		//get the adaptive field
		Node adaptiveNode = assessmentItemPath.selectSingleNode(document).selectSingleNode("@adaptive");
		boolean assessmentItemAdaptive = new Boolean(adaptiveNode.getText());
		
		//notes only has one interaction and one response
		JSONObject interaction = new JSONObject();
		JSONObject responseDeclaration = new JSONObject();
		
		if(interactions.size() == 1) {
			/*
			 * get the interaction, there is only one for notes. if there
			 * are more interactions we would be generating an assessment list
			 * in which case this function would not be called
			 */
			interaction = interactions.get(0);
		}
		
		if(responses.size() == 1) {
			/*
			 * get the response, there is only one for notes. if there
			 * are more responses we would be generating an assessment list
			 * in which case this function would not be called
			 */
			responseDeclaration = responses.get(0);
		}
		
		//get teh time dependent field
		Node timeDependentNode = assessmentItemPath.selectSingleNode(document).selectSingleNode("@timeDependent");
		boolean assessmentItemTimeDependent = new Boolean(timeDependentNode.getText());
		
		//the assessment item object that contains the interaction and response and other fields
		JSONObject assessmentItem = new JSONObject();
		try {
			//set the attributes of the assessment item
			assessmentItem.put("adaptive", assessmentItemAdaptive);
			assessmentItem.put("identifier", assessmentItemIdentifier);
			assessmentItem.put("interaction", interaction);
			assessmentItem.put("responseDeclaration", responseDeclaration);
			assessmentItem.put("timeDependent", assessmentItemTimeDependent);
			
			JSONObject starterSentence = new JSONObject();
			
			//set the starter sentence
			if(interaction.has("placeholderText")) {
				String sentence = interaction.getString("placeholderText");
				
				if(sentence.trim().equals("")) {
					//display 0 is do not offer starter sentence
					starterSentence.put("display", "0");
					starterSentence.put("sentence", sentence);
				} else {
					//display 2 is show starter sentence immediately
					starterSentence.put("display", "2");
					starterSentence.put("sentence", sentence);						
				}
			}
			
			notesJSON.put("assessmentItem", assessmentItem);
			notesJSON.put("isRichTextEditorAllowed", false);
			notesJSON.put("starterSentence", starterSentence);
			notesJSON.put("type", getType());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//return the JSON for the notes step
		return notesJSON;
	}
	
	/**
	 * Get the prompt from the interaction
	 * @param interaction the xml interaction object
	 * @return a String containing the prompt for the interaction
	 */
	protected String getPrompt(Element interaction) {
		String prompt = "";
		
		//get an iterator for the children
		Iterator interactionChildrenIterator = interaction.nodeIterator();
		
		//loop through all the children
		while(interactionChildrenIterator.hasNext()) {
			//get a child
			Object nextInteractionChild = interactionChildrenIterator.next();
			
			if(nextInteractionChild instanceof Element) {
				Element interactionChild = (Element) nextInteractionChild;
				
				if(interactionChild.getName().equals("prompt")) {
					//the child is a prompt
					
					//get the prmopt text
					String interactionChildText = interactionChild.getText();
					prompt = interactionChildText;
				}
			}
		}
		
		//return the prompt String
		return prompt;
	}
	
	/**
	 * Get the choices for the interaction
	 * @param interaction the xml interaction object
	 * @return a JSONArray containing choice JSONObjects
	 */
	protected JSONArray getChoices(Element interaction) {
		//the array that will contain the choices we will return
		JSONArray choices = new JSONArray();
		
		//get the children of the interaction
		Iterator interactionChildrenIterator = interaction.nodeIterator();
		
		//loop through all the children
		while(interactionChildrenIterator.hasNext()) {
			Object nextInteractionChild = interactionChildrenIterator.next();
			
			if(nextInteractionChild instanceof Element) {
				Element interactionChild = (Element) nextInteractionChild;
				
				if(interactionChild.getName().equals("simpleChoice")) {
					//the child is a simple choice
					
					//get the identifier
					Node identifierNode = interactionChild.selectSingleNode("@identifier");
					String identifier = identifierNode.getText();
					
					//get the text for the choice
					String interactionChildText = interactionChild.getText();
					String choiceText = interactionChildText;
					
					//get the feedback xml object
					Element feedbackElement = getChildElementWithName(interactionChild, "feedbackInline");
					
					JSONObject choice = new JSONObject();
					try {
						//set the id and text for the choice
						choice.put("id", identifier);
						choice.put("text", choiceText);
						
						if(feedbackElement != null) {
							//set the feedback text
							String feedback = feedbackElement.getText();
							choice.put("feedback", feedback);
						}		
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					/*
					 * check if there is any text in the choice only add the 
					 * choice object if there is non-white space in the text
					 */
					if(!choiceText.trim().equals("")) {
						choices.put(choice);						
					}
				}
			}
		}
		
		//return the array of choices
		return choices;
	}
	
	/*
	 * needs to be implemented by child classes that will to parse and create
	 * the JSON step by calling the functions in this class
	 */
	abstract protected JSONObject parseStepNode(Node stepNode);
}
