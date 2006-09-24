package org.springframework.webflow.action;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Default implementation of the resultObject-to-event adapter interface.
 * 
 * @author Keith Donald
 */
public class SuccessEventFactory extends EventFactorySupport implements ResultEventFactory {
	public Event createResultEvent(Object source, Object resultObject, RequestContext context) {
		return success(source, resultObject);
	}
}