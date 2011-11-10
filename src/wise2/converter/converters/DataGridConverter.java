package wise2.converter.converters;

import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 DataGrid step into a Wise 4 DataGraph step
 * @author geoffreykwan
 */
public class DataGridConverter extends Converter {

	/**
	 * Get the JSONObject that we will put into the step. Since wise 2
	 * does not provide any data in the export, we are just going to
	 * create an empty data graph step
	 * @param stepNode the xml step node
	 * @return the step JSONObject
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		try {
			//make the prompt with an empty string
			String prompt = "";
			
			//make the display with default values
			JSONObject display = new JSONObject();
			display.put("start", "0");
			display.put("which", "2");
			
			//make the graph with default values
			JSONObject graph = new JSONObject();
			graph.put("bar", true);
			graph.put("line", true);
			graph.put("linePoint", true);
			graph.put("point", true);
			graph.put("range", true);
			
			//make the options with default values
			JSONObject options = new JSONObject();
			options.put("display", display);
			options.put("graph", graph);
			
			//make the table with default values
			JSONObject table = new JSONObject();
			table.put("graphHeight", 573);
			table.put("graphWidth", 800);
			table.put("independentIndex", -1);
			table.put("isQualitative", false);
			table.put("rows", new JSONArray());
			table.put("title", "");
			table.put("titleEditable", true);
			table.put("titleIndex", -1);
			table.put("xLabel", "");
			table.put("xLabelEditable", true);
			table.put("yLabel", "");
			table.put("yLabelEditable", true);
			
			//set the attributes into the step JSONObject
			stepNodeJSONObject.put("options", options);
			stepNodeJSONObject.put("prompt", prompt);
			stepNodeJSONObject.put("table", table);
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
		return "datatable";
	}
	
	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "DataGraphNode";
	}
	
	/**
	 * Get the name of the file we will write the JSON to
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".dg";
	}

	/**
	 * Get the type that will be an attribute in the step JSON
	 */
	protected String getType() {
		return "DataGraph";
	}

}
