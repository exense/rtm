package org.rtm.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.rtm.constants.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient implements Closeable{
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	private PoolingHttpClientConnectionManager cm;
	private CloseableHttpClient httpClient;
	ResponseHandler<String> responseHandler;
	
	private String hostname;
	private int port;
	private int maxConnections;

	public HttpClient(String hostname, int port){
		this(hostname, port, 50);
	}
	
	public HttpClient(String hostname, int port, int maxConnections){
		
		this.hostname = hostname;
		this.port = port;
		this.maxConnections = maxConnections;
		
		cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
		HttpHost host = new HttpHost(hostname, port);
		cm.setMaxPerRoute(new HttpRoute(host), maxConnections);

		httpClient = HttpClients.custom()
		        .setConnectionManager(cm)
		        .build();
		
		responseHandler = new ResponseHandler<String>() {

			@Override
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}

		};
	}
	
	public String call(String request, String tierPrefix, String methodPrefix) throws UnsupportedEncodingException{
		String url = buildRestURLBase(tierPrefix, methodPrefix);
		
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");
		httppost.setEntity( new StringEntity(request));
		
		try {
			return httpClient.execute(httppost, responseHandler);
		} catch (Exception e) {
			logger.error("HttpGet failed : " + httppost, e);
			throw new RuntimeException("Send failed for HttpGet : " + httppost);	
		}
	}

	private String buildRestURLBase(String tierPrefix, String methodPrefix) {
		return "http://" + hostname + ":" + port +
				GlobalConstants.rootAppContext + GlobalConstants.restPrefix +
				tierPrefix + methodPrefix;
	}


	@Override
	public void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
			logger.error("Close failed.", e);
		}		
	}


	@Override
	public String toString() {
		return "{\"hostname\""+this.hostname+", \"port\" : "+this.port+", \"maxConnections\" : "+maxConnections+"}";
	}

}
