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

package org.springframework.ws.soap.security.wss4j2;

import org.springframework.ws.soap.security.WsSecuritySecurementException;

/**
 * WSS4J-specific version of the {@link WsSecuritySecurementException}.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 */
@SuppressWarnings("serial")
public class Wss4jSecuritySecurementException extends WsSecuritySecurementException {

	public Wss4jSecuritySecurementException(String msg) {
		super(msg);
	}

	public Wss4jSecuritySecurementException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
