/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Vector;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.token.Timestamp;
import org.w3c.dom.Document;

import org.springframework.ws.context.MessageContext;

/**
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class Wss4jHandler extends WSHandler {

    /** Keys are constants from {@link WSHandlerConstants}; values are strings. */
    private Properties options = new Properties();

    private CallbackHandler securementCallbackHandler;

    private String securementPassword;

    private Crypto securementEncryptionCrypto;

    private Crypto securementSignatureCrypto;

    public Wss4jHandler() {
        // set up default handler properties
        options.setProperty(WSHandlerConstants.MUST_UNDERSTAND, Boolean.toString(true));
        options.setProperty(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, Boolean.toString(true));
    }

    protected boolean checkReceiverResults(Vector wsResult, Vector actions) {
        return super.checkReceiverResults(wsResult, actions);
    }

    void setOption(String key, String value) {
        options.setProperty(key, value);
    }

    void setOption(String key, boolean value) {
        options.setProperty(key, Boolean.toString(value));
    }

    public Object getOption(String key) {
        return options.getProperty(key);
    }

    void setSecurementCallbackHandler(CallbackHandler securementCallbackHandler) {
        this.securementCallbackHandler = securementCallbackHandler;
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

    /** Gets the password first from securementCallbackHandler, then from securementPassword if not found. */
    public WSPasswordCallback getPassword(String username,
                                          int doAction,
                                          String clsProp,
                                          String refProp,
                                          RequestData reqData) {
        WSPasswordCallback callback;
        if (securementCallbackHandler != null) {
            int reason = 0;

            switch (doAction) {
                case WSConstants.UT:
                case WSConstants.UT_SIGN:
                    reason = WSPasswordCallback.USERNAME_TOKEN;
                    break;
                case WSConstants.SIGN:
                    reason = WSPasswordCallback.SIGNATURE;
                    break;
                case WSConstants.ENCR:
                    reason = WSPasswordCallback.KEY_NAME;
                    break;
            }
            callback = new WSPasswordCallback(username, reason);
            Callback[] callbacks = new Callback[]{callback};
            try {
                securementCallbackHandler.handle(callbacks);
            }
            catch (UnsupportedCallbackException ex) {
                throw new Wss4jSecuritySecurementException(ex.getMessage(), ex);
            }
            catch (IOException ex) {
                throw new Wss4jSecuritySecurementException(ex.getMessage(), ex);
            }
        }
        else {
            callback = new WSPasswordCallback("", WSPasswordCallback.UNKNOWN);
            callback.setPassword(securementPassword);
        }
        return callback;
    }

    public String getPassword(Object msgContext) {
        return securementPassword;
    }

    public Object getProperty(Object msgContext, String key) {
        return ((MessageContext) msgContext).getProperty(key);
    }

    protected Crypto loadEncryptionCrypto(RequestData reqData) throws WSSecurityException {
        return securementEncryptionCrypto;
    }

    public Crypto loadSignatureCrypto(RequestData reqData) throws WSSecurityException {
        return securementSignatureCrypto;
    }

    public void setPassword(Object msgContext, String password) {
        securementPassword = password;
    }

    public void setProperty(Object msgContext, String key, Object value) {
        ((MessageContext) msgContext).setProperty(key, value);
    }

    protected void doSenderAction(int doAction, Document doc, RequestData reqData, Vector actions, boolean isRequest)
            throws WSSecurityException {
        super.doSenderAction(doAction, doc, reqData, actions, isRequest);
    }

    protected boolean verifyTimestamp(Timestamp timestamp, int timeToLive) throws WSSecurityException {
        return super.verifyTimestamp(timestamp, timeToLive);
    }

    protected boolean verifyTrust(X509Certificate cert, RequestData reqData) throws WSSecurityException {
        return super.verifyTrust(cert, reqData);
    }
}
