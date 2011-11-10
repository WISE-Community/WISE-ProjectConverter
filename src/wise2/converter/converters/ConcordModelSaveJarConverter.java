package wise2.converter.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Node;
import org.json.JSONObject;

/**
 * Converts a Wise 2 ConcordModelSaveJar step into a Wise 4 HtmlPage step
 * @author geoffreykwan
 */
public class ConcordModelSaveJarConverter extends HtmlPageConverter {
	
	//the wise 2 project id
	private String projectId = "";

	/**
	 * Create the html file
	 * @param stepNode the xml step node
	 * @param projectFolder the wise 4 project folder
	 * @param stepCounter the global step counter
	 */
	protected void createStepHtmlFile(Node stepNode, File projectFolder, int stepCounter) {
		//create the file we will write the html to
		File stepHtmlFile = new File(projectFolder, getStepHtmlFileName(stepCounter));
		
		StringBuffer html = new StringBuffer();
		
		//get the attributes from the xml
		String codebase = stepNode.selectSingleNode("parameters/codebase").getText();
		String saveJar = stepNode.selectSingleNode("parameters/saveJar").getText();
		String htmlHead = stepNode.selectSingleNode("parameters/htmlHead").getText();
		String htmlIntro = stepNode.selectSingleNode("parameters/htmlIntro").getText();
		//String buttonSubmit = stepNode.selectSingleNode("parameters/buttonSubmit").getText();

		//make the html
		html.append("<html>");
		html.append("<head>");
		html.append("<title>Pedagogica step in project 19530</title>");
		html.append("<script language='Javascript' type='text/javascript'>");
		html.append("function launchJnlp(button, jnlpHref) {");
		html.append("  top.frames['hiddenFrames'].frames['scratchFrame'].location.href = jnlpHref;");
		html.append("  button.disabled = true;");
		html.append("}");
		html.append("</script>");
		html.append("<STYLE TYPE='text/css'>");
		html.append(htmlHead);
		html.append("</STYLE></head>");
		html.append("<body>");
		html.append(htmlIntro);
		html.append("<hr>");
		
		//make the button that will launch the jnlp
		//String button = "<button onClick=\"launchJnlp(this, 'http://wise-dev.berkeley.edu/modules/pedagogica/webstart/startActivity.php?codebase=" + codebase + "&saveJar=" + saveJar + "&projectID=19530')\">Launch</button>";
		String button = "<input type='button' value='Launch' onclick=\"window.open('http://wise.berkeley.edu/modules/pedagogica/webstart/startActivity.php?codebase=" + codebase + "&saveJar=" + saveJar + "&projectID=" + getProjectId() + "', 'launchFrame'); this.disabled = true;\" />";

		//create a frame for the step to open the jnlp in so the browser doesn't need to open a new tab
		String launchFrame = "<iframe id='launchFrame' name='launchFrame' style='display:none'></iframe>";
		
		html.append(button);
		html.append(launchFrame);
		
		html.append("</body>");
		html.append("</html>");

		try {
			//write the .project contents to the actual file
			BufferedWriter out = new BufferedWriter(new FileWriter(stepHtmlFile));
			out.write(html.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			//write the file to disk
			stepHtmlFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is not used by this class but still needs to implement it
	 */
	protected String getHtmlTextXMLPath() {
		return null;
	}

	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "curriculum";
	}

	/**
	 * Set the wise 2 project id which we will use when we create the html
	 * @param projectId
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Get the wise 2 project id
	 * @return the wise 2 project id as a String
	 */
	public String getProjectId() {
		return projectId;
	}

}
