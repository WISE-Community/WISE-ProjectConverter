package wise2.converter.converters;

/**
 * Converts a Wise 2 DisplayPage step into a Wise 4 HtmlPage step
 * @author geoffreykwan
 */
public class DisplayPageConverter extends HtmlPageConverter {
	
	/**
	 * Determines the icon type for the step
	 */
	protected String getClassType() {
		return "display";
	}
	
	/**
	 * Get the xml path to the html in the wise 2 xml node
	 */
	protected String getHtmlTextXMLPath() {
		return "parameters/html";
	}
}
