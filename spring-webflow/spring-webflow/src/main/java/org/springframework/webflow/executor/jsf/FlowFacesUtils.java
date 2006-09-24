package org.springframework.webflow.executor.jsf;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepositoryFactory;

public class FlowFacesUtils {

	/**
	 * The service name of the default {@link FlowExecutionRepositoryFactory}
	 * implementation exported in the Spring Web Application Context.
	 */
	private static final String REPOSITORY_FACTORY_BEAN_NAME = "flowExecutionRepositoryFactory";

	/**
	 * The service name of the default {@link FlowLocator} implementation
	 * exported in the Spring Web Application Context.
	 */
	private static final String FLOW_LOCATOR_BEAN_NAME = "flowLocator";

	/**
	 * Lookup the flow locator service by querying the application context for a
	 * bean with name {@link #FLOW_LOCATOR_BEAN_NAME}.
	 * @param context the faces context
	 * @return the flow locator
	 */
	public static FlowExecutionRepositoryFactory getRepositoryFactory(FacesContext context) {
		ApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(context);
		if (ac.containsBean(REPOSITORY_FACTORY_BEAN_NAME)) {
			return (FlowExecutionRepositoryFactory)ac.getBean(REPOSITORY_FACTORY_BEAN_NAME,
					FlowExecutionRepositoryFactory.class);
		}
		else {
			try {
				FlowLocator flowLocator = (FlowLocator)ac.getBean(FLOW_LOCATOR_BEAN_NAME, FlowLocator.class);
				return new DefaultFlowExecutionRepositoryFactory(flowLocator);
			}
			catch (NoSuchBeanDefinitionException e) {
				String message = "No '" + FLOW_LOCATOR_BEAN_NAME + "' or '" + REPOSITORY_FACTORY_BEAN_NAME
						+ "' bean definition could be found; to use Spring Web Flow with JSF you must "
						+ "configure this PhaseListener with either a FlowLocator "
						+ "(exposing a registry of flow definitions) or a custom FlowExecutionRepositoryFactory "
						+ "(allowing more configuration options).";
				throw new FlowArtifactException(FLOW_LOCATOR_BEAN_NAME, FlowLocator.class, message, e);
			}
		}
	}
}