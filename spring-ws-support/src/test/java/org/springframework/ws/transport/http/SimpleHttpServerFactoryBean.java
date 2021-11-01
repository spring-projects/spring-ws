package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Utility class ONLY used for testing SOAP interactions through the JRE's built-in {@link HttpServer}.
 */
class SimpleHttpServerFactoryBean implements FactoryBean<HttpServer>, InitializingBean, DisposableBean {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private int port = 8080;

	private String hostname;

	private int backlog = -1;

	private int shutdownDelay = 0;

	private Executor executor;

	private Map<String, HttpHandler> contexts;

	private List<Filter> filters;

	private Authenticator authenticator;

	private HttpServer server;

	/**
	 * Specify the HTTP server's port. Default is 8080.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Specify the HTTP server's hostname to bind to. Default is localhost; can be overridden with a specific network
	 * address to bind to.
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Specify the HTTP server's TCP backlog. Default is -1, indicating the system's default value.
	 */
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	/**
	 * Specify the number of seconds to wait until HTTP exchanges have completed when shutting down the HTTP server.
	 * Default is 0.
	 */
	public void setShutdownDelay(int shutdownDelay) {
		this.shutdownDelay = shutdownDelay;
	}

	/**
	 * Set the JDK concurrent executor to use for dispatching incoming requests.
	 *
	 * @see HttpServer#setExecutor
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Register {@link HttpHandler HttpHandlers} for specific context paths.
	 *
	 * @param contexts a Map with context paths as keys and HttpHandler objects as values
	 */
	public void setContexts(Map<String, HttpHandler> contexts) {
		this.contexts = contexts;
	}

	/**
	 * Register common {@link Filter Filters} to be applied to all locally registered {@link #setContexts contexts}.
	 */
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	/**
	 * Register a common {@link Authenticator} to be applied to all locally registered {@link #setContexts contexts}.
	 */
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		InetSocketAddress address = (this.hostname != null ? new InetSocketAddress(this.hostname, this.port)
				: new InetSocketAddress(this.port));
		this.server = HttpServer.create(address, this.backlog);
		if (this.executor != null) {
			this.server.setExecutor(this.executor);
		}
		if (this.contexts != null) {
			this.contexts.forEach((key, context) -> {
				HttpContext httpContext = this.server.createContext(key, context);
				if (this.filters != null) {
					httpContext.getFilters().addAll(this.filters);
				}
				if (this.authenticator != null) {
					httpContext.setAuthenticator(this.authenticator);
				}
			});
		}
		if (logger.isInfoEnabled()) {
			logger.info("Starting HttpServer at address " + address);
		}
		this.server.start();
	}

	@Override
	public HttpServer getObject() {
		return this.server;
	}

	@Override
	public Class<? extends HttpServer> getObjectType() {
		return (this.server != null ? this.server.getClass() : HttpServer.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() {
		logger.info("Stopping HttpServer");
		this.server.stop(this.shutdownDelay);
	}
}
