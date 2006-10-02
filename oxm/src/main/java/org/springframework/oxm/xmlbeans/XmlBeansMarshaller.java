package org.springframework.oxm.xmlbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.XmlValidationError;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.stream.StaxEventContentHandler;
import org.springframework.xml.stream.StaxEventXmlReader;
import org.springframework.xml.stream.StaxStreamContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> interface for XMLBeans. Further options can be set by setting the
 * <code>xmlOptions</code> property. A <code>XmlOptionsFactoryBean</code> is provided to easily wire up
 * <code>XmlObjects</code> instances.
 * <p/>
 * Unmarshalled objects can be validated by setting the <code>validating</code> property, or by calling the
 * <code>validiate()</code> method directly. Invalid objects will result in an <code>XmlBeansValidationFailureException</code>.
 * <p/>
 * <strong>Note</strong> that due to the nature of XMLBeans, this marshaller requires all passed objects to be of type
 * <code>XmlObject</code>.
 *
 * @author Arjen Poutsma
 * @see #setXmlOptions(org.apache.xmlbeans.XmlOptions)
 * @see XmlOptionsFactoryBean
 * @see #setValidating(boolean)
 * @see org.apache.xmlbeans.XmlObject
 */
public class XmlBeansMarshaller extends AbstractMarshaller {

    private XmlOptions xmlOptions;

    /**
     * Sets the <code>XmlOptions</code>.
     *
     * @param xmlOptions the xml options
     */
    public void setXmlOptions(XmlOptions xmlOptions) {
        this.xmlOptions = xmlOptions;
    }

    /**
     * Converts the given XMLBeans exception to an appropriate exception from the <code>org.springframework.oxm</code>
     * hierarchy.
     * <p/>
     * The default implementation delegates to <code>XmlBeansUtils</code>. Can be overridden in subclasses.
     * <p/>
     * A boolean flag is used to indicate whether this exception occurs during marshalling or unmarshalling, since
     * XMLBeans itself does not make this distinction in its exception hierarchy.
     *
     * @param ex          XMLBeans Exception that occured
     * @param marshalling indicates whether the exception occurs during marshalling (<code>true</code>), or
     *                    unmarshalling (<code>false</code>)
     * @return the corresponding <code>XmlMappingException</code>
     * @see XmlBeansUtils#convertXmlBeansException(Exception, boolean)
     */
    public XmlMappingException convertXmlBeansException(Exception ex, boolean marshalling) {
        return XmlBeansUtils.convertXmlBeansException(ex, marshalling);
    }

    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        Document document = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
        Node xmlBeansNode = ((XmlObject) graph).newDomNode(xmlOptions);
        NodeList xmlBeansChildNodes = xmlBeansNode.getChildNodes();
        for (int i = 0; i < xmlBeansChildNodes.getLength(); i++) {
            Node xmlBeansChildNode = xmlBeansChildNodes.item(i);
            Node importedNode = document.importNode(xmlBeansChildNode, true);
            node.appendChild(importedNode);
        }
    }

    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        ((XmlObject) graph).save(outputStream, xmlOptions);
    }

    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        try {
            ((XmlObject) graph).save(contentHandler, lexicalHandler, xmlOptions);
        }
        catch (SAXException ex) {
            throw convertXmlBeansException(ex, true);
        }
    }

    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        ((XmlObject) graph).save(writer, xmlOptions);
    }

    protected void marshalXmlEventWriter(Object graph, XMLEventWriter eventWriter) {
        ContentHandler contentHandler = new StaxEventContentHandler(eventWriter);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected void marshalXmlStreamWriter(Object graph, XMLStreamWriter streamWriter) throws XmlMappingException {
        ContentHandler contentHandler = new StaxStreamContentHandler(streamWriter);
        marshalSaxHandlers(graph, contentHandler, null);
    }

    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        try {
            XmlObject object = XmlObject.Factory.parse(node, xmlOptions);
            validate(object);
            return object;
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        try {
            XmlObject object = XmlObject.Factory.parse(inputStream, xmlOptions);
            validate(object);
            return object;
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            XmlObject object = XmlObject.Factory.parse(reader, xmlOptions);
            validate(object);
            return object;
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {
        XmlSaxHandler saxHandler = XmlObject.Factory.newXmlSaxHandler(xmlOptions);
        xmlReader.setContentHandler(saxHandler.getContentHandler());
        try {
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", saxHandler.getLexicalHandler());
        }
        catch (SAXNotRecognizedException e) {
        }
        catch (SAXNotSupportedException e) {
        }
        try {
            xmlReader.parse(inputSource);
            XmlObject object = saxHandler.getObject();
            validate(object);
            return object;
        }
        catch (SAXException ex) {
            throw convertXmlBeansException(ex, false);
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    protected Object unmarshalXmlEventReader(XMLEventReader eventReader) throws XmlMappingException {
        XMLReader reader = new StaxEventXmlReader(eventReader);
        try {
            return unmarshalSaxReader(reader, new InputSource());
        }
        catch (IOException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    protected Object unmarshalXmlStreamReader(XMLStreamReader streamReader) throws XmlMappingException {
        try {
            XmlObject object = XmlObject.Factory.parse(streamReader, xmlOptions);
            validate(object);
            return object;
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    /**
     * Validates the given <code>XmlObject</code>.
     *
     * @param object the xml object to validate
     * @throws XmlBeansValidationFailureException
     *          if the given object is not valid
     */
    public void validate(XmlObject object) throws XmlBeansValidationFailureException {
        if (isValidating() && object != null) {
            // create a temporary xmlOptions just for validation
            XmlOptions validateOptions = xmlOptions != null ? xmlOptions : new XmlOptions();
            List errorsList = new ArrayList();
            validateOptions.setErrorListener(errorsList);
            if (!object.validate(validateOptions)) {
                StringBuffer buffer = new StringBuffer("Could not validate XmlObject :");
                for (Iterator iterator = errorsList.iterator(); iterator.hasNext();) {
                    XmlError xmlError = (XmlError) iterator.next();
                    if (xmlError instanceof XmlValidationError) {
                        buffer.append(xmlError.toString());
                    }
                }
                XmlException ex = new XmlException(buffer.toString(), null, errorsList);
                throw new XmlBeansValidationFailureException(ex);
            }
        }
    }
}
