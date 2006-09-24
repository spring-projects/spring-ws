/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.action.annotation;

import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.method.MethodKey;
import org.springframework.binding.support.Assert;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests testing bean state persisters.
 * 
 * @author Keith Donald
 */
public class StateManagementTests extends TestCase {

	@SuppressWarnings(value = "all")
	@Stateful(name = "bean")
	public static class Bean {
		private String datum1 = "initial value";

		private Integer datum2;

		private boolean executed;

		@Transient
		private NotSerializable datum3;

		public void execute() {
			this.executed = true;
		}
	}

	public void testStateSavingAndRestoring() throws Exception {
		Bean bean = new Bean();
		LocalBeanInvokingAction action = new LocalBeanInvokingAction(bean);
		action.setStatePersister(new AnnotationBeanStatePersister());
		MockRequestContext context = new MockRequestContext();
		context.setProperty("method", new MethodKey("execute"));
		action.execute(context);
		assertNotNull("Bean memento not saved", context.getFlowScope().get("bean"));
		MapAttributeSource map = new MapAttributeSource((Map)context.getFlowScope().get("bean"));
		assertNotNull("Bean memento not saved", context.getFlowScope().get("bean"));
		Assert.attributeEquals(map, "datum1", "initial value");
		Assert.attributeNotPresent(map, "datum3");
		map.getAttributeMap().put("datum1", "new value");
		map.getAttributeMap().put("datum2", new Integer(12345));
		action.execute(context);
		assertEquals("Wrong value", "new value", bean.datum1);
		assertEquals("Wrong value", new Integer(12345), bean.datum2);
	}

	public static class NotSerializable extends Object {

	}
}