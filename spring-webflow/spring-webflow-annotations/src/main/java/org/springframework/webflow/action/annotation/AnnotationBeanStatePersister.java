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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.BeanStatePersister;

/**
 * Uses annotations to determine which beans are stateful and which fields on
 * those stateful beans should be saved and restored.
 * @author Keith Donald
 */
public class AnnotationBeanStatePersister implements BeanStatePersister {

	public void saveState(Object bean, RequestContext context) throws Exception {
		if (!bean.getClass().isAnnotationPresent(Stateful.class)) {
			return;
		}
		Stateful stateful = bean.getClass().getAnnotation(Stateful.class);
		Map memento = (Map)context.getFlowScope().getAttribute(stateful.name());
		Field[] fields = bean.getClass().getDeclaredFields();
		if (memento == null) {
			memento = new HashMap(fields.length);
			context.getFlowScope().setAttribute(stateful.name(), memento);
		}
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!field.isAnnotationPresent(Transient.class)) {
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				memento.put(field.getName(), field.get(bean));
				field.setAccessible(accessible);
			}
		}
	}

	public void restoreState(Object bean, RequestContext context) throws Exception {
		if (!bean.getClass().isAnnotationPresent(Stateful.class)) {
			return;
		}
		Stateful stateful = bean.getClass().getAnnotation(Stateful.class);
		Map memento = (Map)context.getFlowScope().get(stateful.name());
		if (memento != null) {
			Field[] fields = bean.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (!field.isAnnotationPresent(Transient.class)) {
					boolean accessible = field.isAccessible();
					field.setAccessible(true);
					field.set(bean, memento.get(field.getName()));
					field.setAccessible(accessible);
				}
			}
		}
	}
}