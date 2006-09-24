package org.springframework.webflow.samples.sellitem;

import java.io.File;

import org.easymock.MockControl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.test.AbstractXmlFlowExecutionTests;
import org.springframework.webflow.test.MockFlowServiceLocator;
import org.springframework.webflow.test.MockParameterMap;

public class SellItemFlowExecutionTests extends AbstractXmlFlowExecutionTests {

	private MockControl saleProcessorControl;

	private SaleProcessor saleProcessor;

	@Override
	protected ExternalizedFlowDefinition getFlowDefinition() {
		File flowDir = new File("src/main/webapp/WEB-INF");
		Resource resource = new FileSystemResource(new File(flowDir, "sellitem.xml"));
		return new ExternalizedFlowDefinition("search", resource);
	}

	public void testStartFlow() {
		ApplicationView selectedView = applicationView(startFlow());
		assertModelAttributeNotNull("sale", selectedView);
		assertViewNameEquals("priceAndItemCountForm", selectedView);
	}

	public void testSubmitPriceAndItemCount() {
		testStartFlow();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("itemCount", "4");
		parameters.put("price", "25");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("categoryForm", selectedView);
	}

	public void testSubmitCategoryForm() {
		testSubmitPriceAndItemCount();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("category", "A");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();
	}

	public void testSubmitCategoryFormWithShipping() {
		testSubmitPriceAndItemCount();
		MockParameterMap parameters = new MockParameterMap();
		parameters.put("category", "A");
		parameters.put("shipping", "true");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("shippingDetailsForm", selectedView);
	}

	public void testSubmitShippingDetailsForm() {
		testSubmitCategoryFormWithShipping();

		saleProcessor.process((Sale)getRequiredConversationAttribute("sale", Sale.class));
		saleProcessorControl.replay();

		MockParameterMap parameters = new MockParameterMap();
		parameters.put("shippingType", "E");
		ApplicationView selectedView = applicationView(signalEvent("submit", parameters));
		assertViewNameEquals("costOverview", selectedView);
		assertFlowExecutionEnded();

		saleProcessorControl.verify();
	}

	@Override
	protected FlowServiceLocator createFlowServiceLocator() {
		saleProcessorControl = MockControl.createControl(SaleProcessor.class);
		saleProcessor = (SaleProcessor)saleProcessorControl.getMock();
		MockFlowServiceLocator flowServiceLocator = new MockFlowServiceLocator();
		flowServiceLocator.registerBean("saleProcessor", saleProcessor);
		return flowServiceLocator;
	}
}