package org.springframework.webflow.samples.sellitem;

import org.springframework.jdbc.core.support.JdbcDaoSupport;


public class InMemoryDatabaseCreator extends JdbcDaoSupport {

	@Override
	protected void initDao() throws Exception {
		String createSales = "create table T_SALES (ID int not null identity primary key, ITEM_COUNT int not null, PRICE double NOT NULL, category VARCHAR(1) NOT NULL, SHIPPING_TYPE varchar(1))";
		getJdbcTemplate().execute(createSales);
	}

}
