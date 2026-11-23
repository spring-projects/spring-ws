/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Arjen Poutsma
 */
class XmppConnectionFactoryBeanTests {

	private XmppConnectionFactoryBean factoryBean;

	@BeforeEach
	void createFactoryBean() {
		this.factoryBean = new XmppConnectionFactoryBean();
	}

	@Test
	void noHost() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.factoryBean.afterPropertiesSet());
	}

	@Test
	void noUsername() {

		assertThatIllegalArgumentException().isThrownBy(() -> {

			this.factoryBean.setHost("jabber.org");
			this.factoryBean.afterPropertiesSet();
		});
	}

	@Test
	void wrongPort() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.factoryBean.setPort(-10));
	}

}
