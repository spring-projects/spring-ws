/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.soap.security.wss4j2;

import java.util.List;
import java.util.Properties;

import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.HandlerAction;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandler;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;

/**
 * {@link WSHandler} implementation.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @author Lars Uffmann
 * @since 2.3.0
 */
class Wss4jHandler extends WSHandler {

	/** Keys are constants from {@link ConfigurationConstants}; values are strings. */
	private final Properties options = new Properties();

	private @Nullable String securementPassword;

	private @Nullable Crypto securementEncryptionCrypto;

	private @Nullable Crypto securementSignatureCrypto;

	Wss4jHandler() {
		// set up default handler properties
		this.options.setProperty(ConfigurationConstants.MUST_UNDERSTAND, Boolean.toString(true));
		this.options.setProperty(ConfigurationConstants.ENABLE_SIGNATURE_CONFIRMATION, Boolean.toString(true));
	}

	@Override
	public void doSenderAction(Document doc, RequestData reqData, List<HandlerAction> actions, boolean isRequest)
			throws WSSecurityException {
		super.doSenderAction(doc, reqData, actions, isRequest);
	}

	@Override
	protected boolean checkReceiverResultsAnyOrder(List<WSSecurityEngineResult> wsResult, List<Integer> actions) {
		return super.checkReceiverResultsAnyOrder(wsResult, actions);
	}

	void setOption(String key, String value) {
		this.options.setProperty(key, value);
	}

	void setOption(String key, boolean value) {
		this.options.setProperty(key, Boolean.toString(value));
	}

	@Override
	public Object getOption(String key) {
		return this.options.getProperty(key);
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
	public @Nullable String getPassword(Object msgContext) {
		String contextPassword = (String) getProperty(msgContext,
				Wss4jSecurityInterceptor.SECUREMENT_PASSWORD_PROPERTY_NAME);
		if (StringUtils.hasLength(contextPassword)) {
			return contextPassword;
		}
		return this.securementPassword;
	}

	@Override
	public @Nullable Object getProperty(Object msgContext, String key) {
		return ((MessageContext) msgContext).getProperty(key);
	}

	@Override
	protected @Nullable Crypto loadEncryptionCrypto(RequestData reqData) throws WSSecurityException {
		return this.securementEncryptionCrypto;
	}

	@Override
	public @Nullable Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException {
		return this.securementSignatureCrypto;
	}

	@Override
	public void setPassword(Object msgContext, String password) {
		this.securementPassword = password;
	}

	@Override
	public void setProperty(Object msgContext, String key, Object value) {
		((MessageContext) msgContext).setProperty(key, value);
	}

}
