package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.ws.transport.WebServiceConnection;

public class JdkClientMessageSender extends AbstractHttpWebServiceMessageSender{

	private Duration connectionTimeout = Duration.ofSeconds(60);
	private Duration readTimeout = Duration.ofSeconds(60);

	private final HttpClient client;

	public JdkClientMessageSender() {
		this(HttpClient.newHttpClient());
	}

	public JdkClientMessageSender(HttpClient client) {
		this.client = client;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {

		Builder requestBuilder = HttpRequest.newBuilder(uri);
		JdkClientConnection connection = new JdkClientConnection(this.client, uri);
		if (isAcceptGzipEncoding()) {
			connection.addRequestHeader(
					HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return connection;
	}
}
