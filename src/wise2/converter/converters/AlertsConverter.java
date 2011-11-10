package wise2.converter.converters;

/**
 * Converts a Wise 2 Alert step into a Wise 4 HtmlPage step 
 * @author geoffreykwan
 */
public class AlertsConverter extends HtmlPageConverter {

	/**
	 * Determines the icon type for the step
	 */
	@Override
	protected String getClassType() {
		return "display";
	}

	/**
	 * This is the path in the Wise 2 xml project file where we will
	 * find the html for the step
	 */
	@Override
	protected String getHtmlTextXMLPath() {
		return "parameters/alertText";
	}

}
