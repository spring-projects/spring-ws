package org.springframework.ws.soap.axiom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTestCase;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AxiomSoap11BodyTest extends AbstractSoap11BodyTestCase {

    private AxiomSoapMessage axiomSoapMessage;

    private DocumentBuilder documentBuilder;

    protected SoapBody createSoapBody() throws Exception {
        SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
        axiomSoapMessage = new AxiomSoapMessage(axiomFactory);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return axiomSoapMessage.getSoapBody();
    }

    public void testAddFault() throws Exception {
        QName faultCode = new QName("http://www.springframework.org", "fault", "spring");
        String faultString = "faultString";
        Soap11Fault fault = ((Soap11Body) soapBody).addFault(faultCode, faultString, null);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", soapBody.hasFault());
        assertNotNull("SoapBody has no fault", soapBody.getFault());
        assertEquals("Invalid fault code", faultCode, fault.getFaultCode());
        assertEquals("Invalid fault string", faultString, fault.getFaultString());
        String actor = "http://www.springframework.org/actor";
        fault.setFaultActorOrRole(actor);
        assertEquals("Invalid fault actor", actor, fault.getFaultActorOrRole());
        assertPayloadEqual("<SOAP-ENV:Fault xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' " +
                "xmlns:spring='http://www.springframework.org'>" + "<faultcode>spring:fault</faultcode>" +
                "<faultstring>" + faultString + "</faultstring>" + "<faultactor>" + actor + "</faultactor>" +
                "</SOAP-ENV:Fault>");
    }

    /**
     * Overriden because of http://issues.apache.org/jira/browse/WSCOMMONS-38. Axiom does not return valid xml
     */
    protected void assertPayloadEqual(String expectedPayload) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        axiomSoapMessage.writeTo(os);
        Document document = documentBuilder.parse(new ByteArrayInputStream(os.toByteArray()));
        NodeList nodes = document.getElementsByTagNameNS(SoapVersion.SOAP_11.getEnvelopeNamespaceUri(), "Body");
        assertEquals("No Body found", 1, nodes.getLength());
        assertEquals("No Body found", Node.ELEMENT_NODE, nodes.item(0).getNodeType());
        Element bodyElement = (Element) nodes.item(0);
        Element firstChild = (Element) bodyElement.getFirstChild();
        Result result = new StringResult();
        transformer.transform(new DOMSource(firstChild), result);
        assertXMLEqual("Invalid payload contents", expectedPayload, result.toString());
    }
}
