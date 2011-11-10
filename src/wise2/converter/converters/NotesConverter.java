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
 * Converts a Wise 2 Notes step into a Wise 4 Note or AssessmentList step
 * @author geoffreykwan
 */
public class NotesConverter extends AssessmentConverter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//parse the interactions and responses
		parseInteractionsAndResponses(stepNode);
		
		if(isAssessmentList()) {
			/*
			 * we will make an assessment list step if there is more than
			 * one interaction
			 */
			stepNodeJSONObject = generateAssessmentListJSON(qtiDocument, assessmentItemPath, interactions, responses);
		} else {
			//we will make a note step since there is only one interaction
			stepNodeJSONObject = generateNotesJSON(qtiDocument, assessmentItemPath, interactions, responses);
		}
		
		return stepNodeJSONObject;
	}
	
	/**
	 * Determine if this should be an assessment list or not
	 * @return whether this step should be an assessment list
	 */
	private boolean isAssessmentList() {
		boolean isAssessmentList = false;
		
		if(interactions.size() > 1 && responses.size() > 1) {
			//there is more than one interaction so this will be an assessment list
			isAssessmentList = true;
		}
		
		return isAssessmentList;
	}

	/**
	 * Get the step file name
	 * @param stepCounter the global step counter
	 */
	protected String getStepFileName(int stepCounter) {
		String stepFileName = "";
		
		if(isAssessmentList()) {
			//this is an assessment list
			stepFileName = "node_" + stepCounter + ".al";
		} else {
			//this is a note
			stepFileName = "node_" + stepCounter + ".or";
		}
		
		return stepFileName;
	}
	
	/**
	 * Get the step type
	 */
	protected String getType() {
		String stepType = "";
		
		if(isAssessmentList()) {
			stepType = "AssessmentList";
		} else {
			stepType = "Note";
		}
		
		return stepType;
	}
	
	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		String stepNodeType = "";
		
		if(isAssessmentList()) {
			stepNodeType = "AssessmentListNode";
		} else {
			stepNodeType = "NoteNode";
		}
		
		return stepNodeType;
	}
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		String stepClassType = "";
		
		if(isAssessmentList()) {
			stepClassType = "instantquiz";
		} else {
			stepClassType = "note";
		}
		
		return stepClassType;
	}
}
