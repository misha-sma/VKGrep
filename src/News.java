public class News {
	public String author;
	public String text;
	public int time;

	public News(String author, String text, int time) {
		this.author = author;
		this.text = text;
		this.time = time;
	}

	@Override
	public String toString() {
		return time + "; " + author + "; " + text;
	}
}
