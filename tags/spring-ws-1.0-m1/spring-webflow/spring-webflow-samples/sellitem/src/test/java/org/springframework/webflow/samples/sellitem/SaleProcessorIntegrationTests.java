package org.springframework.webflow.samples.sellitem;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;


public class SaleProcessorIntegrationTests extends AbstractTransactionalDataSourceSpringContextTests {

	private SaleProcessor saleProcessor;

	public void setSaleProcessor(SaleProcessor saleProcessor) {
		this.saleProcessor = saleProcessor;
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/sellitem/applicationContext.xml" };
	}

	public void testProcessSale() {
		int beforeCount = jdbcTemplate.queryForInt("select count(*) from T_SALES");
		Sale sale = new Sale();
		sale.setItemCount(25);
		sale.setPrice(100.0);
		sale.setCategory("A");
		sale.setShippingType("Express");
		saleProcessor.process(sale);
		int afterCount = jdbcTemplate.queryForInt("select count(*) from T_SALES");
		assertEquals("Wrong after count", beforeCount + 1, afterCount);
	}
}