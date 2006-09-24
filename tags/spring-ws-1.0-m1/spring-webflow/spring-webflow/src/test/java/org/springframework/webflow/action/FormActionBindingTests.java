/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.Errors;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit test for the FormAction class, dealing with binding related issues.
 * 
 * @see org.springframework.webflow.action.FormAction
 * 
 * @author Erwin Vervaet
 */
public class FormActionBindingTests extends TestCase {

	public static class TestBean {

		private Long prop;

		public Long getProp() {
			return prop;
		}

		public void setProp(Long prop) {
			this.prop = prop;
		}
	}

	public void testMessageCodesOnBindFailure() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.addParameter("prop", "A");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockRequestContext context = new MockRequestContext();
		context.setExternalContext(new ServletExternalContext(null, request, response));
		context.setAttribute("method", "bindAndValidate");

		// use a FormAction to do the binding
		FormAction formAction = new FormAction();
		formAction.setFormObjectClass(TestBean.class);
		formAction.setFormObjectName("formObject");
		formAction.execute(context);
		Errors formActionErrors = (Errors)context.getRequestScope().get(FormObjectAccessor.getCurrentFormErrorsName());
		assertNotNull(formActionErrors);
		assertTrue(formActionErrors.hasErrors());

		assertEquals(1, formActionErrors.getErrorCount());
		assertEquals(0, formActionErrors.getGlobalErrorCount());
		assertEquals(1, formActionErrors.getFieldErrorCount("prop"));
	}
}
