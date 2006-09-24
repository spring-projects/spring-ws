package org.springframework.ws.soap.axiom;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.xml.transform.StaxSource;

/**
 * Axiom-Specific version of <code>org.springframework.ws.soap.SoapEnvelope</code>.
 *
 * @author Arjen Poutsma
 */
class AxiomSoapEnvelope implements SoapEnvelope {

    private final SOAPEnvelope axiomEnvelope;

    private final SOAPFactory axiomFactory;

    boolean payloadCaching;

    private AxiomSoapBody body;

    public AxiomSoapEnvelope(SOAPEnvelope axiomEnvelope, SOAPFactory axiomFactory, boolean payloadCaching) {
        Assert.notNull(axiomEnvelope, "No axiomEnvelope given");
        Assert.notNull(axiomFactory, "No axiomFactory given");
        this.axiomEnvelope = axiomEnvelope;
        this.axiomFactory = axiomFactory;
        this.payloadCaching = payloadCaching;
    }

    public QName getName() {
        return axiomEnvelope.getQName();
    }

    public Source getSource() {
        try {
            return new StaxSource(axiomEnvelope.getXMLStreamReader());
        }
        catch (OMException ex) {
            throw new AxiomSoapEnvelopeException(ex);
        }
    }

    public SoapHeader getHeader() {
        try {
            if (axiomEnvelope.getHeader() == null) {
                return null;
            }
            else {
                SOAPHeader axiomHeader = axiomEnvelope.getHeader();
                String namespaceURI = axiomEnvelope.getNamespace().getName();
                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    return new AxiomSoapHeader(axiomHeader, axiomFactory);
                }
                else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    return new AxiomSoap12Header(axiomHeader, axiomFactory);
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
                SOAPBody axiomBody = axiomEnvelope.getBody();
                String namespaceURI = axiomEnvelope.getNamespace().getName();
                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    body = new AxiomSoap11Body(axiomBody, axiomFactory, payloadCaching);
                }
                else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(namespaceURI)) {
                    body = new AxiomSoap12Body(axiomBody, axiomFactory, payloadCaching);
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
}
