import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class SolrManager {
	public static final String SOLR_URL = "http://localhost:8983/solr";

	public static void saveNews2Solr(List<News> newsList) {
		List<SolrInputDocument> docs = new LinkedList<SolrInputDocument>();
		for (News news : newsList) {
			SolrInputDocument doc = news.getSolrInputDocument();
			docs.add(doc);
		}
		SolrServer server = new HttpSolrServer(SOLR_URL);
		try {
			server.add(docs);
			server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
