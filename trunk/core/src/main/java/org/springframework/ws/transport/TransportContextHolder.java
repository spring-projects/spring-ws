/*
 * Copyright 2006 the original author or authors.
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
package org.springframework.ws.transport;

/**
 * Simple holder class that associates a <code>TransportContext</code> instance with the current thread. The
 * <code>TransportContext</code> will be inherited by any child threads spawned by the current thread.
 *
 * @author Arjen Poutsma
 * @see TransportContext
 */
public abstract class TransportContextHolder {

    private static final ThreadLocal transportContextHolder = new ThreadLocal();

    /**
     * Associate the given <code>TransportContext</code> with the current thread.
     *
     * @param transportContext the current transport context, or <code>null</code> to reset the thread-bound context
     */
    public static void setTransportContext(TransportContext transportContext) {
        transportContextHolder.set(transportContext);
    }

    /**
     * Return the <code>TransportContext</code> associated with the current thread, if any.
     *
     * @return the current transport context, or <code>null</code> if none
     */
    public static TransportContext getTransportContext() {
        return (TransportContext) transportContextHolder.get();
    }
}
