package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Node;

/**
 * Converts a Wise 2 OTrunk step into a Wise 4 HtmlPage step
 * @author geoffreykwan
 */
public class OTrunkConverter extends HtmlPageConverter {

	/**
	 * Create the .html file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	protected void createStepHtmlFile(Node stepNode, File projectFolder, int stepCounter) {
		//create the file
		File stepHtmlFile = new File(projectFolder, getStepHtmlFileName(stepCounter));
		
		//get the jnlp url
		String launchUrl = stepNode.selectSingleNode("parameters/jnlpHref").getText();

		//create the html for the step
		StringBuffer html = new StringBuffer();
		
		html.append("<html>");
		html.append("<body>");
		
		//the button to launch the otrunk jnlp
		String button = "<input type='button' value='Launch' onclick=\"window.open('" + launchUrl + "', 'launchFrame'); this.disabled = true;\" />";
		
		/*
		 * the frame to launch the jnlp in so that the browser doesn't need to open
		 * a new tab or window
		 */
		String launchFrame = "<iframe id='launchFrame' name='launchFrame' style='display:none'></iframe>";
		
		html.append(button);
		html.append(launchFrame);
		
		html.append("</body>");
		html.append("</html>");
		
		try {
			//write the html to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepHtmlFile));
			out.write(html.toString());
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
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "curriculum";
	}

	/**
	 * This is not used by this class but still needs to implement it
	 */
	protected String getHtmlTextXMLPath() {
		return null;
	}
}
