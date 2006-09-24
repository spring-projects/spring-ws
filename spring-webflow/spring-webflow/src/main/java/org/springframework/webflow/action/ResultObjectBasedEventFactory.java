package org.springframework.webflow.action;

import org.springframework.core.JdkVersion;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Default implementation of the result object-to-event adapter interface.
 * 
 * @author Keith Donald
 */
public class ResultObjectBasedEventFactory extends EventFactorySupport implements ResultEventFactory {

	public Event createResultEvent(Object source, Object resultObject, RequestContext context) {
		if (resultObject instanceof Boolean) {
			return event(source, ((Boolean)resultObject).booleanValue());
		}
		else if (resultObject instanceof LabeledEnum) {
			String resultId = ((LabeledEnum)resultObject).getLabel();
			return event(source, resultId, getResultAttributeName(), resultObject);
		}
		else if (resultObject == null) {
			return event(source, getNullEventId(), getResultAttributeName(), null);
		}
		if (resultObject instanceof Event) {
			return (Event)resultObject;
		}
		else {
			return event(source, String.valueOf(resultObject), getResultAttributeName(), resultObject);
		}
	}

	public boolean isMappedValueType(Class type) {
		if (isBoolean(type) || isJdk5Enum(type) || isLabeledEnum(type) || isString(type)) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isBoolean(Class type) {
		return Boolean.class.equals(type) || boolean.class.equals(type);
	}

	private boolean isJdk5Enum(Class type) {
		if (!(JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15)) {
			return false;
		}
		else {
			return type.isEnum();
		}
	}

	private boolean isLabeledEnum(Class type) {
		return LabeledEnum.class.isAssignableFrom(type);
	}

	private boolean isString(Class type) {
		return String.class.equals(type);
	}

}