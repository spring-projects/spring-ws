/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.transport.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for asynchronous standalone, server-side transport objects. Contains a Spring
 * {@link TaskExecutor}, and various lifecycle callbacks.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractAsyncStandaloneMessageReceiver extends AbstractStandaloneMessageReceiver
		implements BeanNameAware {

	/** Default thread name prefix. */
	public final String DEFAULT_THREAD_NAME_PREFIX = ClassUtils.getShortName(getClass()) + "-";

	private TaskExecutor taskExecutor;

	private String beanName;

	/**
	 * Set the Spring {@link TaskExecutor} to use for running the listener threads. Default is
	 * {@link SimpleAsyncTaskExecutor}, starting up a number of new threads.
	 * <p>
	 * Specify an alternative task executor for integration with an existing thread pool, such as the
	 * {@link org.springframework.scheduling.commonj.WorkManagerTaskExecutor} to integrate with WebSphere or WebLogic.
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (taskExecutor == null) {
			taskExecutor = createDefaultTaskExecutor();
		}
		super.afterPropertiesSet();
	}

	/**
	 * Create a default TaskExecutor. Called if no explicit TaskExecutor has been specified.
	 * <p>
	 * The default implementation builds a {@link org.springframework.core.task.SimpleAsyncTaskExecutor} with the
	 * specified bean name (or the class name, if no bean name specified) as thread name prefix.
	 *
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor#SimpleAsyncTaskExecutor(String)
	 */
	protected TaskExecutor createDefaultTaskExecutor() {
		String threadNamePrefix = beanName != null ? beanName + "-" : DEFAULT_THREAD_NAME_PREFIX;
		return new SimpleAsyncTaskExecutor(threadNamePrefix);
	}

	/**
	 * Executes the given {@link Runnable} via this receiver's {@link TaskExecutor}.
	 *
	 * @see #setTaskExecutor(TaskExecutor)
	 */
	protected void execute(Runnable runnable) {
		taskExecutor.execute(runnable);
	}
}
