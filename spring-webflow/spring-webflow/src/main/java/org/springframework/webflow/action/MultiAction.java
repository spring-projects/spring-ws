/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.action;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.util.DispatchMethodInvoker;

/**
 * Action implementation that bundles two or more action execution methods into
 * a single class. Action execution methods defined by subclasses must adhere to
 * the following signature:
 * 
 * <pre>
 *     public Event ${method}(RequestContext context) throws Exception;
 * </pre>
 * 
 * When this action is invoked, by default the <code>id</code> of the calling
 * action state state is treated as the action execution method name.
 * Alternatively, the execution method name may be explicitly specified as a
 * property of the calling action state.
 * <p>
 * For example, the following action state definition:
 * 
 * <pre>
 *     &lt;action-state id=&quot;search&quot;&gt;
 *         &lt;action bean=&quot;searchAction&quot;/&gt;
 *         &lt;transition on=&quot;success&quot; to=&quot;results&quot;/&gt;
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * ... when entered, executes the method:
 * 
 * <pre>
 *     public Event search(RequestContext context) throws Exception;
 * </pre>
 * 
 * Alternatively you may explictly specify the method name:
 * 
 * <pre>
 *     &lt;action-state id=&quot;executingSearch&quot;&gt;
 *         &lt;action bean=&quot;phonebook&amp;quot method=&quot;executeSearch&quot;/&gt;
 *         &lt;transition on=&quot;success&quot; to=&quot;results&quot;/&gt;
 *     &lt;/action-state&gt;
 * </pre>
 * 
 * <p>
 * A typical use of the MultiAction is to centralize all command logic for a
 * flow in one place. Another common use is to centralize form setup and submit
 * logic into one place, or CRUD (create/read/update/delete) operations for a
 * single domain object in one place.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>Name </b></td>
 * <td><b>Default </b></td>
 * <td><b>Description </b></td>
 * </tr>
 * <tr>
 * <td>methodResolver</td>
 * <td>Treats the id of the "currentState" as the target method name</td>
 * <td>Set the strategy used to resolve the name (key) of an action execution
 * method. Allows full control over the method resolution algorithm.</td>
 * </tr>
 * </table>
 * 
 * @see MultiAction.MethodResolver
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MultiAction extends AbstractAction {

	/**
	 * A cache for dispatched action execute methods. The default signature is
	 * <code>public Event ${method}(RequestContext context) throws Exception;</code>.
	 */
	private DispatchMethodInvoker methodInvoker;

	/**
	 * The action method resolver strategy.
	 */
	private MethodResolver methodResolver = new DefaultMultiActionMethodResolver();

	/**
	 * Protected default constructor; not invokable by direct MultiAction instantiation.
	 */
	protected MultiAction() {
		setTarget(this);
	}

	/**
	 * Constructs a multi action that invokes methods on the specified target
	 * object. Note: invokable methods on the target must conform to the multi action
	 * method signature:
	 * <pre>
	 *       public Event ${method}(RequestContext context) throws Exception;
	 * </pre>
	 * @param target the target of this multi action's invocation
	 */
	public MultiAction(Object target) {
		setTarget(target);
	}

	/**
	 * Sets the target of this multi action invocation.
	 * @param target the target
	 */
	protected final void setTarget(Object target) {
		methodInvoker = new DispatchMethodInvoker(target, new Class[] { RequestContext.class } ); 
	}

	/**
	 * Get the strategy used to resolve action execution method keys.
	 */
	public MethodResolver getMethodResolver() {
		return methodResolver;
	}

	/**
	 * Set the strategy used to resolve action execution method keys.
	 */
	public void setMethodResolver(MethodResolver methodResolver) {
		this.methodResolver = methodResolver;
	}

	protected final Event doExecute(RequestContext context) throws Exception {
		String method = getMethodResolver().resolveMethod(context);
		return (Event)methodInvoker.invoke(method, new Object[] { context });
	}

	/**
	 * Strategy interface used by the MultiAction to map a request context to
	 * the name (key) of an action execution method.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public interface MethodResolver {

		/**
		 * Resolve a method key from given flow execution request context.
		 * @param context the flow execution request context
		 * @return the key identifying the method that should handle action
		 * execution
		 */
		public String resolveMethod(RequestContext context);
	}
}