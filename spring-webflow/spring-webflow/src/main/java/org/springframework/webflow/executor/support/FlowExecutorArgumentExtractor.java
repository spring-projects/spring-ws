package org.springframework.webflow.executor.support;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.JdkVersion;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ParameterMap;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * A simple helper for extracting {@link FlowExecutor} method input arguments
 * from an request initiated by an {@link ExternalContext}. After request
 * processing, this class is also responsible for supporting response generation
 * by provisioning context attributes necessary to execute subsequent callbacks
 * into Spring Web Flow.
 * <p>
 * By default, this class extracts flow executor method arguments from the
 * {@link ExternalContext#getRequestParameterMap()}.
 * 
 * @author Keith Donald
 */
public class FlowExecutorArgumentExtractor {

	// data and behavior related to request argument extraction

	/**
	 * By default clients can send the id of the flow definition to be launched
	 * using a parameter with this name ("_flowId").
	 */
	private static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * By default clients can send the key of a flow execution to be resumed
	 * using a parameter with this name ("_flowExecutionKey").
	 */
	private static final String FLOW_EXECUTION_KEY_PARAMETER = "_flowExecutionKey";

	/**
	 * By default clients can send the event to be signaled in a parameter with
	 * this name ("_eventId").
	 */
	private static final String EVENT_ID_PARAMETER = "_eventId";

	/**
	 * The default delimiter used when a parameter value is encoed as part of
	 * the name of an event parameter (e.g. "_eventId_submit").
	 * <p>
	 * This form is typically used to support multiple HTML buttons on a form
	 * without resorting to Javascript to communicate the event that corresponds
	 * to a button.
	 */
	private static final String PARAMETER_VALUE_DELIMITER = "_";

	/**
	 * Identifies a flow definition to launch a new execution for, defaults to
	 * {@link #FLOW_ID_PARAMETER}.
	 */
	private String flowIdParameterName = FLOW_ID_PARAMETER;

	/**
	 * The default flowId value that will be returned if no flowId parameter
	 * value can be extracted during {@link #extractFlowId(ExternalContext)}
	 * operation.
	 */
	private String defaultFlowId;

	/**
	 * Input parameter that identifies an existing flow execution to participate
	 * in, defaults to {@link #FLOW_EXECUTION_KEY_PARAMETER }.
	 */
	private String flowExecutionKeyParameterName = FLOW_EXECUTION_KEY_PARAMETER;

	/**
	 * Identifies an event that occured in an existing flow execution, defaults
	 * to ("_eventId_submit").
	 */
	private String eventIdParameterName = EVENT_ID_PARAMETER;

	/**
	 * The embedded parameter name/value delimiter value, used to parse a
	 * parameter value when a value is embedded in a parameter name (e.g.
	 * "_eventId_bar").
	 */
	private String parameterDelimiter = PARAMETER_VALUE_DELIMITER;

	/**
	 * Returns the flow id parameter name, used to request a flow to launch.
	 */
	public String getFlowIdParameterName() {
		return flowIdParameterName;
	}

	/**
	 * Sets the flow id parameter name, used to request a flow to launch.
	 */
	public void setFlowIdParameterName(String flowIdParameterName) {
		this.flowIdParameterName = flowIdParameterName;
	}

	/**
	 * Returns the <i>default</i> flowId parameter value. If no flow id
	 * parameter is provided, the default acts as a fallback.
	 */
	public String getDefaultFlowId() {
		return defaultFlowId;
	}

	/**
	 * Sets the default flowId parameter value.
	 * <p>
	 * This value will be used if no flowId parameter value can be extracted
	 * from the request during {@link #extractFlowId(ExternalContext)}
	 * operation.
	 */
	public void setDefaultFlowId(String defaultFlowId) {
		this.defaultFlowId = defaultFlowId;
	}

	/**
	 * Returns the flow execution key parameter name, used to request that an
	 * executing conversation resume at a specific point in time.
	 */
	public String getFlowExecutionKeyParameterName() {
		return flowExecutionKeyParameterName;
	}

	/**
	 * Sets the flow execution key parameter name, used to request that an
	 * executing conversation resume at a specific point in time.
	 */
	public void setFlowExecutionKeyParameterName(String flowExecutionIdParameterName) {
		this.flowExecutionKeyParameterName = flowExecutionIdParameterName;
	}

	/**
	 * Returns the event id parameter name, used to signal what user action
	 * happened within a paused flow execution.
	 */
	public String getEventIdParameterName() {
		return eventIdParameterName;
	}

	/**
	 * Sets the event id parameter name, used to signal what user action
	 * happened within a paused flow execution.
	 */
	public void setEventIdParameterName(String eventIdParameterName) {
		this.eventIdParameterName = eventIdParameterName;
	}

	/**
	 * Returns true if the flow id is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isFlowIdPresent(ExternalContext context) {
		return context.getRequestParameterMap().contains(flowIdParameterName);
	}

	/**
	 * Extracts the flow id from the external context, falling back on the
	 * defaultFlowId if flow id could be extracted.
	 * @param context the context in which a external user event occurred
	 * @return the extracted flow id
	 * @throws FlowExecutorArgumentExtractionException if the flow id could not
	 * be extracted and no default value was set
	 */
	public String extractFlowId(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String flowId = context.getRequestParameterMap().get(flowIdParameterName);
		if (flowId == null) {
			flowId = defaultFlowId;
		}
		if (!StringUtils.hasText(flowId)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the flowId argument: make sure the client provides the '" + flowIdParameterName
							+ "' parameter as input or set the 'defaultFlowId' property; "
							+ "the parameters provided in this request are: "
							+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return flowId;
	}

	/**
	 * Returns true if the flow execution key is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isFlowExecutionKeyPresent(ExternalContext context) {
		return context.getRequestParameterMap().contains(flowExecutionKeyParameterName);
	}

	/**
	 * Extract the flow execution key from the external context.
	 * @param context the context in which the external user event occured
	 * @return the obtained flow execution key or <code>null</code> if not
	 * found
	 * @throws FlowExecutorArgumentExtractionException if the flow execution key
	 * could not be extracted
	 */
	public String extractFlowExecutionKey(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String encodedKey = context.getRequestParameterMap().get(flowExecutionKeyParameterName);
		if (!StringUtils.hasText(encodedKey)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the flowExecutionKey argument: make sure the client provides the '"
							+ flowExecutionKeyParameterName
							+ "' parameter as input; the parameters provided in this request are: "
							+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return encodedKey;
	}

	/**
	 * Returns true if the flow execution key is extractable from the context.
	 * @param context the context in which a external user event occured
	 * @return true if extractable, false if not
	 */
	public boolean isEventIdPresent(ExternalContext context) {
		return findParameter(eventIdParameterName, context.getRequestParameterMap()) != null;
	}

	/**
	 * Extract the flow execution event id from the external context.
	 * <p>
	 * This method should only be called if a {@link FlowExecutionKey} was
	 * successfully extracted, indicating a request to resume a flow execution.
	 * It should never return null.
	 * 
	 * @param context the context in which a external user event occured
	 * @return the event id
	 * @throws FlowExecutorArgumentExtractionException if the event id could not
	 * be extracted
	 */
	public String extractEventId(ExternalContext context) throws FlowExecutorArgumentExtractionException {
		String eventId = findParameter(eventIdParameterName, context.getRequestParameterMap());
		if (!StringUtils.hasText(eventId)) {
			throw new FlowExecutorArgumentExtractionException(
					"Unable to extract the eventId argument: make sure the client provides the '"
							+ eventIdParameterName + "' parameter as input along with the '"
							+ flowExecutionKeyParameterName
							+ "' parameter; the parameters provided in this request are: "
							+ StylerUtils.style(context.getRequestParameterMap()));
		}
		return eventId;
	}

	/**
	 * Obtain a named parameter from the event parameters. This method will try
	 * to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value using just the given <i>logical</i>
	 * name. This handles parameters of the form <tt>logicalName = value</tt>.
	 * For normal parameters, e.g. submitted using a hidden HTML form field,
	 * this will return the requested value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the event is of the form
	 * <tt>logicalName_value = xyz</tt> with "_" being the specified
	 * delimiter. This deals with parameter values submitted using an HTML form
	 * submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the event
	 * would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param logicalParameterName the <i>logical</i> name of the request
	 * parameter
	 * @param parameters the available parameter map
	 * @return the value of the parameter, or <code>null</code> if the
	 * parameter does not exist in given request
	 */
	private String findParameter(String logicalParameterName, ParameterMap parameters) {
		// first try to get it as a normal name=value parameter
		String value = parameters.get(logicalParameterName);
		if (value != null) {
			return value;
		}
		// if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalParameterName + parameterDelimiter;
		Iterator paramNames = parameters.getMap().keySet().iterator();
		while (paramNames.hasNext()) {
			String paramName = (String)paramNames.next();
			if (paramName.startsWith(prefix)) {
				String strValue = paramName.substring(prefix.length());
				// support images buttons, which would submit parameters as
				// name_value.x=123
				if (strValue.endsWith(".x") || strValue.endsWith(".y")) {
					strValue = strValue.substring(0, strValue.length() - 2);
				}
				return strValue;
			}
		}
		// we couldn't find the parameter value
		return null;
	}

	// data and behavior response issuance

	/**
	 * The string-encoded id of the flow execution will be exposed to the view
	 * in a model attribute with this name ("flowExecutionKey").
	 */
	private static final String FLOW_EXECUTION_KEY_ATTRIBUTE = "flowExecutionKey";

	/**
	 * The flow execution context itself will be exposed to the view in a model
	 * attribute with this name ("flowExecutionContext").
	 */
	private static final String FLOW_EXECUTION_CONTEXT_ATTRIBUTE = "flowExecutionContext";

	/**
	 * The default URL encoding scheme.
	 */
	private static final String DEFAULT_URL_ENCODING_SCHEME = "UTF-8";

	/**
	 * Context attribuet that identifies the the flow execution participated in,
	 * defaults to {@link #FLOW_EXECUTION_KEY_ATTRIBUTE }.
	 */
	private String flowExecutionKeyAttributeName = FLOW_EXECUTION_KEY_ATTRIBUTE;

	/**
	 * Context attribute that provides state about the flow execution
	 * participated in, defaults to {@link #FLOW_EXECUTION_CONTEXT_ATTRIBUTE }.
	 */
	private String flowExecutionContextAttributeName = FLOW_EXECUTION_CONTEXT_ATTRIBUTE;

	/**
	 * A flag indicating whether to interpret a redirect URL that starts with a
	 * slash ("/") as relative to the current ServletContext, i.e. as relative
	 * to the web application root, as opposed to absolute.
	 */
	private boolean redirectContextRelative = true;

	/**
	 * The url encoding scheme to be used to encode URLs built by this parameter
	 * extractor.
	 */
	private String urlEncodingScheme = DEFAULT_URL_ENCODING_SCHEME;

	/**
	 * Returns the flow execution key attribute name, used as a context
	 * attribute for identifying the executing flow being participated in.
	 */
	public String getFlowExecutionKeyAttributeName() {
		return flowExecutionKeyAttributeName;
	}

	/**
	 * Sets the flow execution key attribute name, used as a context attribute
	 * for identifying the current state of the executing flow being
	 * participated in (typically used by view templates during rendering).
	 */
	public void setFlowExecutionKeyAttributeName(String flowExecutionKeyAttributeName) {
		this.flowExecutionKeyAttributeName = flowExecutionKeyAttributeName;
	}

	/**
	 * Returns the flow execution context attribute name.
	 */
	public String getFlowExecutionContextAttributeName() {
		return flowExecutionContextAttributeName;
	}

	/**
	 * Sets the flow execution context attribute name.
	 */
	public void setFlowExecutionContextAttributeName(String flowExecutionContextAttributeName) {
		this.flowExecutionContextAttributeName = flowExecutionContextAttributeName;
	}

	/**
	 * Set whether to interpret a given redirect URL that starts with a slash
	 * ("/") as relative to the current ServletContext, i.e. as relative to the
	 * web application root.
	 * <p>
	 * Default is "true": A redirect URL that starts with a slash will be
	 * interpreted as relative to the web application root, i.e. the context
	 * path will be prepended to the URL.
	 */
	public void setRedirectContextRelative(boolean redirectContextRelative) {
		this.redirectContextRelative = redirectContextRelative;
	}

	/**
	 * Return whether to interpret a given redirect URL that starts with a slash
	 * ("/") as relative to the current ServletContext, i.e. as relative to the
	 * web application root.
	 */
	protected boolean isRedirectContextRelative() {
		return redirectContextRelative;
	}

	/**
	 * Create a URL that when redirected to launches a entirely new execution of
	 * a flow (starts a new conversation). Used to support the <i>restart flow</i>
	 * and <i>redirect to flow</i> use cases.
	 * @param flowRedirect the flow redirect selection
	 * @param externalContext the external context
	 * @return the relative flow URL path to redirect to
	 */
	public String createFlowUrl(FlowRedirect flowRedirect, ExternalContext externalContext) {
		StringBuffer flowUrl = new StringBuffer();
		flowUrl.append(externalContext.getContextPath());
		flowUrl.append(externalContext.getDispatcherPath());
		flowUrl.append('?');
		appendQueryParameter(flowIdParameterName, flowRedirect.getFlowId(), flowUrl);
		if (!flowRedirect.getInput().isEmpty()) {
			flowUrl.append('&');
		}
		appendQueryParameters(flowRedirect.getInput(), flowUrl);
		return flowUrl.toString();
	}

	/**
	 * Create a URL path that when redirected to renders the <i>current</i> (or
	 * last) view selection</i> made by the flow execution identified by the
	 * flow execution key. Used to support the <i>flow execution redirect</i>
	 * use case.
	 * @param flowExecutionKey the flow execution key
	 * @param flowExecution the flow execution
	 * @param context the external context
	 * @return the relative conversation URL path
	 */
	public String createFlowExecutionUrl(String flowExecutionKey, FlowExecutionContext flowExecution,
			ExternalContext context) {
		StringBuffer flowExecutionUrl = new StringBuffer();
		flowExecutionUrl.append(context.getContextPath());
		flowExecutionUrl.append(context.getDispatcherPath());
		flowExecutionUrl.append('?');
		appendQueryParameter(flowExecutionKeyParameterName, flowExecutionKey, flowExecutionUrl);
		return flowExecutionUrl.toString();
	}

	/**
	 * Create a URL path to that when redirected to communicates with an
	 * external system outside of Spring Web Flow.
	 * @param redirect the external redirect request
	 * @param flowExecutionKey the flow execution key to send through the
	 * redirect (may be null if the conversation has ended)
	 * @param context the external context
	 */
	public String createExternalUrl(ExternalRedirect redirect, String flowExecutionKey, ExternalContext context) {
		StringBuffer externalUrl = new StringBuffer();
		if (redirect.getUrl().startsWith("/") && isRedirectContextRelative()) {
			externalUrl.append(context.getContextPath());
		}
		externalUrl.append(redirect.getUrl());
		if (flowExecutionKey != null) {
			boolean first = redirect.getUrl().indexOf('?') < 0;
			if (first) {
				externalUrl.append('?');
			}
			else {
				externalUrl.append('&');
			}
			appendQueryParameter(flowExecutionKeyParameterName, flowExecutionKey, externalUrl);
		}
		return externalUrl.toString();
	}

	/**
	 * Put the flow execution key into the provided model map under the
	 * configured context attribute name.
	 * @param flowExecutionKey the flow execution key (may be null if the
	 * conversation has ended).
	 * @param model the model
	 */
	public void put(String flowExecutionKey, Map model) {
		if (flowExecutionKey != null) {
			model.put(flowExecutionKeyAttributeName, flowExecutionKey);
		}
	}

	/**
	 * Put the flow execution context into the provided model map under the
	 * configured context attribute name.
	 * @param context the flow execution context
	 * @param model the model
	 */
	public void put(FlowExecutionContext context, Map model) {
		model.put(flowExecutionContextAttributeName, context);
	}

	/**
	 * URL-encode the given input String with the given encoding scheme.
	 * @param value the unencoded value
	 * @return the encoded output String
	 */
	protected String encodeValue(Object value) {
		return value != null ? urlEncode(value.toString()) : "";
	}

	/**
	 * Append query properties to the redirect URL. Stringifies, URL-encodes and
	 * formats model attributes as query properties.
	 * @param targetUrl the StringBuffer to append the properties to
	 * @param map Map that contains attributes
	 */
	protected void appendQueryParameters(Map map, StringBuffer targetUrl) {
		Iterator entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			appendQueryParameter(entry.getKey(), entry.getValue(), targetUrl);
			if (entries.hasNext()) {
				targetUrl.append('&');
			}
		}
	}

	/**
	 * Appends a single query parameter to a URL.
	 * @param key the parameter name
	 * @param value the parameter value
	 * @param targetUrl the target url
	 */
	protected void appendQueryParameter(Object key, Object value, StringBuffer targetUrl) {
		String encodedKey = urlEncode(key.toString());
		String encodedValue = encodeValue(value);
		targetUrl.append(encodedKey).append('=').append(encodedValue);
	}

	/**
	 * URL-encode the given input String with the given encoding scheme.
	 * <p>
	 * Default implementation uses <code>URLEncoder.encode(input, enc)</code>
	 * on JDK 1.4+, falling back to <code>URLEncoder.encode(input)</code>
	 * (which uses the platform default encoding) on JDK 1.3.
	 * @param input the unencoded input String
	 * @param encodingScheme the encoding scheme
	 * @return the encoded output String
	 * @see java.net.URLEncoder#encode(String, String)
	 * @see java.net.URLEncoder#encode(String)
	 */
	private String urlEncode(String input) {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return URLEncoder.encode(input);
		}
		try {
			return URLEncoder.encode(input, urlEncodingScheme);
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Cannot encode URL " + input);
		}
	}
}