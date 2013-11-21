import org.apache.commons.lang3.StringEscapeUtils;

public class Util {
	public static String unEscapeHtml(String text) {
		text = StringEscapeUtils.unescapeHtml4(text);
		text = text.replace("<br>", "\n");
		return text;
	}
}
