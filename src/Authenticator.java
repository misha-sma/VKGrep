import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class Authenticator {
	public static int TIMEOUT = 5000;

	public static void main(String[] args) throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = login(LoginPassword.LOGIN, LoginPassword.PASSWORD);
		List<News> newsList = new LinkedList<News>();
		for (int offset = 0; offset < 50; offset += 10) {
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Start download offset=" + offset);
			HttpPost post = new HttpPost("http://vk.com/al_feed.php?sm_news");
			post.setHeader("Referer", "http://vk.com/");
			post.setHeader("User-Agent", "User-Agent=Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("al", "1"));
			nvps.add(new BasicNameValuePair("more", "1"));
			nvps.add(new BasicNameValuePair("offset", String.valueOf(offset)));
			nvps.add(new BasicNameValuePair("part", "1"));
			nvps.add(new BasicNameValuePair("section", "news"));
			nvps.add(new BasicNameValuePair("subsection", "recent"));

			try {
				post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				ex.printStackTrace();
			}

			HttpResponse rsp = httpclient.execute(post);
			HttpEntity entity = rsp.getEntity();
			StatusLine statusLine = rsp.getStatusLine();
			int status = statusLine.getStatusCode();
			if (status != 200) {
				System.out.println("status != 200");
				return;
			}
			String html = EntityUtils.toString(entity);

			FileOutputStream output = new FileOutputStream("html/feed_" + offset + ".html");
			output.write(html.getBytes("UTF8"));
			output.close();
			System.out.println("Save feed offset=" + offset);
			newsList.addAll(Parser.parse(html));
		}
		int i = 0;
		for (News news : newsList) {
			++i;
			System.out.println(i + "; " + news.toString());
		}

		long initTime = System.currentTimeMillis();
		System.out.println("Start saving news to solr");
		SolrManager.saveNews2Solr(newsList);
		System.out.println("End saving news to solr Time=" + (System.currentTimeMillis() - initTime));
		System.out.println("ENDDDDDDDDDDDDDD!!!!!!!!!!!!!!!!!!!!!");
	}

	static public DefaultHttpClient login(String email, String password) {
		DefaultHttpClient client = createHttpClientWithCookiePolicy();
		client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		String login_page = getLoginPage(client);
		HttpPost formPost = buildFormPostRequest(login_page, email, password);
		doLogin(client, formPost);
		return client;
	}

	static private void doLogin(DefaultHttpClient client, HttpPost formPost) {
		try {
			HttpEntity entity;
			HttpResponse response = client.execute(formPost);
			EntityUtils.consume(response.getEntity());

			HttpGet get = new HttpGet(response.getHeaders("location")[0].getValue());
			get.setHeader("User-Agent", "User-Agent=Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			get.setHeader("Referer", "http://vk.com/");

			response = client.execute(get);
			entity = response.getEntity();
			EntityUtils.consume(entity);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static private HttpPost buildFormPostRequest(String login_page, String email, String pass) {
		HttpPost post = new HttpPost("http://login.vk.com");
		post.setHeader("Referer", "http://vk.com/");
		post.setHeader("User-Agent", "User-Agent=Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("act", "login"));
		nvps.add(new BasicNameValuePair("q", "1"));
		nvps.add(new BasicNameValuePair("al_frame", "1"));
		nvps.add(new BasicNameValuePair("expire", ""));
		nvps.add(new BasicNameValuePair("captcha_sid", ""));
		nvps.add(new BasicNameValuePair("captcha_key", ""));
		nvps.add(new BasicNameValuePair("from_host", "vk.com"));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("pass", pass));

		int ip_hash_begin = login_page.indexOf("<input type=\"hidden\" name=\"ip_h\" value=\"") + "<input type=\"hidden\" name=\"ip_h\" value=\"".length();
		String ip_hash = login_page.substring(ip_hash_begin, login_page.indexOf("\"", ip_hash_begin));
		nvps.add(new BasicNameValuePair("ip_h", ip_hash));
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}

		return post;
	}

	static private String getLoginPage(DefaultHttpClient client) {
		String out = null;
		try {
			HttpGet get = new HttpGet("http://vk.com/");
			get.setHeader("User-Agent", "User-Agent=Mozilla/5.0 (Windows NT 5.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			out = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	static private DefaultHttpClient createHttpClientWithCookiePolicy() {
		DefaultHttpClient client = new DefaultHttpClient() {

			@Override
			protected CookieSpecRegistry createCookieSpecRegistry() {
				CookieSpecRegistry registry = new CookieSpecRegistry();
				registry.register(CookiePolicy.BEST_MATCH, new AcceptAllSpecFactory());
				return registry;
			}
		};

		return client;
	}
}

class AcceptAllCookiesSpec extends BestMatchSpec {

	public static final String ID = "_acceptAllCookies";

	public AcceptAllCookiesSpec(String[] patterns, boolean singleHeader) {
		super(patterns, singleHeader);
	}

	public AcceptAllCookiesSpec() {
	}

	public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
		try {
			super.validate(cookie, origin);
		} catch (MalformedCookieException e) {
			if (!e.getMessage().contains("Domain of origin")) {
				throw e;
			}
		}
		System.out.println("cookie validation was passed " + cookie);
	}
}

class AcceptAllSpecFactory implements CookieSpecFactory {

	public CookieSpec newInstance(final HttpParams params) {
		if (params != null) {
			String[] patterns = null;
			Collection<?> param = (Collection<?>) params.getParameter(CookieSpecPNames.DATE_PATTERNS);
			if (param != null) {
				patterns = new String[param.size()];
				patterns = param.toArray(patterns);
			}
			boolean singleHeader = params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false);
			return new AcceptAllCookiesSpec(patterns, singleHeader);
		} else {
			return new AcceptAllCookiesSpec();
		}
	}
}
