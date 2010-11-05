/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.server;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.test.support.matcher.PayloadDiffMatcher;
import org.springframework.ws.test.support.matcher.SchemaValidatingMatcher;
import org.springframework.ws.test.support.matcher.SoapHeaderMatcher;
import org.springframework.xml.transform.ResourceSource;

import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

/**
 * Factory methods for {@link ResponseMatcher} classes. Typically used to provide input for {@link
 * ResponseActions#andExpect(ResponseMatcher)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class ResponseMatchers {

    private ResponseMatchers() {
    }

    // Payload

    /**
     * Expects the given {@link Source} XML payload.
     *
     * @param payload the XML payload
     * @return the response matcher
     */
    public static ResponseMatcher payload(Source payload) {
        return new WebServiceMessageMatcherAdapter(new PayloadDiffMatcher(payload));
    }

    /**
     * Expects the given {@link Resource} XML payload.
     *
     * @param payload the XML payload
     * @return the response matcher
     */
    public ResponseMatcher payload(Resource payload) throws IOException {
        return payload(new ResourceSource(payload));
    }

    /**
     * Expects the payload to validate against the given XSD schema(s).
     *
     * @param schema         the schema
     * @param furtherSchemas further schemas, if necessary
     * @return the response matcher
     */
    public static ResponseMatcher validPayload(Resource schema, Resource... furtherSchemas) throws IOException {
        return new WebServiceMessageMatcherAdapter(new SchemaValidatingMatcher(schema, furtherSchemas));
    }

    /**
     * Expects the given XPath expression to (not) exist or be evaluated to a value.
     *
     * @param xpathExpression the XPath expression
     * @return the XPath expectations, to be further configured
     */
    public static ResponseXPathExpectations xpath(String xpathExpression) {
        return new XPathExpectationsHelperAdapter(xpathExpression, null);
    }

    /**
     * Expects the given XPath expression to (not) exist or be evaluated to a value.
     *
     * @param xpathExpression  the XPath expression
     * @param namespaceMapping the namespaces
     * @return the XPath expectations, to be further configured
     */
    public static ResponseXPathExpectations xpath(String xpathExpression, Map<String, String> namespaceMapping) {
        return new XPathExpectationsHelperAdapter(xpathExpression, namespaceMapping);
    }


    // SOAP

    /**
     * Expects the given SOAP header in the outgoing message.
     *
     * @param soapHeaderName the qualified name of the SOAP header to expect
     * @return the request matcher
     */
    public static ResponseMatcher soapHeader(QName soapHeaderName) {
        Assert.notNull(soapHeaderName, "'soapHeaderName' must not be null");
        return new WebServiceMessageMatcherAdapter(new SoapHeaderMatcher(soapHeaderName));
    }

    /**
     * Expects the response <strong>not</strong> to contain a SOAP fault.
     *
     * @return the response matcher
     */
    public static ResponseMatcher noFault() {
        return new ResponseMatcher() {
            public void match(WebServiceMessage request, WebServiceMessage response)
                    throws IOException, AssertionError {
                if (response instanceof FaultAwareWebServiceMessage) {
                    FaultAwareWebServiceMessage faultMessage = (FaultAwareWebServiceMessage) response;
                    assertTrue("Response has a SOAP Fault", !faultMessage.hasFault());
                }
            }

        };
    }

    /**
     * Expects a {@code MustUnderstand} fault.
     *
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, Locale)
     */
    public static ResponseMatcher mustUnderstandFault() {
        return mustUnderstandFault(null);
    }

    /**
     * Expects a {@code MustUnderstand} fault with a particular fault string or reason.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text. If {@code null} the fault string or
     * reason text will not be verified
     * @see org.springframework.ws.soap.SoapBody#addMustUnderstandFault(String, Locale)
     */
    public static ResponseMatcher mustUnderstandFault(String faultStringOrReason) {
        return new SoapFaultResponseMatcher(faultStringOrReason) {
            @Override
            protected QName getExpectedFaultCode(SoapVersion version) {
                return version.getMustUnderstandFaultName();
            }
        };
    }

    /**
     * Expects a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault.
     *
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseMatcher clientOrSenderFault() {
        return clientOrSenderFault(null);
    }

    /**
     * Expects a {@code Client} (SOAP 1.1) or {@code Sender} (SOAP 1.2) fault with a particular fault string or reason.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text. If {@code null} the fault string or
     * reason text will not be verified
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseMatcher clientOrSenderFault(String faultStringOrReason) {
        return new SoapFaultResponseMatcher(faultStringOrReason) {
            @Override
            protected QName getExpectedFaultCode(SoapVersion version) {
                return version.getClientOrSenderFaultName();
            }
        };
    }

    /**
     * Expects a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault.
     *
     * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String, java.util.Locale)
     */
    public static ResponseMatcher serverOrReceiverFault() {
        return serverOrReceiverFault(null);
    }

    /**
     * Expects a {@code Server} (SOAP 1.1) or {@code Receiver} (SOAP 1.2) fault with a particular fault string or reason.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text. If {@code null} the fault string or
     * reason text will not be verified
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseMatcher serverOrReceiverFault(String faultStringOrReason) {
        return new SoapFaultResponseMatcher(faultStringOrReason) {
            @Override
            protected QName getExpectedFaultCode(SoapVersion version) {
                return version.getServerOrReceiverFaultName();
            }
        };
    }

    /**
     * Expects a {@code VersionMismatch} fault.
     *
     * @see org.springframework.ws.soap.SoapBody#addVersionMismatchFault(String, java.util.Locale)
     */
    public static ResponseMatcher versionMismatchFault() {
        return versionMismatchFault(null);
    }

    /**
     * Expects a {@code VersionMismatch} fault with a particular fault string or reason.
     *
     * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text. If {@code null} the fault string or
     * reason text will not be verified
     * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String, Locale)
     */
    public static ResponseMatcher versionMismatchFault(String faultStringOrReason) {
        return new SoapFaultResponseMatcher(faultStringOrReason) {
            @Override
            protected QName getExpectedFaultCode(SoapVersion version) {
                return version.getVersionMismatchFaultName();
            }
        };
    }

}
