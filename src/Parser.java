import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Parser {
	public static List<News> parse(String html) {
		String[] newsArray = html.split("</table><div class=\"wall_post_text\">");
		List<News> newsList = new LinkedList<News>();
		for (int i = 1; i < newsArray.length; ++i) {
			System.out.println("i=" + i);
			String news = newsArray[i];
			int divIndex = news.indexOf("</div>");
			if (divIndex == -1) {
				System.out.println("Cant parse divIndex!!!");
				System.out.println("news=" + news);
				continue;
			}
			String text = news.substring(0, divIndex);

			int time = -1;
			int timeIndex = news.indexOf("<span class=\"rel_date rel_date_needs_update\"");
			if (timeIndex == -1) {
				timeIndex = news.indexOf("<span class=\"rel_date\">");
				if (timeIndex == -1) {
					System.out.println("Cant parse timeIndex!!!");
					System.out.println("news=" + news);
					continue;
				}
				String timeStr = parseForPrefixWithDelimeter(news.substring(timeIndex), ">", "<");
				if (timeStr.startsWith("сегодня в")) {
					timeStr = timeStr.substring(9).trim();
					String[] parts = timeStr.split(":");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
					calendar.set(Calendar.SECOND, 0);
					time = (int) (calendar.getTimeInMillis() / 1000);
				} else if (timeStr.startsWith("вчера в")) {
					timeStr = timeStr.substring(7).trim();
					String[] parts = timeStr.split(":");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
					calendar.set(Calendar.SECOND, 0);
					time = (int) (calendar.getTimeInMillis() / 1000) - 3600 * 24;
				} else {
					System.out.println("ACHTUNG!!! time=" + timeStr);
				}
			} else {
				String timeStr = parseForPrefixWithDelimeter(news.substring(timeIndex), "\" time=\"", "\"");
				if (timeStr == null) {
					System.out.println("Cant parse timeStr!!!");
					System.out.println("news=" + news);
					continue;
				}
				time = Integer.parseInt(timeStr);
			}

			news = newsArray[i - 1];
			int authorIndex = news.lastIndexOf("<a class=\"author\"");
			if (authorIndex == -1) {
				System.out.println("Cant parse authorIndex!!!");
				System.out.println("news=" + news);
				continue;
			}
			String author = parseForPrefixWithDelimeter(news.substring(authorIndex), ">", "</a></div>");
			if (author == null) {
				System.out.println("Cant parse author!!!");
				System.out.println("news=" + news);
				continue;
			}

			newsList.add(new News(author, text, time));
		}
		return newsList;
	}

	private static String parseForPrefixWithDelimeter(String text, String prefix, String delimeter) {
		int prefixIndex = text.indexOf(prefix);
		if (prefixIndex == -1) {
			return null;
		}
		int delimeterIndex = text.indexOf(delimeter, prefixIndex + prefix.length());
		if (delimeterIndex == -1) {
			return null;
		}
		return text.substring(prefixIndex + prefix.length(), delimeterIndex);
	}
}
