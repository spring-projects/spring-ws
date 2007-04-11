/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;

/** TestCase for {@link AbstractAnnotationEndpointMapping} */
public class AnnotationEndpointMappingTest extends TestCase {

    private AbstractAnnotationEndpointMapping mapping;

    private StaticApplicationContext applicationContext;

    protected void setUp() throws Exception {
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", MyAnnotationEndpointMapping.class);
        applicationContext.registerSingleton("endpoint", MyEndpoint.class);
        applicationContext.registerSingleton("other", OtherBean.class);
        applicationContext.refresh();
        mapping = (AbstractAnnotationEndpointMapping) applicationContext.getBean("mapping");
    }

    public void testRegistration() throws NoSuchMethodException {
        MethodEndpoint endpoint = mapping.lookupEndpoint("arg");
        assertNotNull("MethodEndpoint not registered", endpoint);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, endpoint);

        assertNull("Invalid endpoint registered", mapping.lookupEndpoint("arg2"));
    }

    private static class MyAnnotationEndpointMapping extends AbstractAnnotationEndpointMapping {

        protected String getLookupKeyForMethod(Method method) {
            MyAnnotation annotation = AnnotationUtils.getAnnotation(method, MyAnnotation.class);
            if (annotation != null) {
                return annotation.value();
            }
            else {
                return null;
            }
        }

        protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
            return "arg";
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyAnnotation {

        String value();

    }

    @Endpoint
    private static class MyEndpoint {

        @MyAnnotation("arg")
        public void doIt() {

        }

    }

    private static class OtherBean {

        @MyAnnotation("arg2")
        public void doIt() {

        }

    }

}