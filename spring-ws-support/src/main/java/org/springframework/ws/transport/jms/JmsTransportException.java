/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.jms;

import javax.jms.JMSException;

import org.springframework.ws.transport.TransportException;

/**
 * Exception that is thrown when an error occurs in the JMS transport.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class JmsTransportException extends TransportException {

	private final JMSException jmsException;

	public JmsTransportException(String msg, JMSException ex) {
		super(msg + ": " + ex.getMessage());
		initCause(ex);
		jmsException = ex;
	}

	public JmsTransportException(JMSException ex) {
		super(ex.getMessage());
		initCause(ex);
		jmsException = ex;
	}

	public JMSException getJmsException() {
		return jmsException;
	}
}
