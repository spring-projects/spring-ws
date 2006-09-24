package org.springframework.webflow.registry;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.BaseFlowServiceLocator;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.builder.SimpleFlowBuilder;

public class FlowRegistryPopulationTests extends TestCase {
	public void testDefaultPopulation() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowServiceLocator factory = new BaseFlowServiceLocator();
		FlowBuilder builder1 = new AbstractFlowBuilder(factory) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		new FlowAssembler("flow1", builder1).assembleFlow();

		FlowBuilder builder2 = new AbstractFlowBuilder(factory) {
			public void buildStates() throws FlowBuilderException {
				addEndState("end");
			}
		};
		new FlowAssembler("flow2", builder2).assembleFlow();

		registry.registerFlow(new StaticFlowHolder(builder1.getFlow()));
		registry.registerFlow(new StaticFlowHolder(builder2.getFlow()));
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
	}

	public void testXmlPopulationWithRecursion() {
		FlowRegistryImpl registry = new FlowRegistryImpl();
		FlowServiceLocator flowArtifactFactory = new DefaultFlowServiceLocator(registry,
				new DefaultListableBeanFactory());
		File parent = new File("src/test/java/org/springframework/webflow/registry");
		XmlFlowRegistrar registrar = new XmlFlowRegistrar();
		registrar.addFlowLocation(new FileSystemResource(new File(parent, "flow1.xml")));
		registrar.addFlowLocation(new FileSystemResource(new File(parent, "flow2.xml")));
		registrar.addFlowDefinition(new ExternalizedFlowDefinition("flow3", new FileSystemResource(new File(parent,
				"flow2.xml"))));
		registrar.registerFlows(registry, flowArtifactFactory);
		assertEquals("Wrong registry definition count", 3, registry.getFlowCount());
		registry.refresh();
		assertEquals("Wrong registry definition count", 3, registry.getFlowCount());
	}

	public void testFlowRegistryFactoryBean() throws Exception {
		GenericApplicationContext beanFactory = new GenericApplicationContext();
		FlowRegistryFactoryBean factoryBean = new FlowRegistryFactoryBean();
		factoryBean.setFlowRegistrar(new MyFlowRegistrar());
		factoryBean.setBeanFactory(beanFactory);
		factoryBean.afterPropertiesSet();
		FlowRegistry registry = factoryBean.populateFlowRegistry();
		assertEquals("Wrong registry definition count", 3, registry.getFlowCount());
	}

	public void testXmlFlowRegistryFactoryBean() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry2");
		assertEquals("Wrong registry definition count", 7, registry.getFlowCount());
	}

	public void testXmlFlowRegistryFactoryBeanFlowDefinitionProperties() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry3");
		assertEquals("Wrong registry definition count", 2, registry.getFlowCount());
		registry.getFlow("flow1");
		registry.getFlow("flow2");
	}

	public void testCustomFlowRegistryServicesWithParentRegistry() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml", getClass()));
		FlowRegistry registry = (FlowRegistry)ac.getBean("flowRegistry4");
		assertEquals("Wrong registry definition count", 7, registry.getFlowCount());
		registry.getFlow("flow1");
		registry.getFlow("flow2");
		registry.getFlow("flow5");
		registry.getFlow("flow6");
	}
	
	public static class MyFlowRegistrar extends FlowRegistrarSupport {
		public void registerFlows(FlowRegistry registry, FlowServiceLocator flowArtifactFactory) {
			File parent = new File("src/test/org/springframework/webflow/registry");
			registerXmlFlow(new FileSystemResource(new File(parent, "flow1.xml")), registry, flowArtifactFactory);
			registerXmlFlow(new FileSystemResource(new File(parent, "flow2.xml")), registry, flowArtifactFactory);
			registerFlow("flow3", registry, new SimpleFlowBuilder());
		}
	}
}