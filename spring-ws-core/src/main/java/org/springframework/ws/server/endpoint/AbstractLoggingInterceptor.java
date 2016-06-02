/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Abstract base class for {@code EndpointInterceptor} instances that log a part of a
 * {@code WebServiceMessage}. By default, both request and response messages are logged, but this behaviour can be
 * changed using the {@code logRequest} and {@code logResponse} properties.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractLoggingInterceptor extends TransformerObjectSupport implements EndpointInterceptor {

	/**
	 * The default {@code Log} instance used to write trace messages. This instance is mapped to the implementing
	 * {@code Class}.
	 */
	protected transient Log logger = LogFactory.getLog(getClass());

	private boolean logRequest = true;

	private boolean logResponse = true;

	/** Indicates whether the request should be logged. Default is {@code true}. */
	public final void setLogRequest(boolean logRequest) {
		this.logRequest = logRequest;
	}

	/** Indicates whether the response should be logged. Default is {@code true}. */
	public final void setLogResponse(boolean logResponse) {
		this.logResponse = logResponse;
	}

	/**
	 * Set the name of the logger to use. The name will be passed to the underlying logger implementation through
	 * Commons Logging, getting interpreted as log category according to the logger's configuration.
	 *
	 * <p>This can be specified to not log into the category of a class but rather into a specific named category.
	 *
	 * @see org.apache.commons.logging.LogFactory#getLog(String)
	 * @see org.apache.log4j.Logger#getLogger(String)
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public void setLoggerName(String loggerName) {
		this.logger = LogFactory.getLog(loggerName);
	}

	/**
	 * Logs the request message payload. Logging only occurs if {@code logRequest} is set to {@code true},
	 * which is the default.
	 *
	 * @param messageContext the message context
	 * @return {@code true}
	 * @throws TransformerException when the payload cannot be transformed to a string
	 */
	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws TransformerException {
		if (logRequest && isLogEnabled()) {
			logMessageSource("Request: ", getSource(messageContext.getRequest()));
		}
		return true;
	}

	/**
	 * Logs the response message payload. Logging only occurs if {@code logResponse} is set to {@code true},
	 * which is the default.
	 *
	 * @param messageContext the message context
	 * @return {@code true}
	 * @throws TransformerException when the payload cannot be transformed to a string
	 */
	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		if (logResponse && isLogEnabled()) {
			logMessageSource("Response: ", getSource(messageContext.getResponse()));
		}
		return true;
	}

	/** Does nothing by default. Faults are not logged. */
	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/** Does nothing by default*/
	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
	}

	/**
	 * Determine whether the {@link #logger} field is enabled.
	 *
	 * <p>Default is {@code true} when the "debug" level is enabled. Subclasses can override this to change the level
	 * under which logging occurs.
	 */
	protected boolean isLogEnabled() {
		return logger.isDebugEnabled();
	}

	private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
		Transformer transformer = createTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		return transformer;
	}

	/**
	 * Logs the given {@link Source source} to the {@link #logger}, using the message as a prefix.
	 *
	 * <p>By default, this message creates a string representation of the given source, and delegates to {@link
	 * #logMessage(String)}.
	 *
	 * @param logMessage the log message
	 * @param source	 the source to be logged
	 * @throws TransformerException in case of errors
	 */
	protected void logMessageSource(String logMessage, Source source) throws TransformerException {
		if (source != null) {
			Transformer transformer = createNonIndentingTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(source, new StreamResult(writer));
			String message = logMessage + writer.toString();
			logMessage(message);
		}
	}

	/**
	 * Logs the given string message.
	 *
	 * <p>By default, this method uses a "debug" level of logging. Subclasses can override this method to change the level
	 * of logging used by the logger.
	 *
	 * @param message the message
	 */
	protected void logMessage(String message) {
		logger.debug(message);
	}

	/**
	 * Abstract template method that returns the {@code Source} for the given {@code WebServiceMessage}.
	 *
	 * @param message the message
	 * @return the source of the message
	 */
	protected abstract Source getSource(WebServiceMessage message);
}
