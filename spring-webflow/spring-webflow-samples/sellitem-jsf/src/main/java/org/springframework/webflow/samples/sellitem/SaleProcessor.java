package org.springframework.webflow.samples.sellitem;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SaleProcessor {
	public void process(Sale sale);
}
