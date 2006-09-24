package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.AbstractBeanInvokingAction;

/**
 * A support interface used by FlowBuilders at configuration time. Acts as a
 * "service locator" responsible for:
 * <ol>
 * <li> retrieving dependent (but externally managed) flow services needed to
 * configure flow and state definitions. Such services are usually hosted in a
 * backing registry, and may be shared by multiple flows.
 * <li> providing access to abstract factories to create core flow definitional
 * artifacts such as {@link Flow}, {@link State}, {@link Transition}, and
 * {@link AbstractBeanInvokingAction bean invoking actions}. These artifacts
 * are unique to each flow and are typically not shared.
 * 
 * In general, implementations of this interface act as facades to accessing and
 * creating flow artifacts during {@link FlowAssembler flow assembly}. Finally,
 * this interface also exposes access to generic infrastructure services also
 * needed by flow assemblers such as a {@link ConversionService} and
 * {@link ExpressionParser}.
 * 
 * @author Keith Donald
 */
public interface FlowServiceLocator {

	/**
	 * Returns the Flow to be used as a subflow with the provided id.
	 * @param id the flow id
	 * @return the flow to be used as a subflow
	 * @throws FlowArtifactException when no such flow is found
	 */
	public Flow getSubflow(String id) throws FlowArtifactException;

	/**
	 * Retrieve the action to be executed within a flow with the assigned
	 * parameters.
	 * @param id the id of the action
	 * @throws FlowArtifactException when no such action is found
	 */
	public Action getAction(String id) throws FlowArtifactException;

	/**
	 * Returns true if the action with the given <code>actionId</code> is an
	 * actual implementation of the {@link Action} interface. It could be an
	 * arbitrary bean (any <code>java.lang.Object</code>), at which it needs
	 * to be adapted by a
	 * {@link AbstractBeanInvokingAction bean invoking action}.
	 * @param actionId the action id
	 * @return true if the action is an Action, false otherwise
	 * @throws FlowArtifactException when no such action is found
	 */
	public boolean isAction(String actionId) throws FlowArtifactException;

	/**
	 * Returns the flow attribute mapper with the provided id. Flow attribute
	 * mappers are used from subflow states to map input and output attributes.
	 * @param id the id
	 * @return the attribute mapper
	 * @throws FlowArtifactException when no such mapper is found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException;

	/**
	 * Returns the transition criteria to drive state transitions with the
	 * provided id.
	 * @param id the id
	 * @return the transition criteria
	 * @throws FlowArtifactException when no such criteria is found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException;

	/**
	 * Returns the view selector to make view selections in view states with the
	 * provided id.
	 * @param id the id
	 * @return the view selector
	 * @throws FlowArtifactException when no such selector is found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactException;

	/**
	 * Returns the exception handler to handle state exceptions with the
	 * provided id.
	 * @param id the id
	 * @return the exception handler
	 * @throws FlowArtifactException when no such handler is found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException;

	/**
	 * Returns the transition target state resolver with the specified id.
	 * @param id the id
	 * @return the target state resolver
	 * @throws FlowArtifactException when no such resolver is found
	 */
	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException;

	/**
	 * Returns the factory for core entity artifacts such as Flow and State.
	 * @return the flow entity factory.
	 */
	public FlowArtifactFactory getFlowArtifactFactory();

	/**
	 * Returns the factory for bean invoking actions.
	 * @return the bean invoking action factory.
	 */
	public BeanInvokingActionFactory getBeanInvokingActionFactory();

	/**
	 * Returns a generic bean (service) registry for accessing arbitrary beans.
	 * @return the generic service registry
	 * @throws UnsupportedOperationException when not supported by this factory
	 */
	public BeanFactory getBeanFactory() throws UnsupportedOperationException;

	/**
	 * Returns a generic resource loader for accessing file-based resources.
	 * @return the generic resource loader
	 */
	public ResourceLoader getResourceLoader();

	/**
	 * Returns the expression parser for parsing expression strings.
	 * @return the expression parser
	 */
	public ExpressionParser getExpressionParser();

	/**
	 * Returns a generic type conversion service for converting between types,
	 * typically from string to a rich value object.
	 * @return the generic conversion service
	 */
	public ConversionService getConversionService();
}