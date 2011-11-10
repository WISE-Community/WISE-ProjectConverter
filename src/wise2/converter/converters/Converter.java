package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The parent class for converters that convert Wise 2 steps to Wise 4 steps
 * @author geoffreykwan
 */
public abstract class Converter {
	
	/**
	 * Create the step object that we will put into the "nodes" JSONArray within
	 * the .project.json file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 * @return a JSONObject containing step attributes that we will put in
	 * the "nodes" JSONArray within the .project.json file 
	 */
	public JSONObject createStep(Node stepNode, File projectFolder, int stepCounter) {
		//create the step file
		createStepFile(stepNode, projectFolder, stepCounter);
		
		Node titleNode = stepNode.selectSingleNode("title");
		
		//get all the attributes for the step
		String type = getNodeType();
		String identifier = getStepFileName(stepCounter);
		String title = titleNode.getText();
		String ref = getStepFileName(stepCounter);
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
	 * Get the JSONObject that we will put into the "nodes" JSONArray within
	 * the .project.json file
	 * @param type
	 * @param identifier
	 * @param title
	 * @param ref
	 * @param previousWorkNodeIds
	 * @param links
	 * @param classType
	 * @return a JSONObject containing the attributes for the step
	 */
	public JSONObject createProjectNodesStepJSON(String type, String identifier, String title, String ref, JSONArray previousWorkNodeIds, JSONArray links, String classType) {
		JSONObject projectStepNode = new JSONObject();
		
		try {
			projectStepNode.put("type", type);
			projectStepNode.put("identifier", identifier);
			projectStepNode.put("title", title);
			projectStepNode.put("ref", ref);
			projectStepNode.put("previousWorkNodeIds", previousWorkNodeIds);
			projectStepNode.put("links", links);
			projectStepNode.put("class", classType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return projectStepNode;
	}
	

	/**
	 * Create the step file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	public void createStepFile(Node stepNode, File projectFolder, int stepCounter) {
		//the JSONObject that we will write into the step file
		JSONObject stepJSON = parseStepNode(stepNode);
		
		//get the step file name
		String stepFileName = getStepFileName(stepCounter);
		
		//create the step file
		File stepFile = new File(projectFolder, stepFileName);
		
		try {
			//write the step contents to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepFile));

			//indent the JSON by passing in the argument 3 (for 3 spaces per indent)
			String stepJSONString = stepJSON.toString(3);
			
			/*
			 * when the the toString() function of JSONObject escapes the '/' so that
			 * html closing tags will be output as <\/font> so we need to fix that
			 * by replacing all \/ with /
			 */
			stepJSONString = stepJSONString.replaceAll("\\\\/", "/");
			
			//write the step JSON to the step file
			out.write(stepJSONString);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			//create the step file
			stepFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the child element with the given name
	 * @param element the xml node
	 * @param name the name of the child node we are looking for
	 * @return the first child xml node with the name we are looking for or
	 * null if none was found
	 */
	public Element getChildElementWithName(Element element, String name) {
		//get an iterator of the children
		Iterator elementChildrenIter = element.nodeIterator();
		Element elementFound = null;
		
		//loop through all the children
		while(elementChildrenIter.hasNext()) {
			//get a child
			Object nextElementChild = elementChildrenIter.next();
			
			if(nextElementChild instanceof Element) {
				Element elementChild = (Element) nextElementChild;
				
				//check the name of the child node
				if(elementChild.getName().equals(name)) {
					//child name matches the name we are looking for
					elementFound = elementChild;
					
					//we have found what we wanted so we will break out of the while loop
					break;					
				}
			}
		}
		
		return elementFound;
	}
	
	/**
	 * Get the child elements with the given name
	 * @param element the xml node
	 * @param name the name of the child nodes we are looking for
	 * @return a list of child nodes with the name we are looking for
	 */
	public ArrayList<Element> getChildElementsWithName(Element element, String name) {
		//get an iterator of the children
		Iterator elementChildrenIter = element.nodeIterator();
		ArrayList<Element> elementsFound = new ArrayList<Element>();
		
		//loop through all the children
		while(elementChildrenIter.hasNext()) {
			//get a child
			Object nextElementChild = elementChildrenIter.next();
			
			if(nextElementChild instanceof Element) {
				Element elementChild = (Element) nextElementChild;
				
				//check the name of the child node
				if(elementChild.getName().equals(name)) {
					/*
					 * child name matches the name we are looking for so we will
					 * add it to our list
					 */
					elementsFound.add(elementChild);
				}
			}
		}
		
		return elementsFound;
	}
	
	/**
	 * Parse the xml step node to create a step JSONObject
	 * @return the JSONObject for the step
	 */
	abstract protected JSONObject parseStepNode(Node stepNode);
	
	/**
	 * Get the node type
	 * @return a String containing the node type
	 */
	abstract protected String getNodeType();
	
	/**
	 * Get the step file name
	 * @param stepCounter the global step counter
	 * @return a String containing the step file name
	 */
	abstract protected String getStepFileName(int stepCounter);
	
	/**
	 * Determines the icon type for the step
	 * @return a String containing the class type
	 */
	abstract protected String getClassType();
	
	/**
	 * Get the type of the step (this is usually the same as
	 * getNodeType() except the String does not end with "Node"
	 * @return a String containing the step type
	 */
	abstract protected String getType();
}
