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

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.HandlerAction;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.message.token.Timestamp;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.SignatureTrustValidator;
import org.apache.wss4j.dom.validate.TimestampValidator;
import org.apache.wss4j.dom.validate.Validator;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.AbstractWsSecurityInterceptor;
import org.springframework.ws.soap.security.WsSecuritySecurementException;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.callback.CallbackHandlerChain;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.wss4j2.callback.UsernameTokenPrincipalCallback;

/**
 * A WS-Security endpoint interceptor based on Apache's WSS4J. This interceptor supports
 * messages created by {@link org.springframework.ws.soap.axiom.AxiomSoapMessageFactory}
 * and {@link org.springframework.ws.soap.saaj.SaajSoapMessageFactory}.
 * <p>
 * The validation and securement actions executed by this interceptor are configured via
 * {@code validationActions} and {@code securementActions} properties, respectively.
 * Actions should be passed as a space-separated strings.
 * <p>
 * Valid <strong>validation</strong> actions are: <blockquote>
 * <table>
 * <caption>Validation Actions</caption>
 * <tr>
 * <th>Validation action</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@code UsernameToken}</td>
 * <td>Validates username token</td>
 * </tr>
 * <tr>
 * <td>{@code Timestamp}</td>
 * <td>Validates the timestamp</td>
 * </tr>
 * <tr>
 * <td>{@code Encrypt}</td>
 * <td>Decrypts the message</td>
 * </tr>
 * <tr>
 * <td>{@code Signature}</td>
 * <td>Validates the signature</td>
 * </tr>
 * <tr>
 * <td>{@code NoSecurity}</td>
 * <td>No action performed</td>
 * </tr>
 * </table>
 * </blockquote>
 * <p>
 * <strong>Securement</strong> actions are: <blockquote>
 * <table>
 * <caption>Securement Actions</caption>
 * <tr>
 * <th>Securement action</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>{@code UsernameToken}</td>
 * <td>Adds a username token</td>
 * </tr>
 * <tr>
 * <td>{@code UsernameTokenSignature}</td>
 * <td>Adds a username token and a signature username token secret key</td>
 * </tr>
 * <tr>
 * <td>{@code Timestamp}</td>
 * <td>Adds a timestamp</td>
 * </tr>
 * <tr>
 * <td>{@code Encrypt}</td>
 * <td>Encrypts the response</td>
 * </tr>
 * <tr>
 * <td>{@code Signature}</td>
 * <td>Signs the response</td>
 * </tr>
 * <tr>
 * <td>{@code NoSecurity}</td>
 * <td>No action performed</td>
 * </tr>
 * </table>
 * </blockquote>
 * <p>
 * The order of the actions that the client performed to secure the messages is
 * significant and is enforced by the interceptor.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Jamin Hitchcock
 * @author Rob Leland
 * @author Lars Uffmann
 * @author Andreas Winter
 * @since 2.3.0
 * @see <a href="http://ws.apache.org/wss4j/">Apache WSS4J 2.0+</a>
 */
public class Wss4jSecurityInterceptor extends AbstractWsSecurityInterceptor implements InitializingBean {

	/**
	 * Property name for securement user.
	 */
	public static final String SECUREMENT_USER_PROPERTY_NAME = "Wss4jSecurityInterceptor.securementUser";

	/**
	 * Property name for securement password.
	 */
	public static final String SECUREMENT_PASSWORD_PROPERTY_NAME = "Wss4jSecurityInterceptor.securementPassword";

	private @Nullable String securementActions;

	private @Nullable String securementUsername;

	private @Nullable CallbackHandler validationCallbackHandler;

	private @Nullable String validationActions;

	private List<Integer> validationActionsVector = Collections.emptyList();

	private @Nullable String validationActor;

	private @Nullable Crypto validationDecryptionCrypto;

	private @Nullable Crypto validationSignatureCrypto;

	private boolean timestampStrict = true;

	private boolean enableSignatureConfirmation;

	private int validationTimeToLive = 300;

	private int securementTimeToLive = 300;

	private int futureTimeToLive = 60;

	private @Nullable WSSConfig wssConfig;

	private final Wss4jHandler handler = new Wss4jHandler();

	private final WSSecurityEngine securityEngine;

	private boolean enableRevocation;

	private boolean bspCompliant;

	private boolean addInclusivePrefixes = true;

	private boolean securementUseDerivedKey;

	private @Nullable CallbackHandler samlCallbackHandler;

	private @Nullable CallbackHandler attachmentCallbackHandler;

	// Allow RSA 15 to maintain default behavior
	private boolean allowRSA15KeyTransportAlgorithm = true;

	// To maintain same behavior as default, this flag is set to true
	private boolean removeSecurityHeader = true;

	private List<Pattern> signatureSubjectDnPatterns = Collections.emptyList();

	/**
	 * Create a {@link WSSecurityEngine} by default.
	 */
	public Wss4jSecurityInterceptor() {
		this.securityEngine = new WSSecurityEngine();
	}

	/**
	 * Inject a customize {@link WSSecurityEngine}.
	 * @param securityEngine the security engine to use
	 */
	public Wss4jSecurityInterceptor(WSSecurityEngine securityEngine) {
		this.securityEngine = securityEngine;
	}

	public void setSecurementActions(String securementActions) {
		this.securementActions = securementActions;
	}

	/**
	 * Set a WSS4J option.
	 * @param key the id of the option as defined in {@link WSHandlerConstants}
	 * @param value the value of the option
	 * @since 4.1.0
	 */
	public void setWsHandlerOption(String key, String value) {
		this.handler.setOption(key, value);
	}

	/**
	 * Set a WSS4J flag option.
	 * @param key the id of the option as defined in {@link WSHandlerConstants}
	 * @param value whether the option is enabled
	 * @since 4.1.0
	 */
	public void setWsHandlerOption(String key, boolean value) {
		this.handler.setOption(key, value);
	}

	/**
	 * The actor name of the {@code wsse:Security} header.
	 * <p>
	 * If this parameter is omitted, the actor name is not set.
	 * <p>
	 * The value of the actor or role has to match the receiver's setting or may contain
	 * standard values.
	 */
	public void setSecurementActor(String securementActor) {
		setWsHandlerOption(WSHandlerConstants.ACTOR, securementActor);
	}

	public void setSecurementEncryptionCrypto(Crypto securementEncryptionCrypto) {
		this.handler.setSecurementEncryptionCrypto(securementEncryptionCrypto);
	}

	/**
	 * Defines which key identifier type to use. The WS-Security specifications recommends
	 * to use the identifier type {@code IssuerSerial}. For possible encryption key
	 * identifier types refer to {@link WSHandlerConstants}. For encryption
	 * {@code IssuerSerial}, {@code X509KeyIdentifier}, {@code DirectReference},
	 * {@code Thumbprint}, {@code SKIKeyIdentifier}, and {@code EmbeddedKeyName} are valid
	 * only.
	 */
	public void setSecurementEncryptionKeyIdentifier(String securementEncryptionKeyIdentifier) {
		setWsHandlerOption(WSHandlerConstants.ENC_KEY_ID, securementEncryptionKeyIdentifier);
	}

	/**
	 * Defines which algorithm to use to encrypt the generated symmetric key. Currently,
	 * WSS4J supports {@link WSConstants#KEYTRANSPORT_RSA15} and
	 * {@link WSConstants#KEYTRANSPORT_RSAOAEP}.
	 */
	public void setSecurementEncryptionKeyTransportAlgorithm(String securementEncryptionKeyTransportAlgorithm) {
		setWsHandlerOption(WSHandlerConstants.ENC_KEY_TRANSPORT, securementEncryptionKeyTransportAlgorithm);
	}

	/**
	 * Property to define which parts of the request shall be encrypted.
	 * <p>
	 * The value of this property is a list of semicolon separated element names that
	 * identify the elements to encrypt. An encryption mode specifier and a namespace
	 * identification, each inside a pair of curly brackets, may precede each element
	 * name.
	 * <p>
	 * The encryption mode specifier is either {@code {Content}} or {@code {Element}}.
	 * Please refer to the W3C XML Encryption specification about the differences between
	 * Element and Content encryption. The encryption mode defaults to {@code Content} if
	 * it is omitted. Example of a list: <pre><code class="xml">
	 * &lt;property name="securementEncryptionParts"
	 *	 value="{Content}{http://example.org/paymentv2}CreditCard;
	 *               {Element}{}UserName" /&gt;
	 * </code></pre> The first entry of the list identifies the element {@code CreditCard}
	 * in the namespace {@code http://example.org/paymentv2}, and will encrypt its
	 * content. Be aware that the element name, the namespace identifier, and the
	 * encryption modifier are case-sensitive.
	 * <p>
	 * The encryption modifier and the namespace identifier can be omitted. In this case
	 * the encryption mode defaults to {@code Content} and the namespace is set to the
	 * SOAP namespace.
	 * <p>
	 * An empty encryption mode defaults to {@code Content}, an empty namespace identifier
	 * defaults to the SOAP namespace. The second line of the example defines
	 * {@code Element} as encryption mode for an {@code UserName} element in the SOAP
	 * namespace.
	 * <p>
	 * To specify an element without a namespace use the string {@code Null} as the
	 * namespace name (this is a case-sensitive string)
	 * <p>
	 * If no list is specified, the handler encrypts the SOAP Body in {@code Content} mode
	 * by default.
	 */
	public void setSecurementEncryptionParts(String securementEncryptionParts) {
		setWsHandlerOption(WSHandlerConstants.ENCRYPTION_PARTS, securementEncryptionParts);
	}

	/**
	 * Defines which symmetric encryption algorithm to use. WSS4J supports the following
	 * algorithms: {@link WSConstants#TRIPLE_DES}, {@link WSConstants#AES_128},
	 * {@link WSConstants#AES_256}, and {@link WSConstants#AES_192}. Except for AES 192
	 * all of these algorithms are required by the XML Encryption specification.
	 */
	public void setSecurementEncryptionSymAlgorithm(String securementEncryptionSymAlgorithm) {
		setWsHandlerOption(WSHandlerConstants.ENC_SYM_ALGO, securementEncryptionSymAlgorithm);
	}

	/**
	 * The user's name for encryption.
	 * <p>
	 * The encryption functions uses the public key of this user's certificate to encrypt
	 * the generated symmetric key.
	 * <p>
	 * If this parameter is not set, then the encryption function falls back to the
	 * {@link WSHandlerConstants#USER} parameter to get the certificate.
	 * <p>
	 * If <b>only</b> encryption of the SOAP body data is requested, it is recommended to
	 * use this parameter to define the username. The application can then use the
	 * standard user and password functions, see example at
	 * {@link WSHandlerConstants#USER} to enable HTTP authentication functions.
	 * <p>
	 * Encryption only does not authenticate a user / sender, therefore it does not need a
	 * password.
	 * <p>
	 * Placing the username of the encryption certificate in the configuration file is not
	 * a security risk, because the public key of that certificate is used only.
	 */
	public void setSecurementEncryptionUser(String securementEncryptionUser) {
		setWsHandlerOption(WSHandlerConstants.ENCRYPTION_USER, securementEncryptionUser);
	}

	public void setSecurementPassword(String securementPassword) {
		this.handler.setSecurementPassword(securementPassword);
	}

	/**
	 * Specific parameter for UsernameToken action to define the encoding of the password.
	 * <p>
	 * The parameter can be set to either {@link WSConstants#PW_DIGEST} or to
	 * {@link WSConstants#PW_TEXT}.
	 * <p>
	 * The default setting is PW_DIGEST.
	 */
	public void setSecurementPasswordType(String securementUsernameTokenPasswordType) {
		setWsHandlerOption(WSHandlerConstants.PASSWORD_TYPE, securementUsernameTokenPasswordType);
	}

	/**
	 * Defines which signature algorithm to use.
	 * @see WSConstants#RSA
	 * @see WSConstants#DSA
	 */
	public void setSecurementSignatureAlgorithm(String securementSignatureAlgorithm) {
		setWsHandlerOption(WSHandlerConstants.SIG_ALGO, securementSignatureAlgorithm);
	}

	/**
	 * Defines which signature digest algorithm to use.
	 */
	public void setSecurementSignatureDigestAlgorithm(String digestAlgorithm) {
		setWsHandlerOption(WSHandlerConstants.SIG_DIGEST_ALGO, digestAlgorithm);
	}

	public void setSecurementSignatureCrypto(Crypto securementSignatureCrypto) {
		this.handler.setSecurementSignatureCrypto(securementSignatureCrypto);
	}

	/**
	 * Defines which key identifier type to use. The WS-Security specifications recommends
	 * to use the identifier type {@code IssuerSerial}. For possible signature key
	 * identifier types refer to {@link WSHandlerConstants}. For signature
	 * {@code IssuerSerial} and {@code DirectReference} are valid only.
	 */
	public void setSecurementSignatureKeyIdentifier(String securementSignatureKeyIdentifier) {
		setWsHandlerOption(WSHandlerConstants.SIG_KEY_ID, securementSignatureKeyIdentifier);
	}

	/**
	 * Property to define which parts of the request shall be signed.
	 * <p>
	 * Refer to {@link #setSecurementEncryptionParts(String)} for a detailed description
	 * of the format of the value string.
	 * <p>
	 * If this property is not specified the handler signs the SOAP Body by default.
	 * <p>
	 * The WS Security specifications define several formats to transfer the signature
	 * tokens (certificates) or references to these tokens. Thus, the plain element name
	 * {@code Token} signs the token and takes care of the different formats.
	 * <p>
	 * To sign the SOAP body <b>and</b> the signature token the value of this parameter
	 * must contain:
	 *
	 * <pre>
	 * &lt;property name="securementSignatureParts"
	 *	 value="{}{http://schemas.xmlsoap.org/soap/envelope/}Body; Token" />
	 * </pre>
	 *
	 * To specify an element without a namespace use the string {@code Null} as the
	 * namespace name (this is a case-sensitive string)
	 * <p>
	 * If there is no other element in the request with a local name of {@code Body} then
	 * the SOAP namespace identifier can be empty ({@code {}}).
	 */
	public void setSecurementSignatureParts(String securementSignatureParts) {
		setWsHandlerOption(WSHandlerConstants.SIGNATURE_PARTS, securementSignatureParts);
	}

	/**
	 * The user's name for signature.
	 * <p>
	 * This name is used as the alias name in the keystore to get user's certificate and
	 * private key to perform signing.
	 * <p>
	 * If this parameter is not set, then the signature function falls back to the alias
	 * specified by {@link #setSecurementUsername(String)}.
	 */
	public void setSecurementSignatureUser(String securementSignatureUser) {
		setWsHandlerOption(WSHandlerConstants.SIGNATURE_USER, securementSignatureUser);
	}

	/**
	 * Sets the username for securement username token or/and the alias of the private key
	 * for securement signature.
	 */
	public void setSecurementUsername(String securementUsername) {
		this.securementUsername = securementUsername;
	}

	/**
	 * Sets the time to live on the outgoing message.
	 */
	public void setSecurementTimeToLive(int securementTimeToLive) {

		if (securementTimeToLive <= 0) {
			throw new IllegalArgumentException("timeToLive must be positive");
		}
		this.securementTimeToLive = securementTimeToLive;
	}

	/**
	 * Enables the derivation of keys as per the UsernameTokenProfile 1.1 spec. Default is
	 * {@code true}.
	 */
	public void setSecurementUseDerivedKey(boolean securementUseDerivedKey) {
		this.securementUseDerivedKey = securementUseDerivedKey;
	}

	/**
	 * Sets the SAML Callback used for generating SAML tokens.
	 * @param samlCallbackHandler the SAML callback handler
	 */
	public void setSecurementSamlCallbackHandler(CallbackHandler samlCallbackHandler) {
		this.samlCallbackHandler = samlCallbackHandler;
	}

	/**
	 * Set the {@link CallbackHandler} to use to sign/encrypt attachments.
	 * @param attachmentCallbackHandler the attachment callback handler
	 * @since 4.1.0
	 */
	public void setAttachmentCallbackHandler(CallbackHandler attachmentCallbackHandler) {
		this.attachmentCallbackHandler = attachmentCallbackHandler;
	}

	/**
	 * Sets the server-side time to live.
	 */
	public void setValidationTimeToLive(int validationTimeToLive) {

		if (validationTimeToLive <= 0) {
			throw new IllegalArgumentException("timeToLive must be positive");
		}
		this.validationTimeToLive = validationTimeToLive;
	}

	/** Sets the validation actions to be executed by the interceptor. */
	public void setValidationActions(String actions) {

		this.validationActions = actions;
		try {
			this.validationActionsVector = WSSecurityUtil.decodeAction(actions);
		}
		catch (WSSecurityException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public void setValidationActor(String validationActor) {
		this.validationActor = validationActor;
	}

	/**
	 * Sets the {@link CallbackHandler} to use when validating messages.
	 * @see #setValidationCallbackHandlers(CallbackHandler[])
	 */
	public void setValidationCallbackHandler(CallbackHandler callbackHandler) {
		this.validationCallbackHandler = callbackHandler;
	}

	/**
	 * Sets the {@link CallbackHandler}s to use when validating messages.
	 * @see #setValidationCallbackHandler(CallbackHandler)
	 */
	public void setValidationCallbackHandlers(CallbackHandler[] callbackHandler) {
		this.validationCallbackHandler = new CallbackHandlerChain(callbackHandler);
	}

	/**
	 * Sets the Crypto to use to decrypt incoming messages.
	 */
	public void setValidationDecryptionCrypto(Crypto decryptionCrypto) {
		this.validationDecryptionCrypto = decryptionCrypto;
	}

	/**
	 * Sets the Crypto to use to verify the signature of incoming messages.
	 */
	public void setValidationSignatureCrypto(Crypto signatureCrypto) {
		this.validationSignatureCrypto = signatureCrypto;
	}

	/**
	 * Certificate constraints which will be applied to the subject DN of the certificate
	 * used for signature validation, after trust verification of the certificate chain
	 * associated with the certificate.
	 * @param patterns a list of regex patterns which will be applied to the subject DN.
	 * @see ConfigurationConstants#SIG_SUBJECT_CERT_CONSTRAINTS
	 */
	public void setValidationSubjectDnConstraints(List<Pattern> patterns) {
		this.signatureSubjectDnPatterns = patterns;
	}

	/**
	 * Whether to enable signatureConfirmation or not. By default, signatureConfirmation
	 * is enabled.
	 */
	public void setEnableSignatureConfirmation(boolean enableSignatureConfirmation) {

		setWsHandlerOption(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION, enableSignatureConfirmation);
		this.enableSignatureConfirmation = enableSignatureConfirmation;
	}

	/**
	 * Sets if the generated timestamp header's precision is in milliseconds.
	 */
	public void setTimestampPrecisionInMilliseconds(boolean timestampPrecisionInMilliseconds) {
		setWsHandlerOption(WSHandlerConstants.TIMESTAMP_PRECISION, timestampPrecisionInMilliseconds);
	}

	/**
	 * Sets whether timestamp verification is done with the server-side time to live.
	 */
	public void setTimestampStrict(boolean timestampStrict) {
		this.timestampStrict = timestampStrict;
	}

	/**
	 * Enables the {@code mustUnderstand} attribute on WS-Security headers on outgoing
	 * messages. Default is {@code true}.
	 */
	public void setSecurementMustUnderstand(boolean securementMustUnderstand) {
		setWsHandlerOption(WSHandlerConstants.MUST_UNDERSTAND, securementMustUnderstand);
	}

	/**
	 * Sets whether a {@code Nonce} element is added to the {@code UsernameToken}s.
	 * Default is {@code false}.
	 */
	public void setSecurementUsernameTokenNonce(boolean securementUsernameTokenNonce) {
		setWsHandlerOption(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE, securementUsernameTokenNonce);
	}

	/**
	 * Sets whether a {@code Created} element is added to the {@code UsernameToken}s.
	 * Default is {@code false}.
	 */
	public void setSecurementUsernameTokenCreated(boolean securementUsernameTokenCreated) {
		setWsHandlerOption(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED, securementUsernameTokenCreated);
	}

	/**
	 * Sets the web service specification settings.
	 * <p>
	 * The default settings follow the latest OASIS and changing anything might violate
	 * the OASIS specs.
	 * @param config web service security configuration or {@code null} to use default
	 * settings
	 */
	public void setWssConfig(WSSConfig config) {

		this.securityEngine.setWssConfig(config);
		this.wssConfig = config;
	}

	/**
	 * Set whether to enable CRL checking or not when verifying trust in a certificate.
	 */
	public void setEnableRevocation(boolean enableRevocation) {
		this.enableRevocation = enableRevocation;
	}

	/**
	 * Set the WS-I Basic Security Profile compliance mode. Default is {@code true}.
	 */
	public void setBspCompliant(boolean bspCompliant) {

		setWsHandlerOption(WSHandlerConstants.IS_BSP_COMPLIANT, bspCompliant);
		this.bspCompliant = bspCompliant;
	}

	/**
	 * Sets whether to add an InclusiveNamespaces PrefixList as a CanonicalizationMethod
	 * child when generating Signatures using WSConstants.C14N_EXCL_OMIT_COMMENTS. Default
	 * is {@code true}.
	 */
	public void setAddInclusivePrefixes(boolean addInclusivePrefixes) {

		setWsHandlerOption(WSHandlerConstants.ADD_INCLUSIVE_PREFIXES, addInclusivePrefixes);
		this.addInclusivePrefixes = addInclusivePrefixes;
	}

	/**
	 * Set whether to use a single certificate or a whole certificate chain when
	 * constructing a {@code BinarySecurityToken} used for direct reference in signature.
	 * default is {@code true}, meaning that only a single certificate is used.
	 * @param useSingleCertificate whether to use a single certificate
	 * @since 4.1.0
	 * @see WSHandlerConstants#USE_SINGLE_CERTIFICATE
	 */
	public void setUseSingleCertificate(boolean useSingleCertificate) {
		setWsHandlerOption(WSHandlerConstants.USE_SINGLE_CERTIFICATE, useSingleCertificate);
	}

	/**
	 * Sets whether the RSA 1.5 key transport algorithm is allowed.
	 */
	public void setAllowRSA15KeyTransportAlgorithm(boolean allow) {
		this.allowRSA15KeyTransportAlgorithm = allow;
	}

	/**
	 * Sets the time in seconds in the future within which the Created time of an incoming
	 * Timestamp is valid. The default is 60 seconds.
	 */
	public void setFutureTimeToLive(int futureTimeToLive) {

		if (futureTimeToLive <= 0) {
			throw new IllegalArgumentException("futureTimeToLive must be positive");
		}
		this.futureTimeToLive = futureTimeToLive;
	}

	public boolean getRemoveSecurityHeader() {
		return this.removeSecurityHeader;
	}

	public void setRemoveSecurityHeader(boolean removeSecurityHeader) {
		this.removeSecurityHeader = removeSecurityHeader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		Assert.isTrue(this.validationActions != null || this.securementActions != null,
				"validationActions or securementActions are required");
		if (this.validationActions != null) {
			if (this.validationActionsVector.contains(WSConstants.UT)) {
				Assert.notNull(this.validationCallbackHandler, "validationCallbackHandler is required");
			}

			if (this.validationActionsVector.contains(WSConstants.SIGN)) {
				Assert.notNull(this.validationSignatureCrypto, "validationSignatureCrypto is required");
			}
		}
		// securement actions are not to be validated at start up as they could
		// be configured dynamically via the message context
	}

	@Override
	protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
			throws WsSecuritySecurementException {

		List<HandlerAction> securementActionsVector;
		try {
			securementActionsVector = WSSecurityUtil.decodeHandlerAction(this.securementActions, this.wssConfig);
		}
		catch (WSSecurityException ex) {
			throw new Wss4jSecuritySecurementException(ex.getMessage(), ex);
		}

		if (securementActionsVector.isEmpty() && !this.enableSignatureConfirmation) {
			return;
		}
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Securing message [" + soapMessage + "] with actions [" + this.securementActions + "]");
		}
		RequestData requestData = initializeRequestData(messageContext);

		Document envelopeAsDocument = soapMessage.getDocument();
		try {
			this.handler.doSenderAction(envelopeAsDocument, requestData, securementActionsVector, false);
		}
		catch (WSSecurityException ex) {
			throw new Wss4jSecuritySecurementException(ex.getMessage(), ex);
		}

		soapMessage.setDocument(envelopeAsDocument);
	}

	/**
	 * Creates and initializes a request data for the given message context.
	 * @param messageContext the message context
	 * @return the request data
	 */
	protected RequestData initializeRequestData(MessageContext messageContext) {

		RequestData requestData = new RequestData();
		requestData.setMsgContext(messageContext);

		// reads securementUsername first from the context then from the property
		String contextUsername = (String) messageContext.getProperty(SECUREMENT_USER_PROPERTY_NAME);
		if (StringUtils.hasLength(contextUsername)) {
			requestData.setUsername(contextUsername);
		}
		else {
			requestData.setUsername(this.securementUsername);
		}

		requestData.setTimeStampTTL(this.securementTimeToLive);

		requestData.setUseDerivedKeyForMAC(this.securementUseDerivedKey);

		requestData.setWssConfig(this.wssConfig);

		messageContext.setProperty(WSHandlerConstants.TTL_TIMESTAMP, Integer.toString(this.securementTimeToLive));

		if (this.samlCallbackHandler != null) {
			messageContext.setProperty(WSHandlerConstants.SAML_CALLBACK_REF, this.samlCallbackHandler);
		}
		if (this.attachmentCallbackHandler != null) {
			requestData.setAttachmentCallbackHandler(this.attachmentCallbackHandler);
		}

		// allow for qualified password types for .Net interoperability
		requestData.setAllowNamespaceQualifiedPasswordTypes(true);

		requestData.setSubjectCertConstraints(this.signatureSubjectDnPatterns);
		return requestData;
	}

	/**
	 * Creates and initializes a request data for the given message context.
	 * @param messageContext the message context
	 * @return the request data
	 */
	protected RequestData initializeValidationRequestData(MessageContext messageContext) {

		RequestData requestData = new RequestData();
		requestData.setMsgContext(messageContext);

		requestData.setWssConfig(this.wssConfig);

		requestData.setDecCrypto(this.validationDecryptionCrypto);

		requestData.setSigVerCrypto(this.validationSignatureCrypto);

		requestData.setCallbackHandler(this.validationCallbackHandler);

		messageContext.setProperty(WSHandlerConstants.TIMESTAMP_STRICT, this.timestampStrict);
		messageContext.setProperty(WSHandlerConstants.TTL_TIMESTAMP, Integer.toString(this.validationTimeToLive));
		messageContext.setProperty(WSHandlerConstants.TTL_FUTURE_TIMESTAMP, Integer.toString(this.futureTimeToLive));

		requestData.setTimeStampStrict(this.timestampStrict);
		requestData.setTimeStampTTL(this.validationTimeToLive);
		requestData.setTimeStampFutureTTL(this.futureTimeToLive);

		requestData.setAllowRSA15KeyTransportAlgorithm(this.allowRSA15KeyTransportAlgorithm);

		requestData.setDisableBSPEnforcement(!this.bspCompliant);
		if (requestData.getBSPEnforcer() != null) {
			requestData.getBSPEnforcer().setDisableBSPRules(!this.bspCompliant);
		}
		requestData.setAddInclusivePrefixes(this.addInclusivePrefixes);
		// allow for qualified password types for .Net interoperability
		requestData.setAllowNamespaceQualifiedPasswordTypes(true);

		requestData.setSubjectCertConstraints(this.signatureSubjectDnPatterns);
		return requestData;
	}

	@Override
	protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
			throws WsSecurityValidationException {

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Validating message [" + soapMessage + "] with actions [" + this.validationActions + "]");
		}

		if (CollectionUtils.isEmpty(this.validationActionsVector)) {
			return;
		}

		Document envelopeAsDocument = soapMessage.getDocument();

		// Header processing

		try {
			RequestData validationData = initializeValidationRequestData(messageContext);

			String actor = this.validationActor;
			if (actor == null) {
				actor = "";
			}

			Element elem = WSSecurityUtil.getSecurityHeader(envelopeAsDocument, actor);
			WSHandlerResult result = this.securityEngine.processSecurityHeader(elem, validationData);

			// Results verification
			if (CollectionUtils.isEmpty(result.getResults())) {
				throw new Wss4jSecurityValidationException("No WS-Security header found");
			}

			checkResults(result.getResults(), this.validationActionsVector);

			// puts the results in the context
			// useful for Signature Confirmation
			updateContextWithResults(messageContext, result.getResults());

			verifyCertificateTrust(result);

			verifyTimestamp(result);

			processPrincipal(result);
		}
		catch (WSSecurityException ex) {
			throw new Wss4jSecurityValidationException(ex.getMessage(), ex);
		}

		soapMessage.setDocument(envelopeAsDocument);

		if (this.getRemoveSecurityHeader()) {
			SoapHeader header = soapMessage.getEnvelope().getHeader();
			if (header != null) {
				header.removeHeaderElement(WS_SECURITY_NAME);
			}
		}
	}

	/**
	 * Checks whether the received headers match the configured validation actions.
	 * Subclasses could override this method for custom verification behavior.
	 * @param results the results of the validation function
	 * @param validationActions the decoded validation actions
	 * @throws Wss4jSecurityValidationException if the results are deemed invalid
	 */
	protected void checkResults(List<WSSecurityEngineResult> results, List<Integer> validationActions)
			throws Wss4jSecurityValidationException {

		if (!this.handler.checkReceiverResultsAnyOrder(results, validationActions)) {
			throw new Wss4jSecurityValidationException("Security processing failed (actions mismatch)");
		}
	}

	/**
	 * Puts the results of WS-Security headers processing in the message context. Some
	 * actions like Signature Confirmation require this.
	 */
	@SuppressWarnings("unchecked")
	private void updateContextWithResults(MessageContext messageContext, List<WSSecurityEngineResult> results) {

		List<WSHandlerResult> handlerResults = (List<WSHandlerResult>) messageContext
			.getProperty(WSHandlerConstants.RECV_RESULTS);
		if (handlerResults == null) {
			handlerResults = new ArrayList<>();
			messageContext.setProperty(WSHandlerConstants.RECV_RESULTS, handlerResults);
		}
		WSHandlerResult rResult = new WSHandlerResult(this.validationActor, results, Collections.emptyMap());
		handlerResults.add(0, rResult);
		messageContext.setProperty(WSHandlerConstants.RECV_RESULTS, handlerResults);
	}

	/**
	 * Verifies the trust of a certificate.
	 * @param result the {@link WSHandlerResult} to use
	 */
	protected void verifyCertificateTrust(WSHandlerResult result) throws WSSecurityException {

		List<WSSecurityEngineResult> results = result.getActionResults().get(WSConstants.SIGN);

		if (!CollectionUtils.isEmpty(results)) {
			WSSecurityEngineResult actionResult = results.get(0);
			X509Certificate returnCert = (X509Certificate) actionResult
				.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
			Credential credential = new Credential();
			credential.setCertificates(new X509Certificate[] { returnCert });

			RequestData requestData = new RequestData();
			requestData.setSigVerCrypto(this.validationSignatureCrypto);
			requestData.setEnableRevocation(this.enableRevocation);
			requestData.setSubjectCertConstraints(this.signatureSubjectDnPatterns);

			Validator validator = (this.wssConfig != null) ? this.wssConfig.getValidator(WSConstants.SIGNATURE) : null;
			if (validator == null) {
				validator = new SignatureTrustValidator();
			}
			validator.validate(credential, requestData);
		}
	}

	/**
	 * Verifies the timestamp.
	 * @param result the {@link WSHandlerResult} to use
	 */
	protected void verifyTimestamp(WSHandlerResult result) throws WSSecurityException {

		List<WSSecurityEngineResult> results = result.getActionResults().get(WSConstants.TS);

		if (!CollectionUtils.isEmpty(results)) {
			WSSecurityEngineResult actionResult = results.get(0);
			Timestamp timestamp = (Timestamp) actionResult.get(WSSecurityEngineResult.TAG_TIMESTAMP);
			if (timestamp != null && this.timestampStrict) {
				Credential credential = new Credential();
				credential.setTimestamp(timestamp);

				RequestData requestData = new RequestData();
				requestData.setWssConfig(WSSConfig.getNewInstance());
				requestData.setTimeStampTTL(this.validationTimeToLive);
				requestData.setTimeStampStrict(this.timestampStrict);
				requestData.setTimeStampFutureTTL(this.futureTimeToLive);

				TimestampValidator validator = new TimestampValidator();
				validator.validate(credential, requestData);
			}
		}
	}

	private void processPrincipal(WSHandlerResult result) {

		List<WSSecurityEngineResult> results = result.getActionResults().get(WSConstants.UT);

		if (!CollectionUtils.isEmpty(results)) {
			WSSecurityEngineResult actionResult = results.get(0);
			Principal principal = (Principal) actionResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
			if (principal instanceof WSUsernameTokenPrincipalImpl usernameTokenPrincipal) {
				UsernameTokenPrincipalCallback callback = new UsernameTokenPrincipalCallback(usernameTokenPrincipal);
				try {
					Objects.requireNonNull(this.validationCallbackHandler).handle(new Callback[] { callback });
				}
				catch (IOException ex) {
					this.logger.warn("Principal callback resulted in IOException", ex);
				}
				catch (UnsupportedCallbackException ex) {
					// ignore
				}
			}
		}
	}

	@Override
	protected void cleanUp() {

		if (this.validationCallbackHandler != null) {
			try {
				CleanupCallback cleanupCallback = new CleanupCallback();
				this.validationCallbackHandler.handle(new Callback[] { cleanupCallback });
			}
			catch (IOException ex) {
				this.logger.warn("Cleanup callback resulted in IOException", ex);
			}
			catch (UnsupportedCallbackException ex) {
				// ignore
			}
		}
	}

}
