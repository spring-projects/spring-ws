/*
 * Copyright 2005-2014 the original author or authors.
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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

/**
 * Abstract base class for standalone, server-side transport objects. Provides a basic, thread-safe implementation of
 * the {@link Lifecycle} interface, and various template methods to be implemented by concrete sub classes.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractStandaloneMessageReceiver extends SimpleWebServiceMessageReceiverObjectSupport
        implements Lifecycle, DisposableBean {

    private volatile boolean active = false;

    private boolean autoStartup = true;

    private boolean running = false;

    private final Object lifecycleMonitor = new Object();

    /** Return whether this server is currently active, that is, whether it has been set up but not shut down yet. */
    public final boolean isActive() {
        synchronized (lifecycleMonitor) {
            return active;
        }
    }

    /** Return whether this server is currently running, that is, whether it has been started and not stopped yet. */
    @Override
    public final boolean isRunning() {
        synchronized (lifecycleMonitor) {
            return running;
        }
    }

    /**
     * Set whether to automatically start the receiver after initialization.
     *
     * <p>Default is {@code true}; set this to {@code false} to allow for manual startup.
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /** Calls {@link #activate()} when the BeanFactory initializes the receiver instance. */
    @Override
    public void afterPropertiesSet() throws Exception {
        activate();
    }

    /** Calls {@link #shutdown()} when the BeanFactory destroys the receiver instance. */
    @Override
    public void destroy() {
        shutdown();
    }

    /**
     * Initialize this server. Starts the server if {@link #setAutoStartup(boolean) autoStartup} hasn't been turned
     * off.
     */
    public final void activate() throws Exception {
        synchronized (lifecycleMonitor) {
            active = true;
        }
        onActivate();
        if (autoStartup) {
            start();
        }
    }

    /** Start this server. */
    @Override
    public final void start() {
        synchronized (lifecycleMonitor) {
            running = true;
        }
        onStart();
    }

    /** Stop this server. */
    @Override
    public final void stop() {
        synchronized (lifecycleMonitor) {
            running = false;
        }
        onStop();
    }

    /** Shut down this server. */
    public final void shutdown() {
        synchronized (lifecycleMonitor) {
            running = false;
            active = false;
        }
        onShutdown();
    }

    /**
     * Template method invoked when {@link #activate()} is invoked.
     *
     * @throws Exception in case of errors
     */
    protected abstract void onActivate() throws Exception;

    /** Template method invoked when {@link #start()} is invoked. */
    protected abstract void onStart();

    /** Template method invoked when {@link #stop()} is invoked. */
    protected abstract void onStop();

    /** Template method invoked when {@link #shutdown()} is invoked. */
    protected abstract void onShutdown();
}
