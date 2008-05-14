package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;

/**
 * Axiom-Specific version of <code>org.springframework.ws.soap.SoapEnvelope</code>.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapEnvelope extends AxiomSoapElement implements SoapEnvelope {

    boolean payloadCaching;

    private AxiomSoapBody body;

    AxiomSoapEnvelope(SOAPEnvelope axiomEnvelope, SOAPFactory axiomFactory, boolean payloadCaching) {
        super(axiomEnvelope, axiomFactory);
        this.payloadCaching = payloadCaching;
    }

    public SoapHeader getHeader() {
        try {
            if (getAxiomEnvelope().getHeader() == null) {
                return null;
            }
            else {
                SOAPHeader axiomHeader = getAxiomEnvelope().getHeader();
                String namespaceURI = getAxiomEnvelope().getNamespace().getNamespaceURI();
                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    return new AxiomSoap11Header(axiomHeader, getAxiomFactory());
                }
                else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    return new AxiomSoap12Header(axiomHeader, getAxiomFactory());
                }
                else {
                    throw new AxiomSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
                }
            }
        }
        catch (OMException ex) {
            throw new AxiomSoapHeaderException(ex);
        }
    }

    public SoapBody getBody() {
        if (body == null) {
            try {
                SOAPBody axiomBody = getAxiomEnvelope().getBody();
                String namespaceURI = getAxiomEnvelope().getNamespace().getNamespaceURI();
                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    body = new AxiomSoap11Body(axiomBody, getAxiomFactory(), payloadCaching);
                }
                else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    body = new AxiomSoap12Body(axiomBody, getAxiomFactory(), payloadCaching);
                }
                else {
                    throw new AxiomSoapEnvelopeException("Unknown SOAP namespace \"" + namespaceURI + "\"");
                }
            }
            catch (OMException ex) {
                throw new AxiomSoapBodyException(ex);
            }
        }
        return body;
    }

    protected SOAPEnvelope getAxiomEnvelope() {
        return (SOAPEnvelope) getAxiomElement();
    }

}
