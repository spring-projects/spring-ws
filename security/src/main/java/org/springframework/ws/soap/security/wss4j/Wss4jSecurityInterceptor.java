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

package org.springframework.ws.soap.security.wss4j;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Vector;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.SOAPException;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageException;
import org.springframework.ws.soap.security.AbstractWsSecurityInterceptor;
import org.springframework.ws.soap.security.WsSecuritySecurementException;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.callback.CallbackHandlerChain;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.wss4j.callback.UsernameTokenPrincipalCallback;

/**
 * A WS-Security endpoint interceptor based on Apache's WSS4J. This inteceptor supports messages created by the {@link
 * org.springframework.ws.soap.axiom.AxiomSoapMessageFactory} and the {@link org.springframework.ws.soap.saaj.SaajSoapMessageFactory}.
 * <p/>
 * The validation and securement actions executed by this interceptor are configured via <code>validationActions</code>
 * and <code>securementActions</code> properties, respectively. Actions should be passed as a space-separated strings.
 * <p/>
 * Valid <strong>validation</strong> actions are:
 * <p/>
 * <blockquote><table> <tr><th>Validation action</th><th>Description</th></tr> <tr><td><code>UsernameToken</code></td><td>Validates
 * username token</td></tr> <tr><td><code>Timestamp</code></td><td>Validates the timestamp</td></tr>
 * <tr><td><code>Encrypt</code></td><td>Decrypts the message</td></tr> <tr><td><code>Signature</code></td><td>Validates
 * the signature</td></tr> <tr><td><code>NoSecurity</code></td><td>No action performed</td></tr> </table></blockquote>
 * <p/>
 * <strong>Securement</strong> actions are: <blockquote><table> <tr><th>Securement action</th><th>Description</th></tr>
 * <tr><td><code>UsernameToken</td></code><td>Adds a username token</td></tr> <tr><td><code>UsernameTokenSignature</td></code><td>Adds
 * a username token and a signature username token secret key</td></tr> <tr><td><code>Timestamp</td></code><td>Adds a
 * timestamp</td></tr> <tr><td><code>Encrypt</td></code><td>Encrypts the response</td></tr>
 * <tr><td><code>Signature</td></code><td>Signs the response</td></tr> <tr><td><code>NoSecurity</td></code><td>No action
 * performed</td></tr> </table></blockquote>
 * <p/>
 * The order of the actions that the client performed to secure the messages is significant and is enforced by the
 * interceptor.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @see <a href="http://ws.apache.org/wss4j/">Apache WSS4J</a>
 * @since 1.5.0
 */
public class Wss4jSecurityInterceptor extends AbstractWsSecurityInterceptor implements InitializingBean {

    public static final String SECUREMENT_USER_PROPERTY_NAME = "Wss4jSecurityInterceptor.securementUser";

    private int securementAction;

    private String securementActions;

    private Vector securementActionsVector;

    private String securementUsername;

    private CallbackHandler validationCallbackHandler;

    private int validationAction;

    private String validationActions;

    private Vector validationActionsVector;

    private String validationActor;

    private Crypto validationDecryptionCrypto;

    private Crypto validationSignatureCrypto;

    private boolean timestampStrict = true;

    private boolean enableSignatureConfirmation;

    private int timeToLive = 300;

    private Wss4jHandler handler = new Wss4jHandler();

    public void setSecurementActions(String securementActions) {
        this.securementActions = securementActions;
        securementActionsVector = new Vector();
        try {
            securementAction = WSSecurityUtil.decodeAction(securementActions, securementActionsVector);
        }
        catch (WSSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * The actor name of the <code>wsse:Security</code> header.
     * <p/>
     * If this parameter is omitted, the actor name is not set.
     * <p/>
     * The value of the actor or role has to match the receiver's setting or may contain standard values.
     */
    public void setSecurementActor(String securementActor) {
        handler.setOption(WSHandlerConstants.ACTOR, securementActor);
    }

    /**
     * Sets the {@link org.apache.ws.security.WSPasswordCallback} handler to use when securing messages.
     *
     * @see #setSecurementCallbackHandlers(CallbackHandler[])
     */
    public void setSecurementCallbackHandler(CallbackHandler securementCallbackHandler) {
        handler.setSecurementCallbackHandler(securementCallbackHandler);
    }

    /**
     * Sets the {@link org.apache.ws.security.WSPasswordCallback} handlers to use when securing messages.
     *
     * @see #setSecurementCallbackHandler(CallbackHandler)
     */
    public void setSecurementCallbackHandlers(CallbackHandler[] securementCallbackHandler) {
        handler.setSecurementCallbackHandler(new CallbackHandlerChain(securementCallbackHandler));
    }

    public void setSecurementEncryptionCrypto(Crypto securementEncryptionCrypto) {
        handler.setSecurementEncryptionCrypto(securementEncryptionCrypto);
    }

    /** Sets the key name that needs to be sent for encryption. */
    public void setSecurementEncryptionEmbeddedKeyName(String securementEncryptionEmbeddedKeyName) {
        handler.setOption(WSHandlerConstants.ENC_KEY_NAME, securementEncryptionEmbeddedKeyName);
    }

    /**
     * Defines which key identifier type to use. The WS-Security specifications recommends to use the identifier type
     * <code>IssuerSerial</code>. For possible encryption key identifier types refer to {@link
     * org.apache.ws.security.handler.WSHandlerConstants#keyIdentifier}. For encryption <code>IssuerSerial</code>,
     * <code>X509KeyIdentifier</code>,  <code>DirectReference</code>, <code>Thumbprint</code>,
     * <code>SKIKeyIdentifier</code>, and <code>EmbeddedKeyName</code> are valid only.
     */
    public void setSecurementEncryptionKeyIdentifier(String securementEncryptionKeyIdentifier) {
        handler.setOption(WSHandlerConstants.ENC_KEY_ID, securementEncryptionKeyIdentifier);
    }

    /**
     * Defines which algorithm to use to encrypt the generated symmetric key. Currently WSS4J supports {@link
     * WSConstants#KEYTRANSPORT_RSA15} and {@link WSConstants#KEYTRANSPORT_RSAOEP}.
     */
    public void setSecurementEncryptionKeyTransportAlgorithm(String securementEncryptionKeyTransportAlgorithm) {
        handler.setOption(WSHandlerConstants.ENC_KEY_TRANSPORT, securementEncryptionKeyTransportAlgorithm);
    }

    /**
     * Property to define which parts of the request shall be encrypted.
     * <p/>
     * The value of this property is a list of semi-colon separated element names that identify the elements to encrypt.
     * An encryption mode specifier and a namespace identification, each inside a pair of curly brackets, may precede
     * each element name.
     * <p/>
     * The encryption mode specifier is either <code>{Content}</code> or <code>{Element}</code>. Please refer to the W3C
     * XML Encryption specification about the differences between Element and Content encryption. The encryption mode
     * defaults to <code>Content</code> if it is omitted. Example of a list:
     * <pre>
     * &lt;property name="securementEncryptionParts"
     *   value="{Content}{http://example.org/paymentv2}CreditCard;
     *             {Element}{}UserName" />
     * </pre>
     * The the first entry of the list identifies the element <code>CreditCard</code> in the namespace
     * <code>http://example.org/paymentv2</code>, and will encrypt its content. Be aware that the element name, the
     * namespace identifier, and the encryption modifier are case sensitive.
     * <p/>
     * The encryption modifier and the namespace identifier can be omitted. In this case the encryption mode defaults to
     * <code>Content</code> and the namespace is set to the SOAP namespace.
     * <p/>
     * An empty encryption mode defaults to <code>Content</code>, an empty namespace identifier defaults to the SOAP
     * namespace. The second line of the example defines <code>Element</code> as encryption mode for an
     * <code>UserName</code> element in the SOAP namespace.
     * <p/>
     * To specify an element without a namespace use the string <code>Null</code> as the namespace name (this is a case
     * sensitive string)
     * <p/>
     * If no list is specified, the handler encrypts the SOAP Body in <code>Content</code> mode by default.
     */
    public void setSecurementEncryptionParts(String securementEncryptionParts) {
        handler.setOption(WSHandlerConstants.ENCRYPTION_PARTS, securementEncryptionParts);
    }

    /**
     * Defines which symmetric encryption algorithm to use. WSS4J supports the following alorithms: {@link
     * WSConstants#TRIPLE_DES}, {@link WSConstants#AES_128}, {@link WSConstants#AES_256}, and {@link
     * WSConstants#AES_192}. Except for AES 192 all of these algorithms are required by the XML Encryption
     * specification.
     */
    public void setSecurementEncryptionSymAlgorithm(String securementEncryptionSymAlgorithm) {
        this.handler.setOption(WSHandlerConstants.ENC_SYM_ALGO, securementEncryptionSymAlgorithm);
    }

    /**
     * The user's name for encryption.
     * <p/>
     * The encryption functions uses the public key of this user's certificate to encrypt the generated symmetric key.
     * <p/>
     * If this parameter is not set, then the encryption function falls back to the {@link
     * org.apache.ws.security.handler.WSHandlerConstants#USER} parameter to get the certificate.
     * <p/>
     * If <b>only</b> encryption of the SOAP body data is requested, it is recommended to use this parameter to define
     * the username. The application can then use the standard user and password functions (see example at {@link
     * org.apache.ws.security.handler.WSHandlerConstants#USER} to enable HTTP authentication functions.
     * <p/>
     * Encryption only does not authenticate a user / sender, therefore it does not need a password.
     * <p/>
     * Placing the username of the encryption certificate in the configuration file is not a security risk, because the
     * public key of that certificate is used only.
     * <p/>
     */
    public void setSecurementEncryptionUser(String securementEncryptionUser) {
        handler.setOption(WSHandlerConstants.ENCRYPTION_USER, securementEncryptionUser);
    }

    public void setSecurementPassword(String securementPassword) {
        this.handler.setSecurementPassword(securementPassword);
    }

    /**
     * Specific parameter for UsernameToken action to define the encoding of the passowrd.
     * <p/>
     * The parameter can be set to either {@link WSConstants#PW_DIGEST} or to {@link WSConstants#PW_TEXT}.
     * <p/>
     * The default setting is PW_DIGEST.
     */
    public void setSecurementPasswordType(String securementUsernameTokenPasswordType) {
        handler.setOption(WSHandlerConstants.PASSWORD_TYPE, securementUsernameTokenPasswordType);
    }

    /**
     * Defines which signature algorithm to use. Currently this parameter is ignored - SHA1RSA is the only supported
     * algorithm, will be enhanced soon.
     */
    public void setSecurementSignatureAlgorithm(String securementSignatureAlgorithm) {
        handler.setOption(WSHandlerConstants.SIG_ALGO, securementSignatureAlgorithm);
    }

    public void setSecurementSignatureCrypto(Crypto securementSignatureCrypto) {
        handler.setSecurementSignatureCrypto(securementSignatureCrypto);
    }

    /**
     * Defines which key identifier type to use. The WS-Security specifications recommends to use the identifier type
     * <code>IssuerSerial</code>. For possible signature key identifier types refer to {@link
     * org.apache.ws.security.handler.WSHandlerConstants#keyIdentifier}. For signature <code>IssuerSerial</code> and
     * <code>DirectReference</code> are valid only.
     */
    public void setSecurementSignatureKeyIdentifier(String securementSignatureKeyIdentifier) {
        handler.setOption(WSHandlerConstants.SIG_KEY_ID, securementSignatureKeyIdentifier);
    }

    /**
     * Property to define which parts of the request shall be signed.
     * <p/>
     * Refer to {@link #setSecurementEncryptionParts(String)} for a detailed description of the format of the value
     * string.
     * <p/>
     * If this property is not specified the handler signs the SOAP Body by default.
     * <p/>
     * The WS Security specifications define several formats to transfer the signature tokens (certificates) or
     * references to these tokens. Thus, the plain element name <code>Token</code> signs the token and takes care of the
     * different formats.
     * <p/>
     * To sign the SOAP body <b>and</b> the signature token the value of this parameter must contain:
     * <pre>
     * &lt;property name="securementSignatureParts"
     *   value="{}{http://schemas.xmlsoap.org/soap/envelope/}Body; Token" />
     * </pre>
     * To specify an element without a namespace use the string <code>Null</code> as the namespace name (this is a case
     * sensitive string)
     * <p/>
     * If there is no other element in the request with a local name of <code>Body</code> then the SOAP namespace
     * identifier can be empty (<code>{}</code>).
     */
    public void setSecurementSignatureParts(String securementSignatureParts) {
        handler.setOption(WSHandlerConstants.SIGNATURE_PARTS, securementSignatureParts);
    }

    /** Sets the username for securement username token or/and the alias of the private key for securement signature */
    public void setSecurementUsername(String securementUsername) {
        this.securementUsername = securementUsername;
    }

    /** Sets the server-side time to live */
    public void setTimeToLive(int timeToLive) {
        if (timeToLive <= 0) {
            throw new IllegalArgumentException("timeToLive must be positive");
        }
        this.timeToLive = timeToLive;
    }

    /** Sets the validation actions to be executed by the interceptor. */
    public void setValidationActions(String actions) {
        this.validationActions = actions;
        try {
            validationActionsVector = new Vector();
            validationAction = WSSecurityUtil.decodeAction(actions, validationActionsVector);
        }
        catch (WSSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public void setValidationActor(String validationActor) {
        this.validationActor = validationActor;
    }

    /**
     * Sets the {@link org.apache.ws.security.WSPasswordCallback} handler to use when validating messages.
     *
     * @see #setValidationCallbackHandlers(CallbackHandler[])
     */
    public void setValidationCallbackHandler(CallbackHandler callbackHandler) {
        this.validationCallbackHandler = callbackHandler;
    }

    /**
     * Sets the {@link org.apache.ws.security.WSPasswordCallback} handlers to use when validating messages.
     *
     * @see #setValidationCallbackHandler(CallbackHandler)
     */
    public void setValidationCallbackHandlers(CallbackHandler[] callbackHandler) {
        this.validationCallbackHandler = new CallbackHandlerChain(callbackHandler);
    }

    /** Sets the Crypto to use to decrypt incoming messages */
    public void setValidationDecryptionCrypto(Crypto decryptionCrypto) {
        this.validationDecryptionCrypto = decryptionCrypto;
    }

    /** Sets the Crypto to use to verify the signature of incoming messages */
    public void setValidationSignatureCrypto(Crypto signatureCrypto) {
        this.validationSignatureCrypto = signatureCrypto;
    }

    /** Whether to enable signatureConfirmation or not. By default signatureConfirmation is enabled */
    public void setEnableSignatureConfirmation(boolean enableSignatureConfirmation) {
        handler.setOption(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, enableSignatureConfirmation);
        this.enableSignatureConfirmation = enableSignatureConfirmation;
    }

    /** Sets if the generated timestamp header's precision is in milliseconds. */
    public void setTimestampPrecisionInMilliseconds(boolean timestampPrecisionInMilliseconds) {
        handler.setOption(WSHandlerConstants.TIMESTAMP_PRECISION, timestampPrecisionInMilliseconds);
    }

    /** Sets whether or not timestamp verification is done with the server-side time to live */
    public void setTimestampStrict(boolean timestampStrict) {
        this.timestampStrict = timestampStrict;
    }

    /**
     * Enables the <code>mustUnderstand</code> attribute on WS-Security headers on outgoing messages. Default is
     * <code>true</code>.
     */
    public void setSecurementMustUnderstand(boolean securementMustUnderstand) {
        handler.setOption(WSHandlerConstants.MUST_UNDERSTAND, securementMustUnderstand);
    }

    /**
     * Sets the additional elements in <code>UsernameToken</code>s.
     * <p/>
     * The value of this parameter is a list of element names that are added to the UsernameToken. The names of the list
     * a separated by spaces.
     * <p/>
     * The list may contain the names <code>Nonce</code> and <code>Created</code> only (case sensitive). Use this option
     * if the password type is <code>passwordText</code> and the handler shall add the <code>Nonce</code> and/or
     * <code>Created</code> elements.
     */
    public void setSecurementUsernameTokenElements(String securementUsernameTokenElements) {
        handler.setOption(WSHandlerConstants.ADD_UT_ELEMENTS, securementUsernameTokenElements);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(validationActions != null || securementActions != null,
                "validationActions or securementActions are required");
        if (validationActions != null) {
            if ((validationAction & WSConstants.UT) != 0) {
                Assert.notNull(validationCallbackHandler, "validationCallbackHandler is required");
            }

            if ((validationAction & WSConstants.SIGN) != 0) {
                Assert.notNull(validationSignatureCrypto, "validationSignatureCrypto is required");
            }
        }
        // securement actions are not to be validated at start up as they could
        // be configured dynamically via the message context
    }

    protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
            throws WsSecuritySecurementException {
        if (securementAction == WSConstants.NO_SECURITY && !enableSignatureConfirmation) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Securing message [" + soapMessage + "] with actions [" + securementActions + "]");
        }
        RequestData requestData = initializeRequestData(messageContext);

        Document envelopeAsDocument = toDocument(soapMessage);
        try {
            // In case on signature confirmation with no other securement
            // action, we need to pass an empty securementActionsVector to avoid
            // NPE
            if (securementAction == WSConstants.NO_SECURITY) {
                securementActionsVector = new Vector(0);
            }

            handler.doSenderAction(securementAction, envelopeAsDocument, requestData, securementActionsVector, false);
        }
        catch (WSSecurityException ex) {
            throw new Wss4jSecuritySecurementException(ex.getMessage(), ex);
        }

        replaceMessage(soapMessage, envelopeAsDocument);
    }

    /** Creates and initializes a request data */
    private RequestData initializeRequestData(MessageContext messageContext) {
        RequestData requestData = new RequestData();
        requestData.setMsgContext(messageContext);

        // reads securementUsername first from the context then from the
        // property
        String contextUsername = (String) messageContext.getProperty(SECUREMENT_USER_PROPERTY_NAME);
        if (StringUtils.hasLength(contextUsername)) {
            requestData.setUsername(contextUsername);
        }
        else {
            requestData.setUsername(securementUsername);
        }
        return requestData;
    }

    protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
            throws WsSecurityValidationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating message [" + soapMessage + "] with actions [" + validationActions + "]");
        }

        if (validationAction == WSConstants.NO_SECURITY) {
            return;
        }

        Document envelopeAsDocument = toDocument(soapMessage);

        // Header processing
        WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();

        try {
            Vector results = securityEngine.processSecurityHeader(envelopeAsDocument, validationActor,
                    validationCallbackHandler, validationSignatureCrypto, validationDecryptionCrypto);

            // Results verification
            if (results == null) {
                throw new Wss4jSecurityValidationException("No WS-Security header found");
            }

            if (!handler.checkReceiverResults(results, validationActionsVector)) {
                throw new Wss4jSecurityValidationException("Security processing failed (actions mismatch)");
            }

            // puts the results in the context
            // useful for Signature Confirmation
            updateContextWithResults(messageContext, results);

            verifyCertificateTrust(results);

            verifyTimestamp(results);

            processPrincipal(results);
        }
        catch (WSSecurityException ex) {
            throw new Wss4jSecurityValidationException(ex.getMessage(), ex);
        }

        replaceMessage(soapMessage, envelopeAsDocument);

        soapMessage.getEnvelope().getHeader().removeHeaderElement(WS_SECURITY_NAME);
    }

    /**
     * Puts the results of WS-Security headers processing in the message context. Some actions like Signature
     * Confirmation require this.
     */
    private void updateContextWithResults(MessageContext messageContext, Vector results) {
        Vector handlerResults;
        if ((handlerResults = (Vector) messageContext
                .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            handlerResults = new Vector();
            messageContext.setProperty(WSHandlerConstants.RECV_RESULTS, handlerResults);
        }
        WSHandlerResult rResult = new WSHandlerResult(validationActor, results);
        handlerResults.add(0, rResult);
        messageContext.setProperty(WSHandlerConstants.RECV_RESULTS, handlerResults);
    }

    /** Verifies the trust of a certificate. */
    protected void verifyCertificateTrust(Vector results) throws WSSecurityException {
        RequestData requestData = new RequestData();
        requestData.setSigCrypto(validationSignatureCrypto);
        WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);

        if (actionResult != null) {
            X509Certificate returnCert =
                    (X509Certificate) actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
            if (!handler.verifyTrust(returnCert, requestData)) {
                throw new Wss4jSecurityValidationException("The certificate used for the signature is not trusted");
            }
        }
    }

    /** Verifies the timestamp. */
    protected void verifyTimestamp(Vector results) throws WSSecurityException {
        WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.TS);

        if (actionResult != null) {
            Timestamp timestamp = (Timestamp) actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
            if (timestamp != null && timestampStrict) {
                if (!handler.verifyTimestamp(timestamp, timeToLive)) {
                    throw new Wss4jSecurityValidationException("Invalid timestamp : " + timestamp.getID());
                }
            }
        }

    }

    private void processPrincipal(Vector results) {
        WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.UT);

        if (actionResult != null) {
            Principal principal = (Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
            if (principal != null && principal instanceof WSUsernameTokenPrincipal) {
                WSUsernameTokenPrincipal usernameTokenPrincipal = (WSUsernameTokenPrincipal) principal;
                UsernameTokenPrincipalCallback callback = new UsernameTokenPrincipalCallback(usernameTokenPrincipal);
                try {
                    validationCallbackHandler.handle(new Callback[]{callback});
                }
                catch (IOException ex) {
                    logger.warn("Principal callback resulted in IOException", ex);
                }
                catch (UnsupportedCallbackException ex) {
                    // ignore
                }
            }
        }
    }

    /** Converts the given {@link SoapMessage} into a {@link Document}. */
    private Document toDocument(SoapMessage soapMessage) {
        if (soapMessage instanceof SaajSoapMessage) {
            javax.xml.soap.SOAPMessage saajMessage = ((SaajSoapMessage) soapMessage).getSaajMessage();
            try {
                saajMessage.saveChanges();
            }
            catch (SOAPException ex) {
                throw new SaajSoapMessageException("Could not save changes", ex);
            }
            return saajMessage.getSOAPPart();
        }
        else if (soapMessage instanceof AxiomSoapMessage) {
            AxiomSoapMessage axiomMessage = (AxiomSoapMessage) soapMessage;
            return AxiomUtils.toDocument(axiomMessage.getAxiomMessage().getSOAPEnvelope());
        }
        else {
            throw new IllegalArgumentException("Message type not supported [" + soapMessage + "]");
        }
    }

    /**
     * Replaces the contents of the given {@link SoapMessage} with that of the document parameter. Only required when
     * using Axiom, since the document returned by {@link #toDocument(org.springframework.ws.soap.SoapMessage)} is live
     * for a {@link SaajSoapMessage}.
     */
    private void replaceMessage(SoapMessage soapMessage, Document envelope) {
        if (soapMessage instanceof SaajSoapMessage) {
            javax.xml.soap.SOAPMessage saajMessage = ((SaajSoapMessage) soapMessage).getSaajMessage();
            try {
                saajMessage.saveChanges();
            }
            catch (SOAPException ex) {
                throw new SaajSoapMessageException("Could not save changes", ex);
            }
        }
        else if (soapMessage instanceof AxiomSoapMessage) {
            // construct a new Axiom message with the processed envelope
            AxiomSoapMessage axiomMessage = (AxiomSoapMessage) soapMessage;
            SOAPEnvelope envelopeFromDOMDocument = AxiomUtils.toEnvelope(envelope);
            SOAPFactory factory = (SOAPFactory) axiomMessage.getAxiomMessage().getOMFactory();
            SOAPMessage newMessage = factory.createSOAPMessage();
            newMessage.setSOAPEnvelope(envelopeFromDOMDocument);

            // replace the Axiom message
            axiomMessage.setAxiomMessage(newMessage);
        }
    }

    protected void cleanUp() {
        if (validationCallbackHandler != null) {
            try {
                CleanupCallback cleanupCallback = new CleanupCallback();
                validationCallbackHandler.handle(new Callback[]{cleanupCallback});
            }
            catch (IOException ex) {
                logger.warn("Cleanup callback resulted in IOException", ex);
            }
            catch (UnsupportedCallbackException ex) {
                // ignore
            }
        }
    }
}
