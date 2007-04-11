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

package org.springframework.ws.transport.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for standalone, server-side transport objects. Contains a Spring {@link TaskExecutor}, and
 * various lifecycle callbacks.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMessagingContainer extends SimpleWebServiceMessageReceiverObjectSupport
        implements Lifecycle, DisposableBean, BeanNameAware {

    /** Default thread name prefix. */
    public final String DEFAULT_THREAD_NAME_PREFIX = ClassUtils.getShortName(getClass()) + "-";

    private volatile boolean active = false;

    private boolean autoStartup = true;

    private boolean running = false;

    private final Object lifecycleMonitor = new Object();

    private TaskExecutor taskExecutor;

    private String beanName;

    /**
     * Set whether to automatically start the listener after initialization.
     * <p/>
     * Default is <code>true</code>; set this to <code>false</code> to allow for manual startup.
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /**
     * Set the Spring {@link TaskExecutor} to use for running the listener threads. Default is {@link
     * SimpleAsyncTaskExecutor}, starting up a number of new threads.
     * <p/>
     * Specify an alternative task executor for integration with an existing thread pool, such as the {@link
     * org.springframework.scheduling.commonj.WorkManagerTaskExecutor} to integrate with WebSphere or WebLogic.
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /** Returns the task executor. */
    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /** Return whether this server is currently active, that is, whether it has been set up but not shut down yet. */
    public final boolean isActive() {
        synchronized (lifecycleMonitor) {
            return active;
        }
    }

    /** Return whether this server is currently running, that is, whether it has been started and not stopped yet. */
    public final boolean isRunning() {
        synchronized (lifecycleMonitor) {
            return running;
        }
    }

    /**
     * Create a default TaskExecutor. Called if no explicit TaskExecutor has been specified.
     * <p/>
     * The default implementation builds a {@link org.springframework.core.task.SimpleAsyncTaskExecutor} with the
     * specified bean name (or the class name, if no bean name specified) as thread name prefix.
     *
     * @see org.springframework.core.task.SimpleAsyncTaskExecutor#SimpleAsyncTaskExecutor(String)
     */
    protected TaskExecutor createDefaultTaskExecutor() {
        String threadNamePrefix = beanName != null ? beanName + "-" : DEFAULT_THREAD_NAME_PREFIX;
        return new SimpleAsyncTaskExecutor(threadNamePrefix);
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (taskExecutor == null) {
            taskExecutor = createDefaultTaskExecutor();
        }
        activate();
    }

    /**
     * Calls <code>shutdown</code> when the BeanFactory destroys the server instance.
     *
     * @see #shutdown()
     */
    public void destroy() {
        shutdown();
    }

    /** Initialize this server. Starts the server if <code>autoStartup</code> hasn't been turned off. */
    public final void activate() throws Exception {
        synchronized (lifecycleMonitor) {
            active = true;
            lifecycleMonitor.notifyAll();
        }
        onActivate();
        if (autoStartup) {
            start();
        }
    }

    /** Start this server. */
    public final void start() {
        synchronized (lifecycleMonitor) {
            running = true;
            lifecycleMonitor.notifyAll();
        }
        onStart();
    }

    /** Stop this server. */
    public final void stop() {
        synchronized (lifecycleMonitor) {
            running = false;
            lifecycleMonitor.notifyAll();
        }
        onStop();
    }

    /** Shut down the registered listeners and close this listener container. */
    public final void shutdown() {
        synchronized (lifecycleMonitor) {
            running = false;
            active = false;
            lifecycleMonitor.notifyAll();
        }
        onShutdown();
    }

    protected abstract void onActivate() throws Exception;

    protected abstract void onStart();

    protected abstract void onStop();

    protected abstract void onShutdown();

}
