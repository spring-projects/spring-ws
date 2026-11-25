/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractWebServiceMessageFactoryTests {

	protected WebServiceMessageFactory messageFactory;

	@BeforeEach
	public final void setUp() throws Exception {
		this.messageFactory = createMessageFactory();
	}

	@Test
	protected void testCreateEmptyMessage() {

		WebServiceMessage message = this.messageFactory.createWebServiceMessage();

		assertThat(message).isNotNull();
	}

	@Test
	void maxMessageSizeCanBeSetInXmlConfiguration() {
		WebServiceMessageFactory factory = loadMessageFactoryFromXmlConfiguration("20971520");
		assertThat(ReflectionTestUtils.getField(factory, "maxMessageSize")).isEqualTo(DataSize.ofMegabytes(20));
	}

	@Test
	void maxMessageSizeCanBeDisabledInXmlConfiguration() {
		WebServiceMessageFactory factory = loadMessageFactoryFromXmlConfiguration("-1");
		assertThat(ReflectionTestUtils.getField(factory, "maxMessageSize")).isEqualTo(DataSize.ofBytes(-1));
	}

	private WebServiceMessageFactory loadMessageFactoryFromXmlConfiguration(String maxMessageBytes) {
		String xml = """
				<beans xmlns="http://www.springframework.org/schema/beans"
				       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				       xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd">
				    <bean id="messageFactory" class="%s">
				        <property name="maxMessageBytes" value="%s"/>
				    </bean>
				</beans>
				"""
			.formatted(this.messageFactory.getClass().getName(), maxMessageBytes);
		try (GenericXmlApplicationContext context = new GenericXmlApplicationContext()) {
			context.load(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8)));
			context.refresh();
			return context.getBean("messageFactory", WebServiceMessageFactory.class);
		}
	}

	protected abstract WebServiceMessageFactory createMessageFactory() throws Exception;

}
