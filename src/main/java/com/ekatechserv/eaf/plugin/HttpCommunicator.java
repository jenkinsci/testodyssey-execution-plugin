package com.ekatechserv.eaf.plugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * HttpCommunicator a singleton class. Communicates with Test-Odyssey using
 * HttpComponent api.
 *
 * @author Eka Techserv
 */
public class HttpCommunicator {

    private static HttpCommunicator instance;
    private final CloseableHttpClient client;

    private final static String USER_AGENT = "Mozilla/5.0";

    public final static String TEST_ODYSSEY_URL = "http://test-odyssey.com";
    //  public final static String TEST_ODYSSEY_URL = "http://192.168.1.200:6060/eaf";
//    public final static String TEST_ODYSSEY_URL = "http://ekatechserv.co.in";

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static HttpCommunicator getInstance() {
        if (instance == null) {
            instance = new HttpCommunicator();
        }
        return instance;
    }

    private HttpCommunicator() {
        this.client = HttpClients.createDefault();
    }

    public HttpResponse doPost(String actionUri, List<NameValuePair> fields) throws UnsupportedEncodingException, IOException {
        HttpPost httpPost = new HttpPost(this.getUrl() + actionUri);
        httpPost.setEntity(new UrlEncodedFormEntity(fields, StandardCharsets.UTF_8));
        httpPost.addHeader("User-Agent", USER_AGENT);
        return client.execute(httpPost);
    }

    public HttpResponse doGet(String actionUri) throws IOException {
        HttpGet httpGet = new HttpGet(this.getUrl() + actionUri);
        httpGet.addHeader("User-Agent", USER_AGENT);
        return client.execute(httpGet);
    }

}
