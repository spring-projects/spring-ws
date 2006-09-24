/**
 * 
 */
package org.springframework.webflow.executor;

import java.util.Map;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;

/**
 * Simple little attribute mapper implementation that puts all entries in the
 * request parameter map of a source {@link ExternalContext} into the
 * FlowExecution inputMap. This makes request parameters available to launching
 * flows for input mapping.
 * <p>
 * Used by {@link FlowExecutorImpl} as the default AttributeMapper
 * implementation.
 * 
 * @author Keith Donald
 */
class RequestParameterInputMapper implements AttributeMapper {
	public void map(Object source, Object target, Map mappingContext) {
		ExternalContext context = (ExternalContext)source;
		AttributeMap inputMap = (AttributeMap)target;
		inputMap.putAll(context.getRequestParameterMap().asAttributeMap());
	}
}