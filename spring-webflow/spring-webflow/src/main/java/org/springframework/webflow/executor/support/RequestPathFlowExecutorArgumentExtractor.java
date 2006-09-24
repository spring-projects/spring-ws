package org.springframework.webflow.executor.support;

import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Extracts flow executor arguments from the request path.
 * <p>
 * This allows for REST-style URLs to launch flows in the general format:
 * <code>http://${host}/${context}/${servlet}/${flowId}</code>
 * <p>
 * For example, the url
 * <code>http://localhost/springair/reservation/booking</code> would launch a
 * new execution of the <code>booking</code> flow, assuming a context path of
 * <code>/springair</code> and a servlet mapping of
 * <code>/reservation/*</code>.
 * <p>
 * Note: this implementation only works with <code>ExternalContext</code>
 * implementations that return a valid
 * {@link ExternalContext#getRequestPathInfo()} such as the
 * {@link ServletExternalContext}.
 * 
 * @author Keith Donald
 */
public class RequestPathFlowExecutorArgumentExtractor extends FlowExecutorArgumentExtractor {

	private static final char PATH_SEPARATOR_CHARACTER = '/';

	/**
	 * Flag indicating if flow input attributes should be appended to the
	 * request path on a flow redirect request, instead of being appended as
	 * standard URL query parameters.
	 */
	private boolean appendFlowInputAttributesToRequestPath = false;

	/**
	 * Returns the flag indicating if flow input attributes should be appended
	 * to the request path to build a flow redirect request, instead of being
	 * appended as standard URL query parameters.
	 */
	public boolean isAppendFlowInputAttributesToRequestPath() {
		return appendFlowInputAttributesToRequestPath;
	}

	/**
	 * Sets a flag indicating if flow input attributes should be appended to the
	 * request path to build a flow redirect request, instead of being appended
	 * as standard URL query parameters.
	 * 
	 * For example:
	 * <ul>
	 * <li>With request path appending turned on:
	 * 
	 * <pre>
	 *        /booking/12345
	 * </pre>
	 * 
	 * <li>With request path appending turned off:
	 * 
	 * <pre>
	 *        /booking?bookingId=12345
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param appendFlowInputAttributesToRequestPath the boolean flag value
	 */
	public void setAppendFlowInputAttributesToRequestPath(boolean appendFlowInputAttributesToRequestPath) {
		this.appendFlowInputAttributesToRequestPath = appendFlowInputAttributesToRequestPath;
	}

	public boolean isFlowIdPresent(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		boolean hasFileName = StringUtils.hasText(WebUtils.extractFilenameFromUrlPath(requestPathInfo));
		return hasFileName || super.isFlowIdPresent(context);
	}

	public String extractFlowId(ExternalContext context) {
		String requestPathInfo = getRequestPathInfo(context);
		String extractedFilename = WebUtils.extractFilenameFromUrlPath(requestPathInfo);
		return StringUtils.hasText(extractedFilename) ? extractedFilename : super.extractFlowId(context);
	}

	public String createFlowUrl(FlowRedirect flowRedirect, ExternalContext context) {
		StringBuffer flowUrl = new StringBuffer();
		flowUrl.append(context.getContextPath());
		flowUrl.append(context.getDispatcherPath());
		flowUrl.append(PATH_SEPARATOR_CHARACTER);
		flowUrl.append(flowRedirect.getFlowId());
		if (appendFlowInputAttributesToRequestPath) {
			appendRequestPathElements(flowRedirect.getInput(), flowUrl);
		}
		else {
			if (!flowRedirect.getInput().isEmpty()) {
				flowUrl.append('?');
				appendQueryParameters(flowRedirect.getInput(), flowUrl);
			}
		}
		return flowUrl.toString();
	}

	private void appendRequestPathElements(Map map, StringBuffer url) {
		if (!map.isEmpty()) {
			url.append(PATH_SEPARATOR_CHARACTER);
			Iterator it = map.values().iterator();
			while (it.hasNext()) {
				url.append(encodeValue(it.next()));
				if (it.hasNext()) {
					url.append(PATH_SEPARATOR_CHARACTER);
				}
			}
		}
	}

	public String createFlowExecutionUrl(String flowExecutionKey, FlowExecutionContext flowExecution,
			ExternalContext context) {
		StringBuffer flowExecutionUrl = new StringBuffer();
		flowExecutionUrl.append(context.getContextPath());
		flowExecutionUrl.append(context.getDispatcherPath());
		flowExecutionUrl.append(PATH_SEPARATOR_CHARACTER);
		flowExecutionUrl.append(flowExecution.getActiveSession().getFlow().getId());
		flowExecutionUrl.append('?');
		appendQueryParameter(getFlowExecutionKeyParameterName(), flowExecutionKey, flowExecutionUrl);
		return flowExecutionUrl.toString();
	}

	private String getRequestPathInfo(ExternalContext context) {
		String requestPathInfo = context.getRequestPathInfo();
		return requestPathInfo != null ? requestPathInfo : "";
	}
}