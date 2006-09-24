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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a bean has state that should be managed by Web Flow. Beans
 * marked stateful will automatically have all non
 * &#64;Transient fields saved out to flow scope after each invocation. In addition,
 * persistent fields will be restored from flow scope before any subsequent
 * invocations.
 * 
 * @author Keith Donald
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Stateful {

	/**
	 * Returns the name of the stateful bean, typically used to index the
	 * memento responisble for holding the bean's state in flow scope.
	 * @return the name of the bean
	 */
	String name();
}
