package org.springframework.webflow.samples.sellitem;

import org.springframework.jdbc.core.support.JdbcDaoSupport;


public class JdbcSaleProcessor extends JdbcDaoSupport implements SaleProcessor {
	public void process(Sale sale) {
		getJdbcTemplate().update("insert into T_SALES values (?, ?, ?, ?, ?)",
				new Object[] { null, sale.getPrice(), sale.getItemCount(), sale.getCategory(), sale.getShippingType() });
	}
}
