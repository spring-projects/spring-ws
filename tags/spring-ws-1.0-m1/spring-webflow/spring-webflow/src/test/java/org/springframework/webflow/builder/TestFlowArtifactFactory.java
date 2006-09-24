package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.registry.NoSuchFlowDefinitionException;

/**
 * Flow service locator for the services needed by the testFlow (defined in
 * testFlow.xml)
 * 
 * @author Erwin Vervaet
 */
public class TestFlowArtifactFactory extends BaseFlowServiceLocator {

	public StaticListableBeanFactory registry = new StaticListableBeanFactory();
	
	public TestFlowArtifactFactory() {
		init();
	}
	
	public void init() {
		registry.addBean("action1", new TestAction());
		registry.addBean("action2", new TestAction());
		registry.addBean("multiAction", new TestMultiAction());
		registry.addBean("pojoAction", new TestPojo());
		registry.addBean("attributeMapper1", new TestAttributeMapper());
	}
	
	public Flow getSubflow(String id) throws FlowArtifactException {
		if ("subFlow1".equals(id) || "subFlow2".equals(id)) {
			Flow flow = new Flow(id);
			new EndState(flow, "finish");
			return flow;
		}
		throw new NoSuchFlowDefinitionException(id, new String[] {"subFlow1", "subFlow2" });
	}

	public class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			if (context.getFlowExecutionContext().getFlow().getAttributeMap().contains("scenario2")) {
				return new Event(this, "event2");
			}
			return new Event(this, "event1");
		}
	}
	
	public class TestMultiAction extends MultiAction {
		public Event actionMethod(RequestContext context) throws Exception {
			throw new MyCustomException("Oops!");
		}
	}

	public class TestPojo {
		public boolean booleanMethod() {
			return true;
		}

		public LabeledEnum enumMethod() {
			return FlowSessionStatus.CREATED;
		}
	}
	
	public class TestAttributeMapper implements FlowAttributeMapper {
		public AttributeMap createSubflowInput(RequestContext context) {
			return new AttributeMap();
		}

		public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		}
	}

	public BeanFactory getBeanFactory() throws UnsupportedOperationException {
		return registry;
	}
	
}