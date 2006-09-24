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
package org.springframework.webflow.registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;

/**
 * A factory bean that produces a populated flow registry using a
 * {@link XmlFlowRegistrar}. This is the simplest implementation to use when
 * using a Spring BeanFactory to deploy an explicit registry of XML-based Flow
 * definitions for execution.
 * <p>
 * By default, a configured flow definition will be assigned a registry
 * identifier equal to the filename of the underlying definition resource, minus
 * the filename extension. For example, a XML-based flow definition defined in
 * the file <code>flow1.xml</code> will be identified as <code>flow1</code>
 * in the registry created by this factory bean.
 * <p>
 * This class is also <code>BeanFactoryAware</code> and when used with Spring
 * will automatically create a configured {@link DefaultFlowServiceLocator}
 * for loading Flow artifacts like Actions from the Spring bean factory during
 * the Flow registration process.
 * <p>
 * This class is also <code>ResourceLoaderAware</code>; when an instance is
 * created by a Spring BeanFactory the factory will automatically configure the
 * XmlFlowRegistrar with a context-relative resource loader for accessing other
 * resources during Flow assembly.
 * 
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowLocations&quot;&gt; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt; 
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 */
public class XmlFlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {

	/**
	 * The flow registrar that will perform the definition registrations.
	 */
	private XmlFlowRegistrar flowRegistrar = createFlowRegistrar();

	/**
	 * Temporary holder for flow definitions configured using a property map.
	 */
	private Properties flowDefinitions;

	/**
	 * Factory method that returns a new externalized flow registrar. Subclasses
	 * may override.
	 * @return the flow registrar to use
	 */
	protected XmlFlowRegistrar createFlowRegistrar() {
		return new XmlFlowRegistrar();
	}

	/**
	 * Returns the configured externalized flow registrar.
	 */
	protected XmlFlowRegistrar getFlowRegistrar() {
		return flowRegistrar;
	}

	/**
	 * Sets the locations (resource file paths) pointing to XML-based flow
	 * definitions.
	 * <p>
	 * When configuring as a spring bean definition, ANT-style resource
	 * patterns/wildcards are also supported, taking advantage of Spring's built
	 * in ResourceArrayPropertyEditor machinery.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 *     &lt;bean id=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
	 *         &lt;property name=&quot;flowLocations&quot;&gt; value=&quot;/WEB-INF/flows/*-flow.xml&quot;/&gt; 
	 *     &lt;/bean&gt;
	 * </pre>
	 * 
	 * Another example:
	 * 
	 * <pre>
	 *    &lt;bean id=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
	 *        &lt;property name=&quot;flowLocations&quot;&gt; value=&quot;classpath*:/example/flows/*-flow.xml&quot;/&gt; 
	 *    &lt;/bean&gt;
	 * </pre>
	 * 
	 * Flows registered from this set will be automatically assigned an id based
	 * on the filename of the matched XML resource.
	 * @param locations the resource locations
	 */
	public void setFlowLocations(Resource[] locations) {
		getFlowRegistrar().setFlowLocations(locations);
	}

	/**
	 * Convenience method that allows for setting externalized flow definitions
	 * from a <code>java.util.Properties</code> map. Allows for more control
	 * over the definition, including which <code>flowId</code> is assigned.
	 * <p>
	 * 
	 * Each property key is the <code>flowId</code> and each property value is
	 * the string encoded location of the externalized flow definition resource.
	 * <p>
	 * Here is the exact format:
	 * 
	 * <pre>
	 *     flow id=resource
	 * </pre>
	 * 
	 * For example:
	 * 
	 * <pre>
	 *     &lt;bean id=&quot;flowRegistry&quot; class=&quot;org.springframework.webflow.registry.XmlFlowRegistryFactoryBean&quot;&gt;
	 *         &lt;property name=&quot;flowDefinitions&quot;&gt;
	 *             &lt;value&gt;
	 *                 searchFlow=/WEB-INF/flows/search.xml
	 *                 detailFlow=/WEB-INF/flows/detail.xml
	 *             &lt;/value&gt;
	 *         &lt;/property&gt;
	 *     &lt;/bean&gt;
	 * </pre>
	 * 
	 * The flow id key and resource value is required. Flow attributes (e.g.
	 * attr1) are optional.
	 * @param flowDefinitions the flow definition, defined within a properties map
	 */
	public void setFlowDefinitions(Properties flowDefinitions) {
		this.flowDefinitions = flowDefinitions;
	}

	/**
	 * Sets whether or not the flow builder used to build the flow definitions in this 
	 * registry should perform build-time validation.
	 * @param builderValidating the validating flag
	 */
	public void setBuilderValidating(boolean builderValidating) {
		getFlowRegistrar().setBuilderValidating(builderValidating);
	}
	
	/**
	 * Sets the entity resolver to use during Xml flow definition building.
	 * @param entityResolver the entity resolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		getFlowRegistrar().setEntityResolver(entityResolver);
	}
	
	protected void doPopulate(FlowRegistry registry) {
		addFlowDefinitionsFromPropertiesIfNecessary();
		getFlowRegistrar().registerFlows(registry, getFlowServiceLocator());
	}

	private void addFlowDefinitionsFromPropertiesIfNecessary() {
		if (flowDefinitions != null && flowDefinitions.size() > 0) {
			List flows = new ArrayList(flowDefinitions.size());
			Iterator it = flowDefinitions.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String flowId = (String)entry.getKey();
				String location = (String)entry.getValue();
				Resource resource = getFlowServiceLocator().getResourceLoader().getResource(location);
				flows.add(new ExternalizedFlowDefinition(flowId, resource));
			}
			getFlowRegistrar().addFlowDefinitions(
					(ExternalizedFlowDefinition[])flows.toArray(new ExternalizedFlowDefinition[0]));
			flowDefinitions = null;
		}
	}
}