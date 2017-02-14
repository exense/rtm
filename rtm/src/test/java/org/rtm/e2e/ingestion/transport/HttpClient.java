package org.rtm.e2e.ingestion.transport;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.rtm.commons.MeasurementUtils;
import org.rtm.rest.GlobalConstants;
import org.rtm.rest.ingestion.IngestionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient implements TransportClient {
	
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
	
	@Override
	public String sendStructuredMeasurement(Map<String, Object> measurement) throws TransportException {
		String url = buildRestURLBase() + MeasurementUtils.mapToURI(measurement);
		
		HttpGet httpget = new HttpGet(url);
		logger.debug("Sending measurement : " + url);
		
		try {
			return httpClient.execute(httpget, responseHandler);
		} catch (Exception e) {
			logger.error("HttpGet failed : " + httpget, e);
			throw new TransportException("Send failed for HttpGet : " + httpget);	
		}
	}

	private String buildRestURLBase() {
		return "http://" + hostname + ":" + port +
				GlobalConstants.rootAppContext + GlobalConstants.restPrefix +
				IngestionConstants.servletPrefix + IngestionConstants.structuredPrefix;
	}

	@Override
	public String sendStructuredMeasurement(String json) throws TransportException {
		try {
			return sendStructuredMeasurement(MeasurementUtils.stringToMap(json));
		} catch (IOException e) {
			logger.error("Send failed.", e);
			return null;
		}
	}

	@Override
	public String sendGenericMeasurement(Map<String, Object> measurement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sendGenericMeasurement(String json) {
		// TODO Auto-generated method stub
		return null;
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
	public boolean isSuccessful(String response) {
		return response.contains("SUCCESS");
	}
	
	@Override
	public String toString() {
		return "{\"hostname\""+this.hostname+", \"port\" : "+this.port+", \"maxConnections\" : "+maxConnections+"}";
	}

}
