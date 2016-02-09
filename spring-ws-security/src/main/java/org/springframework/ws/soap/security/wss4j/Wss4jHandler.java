/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j;

import java.util.List;
import java.util.Properties;

import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.w3c.dom.Document;

import org.springframework.ws.context.MessageContext;

/**
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @since 1.5.0
 * @deprecated Transition to {@link org.springframework.ws.soap.security.wss4j2.Wss4jHandler}.
 */
@Deprecated
class Wss4jHandler extends WSHandler {

	/** Keys are constants from {@link WSHandlerConstants}; values are strings. */
	private Properties options = new Properties();

	private String securementPassword;

	private Crypto securementEncryptionCrypto;

	private Crypto securementSignatureCrypto;

	Wss4jHandler() {
		// set up default handler properties
		options.setProperty(WSHandlerConstants.MUST_UNDERSTAND, Boolean.toString(true));
		options.setProperty(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, Boolean.toString(true));
	}

	@Override
	protected boolean checkReceiverResultsAnyOrder(List<WSSecurityEngineResult> wsResult, List<Integer> actions) {
		return super.checkReceiverResultsAnyOrder(wsResult, actions);
	}

	void setOption(String key, String value) {
		options.setProperty(key, value);
	}

	void setOption(String key, boolean value) {
		options.setProperty(key, Boolean.toString(value));
	}

	@Override
	public Object getOption(String key) {
		return options.getProperty(key);
	}

	void setSecurementPassword(String securementPassword) {
		this.securementPassword = securementPassword;
	}

	void setSecurementEncryptionCrypto(Crypto securementEncryptionCrypto) {
		this.securementEncryptionCrypto = securementEncryptionCrypto;
	}

	void setSecurementSignatureCrypto(Crypto securementSignatureCrypto) {
		this.securementSignatureCrypto = securementSignatureCrypto;
	}

	@Override
	public String getPassword(Object msgContext) {
		return securementPassword;
	}

	@Override
	public Object getProperty(Object msgContext, String key) {
		return ((MessageContext) msgContext).getProperty(key);
	}

	@Override
	protected Crypto loadEncryptionCrypto(RequestData reqData) throws WSSecurityException {
		return securementEncryptionCrypto;
	}

	@Override
	public Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException {
		return securementSignatureCrypto;
	}

	@Override
	public void setPassword(Object msgContext, String password) {
		securementPassword = password;
	}

	@Override
	public void setProperty(Object msgContext, String key, Object value) {
		((MessageContext) msgContext).setProperty(key, value);
	}

	@Override
	protected void doSenderAction(int doAction,
								  Document doc,
								  RequestData reqData,
								  List<Integer> actions,
								  boolean isRequest) throws WSSecurityException {
		super.doSenderAction(doAction, doc, reqData, actions, isRequest);
	}
}
