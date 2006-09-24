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
package org.springframework.webflow.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.method.MethodSignature;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.webflow.Action;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowVariable;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.ResultSpecification;
import org.springframework.webflow.support.BeanFactoryFlowVariable;
import org.springframework.webflow.support.CollectionAddingPropertyExpression;
import org.springframework.webflow.support.ImmutableFlowAttributeMapper;
import org.springframework.webflow.support.SimpleFlowVariable;
import org.springframework.webflow.support.TransitionCriteriaChain;
import org.springframework.webflow.support.TransitionExecutingStateExceptionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Flow builder that builds flows as defined in an XML document object model
 * (DOM) element. The element is supposed to be read from an XML file that uses
 * the following doctype:
 * 
 * <pre>
 *     &lt;!DOCTYPE flow PUBLIC &quot;-//SPRING//DTD WEBFLOW 1.0//EN&quot;
 *     &quot;http://www.springframework.org/dtd/spring-webflow-1.0.dtd&quot;&gt;
 * </pre>
 * 
 * <p>
 * Consult the <a
 * href="http://www.springframework.org/dtd/spring-webflow-1.0.dtd">web flow DTD</a>
 * for more information on the XML flow definition format.
 * <p>
 * This builder will setup a flow-local bean factory for the flow being
 * constructed. That flow-local bean factory will be populated with XML bean
 * definitions contained in files referenced using the "import" element. The
 * flow-local bean factory will use the bean factory defing this flow builder as
 * a parent. As such, the flow can access artifacts in either its flow-local
 * bean factory or in the parent bean factory hierarchy, e.g. the bean factory
 * of the dispatcher.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>location</td>
 * <td><i>null</i></td>
 * <td>Specifies the resource location from which the XML-based flow definition
 * is loaded. This "input stream source" is a required property.</td>
 * </tr>
 * <tr>
 * <td>validating</td>
 * <td><i>true</i></td>
 * <td>Set if the XML parser should validate the document and thus enforce a
 * DTD.</td>
 * </tr>
 * <tr>
 * <td>entityResolver</td>
 * <td><i>{@link WebFlowDtdResolver}</i></td>
 * <td>Set a SAX entity resolver to be used for parsing.</td>
 * </tr>
 * </table>
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class XmlFlowBuilder extends BaseFlowBuilder implements ResourceHolder {

	private static final Log logger = LogFactory.getLog(XmlFlowBuilder.class);

	// recognized XML elements and attributes

	private static final String ID_ATTRIBUTE = "id";

	private static final String BEAN_ATTRIBUTE = "bean";

	private static final String FLOW_ELEMENT = "flow";

	private static final String START_STATE_ATTRIBUTE = "start-state";

	private static final String ACTION_STATE_ELEMENT = "action-state";

	private static final String ACTION_ELEMENT = "action";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String RESULT_NAME_ATTRIBUTE = "result-name";

	private static final String RESULT_SCOPE_ATTRIBUTE = "result-scope";

	private static final String DEFAULT_VALUE = "default";

	private static final String VIEW_STATE_ELEMENT = "view-state";

	private static final String VIEW_ATTRIBUTE = "view";

	private static final String DECISION_STATE_ELEMENT = "decision-state";

	private static final String IF_ELEMENT = "if";

	private static final String TEST_ATTRIBUTE = "test";

	private static final String THEN_ATTRIBUTE = "then";

	private static final String ELSE_ATTRIBUTE = "else";

	private static final String SUBFLOW_STATE_ELEMENT = "subflow-state";

	private static final String FLOW_ATTRIBUTE = "flow";

	private static final String ATTRIBUTE_MAPPER_ELEMENT = "attribute-mapper";

	private static final String OUTPUT_MAPPER_ELEMENT = "output-mapper";

	private static final String INPUT_MAPPER_ELEMENT = "input-mapper";

	private static final String MAPPING_ELEMENT = "mapping";

	private static final String SOURCE_ATTRIBUTE = "source";

	private static final String TARGET_ATTRIBUTE = "target";

	private static final String FROM_ATTRIBUTE = "from";

	private static final String TO_ATTRIBUTE = "to";

	private static final String TARGET_COLLECTION_ATTRIBUTE = "target-collection";

	private static final String END_STATE_ELEMENT = "end-state";

	private static final String TRANSITION_ELEMENT = "transition";

	private static final String GLOBAL_TRANSITIONS_ELEMENT = "global-transitions";

	private static final String ON_ATTRIBUTE = "on";

	private static final String ON_EXCEPTION_ATTRIBUTE = "on-exception";

	private static final String ATTRIBUTE_ELEMENT = "attribute";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String VALUE_ELEMENT = "value";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String VAR_ELEMENT = "var";

	private static final String SCOPE_ATTRIBUTE = "scope";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String START_ACTIONS_ELEMENT = "start-actions";

	private static final String END_ACTIONS_ELEMENT = "end-actions";

	private static final String ENTRY_ACTIONS_ELEMENT = "entry-actions";

	private static final String EXIT_ACTIONS_ELEMENT = "exit-actions";

	private static final String EXCEPTION_HANDLER_ELEMENT = "exception-handler";

	private static final String INLINE_FLOW_ELEMENT = "inline-flow";

	private static final String IMPORT_ELEMENT = "import";

	private static final String RESOURCE_ATTRIBUTE = "resource";

	/**
	 * The resource from which the document element being parsed was read. Used
	 * as a location for relative resource lookup.
	 */
	protected Resource location;

	/**
	 * A flow service locator local to this builder that first looks in a
	 * locally-managed Spring application context for services before searching
	 * the externally managed {@link #getFlowServiceLocator()}.
	 */
	private LocalFlowServiceLocator localFlowServiceLocator;

	/**
	 * Flag indicating if the the XML document parser will perform DTD
	 * validation.
	 */
	private boolean validating = true;

	/**
	 * The spring-webflow DTD resolution strategy.
	 */
	private EntityResolver entityResolver = new WebFlowDtdResolver();

	/**
	 * The in-memory document object model (DOM) of the XML Document read from
	 * the flow definition resource.
	 */
	private Document document;

	/**
	 * Create a new XML flow builder parsing the document at the specified
	 * location.
	 * @param location the location of the xml-based flow definition resource
	 */
	public XmlFlowBuilder(Resource location) {
		setLocation(location);
	}

	/**
	 * Create a new XML flow builder parsing the document at the specified
	 * location, using the provided factory to access externally managed flow
	 * artifacts.
	 * @param location the location of the xml-based flow definition resource
	 * @param flowServiceLocator the locator for services needed by this builder
	 * to build its Flow
	 */
	public XmlFlowBuilder(Resource location, FlowServiceLocator flowServiceLocator) {
		super(flowServiceLocator);
		setLocation(location);
	}

	/**
	 * Returns the resource from which the document element was loaded. This is
	 * used for location relative loading of other resources.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Sets the resource from which the document element was loaded. This is
	 * used for location relative loading of other resources.
	 */
	public void setLocation(Resource location) {
		Assert.notNull(location,
				"The location property specifying the XML flow definition resource location is required");
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.webflow.builder.ResourceHolder#getResource()
	 */
	public Resource getResource() {
		return location;
	}

	/**
	 * Returns whether or not the XML parser will validate the document.
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a
	 * DTD. Defaults to true.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Returns the SAX entity resolver used by the XML parser.
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * WebFlowDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * 
	 * @see org.springframework.webflow.builder.WebFlowDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public void init(String id, AttributeCollection attributes) throws FlowBuilderException {
		localFlowServiceLocator = new LocalFlowServiceLocator(getFlowServiceLocator());
		try {
			document = loadDocument();
			Assert.notNull(document, "Document should never be null");
		}
		catch (IOException e) {
			throw new FlowBuilderException(this, "Could not load the XML flow definition resource at " + getLocation(),
					e);
		}
		catch (ParserConfigurationException e) {
			throw new FlowBuilderException(this, "Could not configure the parser to parse the XML flow definition at "
					+ getLocation(), e);
		}
		catch (SAXException e) {
			throw new FlowBuilderException(this,
					"Could not parse the flow definition XML document at " + getLocation(), e);
		}
		setFlow(parseFlow(id, attributes, getDocumentElement()));
	}

	public void buildVariables() throws FlowBuilderException {
		parseAndAddFlowVariables(getDocumentElement(), getFlow());
	}

	public void buildInputMapper() throws FlowBuilderException {
		getFlow().setInputMapper(parseInputMapper(getDocumentElement()));
	}

	public void buildStartActions() throws FlowBuilderException {
		parseAndAddStartActions(getDocumentElement(), getFlow());
	}

	public void buildInlineFlows() throws FlowBuilderException {
		parseAndAddInlineFlowDefinitions(getDocumentElement(), getFlow());
	}

	public void buildStates() throws FlowBuilderException {
		parseAndAddStateDefinitions(getDocumentElement(), getFlow());
	}

	public void buildGlobalTransitions() throws FlowBuilderException {
		parseAndAddGlobalTransitions(getDocumentElement(), getFlow());
	}

	public void buildEndActions() throws FlowBuilderException {
		parseAndAddEndActions(getDocumentElement(), getFlow());
	}

	public void buildOutputMapper() throws FlowBuilderException {
		getFlow().setOutputMapper(parseOutputMapper(getDocumentElement()));
	}

	public void buildExceptionHandlers() throws FlowBuilderException {
		getFlow().getExceptionHandlerSet().addAll(parseExceptionHandlers(getDocumentElement()));
	}

	public void dispose() {
		destroyLocalServiceRegistry(getFlow());
		document = null;
	}

	private Document loadDocument() throws IOException, ParserConfigurationException, SAXException {
		InputStream is = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(isValidating());
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new SimpleSaxErrorHandler(logger));
			docBuilder.setEntityResolver(getEntityResolver());
			is = getLocation().getInputStream();
			return docBuilder.parse(is);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}

	private Flow parseFlow(String id, AttributeCollection attributes, Element flowElement) {
		if (!isFlowElement(flowElement)) {
			throw new IllegalStateException("This is not the '" + FLOW_ELEMENT + "' element");
		}
		Flow flow = getFlowArtifactFactory().createFlow(id, parseAttributes(flowElement).union(attributes));
		initLocalServiceRegistry(flowElement, flow);
		return flow;
	}

	private FlowArtifactFactory getFlowArtifactFactory() {
		return getLocalFlowServiceLocator().getFlowArtifactFactory();
	}

	private boolean isFlowElement(Element flowElement) {
		return FLOW_ELEMENT.equals(flowElement.getTagName());
	}

	/**
	 * Returns the document.
	 */
	protected Document getDocument() {
		return document;
	}

	/**
	 * Returns the root document element.
	 */
	protected Element getDocumentElement() {
		return document.getDocumentElement();
	}

	/**
	 * Returns the flow service locator local to this builder.
	 */
	protected FlowServiceLocator getLocalFlowServiceLocator() {
		return localFlowServiceLocator;
	}

	private void initLocalServiceRegistry(Element flowElement, Flow flow) {
		List importElements = DomUtils.getChildElementsByTagName(flowElement, IMPORT_ELEMENT);
		Resource[] resources = new Resource[importElements.size()];
		for (int i = 0; i < importElements.size(); i++) {
			Element importElement = (Element)importElements.get(i);
			try {
				resources[i] = getLocation().createRelative(importElement.getAttribute(RESOURCE_ATTRIBUTE));
			}
			catch (IOException e) {
				throw new FlowBuilderException(this, "Could not access flow-relative artifact resource '"
						+ importElement.getAttribute(RESOURCE_ATTRIBUTE) + "'", e);
			}
		}
		localFlowServiceLocator.push(new LocalFlowServiceRegistry(flow, resources));
	}

	private void parseAndAddFlowVariables(Element flowElement, Flow flow) {
		List varElements = DomUtils.getChildElementsByTagName(flowElement, VAR_ELEMENT);
		if (varElements.isEmpty()) {
			return;
		}
		for (int i = 0; i < varElements.size(); i++) {
			flow.addVariable(parseVariable((Element)varElements.get(i)));
		}
	}

	private FlowVariable parseVariable(Element element) {
		ScopeType scope = null;
		if (element.hasAttribute(SCOPE_ATTRIBUTE) && !element.getAttribute(SCOPE_ATTRIBUTE).equals(DEFAULT_VALUE)) {
			scope = (ScopeType)fromStringTo(ScopeType.class).execute(element.getAttribute(SCOPE_ATTRIBUTE));
		}
		if (StringUtils.hasText(element.getAttribute(BEAN_ATTRIBUTE))) {
			BeanFactory beanFactory = getLocalFlowServiceLocator().getBeanFactory();
			return new BeanFactoryFlowVariable(element.getAttribute(NAME_ATTRIBUTE), element
					.getAttribute(BEAN_ATTRIBUTE), beanFactory, scope);
		}
		else {
			if (StringUtils.hasText(element.getAttribute(CLASS_ATTRIBUTE))) {
				Class variableClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(CLASS_ATTRIBUTE));
				return new SimpleFlowVariable(element.getAttribute(NAME_ATTRIBUTE), variableClass, scope);
			}
			else {
				BeanFactory beanFactory = getLocalFlowServiceLocator().getBeanFactory();
				return new BeanFactoryFlowVariable(element.getAttribute(NAME_ATTRIBUTE), null, beanFactory, scope);
			}
		}
	}

	private void parseAndAddStartActions(Element element, Flow flow) {
		List startElements = DomUtils.getChildElementsByTagName(element, START_ACTIONS_ELEMENT);
		if (!startElements.isEmpty()) {
			Element startElement = (Element)startElements.get(0);
			flow.getStartActionList().addAll(parseAnnotatedActions(startElement));
		}
	}

	private void parseAndAddEndActions(Element element, Flow flow) {
		List endElements = DomUtils.getChildElementsByTagName(element, END_ACTIONS_ELEMENT);
		if (!endElements.isEmpty()) {
			Element endElement = (Element)endElements.get(0);
			flow.getEndActionList().addAll(parseAnnotatedActions(endElement));
		}
	}

	private void parseAndAddGlobalTransitions(Element element, Flow flow) {
		List globalTransitionElements = DomUtils.getChildElementsByTagName(element, GLOBAL_TRANSITIONS_ELEMENT);
		if (!globalTransitionElements.isEmpty()) {
			Element globalTransitionsElement = (Element)globalTransitionElements.get(0);
			flow.getGlobalTransitionSet().addAll(parseTransitions(globalTransitionsElement));
		}
	}

	private void parseAndAddInlineFlowDefinitions(Element parentFlowElement, Flow flow) {
		List inlineFlowElements = DomUtils.getChildElementsByTagName(parentFlowElement, INLINE_FLOW_ELEMENT);
		if (inlineFlowElements.isEmpty()) {
			return;
		}
		for (int i = 0; i < inlineFlowElements.size(); i++) {
			Element inlineFlowElement = (Element)inlineFlowElements.get(i);
			String inlineFlowId = inlineFlowElement.getAttribute(ID_ATTRIBUTE);
			Element flowElement = (Element)inlineFlowElement.getElementsByTagName(FLOW_ATTRIBUTE).item(0);
			Flow inlineFlow = parseFlow(inlineFlowId, null, flowElement);
			buildInlineFlow(flowElement, inlineFlow);
			flow.addInlineFlow(inlineFlow);
		}
	}

	private void buildInlineFlow(Element flowElement, Flow inlineFlow) {
		parseAndAddFlowVariables(flowElement, getFlow());
		getFlow().setInputMapper(parseInputMapper(getDocumentElement()));
		parseAndAddStartActions(flowElement, getFlow());
		parseAndAddInlineFlowDefinitions(flowElement, inlineFlow);
		parseAndAddStateDefinitions(flowElement, inlineFlow);
		parseAndAddGlobalTransitions(flowElement, inlineFlow);
		inlineFlow.getExceptionHandlerSet().addAll(parseExceptionHandlers(flowElement));
		destroyLocalServiceRegistry(inlineFlow);
	}

	private void parseAndAddStateDefinitions(Element flowElement, Flow flow) {
		NodeList nodeList = flowElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element stateElement = (Element)node;
				if (ACTION_STATE_ELEMENT.equals(stateElement.getNodeName())) {
					parseAndAddActionState(stateElement, flow);
				}
				else if (VIEW_STATE_ELEMENT.equals(stateElement.getNodeName())) {
					parseAndAddViewState(stateElement, flow);
				}
				else if (DECISION_STATE_ELEMENT.equals(stateElement.getNodeName())) {
					parseAndAddDecisionState(stateElement, flow);
				}
				else if (SUBFLOW_STATE_ELEMENT.equals(stateElement.getNodeName())) {
					parseAndAddSubflowState(stateElement, flow);
				}
				else if (END_STATE_ELEMENT.equals(stateElement.getNodeName())) {
					parseAndAddEndState(stateElement, flow);
				}
			}
		}
		parseAndSetStartState(flowElement, flow);
	}

	private void parseAndSetStartState(Element element, Flow flow) {
		String startStateId = element.getAttribute(START_STATE_ATTRIBUTE);
		flow.setStartState(startStateId);
	}

	private void parseAndAddActionState(Element element, Flow flow) {
		getFlowArtifactFactory().createActionState(parseId(element), flow, parseEntryActions(element),
				parseAnnotatedActions(element), parseTransitions(element), parseExceptionHandlers(element),
				parseExitActions(element), parseAttributes(element));
	}

	private void parseAndAddViewState(Element element, Flow flow) {
		getFlowArtifactFactory().createViewState(parseId(element), flow, parseEntryActions(element),
				parseViewSelector(TextToViewSelector.VIEW_STATE_TYPE, element), parseTransitions(element),
				parseExceptionHandlers(element), parseExitActions(element), parseAttributes(element));
	}

	private void parseAndAddDecisionState(Element element, Flow flow) {
		getFlowArtifactFactory()
				.createDecisionState(parseId(element), flow, parseEntryActions(element), parseIfs(element),
						parseExceptionHandlers(element), parseExitActions(element), parseAttributes(element));
	}

	private void parseAndAddSubflowState(Element element, Flow flow) {
		getFlowArtifactFactory().createSubflowState(parseId(element), flow, parseEntryActions(element),
				parseSubflow(element), parseFlowAttributeMapper(element), parseTransitions(element),
				parseExceptionHandlers(element), parseExitActions(element), parseAttributes(element));
	}

	private void parseAndAddEndState(Element element, Flow flow) {
		getFlowArtifactFactory().createEndState(parseId(element), flow, parseEntryActions(element),
				parseViewSelector(TextToViewSelector.END_STATE_TYPE, element), parseOutputMapper(element),
				parseExceptionHandlers(element), parseAttributes(element));
	}

	private String parseId(Element element) {
		return element.getAttribute(ID_ATTRIBUTE);
	}

	private Action[] parseEntryActions(Element element) {
		List entryElements = DomUtils.getChildElementsByTagName(element, ENTRY_ACTIONS_ELEMENT);
		if (!entryElements.isEmpty()) {
			Element entryElement = (Element)entryElements.get(0);
			return parseAnnotatedActions(entryElement);
		}
		else {
			return null;
		}
	}

	private Action[] parseExitActions(Element element) {
		List exitElements = DomUtils.getChildElementsByTagName(element, EXIT_ACTIONS_ELEMENT);
		if (!exitElements.isEmpty()) {
			Element exitElement = (Element)exitElements.get(0);
			return parseAnnotatedActions(exitElement);
		}
		else {
			return null;
		}
	}

	private Transition[] parseTransitions(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		for (int i = 0; i < transitionElements.size(); i++) {
			Element transitionElement = (Element)transitionElements.get(i);
			if (!StringUtils.hasText(transitionElement.getAttribute(ON_EXCEPTION_ATTRIBUTE))) {
				transitions.add(parseTransition(transitionElement));
			}
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	private Transition parseTransition(Element element) {
		TransitionCriteria matchingCriteria = (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(ON_ATTRIBUTE));
		TransitionCriteria executionCriteria = TransitionCriteriaChain.criteriaChainFor(parseAnnotatedActions(element));
		TargetStateResolver targetStateResolver = (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(TO_ATTRIBUTE));
		return getFlowArtifactFactory().createTransition(matchingCriteria, executionCriteria, targetStateResolver,
				parseAttributes(element));
	}

	private ViewSelector parseViewSelector(String stateType, Element element) {
		String viewName = element.getAttribute(VIEW_ATTRIBUTE);
		Map context = new HashMap(1, 1);
		context.put(TextToViewSelector.STATE_TYPE_CONTEXT_PARAMETER, stateType);
		return (ViewSelector)fromStringTo(ViewSelector.class).execute(viewName, context);
	}

	private Flow parseSubflow(Element element) {
		return getLocalFlowServiceLocator().getSubflow(element.getAttribute(FLOW_ATTRIBUTE));
	}

	private AnnotatedAction[] parseAnnotatedActions(Element element) {
		List actions = new LinkedList();
		List actionElements = DomUtils.getChildElementsByTagName(element, ACTION_ELEMENT);
		Iterator it = actionElements.iterator();
		while (it.hasNext()) {
			actions.add(parseAnnotatedAction((Element)it.next()));
		}
		return (AnnotatedAction[])actions.toArray(new AnnotatedAction[actions.size()]);
	}

	private AnnotatedAction parseAnnotatedAction(Element element) {
		AnnotatedAction annotated = new AnnotatedAction(parseAction(element));
		if (element.hasAttribute(NAME_ATTRIBUTE)) {
			annotated.setName(element.getAttribute(NAME_ATTRIBUTE));
		}
		if (element.hasAttribute(METHOD_ATTRIBUTE)
				&& getLocalFlowServiceLocator().isAction(element.getAttribute(BEAN_ATTRIBUTE))) {
			annotated.setMethod(element.getAttribute(METHOD_ATTRIBUTE));
		}
		annotated.getAttributeMap().putAll(parseAttributes(element));
		return annotated;
	}

	private Action parseAction(Element element) {
		String actionId = element.getAttribute(BEAN_ATTRIBUTE);
		if (getLocalFlowServiceLocator().isAction(actionId)) {
			return getLocalFlowServiceLocator().getAction(actionId);
		}
		else {
			return parseBeanInvokingAction(actionId, element);
		}
	}

	private Action parseBeanInvokingAction(String beanId, Element element) {
		Assert.isTrue(element.hasAttribute(METHOD_ATTRIBUTE),
				"The method attribute is required for bean invoking actions");
		MethodSignature methodSignature = (MethodSignature)fromStringTo(MethodSignature.class).execute(
				element.getAttribute(METHOD_ATTRIBUTE));
		String resultName = null;
		if (element.hasAttribute(RESULT_NAME_ATTRIBUTE)) {
			resultName = element.getAttribute(RESULT_NAME_ATTRIBUTE);
		}
		ScopeType resultScope = null;
		if (element.hasAttribute(RESULT_SCOPE_ATTRIBUTE)
				&& !element.getAttribute(RESULT_SCOPE_ATTRIBUTE).equals(DEFAULT_VALUE)) {
			resultScope = (ScopeType)fromStringTo(ScopeType.class)
					.execute(element.getAttribute(RESULT_SCOPE_ATTRIBUTE));
		}
		ResultSpecification resultSpecification = null;
		if (resultName != null) {
			resultSpecification = new ResultSpecification(resultName, (resultScope != null ? resultScope
					: ScopeType.REQUEST));
		}
		return getBeanInvokingActionFactory().createBeanInvokingAction(beanId,
				getLocalFlowServiceLocator().getBeanFactory(), methodSignature, resultSpecification,
				getLocalFlowServiceLocator().getConversionService(), null);
	}

	private BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return getFlowServiceLocator().getBeanInvokingActionFactory();
	}

	private UnmodifiableAttributeMap parseAttributes(Element element) {
		AttributeMap attributes = new AttributeMap();
		List propertyElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_ELEMENT);
		for (int i = 0; i < propertyElements.size(); i++) {
			parseAndSetAttribute((Element)propertyElements.get(i), attributes);
		}
		return attributes.unmodifiable();
	}

	private void parseAndSetAttribute(Element element, AttributeMap attributes) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		String value = null;
		if (element.hasAttribute(VALUE_ATTRIBUTE)) {
			value = element.getAttribute(VALUE_ATTRIBUTE);
		}
		else {
			List valueElements = DomUtils.getChildElementsByTagName(element, VALUE_ELEMENT);
			Assert.state(valueElements.size() == 1, "A property value should be specified for property '" + name + "'");
			value = DomUtils.getTextValue((Element)valueElements.get(0));
		}
		attributes.put(name, convertPropertyValue(element, value));
	}

	private Object convertPropertyValue(Element element, String stringValue) {
		if (element.hasAttribute(TYPE_ATTRIBUTE)) {
			ConversionExecutor executor = fromStringTo(element.getAttribute(TYPE_ATTRIBUTE));
			if (executor != null) {
				// convert string value to instance of aliased type
				return executor.execute(stringValue);
			}
			else {
				Class targetClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(TYPE_ATTRIBUTE));
				// convert string value to instance of target class
				return fromStringTo(targetClass).execute(stringValue);
			}
		}
		else {
			return stringValue;
		}
	}

	private Transition[] parseIfs(Element element) {
		List transitions = new LinkedList();
		List transitionElements = DomUtils.getChildElementsByTagName(element, IF_ELEMENT);
		Iterator it = transitionElements.iterator();
		while (it.hasNext()) {
			transitions.addAll(Arrays.asList(parseIf((Element)it.next())));
		}
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	private Transition[] parseIf(Element element) {
		Transition thenTransition = parseThen(element);
		if (StringUtils.hasText(element.getAttribute(ELSE_ATTRIBUTE))) {
			Transition elseTransition = parseElse(element);
			return new Transition[] { thenTransition, elseTransition };
		}
		else {
			return new Transition[] { thenTransition };
		}
	}

	private Transition parseThen(Element element) {
		TransitionCriteria matchingCriteria = (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(
				element.getAttribute(TEST_ATTRIBUTE));
		TargetStateResolver targetStateResolver = (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(THEN_ATTRIBUTE));
		return getFlowArtifactFactory().createTransition(matchingCriteria, null, targetStateResolver, null);
	}

	private Transition parseElse(Element element) {
		TargetStateResolver targetStateResolver = (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(ELSE_ATTRIBUTE));
		return getFlowArtifactFactory().createTransition(null, null, targetStateResolver, null);
	}

	private FlowAttributeMapper parseFlowAttributeMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, ATTRIBUTE_MAPPER_ELEMENT);
		if (mapperElements.isEmpty()) {
			return null;
		}
		Element mapperElement = (Element)mapperElements.get(0);
		if (StringUtils.hasText(mapperElement.getAttribute(BEAN_ATTRIBUTE))) {
			return getLocalFlowServiceLocator().getAttributeMapper(mapperElement.getAttribute(BEAN_ATTRIBUTE));
		}
		else {
			return new ImmutableFlowAttributeMapper(parseInputMapper(mapperElement), parseOutputMapper(mapperElement));
		}
	}

	private AttributeMapper parseInputMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, INPUT_MAPPER_ELEMENT);
		return mapperElements.isEmpty() ? null : parseAttributeMapper((Element)mapperElements.get(0));
	}

	private AttributeMapper parseOutputMapper(Element element) {
		List mapperElements = DomUtils.getChildElementsByTagName(element, OUTPUT_MAPPER_ELEMENT);
		return mapperElements.isEmpty() ? null : parseAttributeMapper((Element)mapperElements.get(0));
	}

	private AttributeMapper parseAttributeMapper(Element element) {
		List mappingElements = DomUtils.getChildElementsByTagName(element, MAPPING_ELEMENT);
		DefaultAttributeMapper mapper = new DefaultAttributeMapper();
		Iterator it = mappingElements.iterator();
		ExpressionParser parser = getLocalFlowServiceLocator().getExpressionParser();
		while (it.hasNext()) {
			Element mappingElement = (Element)it.next();
			Expression source = parser.parseExpression(mappingElement.getAttribute(SOURCE_ATTRIBUTE));
			PropertyExpression target = null;
			if (StringUtils.hasText(mappingElement.getAttribute(TARGET_ATTRIBUTE))) {
				target = parser.parsePropertyExpression(mappingElement.getAttribute(TARGET_ATTRIBUTE));
			}
			else if (StringUtils.hasText(mappingElement.getAttribute(TARGET_COLLECTION_ATTRIBUTE))) {
				target = new CollectionAddingPropertyExpression(parser.parsePropertyExpression(mappingElement
						.getAttribute(TARGET_COLLECTION_ATTRIBUTE)));
			}
			mapper.addMapping(new Mapping(source, target, parseTypeConverter(mappingElement)));
		}
		return mapper;
	}

	private ConversionExecutor parseTypeConverter(Element element) {
		String from = element.getAttribute(FROM_ATTRIBUTE);
		String to = element.getAttribute(TO_ATTRIBUTE);
		if (StringUtils.hasText(from)) {
			if (StringUtils.hasText(to)) {
				ConversionService service = getLocalFlowServiceLocator().getConversionService();
				return service.getConversionExecutor(service.getClassByAlias(from), service.getClassByAlias(to));
			}
			else {
				throw new IllegalArgumentException("Use of the 'from' attribute requires use of the 'to' attribute");
			}
		}
		else {
			Assert.isTrue(!StringUtils.hasText(to), "Use of the 'to' attribute requires use of the 'from' attribute");
		}
		return null;
	}

	private StateExceptionHandler[] parseExceptionHandlers(Element element) {
		StateExceptionHandler[] transitionExecutingHandlers = parseTransitionExecutingExceptionHandlers(element);
		StateExceptionHandler[] customHandlers = parseCustomExceptionHandlers(element);
		StateExceptionHandler[] exceptionHandlers = new StateExceptionHandler[transitionExecutingHandlers.length
				+ customHandlers.length];
		System.arraycopy(transitionExecutingHandlers, 0, exceptionHandlers, 0, transitionExecutingHandlers.length);
		System.arraycopy(customHandlers, 0, exceptionHandlers, transitionExecutingHandlers.length,
				customHandlers.length);
		return exceptionHandlers;
	}

	private StateExceptionHandler[] parseTransitionExecutingExceptionHandlers(Element element) {
		List transitionElements = Collections.EMPTY_LIST;
		if (isFlowElement(element)) {
			List globalTransitionElements = DomUtils.getChildElementsByTagName(element, GLOBAL_TRANSITIONS_ELEMENT);
			if (!globalTransitionElements.isEmpty()) {
				Element globalTransitionsElement = (Element)globalTransitionElements.get(0);
				transitionElements = DomUtils.getChildElementsByTagName(globalTransitionsElement, TRANSITION_ELEMENT);
			}
		}
		else {
			transitionElements = DomUtils.getChildElementsByTagName(element, TRANSITION_ELEMENT);
		}
		List exceptionHandlers = new LinkedList();
		for (int i = 0; i < transitionElements.size(); i++) {
			Element transitionElement = (Element)transitionElements.get(i);
			if (StringUtils.hasText(transitionElement.getAttribute(ON_EXCEPTION_ATTRIBUTE))) {
				exceptionHandlers.add(parseTransitionExecutingExceptionHandler(transitionElement));
			}
		}
		return (StateExceptionHandler[])exceptionHandlers.toArray(new StateExceptionHandler[exceptionHandlers.size()]);
	}

	private StateExceptionHandler parseTransitionExecutingExceptionHandler(Element element) {
		TransitionExecutingStateExceptionHandler handler = new TransitionExecutingStateExceptionHandler();
		Class exceptionClass = (Class)fromStringTo(Class.class).execute(element.getAttribute(ON_EXCEPTION_ATTRIBUTE));
		handler.add(exceptionClass, (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(
				element.getAttribute(TO_ATTRIBUTE)));
		return handler;
	}

	private StateExceptionHandler[] parseCustomExceptionHandlers(Element element) {
		List exceptionHandlers = new LinkedList();
		List handlerElements = DomUtils.getChildElementsByTagName(element, EXCEPTION_HANDLER_ELEMENT);
		for (int i = 0; i < handlerElements.size(); i++) {
			Element handlerElement = (Element)handlerElements.get(i);
			exceptionHandlers.add(parseCustomExceptionHandler(handlerElement));
		}
		return (StateExceptionHandler[])exceptionHandlers.toArray(new StateExceptionHandler[exceptionHandlers.size()]);
	}

	private StateExceptionHandler parseCustomExceptionHandler(Element element) {
		return getLocalFlowServiceLocator().getExceptionHandler(element.getAttribute(BEAN_ATTRIBUTE));
	}

	private void destroyLocalServiceRegistry(Flow flow) {
		localFlowServiceLocator.pop();
	}
}