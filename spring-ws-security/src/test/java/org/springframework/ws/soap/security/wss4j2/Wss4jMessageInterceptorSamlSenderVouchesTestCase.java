package org.springframework.ws.soap.security.wss4j2;

import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.servlet.http.HttpServletRequest;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoType;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.saml.SAMLCallback;
import org.apache.wss4j.common.saml.bean.KeyInfoBean;
import org.apache.wss4j.common.saml.bean.SubjectBean;
import org.apache.wss4j.common.saml.bean.Version;
import org.apache.wss4j.common.saml.builder.SAML2Constants;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.w3c.dom.Document;

public abstract class Wss4jMessageInterceptorSamlSenderVouchesTestCase extends Wss4jTestCase {

	protected Wss4jSecurityInterceptor interceptor;
	
	protected X509Certificate certificate;

	@Override
	protected void onSetup() throws Exception {

		interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("SAMLTokenSigned");
		interceptor.setValidationActions("SAMLTokenSigned Signature");
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setCryptoProvider(Merlin.class);
		cryptoFactoryBean.setKeyStoreType("jceks");
		cryptoFactoryBean.setKeyStorePassword("123456");
		cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
		cryptoFactoryBean.afterPropertiesSet();
		Crypto crypto = cryptoFactoryBean.getObject();

		CryptoType type = new CryptoType(CryptoType.TYPE.ALIAS);
		type.setAlias("rsaKey");
		certificate = crypto.getX509Certificates(type)[0];

		interceptor.setSecurementSignatureCrypto(crypto);
		interceptor.setValidationSignatureCrypto(crypto);
		interceptor.setSecurementSamlCallbackHandler(getSamlCalbackHandler(crypto, certificate));
		interceptor.afterPropertiesSet();
	}

	@Test
	public void testAddSAML() throws Exception {

		interceptor.setSecurementPassword("123456");
		interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);

		assertXpathExists("Absent SAML Assertion element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/saml:Assertion", document);

		// lets verify the signature that we've just generated
		interceptor.validateMessage(message, messageContext);
	}

	@Test
	public void testAddSAMLWithTlsClientCert() throws Exception {

		interceptor.setSecurementPassword("123456");
		interceptor.setSecurementUsername("rsaKey");
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);

		interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);

		assertXpathExists("Absent SAML Assertion element",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/saml:Assertion", document);
		
		// Set the client certificate on the transport context as it would be for mutual authenticated TLS
		HttpServletConnection conn = EasyMock.createNiceMock(HttpServletConnection.class);
		HttpServletRequest req = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(req.getAttribute(EasyMock.eq("javax.servlet.request.X509Certificate")))
			.andReturn(new X509Certificate[] {certificate}).anyTimes();
		EasyMock.replay(req);
		EasyMock.expect(conn.getHttpServletRequest()).andReturn(req).anyTimes();
		EasyMock.replay(conn);
		TransportContext transportCtx = new DefaultTransportContext(conn);
		try {
			TransportContextHolder.setTransportContext(transportCtx);

			// lets verify the signature that we've just generated
			interceptor.validateMessage(message, messageContext);
		} finally {
			TransportContextHolder.setTransportContext(null);
		}
	}

	protected CallbackHandler getSamlCalbackHandler(Crypto crypto, X509Certificate userCert) {
		return new SamlSenderVouchesCallbackHandler(crypto, userCert);
	}

	private class SamlSenderVouchesCallbackHandler implements CallbackHandler {

		private Crypto crypto;

		private X509Certificate userCertificate;

		public SamlSenderVouchesCallbackHandler(Crypto crypto, X509Certificate userCertificate) {

			this.crypto = crypto;
			this.userCertificate = userCertificate;
		}

		@Override
		public void handle(Callback[] callbacks) {

			for (Callback value : callbacks) {
				if (value instanceof SAMLCallback) {

					SAMLCallback callback = (SAMLCallback) value;
					callback.setSamlVersion(Version.SAML_20);
					callback.setIssuerCrypto(crypto);
					callback.setIssuerKeyName("rsaKey");
					callback.setIssuerKeyPassword("123456");
					callback.setIssuer("test-issuer");
					SubjectBean subject = new SubjectBean("test-subject", "", SAML2Constants.CONF_SENDER_VOUCHES);
					KeyInfoBean keyInfo = new KeyInfoBean();
					keyInfo.setCertificate(userCertificate);
					subject.setKeyInfo(keyInfo);
					callback.setSubject(subject);
					callback.setSignAssertion(true);
				}
			}
		}
	}
}
