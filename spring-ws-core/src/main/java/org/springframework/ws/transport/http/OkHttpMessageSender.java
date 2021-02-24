package org.springframework.ws.transport.http;

import java.net.URI;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

public class OkHttpMessageSender extends AbstractHttpWebServiceMessageSender {

	private static final MediaType DEFAULT_REQUEST_MEDIA_TYPE = MediaType.parse("text/xml; charset=utf-8");

	private final OkHttpClient okHttpClient;
	private final MediaType requestMediaType;

	public OkHttpMessageSender(OkHttpClient okHttpClient) {
		this(okHttpClient, DEFAULT_REQUEST_MEDIA_TYPE);
	}

	public OkHttpMessageSender(OkHttpClient okHttpClient, MediaType requestMediaType) {
		Assert.notNull(okHttpClient, "okHttpClient must not be null");
		Assert.notNull(requestMediaType, "requestMediaType must not be null");

		this.okHttpClient = okHttpClient;
		this.requestMediaType = requestMediaType;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) {
		return new OkHttpConnection(okHttpClient, uri, requestMediaType);
	}

}
