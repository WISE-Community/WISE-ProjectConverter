package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The parent class for converters that convert Wise 2 DisplayPage, Evidence,
 * and other pages which are just html pages into a Wise 4 HtmlPage step
 * @author geoffreykwan
 */
abstract class HtmlPageConverter extends Converter {

	/**
	 * Parse the xml step node to create a step JSONObject. We need to override
	 * the parent createStep() function because html files use the .ht file name
	 * for the ref.
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 * @return the JSONObject for the step
	 */
	public JSONObject createStep(Node stepNode, File projectFolder, int stepCounter) {
		//create the step file
		createStepFile(stepNode, projectFolder, stepCounter);
		
		Node titleNode = stepNode.selectSingleNode("title");
		
		//get all the attributes for the step
		String type = getNodeType();
		String identifier = getStepHtFileName(stepCounter);
		String title = titleNode.getText();
		String ref = getStepHtFileName(stepCounter);
		JSONArray previousWorkNodeIds = new JSONArray();
		JSONArray links = new JSONArray();
		String classType = getClassType();
		
		/*
		 * create the JSON object that will be put in the .project.json file. this is not
		 * the JSON that is written in the step file
		 */
		JSONObject stepJSON = createProjectNodesStepJSON(type, identifier, title, ref, previousWorkNodeIds, links, classType);
		
		return stepJSON;
	}
	
	/**
	 * Create the .html file and the .ht file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	public void createStepFile(Node stepNode, File projectFolder, int stepCounter) {
		createStepHtmlFile(stepNode, projectFolder, stepCounter);
		createStepHtFile(stepNode, projectFolder, stepCounter);
	}
	
	/**
	 * Create the .html file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	protected void createStepHtmlFile(Node stepNode, File projectFolder, int stepCounter) {
		//create the file
		File stepHtmlFile = new File(projectFolder, getStepHtmlFileName(stepCounter));
		
		//get the html from the xml node
		String html = stepNode.selectSingleNode(getHtmlTextXMLPath()).getText();

		/*
		 * download all the images and change all the references to point
		 * to the image in the assets folder
		 */
		html = downloadImagesAndReplaceReferences(projectFolder, html);
		
		try {
			//write the html contents to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepHtmlFile));
			out.write(html);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			//create the file on disk
			stepHtmlFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the .ht file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	private void createStepHtFile(Node stepNode, File projectFolder, int stepCounter) {
		String stepHtFileName = getStepHtFileName(stepCounter);
		String stepHtmlFileName = getStepHtmlFileName(stepCounter);
		
		//create the file
		File stepHtFile = new File(projectFolder, stepHtFileName);
		
		JSONObject stepJSON = new JSONObject();
		
		try {
			//set the attributes for the step JSONObject
			stepJSON.put("src", stepHtmlFileName);
			stepJSON.put("type", getType());
			
			//add the hints into the step JSON
			setHints(stepJSON, stepNode);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		try {
			//write the contents to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepHtFile));
			out.write(stepJSON.toString(3));
			out.close();
		} catch (IOException e) {
			System.out.println("Exception ");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			//create the file on disk
			stepHtFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is not used by this class but still needs to implement it
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		return null;
	}
	
	/**
	 * This is not used by this class but still needs to implement it
	 */
	protected String getStepFileName(int stepCounter) {
		return null;
	}
	
	/**
	 * Get the .html file name
	 */
	protected String getStepHtmlFileName(int stepCounter) {
		return "node_" + stepCounter + ".html";
	}
	
	/**
	 * Get the .ht file name
	 */
	protected String getStepHtFileName(int stepCounter) {
		return "node_" + stepCounter + ".ht";
	}
	
	/**
	 * Get the step type
	 */
	protected String getType() {
		return "Html";
	}
	
	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "HtmlNode";
	}
	
	/**
	 * Determines the icon type for the step
	 */
	abstract protected String getClassType();
	
	/**
	 * Get the xml path to the html in the wise 2 xml node
	 */
	abstract protected String getHtmlTextXMLPath();
}
