import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrInputDocument;

public class News {
	@Field
	public String id;
	@Field
	public String author;
	@Field
	public String text;
	@Field
	public int time;

	public News(String author, String text, int time) {
		this.id = UUID.randomUUID().toString();
		this.author = author;
		this.text = text;
		this.time = time;
	}

	@Override
	public String toString() {
		return id + "; " + time + "; " + author + "; " + text;
	}

	public SolrInputDocument getSolrInputDocument() {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", id);
		doc.addField("author", author);
		doc.addField("text", text);
		doc.addField("time", time);
		return doc;
	}
}
