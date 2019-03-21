/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.context;

/**
 * Simple holder class that associates a {@code TransportContext} instance with the current thread. The
 * {@code TransportContext} will be inherited by any child threads spawned by the current thread.
 *
 * @author Arjen Poutsma
 * @see TransportContext
 * @since 1.0.0
 */
public abstract class TransportContextHolder {

	private static final ThreadLocal<TransportContext> transportContextHolder = new TransportThreadLocal();

	/**
	 * Associate the given {@code TransportContext} with the current thread.
	 *
	 * @param transportContext the current transport context, or {@code null} to reset the thread-bound context
	 */
	public static void setTransportContext(TransportContext transportContext) {
		transportContextHolder.set(transportContext);
	}

	/**
	 * Return the {@code TransportContext} associated with the current thread, if any.
	 *
	 * @return the current transport context, or {@code null} if none
	 */
	public static TransportContext getTransportContext() {
		return transportContextHolder.get();
	}

	private static class TransportThreadLocal extends ThreadLocal<TransportContext> {

		public String toString() {
			return "Transport State";
		}
	}
}
