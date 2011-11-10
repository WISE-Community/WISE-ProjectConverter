package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Node;
import org.json.JSONObject;

/**
 * Converts a Wise 2 Bookmark step into a Wise 4 HtmlPage step  
 * @author geoffreykwan
 */
public class BookmarksConverter extends HtmlPageConverter {

	/**
	 * Create the html file for the step
	 * @param stepNode the xml step node
	 * @param projectFolder the folder we are creating the wise 4 project in
	 * @param stepCounter the global counter for all the steps in the project 
	 */
	protected void createStepHtmlFile(Node stepNode, File projectFolder, int stepCounter) {
		//create the file handle
		File stepHtmlFile = new File(projectFolder, getStepHtmlFileName(stepCounter));
		
		/*
		 * get the other data
		 * e.g.
		 * <otherData>a:2:{s:4:"html";s:763:"<p>Now that you have learned about the genetics of CF, use the web to research different genetic disorders.</p>";s:3:"url";s:14:"www.google.com";}</otherData>
		 */
		Node otherDataNode = stepNode.selectSingleNode("otherData");
		String otherDataText = otherDataNode.getText();
		
		/*
		 * get the text between the curly braces
		 * e.g.
		 * s:4:"html";s:763:"<p>Now that you have learned about the genetics of CF, use the web to research different genetic disorders.</p>";s:3:"url";s:14:"www.google.com";
		 */
		int beginIndex = otherDataText.indexOf('{');
		int endIndex = otherDataText.lastIndexOf('}');
		String otherDataTextSubstring = otherDataText.substring(beginIndex + 1, endIndex);
		
		/*
		 * split the text at ';'
		 * e.g.
		 * s:4:"html"
		 * s:763:"<p>Now that you have learned about the genetics of CF, use the web to research different genetic disorders.</p>"
		 * s:3:"url"
		 * s:14:"www.google.com"
		 */
		String[] semiColonSplit = otherDataTextSubstring.split(";");
		
		String html = "";
		String url = "";
		
		//will hold the previous value in quotes
		String lastValue = "";
		
		//loop through all the strings between the ';'s
		for(int x=0; x<semiColonSplit.length; x++) {
			//get a split
			String splitSection = semiColonSplit[x];
			
			//get the text between the quotes
			int beginQuote = splitSection.indexOf("\"");
			int endQuote = splitSection.lastIndexOf("\"");
			String quotedText = splitSection.substring(beginQuote + 1, endQuote);
			
			//the last value of the qoutes determines what this string is
			if(lastValue.equals("html")) {
				//last quoted value was "html" so this is the html field
				html = quotedText;
			} else if(lastValue.equals("url")) {
				//last quoted value was "url" so this is the url field
				if(!quotedText.startsWith("http://")) {
					//add 'http://' if it does not already start with it
					quotedText = "http://" + quotedText;
				}
				
				url = quotedText;
			}
			
			//remember the last value
			lastValue = quotedText;
		}
		
		//wrap the html in html tags and make a link to the url at the bottom
		html = "<html><head></head>" + html + "<br><a href='" + url + "'>" + url + "</a><html>";
		
		try {
			//write the .project contents to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepHtmlFile));
			out.write(html);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			//make the file on disk
			stepHtmlFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "display";
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
	protected String getHtmlTextXMLPath() {
		return null;
	}

}
