package wise2.converter.converters;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts a Wise 2 Wisedraw2 step into a Wise 4 SVGDrawNode step
 * @author geoffreykwan
 */
public class Wisedraw2Converter extends Converter {

	/**
	 * Parse the xml step node to create a step JSONObject
	 * @param stepNode the xml step node
	 * @return the JSONObject for the step
	 */
	protected JSONObject parseStepNode(Node stepNode) {
		JSONObject stepNodeJSONObject = new JSONObject();
		
		//get the prompt
		String prompt = stepNode.selectSingleNode("parameters/html").getText();
		
		JSONArray stamps = new JSONArray();
		
		//get the stamps
		List stampNodes = stepNode.selectNodes("parameters/stamps");
		Iterator stampsIter = stampNodes.iterator();

		//loop through all the stamps
		while(stampsIter.hasNext()) {
			//get a stamp
			Node stampNode = (Node)stampsIter.next();
			
			//get the path of the stamp
			Node stampText = stampNode.selectSingleNode("XML_Serializer_Tag");
			
			if(stampText != null) {
				//get the path text
				String stampPath = stampText.getText();
				
				Image image = null;
				URL url;
				try {
					//retrieve the image from the url path
					url = new URL(stampPath);
					image = ImageIO.read(url);
					
					//create a JLabel so we can find the dimensions of the stamp
					JLabel jLabel = new JLabel();
					
					//get the dimensions
					int width = image.getWidth(jLabel);
					int height = image.getHeight(jLabel);
					
					//get the last '/' so we can get the file name
					int lastSlash = stampPath.lastIndexOf('/');
					
					String title = "";
					
					if(lastSlash != -1) {
						//use the stamp file name as the title
						title = stampPath.substring(lastSlash + 1);
					}
					
					JSONObject stamp = new JSONObject();
					
					//set the attributes of the stamp
					stamp.put("title", title);
					stamp.put("uri", stampPath);
					stamp.put("width", width);
					stamp.put("height", height);
					
					//add the stamp to the array
					stamps.put(stamp);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		String background = "";
		
		//get the background node
		Node backgroundNode = stepNode.selectSingleNode("parameters/backgrounds");
		
		if(backgroundNode != null) {
			//get the background node as xml e.g. <backgrounds><grid99>grid99>http://wise-dev.berkeley.edu/upload/16965/grid_pos_vel99.GIF</grid99></backgrounds>
			String backgroundNodeXML = backgroundNode.asXML();
			
			//create a regular expression to extract the background image path
			Pattern backgroundPattern = Pattern.compile("<backgrounds>.*<.*>(.*)<.*>.*</backgrounds>", Pattern.DOTALL);
			
			//run the matcher
			Matcher matcher = backgroundPattern.matcher(backgroundNodeXML);
			boolean matchFound = matcher.find(); 
			String backgroundPath = "";
			
			if(matchFound) {
				//extract the first group that was found, 0 is the whole string, and 1 is the first captured group
				backgroundPath = matcher.group(1);
			}
			
			Image image = null;
			URL url;
			try {
				//grab the image so we can determine the dimensions
				url = new URL(backgroundPath);
				image = ImageIO.read(url);
				
				/*
				 * set where to place the image (2, 2) will be in the upper left with a little bit
				 * of space from the edge 
				 */
				int x = 2;
				int y = 2;
				
				//get the dimensions of the image
				JLabel jLabel = new JLabel();
				int width = image.getWidth(jLabel);
				int height = image.getHeight(jLabel);
				
				//create the background
				background = "<svg xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 600 450'><g><title>teacher</title><image xlink:href='" + backgroundPath + "' id='svg_1' height='" + height + "' width='" + width + "' y='" + y + "' x='" + x + "'/></g></svg>";
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			//set the attributes of the step
			stepNodeJSONObject.put("description_active", true);
			stepNodeJSONObject.put("description_default", "");
			stepNodeJSONObject.put("prompt", prompt);
			stepNodeJSONObject.put("snapshots_active", false);
			stepNodeJSONObject.put("stamps", stamps);
			stepNodeJSONObject.put("svg_background", background);
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
		return "quickdraw";
	}

	/**
	 * Get the node type
	 */
	protected String getNodeType() {
		return "SVGDrawNode";
	}

	/**
	 * Get the step file name
	 */
	protected String getStepFileName(int stepCounter) {
		return "node_" + stepCounter + ".sd";
	}

	/**
	 * Get the step type
	 */
	protected String getType() {
		return "SVGDraw";
	}

}
