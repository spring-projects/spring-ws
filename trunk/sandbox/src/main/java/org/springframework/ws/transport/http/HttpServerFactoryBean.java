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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;
import org.springframework.util.Assert;

/**
 * Factory bean for the HTTP server built into Java 6.
 *
 * @author Arjen Poutsma
 * @see
 * @since 1.5.0
 */
public class HttpServerFactoryBean implements ApplicationContextAware, InitializingBean, Lifecycle, FactoryBean {

    private static final Log logger = LogFactory.getLog(HttpServerFactoryBean.class);

    public static final int DEFAULT_PORT = 8080;

    private InetAddress bindAddress;

    private int port = DEFAULT_PORT;

    private int backlog = -1;

    private boolean autoStartup = true;

    private boolean running = false;

    private final Object lifecycleMonitor = new Object();

    private HttpServer httpServer;

    private Map handlerMap = new HashMap();

    private ApplicationContext applicationContext;

    /**
     * Set whether to automatically start the receiver after initialization.
     * <p/>
     * Default is <code>true</code>; set this to <code>false</code> to allow for manual startup.
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /** Sets the port the server will bind to. */
    public void setPort(int port) {
        this.port = port;
    }

    /** Sets the server back log. */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public void setHandlers(Properties handlers) {
        this.handlerMap.putAll(handlers);
    }

    public void setHandlerMap(Map handlerMap) {
        this.handlerMap.putAll(handlerMap);
    }

    /**
     * Sets the local internet address the server will bind to. By default, it will accept connections on any/all local
     * addresses.
     *
     * @throws UnknownHostException when the given address is not known
     * @see ServerSocket#ServerSocket(int,int,java.net.InetAddress)
     */
    public void setBindAddress(String bindAddress) throws UnknownHostException {
        this.bindAddress = InetAddress.getByName(bindAddress);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws IOException {
        if (bindAddress == null) {
            bindAddress = InetAddress.getLocalHost();
        }
        InetSocketAddress bindSocketAddress = new InetSocketAddress(bindAddress, port);
        httpServer = HttpServer.create(bindSocketAddress, backlog);
        registerContexts(this.handlerMap);
        if (autoStartup) {
            start();
        }
    }

    protected void registerContexts(Map handlerMap) {
        if (handlerMap.isEmpty()) {
            logger.warn("Neither 'contextMap' nor 'contexts' set on HttpServerFactoryBean");
        }
        else {
            Iterator it = handlerMap.keySet().iterator();
            while (it.hasNext()) {
                String path = (String) it.next();
                Object handler = handlerMap.get(path);
                // Prepend with slash if not already present.
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                registerHandler(path, handler);
            }
        }
    }

    protected void registerHandler(String path, Object handler) throws BeansException, IllegalStateException {
        Assert.notNull(path, "Path must not be null");
        Assert.notNull(handler, "Handler object must not be null");

        // Eagerly resolve handler if referencing singleton via name.
        if (handler instanceof String) {
            String handlerName = (String) handler;
            if (applicationContext.isSingleton(handlerName)) {
                handler = applicationContext.getBean(handlerName, HttpHandler.class);
            }
        }
        if (handler == null || !(handler instanceof HttpHandler)) {
            throw new IllegalStateException("Cannot resolve handler [" + handler + "] to HttpHandler instance");
        }
        httpServer.createContext(path, (HttpHandler) handler);
        if (logger.isDebugEnabled()) {
            logger.debug("Mapped path [" + path + "] onto handler [" + handler + "]");
        }
    }

    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting HttpServer [" + httpServer.getAddress() + "]");
        }
        synchronized (lifecycleMonitor) {
            running = true;
            lifecycleMonitor.notifyAll();
        }
        httpServer.start();
    }

    public void stop() {
        synchronized (lifecycleMonitor) {
            running = false;
            lifecycleMonitor.notifyAll();
        }
        httpServer.stop(0);
    }

    public boolean isRunning() {
        synchronized (lifecycleMonitor) {
            return running;
        }
    }

    public Object getObject() throws Exception {
        return httpServer;
    }

    public Class getObjectType() {
        return HttpServer.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
