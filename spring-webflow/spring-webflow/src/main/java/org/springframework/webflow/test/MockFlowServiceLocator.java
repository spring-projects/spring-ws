package org.springframework.webflow.test;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.webflow.Flow;
import org.springframework.webflow.registry.DefaultFlowServiceLocator;
import org.springframework.webflow.registry.FlowRegistryImpl;
import org.springframework.webflow.registry.StaticFlowHolder;

/**
 * A stub flow service locator implementation suitable for a test environment.
 * <p>
 * Allows programmatic registration of subflows needed by a flow execution being
 * tested, see {@link #registerSubflow(Flow)}. Subflows registered are
 * typically stubs that verify parent flow input and output scenarios.
 * <p>
 * Also supports programmatic registration of additional custom services needed
 * by a flow (such as Actions) managed in a backing Spring
 * {@link ConfigurableBeanFactory}. See the
 * {@link #registerBean(String, Object)} method. Beans registered are typically
 * mocks or stubs of business services invoked by the flow.
 * 
 * @author Keith Donald
 */
public class MockFlowServiceLocator extends DefaultFlowServiceLocator {

	/**
	 * Creates a new mock flow service locator.
	 */
	public MockFlowServiceLocator() {
		super(new FlowRegistryImpl(), new StaticListableBeanFactory());
	}

	/**
	 * Register a subflow definition in the backing flow registry, typically to
	 * support a flow execution test. For test scenarios, the subflow is often a
	 * stub used to verify parent flow input and output mapping behavior.
	 * @param subflow the subflow
	 */
	public void registerSubflow(Flow subflow) {
		getSubflowRegistry().registerFlow(new StaticFlowHolder(subflow));
	}

	/**
	 * Register a bean in the backing bean factory, typically to support a flow
	 * execution test. For test scenarios, if the bean is a service invoked by a
	 * bean invoking action it is often a stub or dynamic mock implementation of
	 * the service's business interface.
	 * @param beanName the bean name
	 * @param bean the singleton instance
	 */
	public void registerBean(String beanName, Object bean) {
		((StaticListableBeanFactory)getBeanFactory()).addBean(beanName, bean);
	}
}