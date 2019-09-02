package com.ecwid.consul.transport;

import com.ecwid.consul.Utils;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class AbstractHttpTransport implements HttpTransport {

	static final int DEFAULT_MAX_CONNECTIONS = 1000;
	static final int DEFAULT_MAX_PER_ROUTE_CONNECTIONS = 500;
	static final int DEFAULT_CONNECTION_TIMEOUT = 10000; // 10 sec

	// 10 minutes for read timeout due to blocking queries timeout
	// https://www.consul.io/api/index.html#blocking-queries
	static final int DEFAULT_READ_TIMEOUT = 60000 * 10; // 10 min

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	@Override
	public HttpResponse makeGetRequest(String url, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(url);

		addHeadersToRequest(httpGet, headers);

		return executeRequest(httpGet);
	}

	@Override
	public HttpResponse makePutRequest(String url, String content, Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(url);
		httpPut.setEntity(new StringEntity(content, UTF_8));

		addHeadersToRequest(httpPut, headers);

		return executeRequest(httpPut);
	}

	@Override
	public HttpResponse makePutRequest(String url, byte[] content, Map<String, String> headers) {
		HttpPut httpPut = new HttpPut(url);
		httpPut.setEntity(new ByteArrayEntity(content));
		addHeadersToRequest(httpPut, headers);
		return executeRequest(httpPut);
	}

	@Override
	public HttpResponse makeDeleteRequest(String url, Map<String, String> headers) {
		HttpDelete httpDelete = new HttpDelete(url);
		addHeadersToRequest(httpDelete, headers);
		return executeRequest(httpDelete);
	}

	protected abstract HttpClient getHttpClient();

	private HttpResponse executeRequest(HttpUriRequest httpRequest) {
		try {
			return getHttpClient().execute(httpRequest, new ResponseHandler<HttpResponse>() {
				@Override
				public HttpResponse handleResponse(org.apache.http.HttpResponse response) throws IOException {
					int statusCode = response.getStatusLine().getStatusCode();
					String statusMessage = response.getStatusLine().getReasonPhrase();

					String content = EntityUtils.toString(response.getEntity(), UTF_8);

					Long consulIndex = parseUnsignedLong(response.getFirstHeader("X-Consul-Index"));
					Boolean consulKnownLeader = parseBoolean(response.getFirstHeader("X-Consul-Knownleader"));
					Long consulLastContact = parseUnsignedLong(response.getFirstHeader("X-Consul-Lastcontact"));

					return new HttpResponse(statusCode, statusMessage, content, consulIndex, consulKnownLeader, consulLastContact);
				}
			});
		} catch (IOException e) {
			throw new TransportException(e);
		}
	}

	private Long parseUnsignedLong(Header header) {
		if (header == null) {
			return null;
		}

		String value = header.getValue();
		if (value == null) {
			return null;
		}

		try {
			return Utils.parseUnsignedLong(value);
		} catch (Exception e) {
			return null;
		}
	}

	private Boolean parseBoolean(Header header) {
		if (header == null) {
			return null;
		}

		if ("true".equals(header.getValue())) {
			return true;
		}

		if ("false".equals(header.getValue())) {
			return false;
		}

		return null;
	}

	private void addHeadersToRequest(HttpRequestBase request, Map<String, String> headers) {
		if (headers == null) {
			return;
		}

		for (Map.Entry<String, String> headerValue : headers.entrySet()) {
			String name = headerValue.getKey();
			String value = headerValue.getValue();

			request.addHeader(name, value);
		}
	}

}
