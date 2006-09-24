package org.springframework.oxm.xmlbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.springframework.oxm.AbstractMarshaller;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of the <code>Marshaller</code> interface for XMLBeans.
 * <p/>
 * <strong>Note</strong> that due to the nature of XMLBeans, this marshaller requires all passed objects to be of type
 * <code>XmlObject</code>. A
 *
 * @author Arjen Poutsma
 * @see org.apache.xmlbeans.XmlObject
 */
public class XmlBeansMarshaller extends AbstractMarshaller implements Marshaller {

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
     * Abstract template method for marshalling the given object graph to a DOM <code>Node</code>, specifying the child
     * node where the result nodes should be inserted before.
     * <p/>
     * In practice, node and nextSibling should be a <code>Document</code> node, a <code>DocumentFragment</code> node,
     * or a <code>Element</code> node. In other words, a node that accepts children.
     *
     * @param node        The DOM node that will contain the result tree
     * @param nextSibling The child node where the result nodes should be inserted before. Can be <code>null</code>.
     * @throws org.springframework.oxm.XmlMappingException
     *                            if the given object cannot be marshalled to the DOM node
     * @throws ClassCastException if <code>graph</code> does not implement <code>XmlObject</code>
     * @see org.w3c.dom.Document
     * @see org.w3c.dom.DocumentFragment
     * @see org.w3c.dom.Element
     * @see XmlObject
     */
    protected void marshalDomNode(Object graph, Node node, Node nextSibling) throws XmlMappingException {
        Document document = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
        Node xmlBeansNode = ((XmlObject) graph).newDomNode(xmlOptions);
        NodeList xmlBeansChildNodes = xmlBeansNode.getChildNodes();
        for (int i = 0; i < xmlBeansChildNodes.getLength(); i++) {
            Node xmlBeansChildNode = xmlBeansChildNodes.item(i);
            Node importedNode = document.importNode(xmlBeansChildNode, true);
            if (nextSibling == null) {
                node.appendChild(importedNode);
            }
            else {
                node.insertBefore(importedNode, nextSibling);
            }
        }
    }

    /**
     * Abstract template method for marshalling the given object graph to a <code>OutputStream</code>.
     *
     * @param graph        the root of the object graph to marshal
     * @param outputStream the <code>OutputStream</code> to write to
     * @throws org.springframework.oxm.XmlMappingException
     *                             if the given object cannot be marshalled to the writer
     * @throws java.io.IOException if an I/O exception occurs
     * @throws ClassCastException  if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        ((XmlObject) graph).save(outputStream, xmlOptions);
    }

    /**
     * Abstract template method for marshalling the given object graph to a SAX <code>ContentHandler</code>.
     *
     * @param graph          the root of the object graph to marshal
     * @param contentHandler the SAX <code>ContentHandler</code>
     * @param lexicalHandler the SAX2 <code>LexicalHandler</code>. Can be <code>null</code>.
     * @throws org.springframework.oxm.XmlMappingException
     *                            if the given object cannot be marshalled to the handlers
     * @throws ClassCastException if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        try {
            ((XmlObject) graph).save(contentHandler, lexicalHandler, xmlOptions);
        }
        catch (SAXException ex) {
            throw convertXmlBeansException(ex, true);
        }

    }

    /**
     * Abstract template method for marshalling the given object graph to a <code>Writer</code>.
     *
     * @param graph  the root of the object graph to marshal
     * @param writer the <code>Writer</code> to write to
     * @throws org.springframework.oxm.XmlMappingException
     *                             if the given object cannot be marshalled to the writer
     * @throws java.io.IOException if an I/O exception occurs
     * @throws ClassCastException  if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        ((XmlObject) graph).save(writer, xmlOptions);
    }

    /**
     * Abstract template method for unmarshalling from a given DOM <code>Node</code>.
     *
     * @param node The DOM node that contains the objects to be unmarshalled
     * @throws org.springframework.oxm.XmlMappingException
     *                            if the given DOM node cannot be mapped to an object
     * @throws ClassCastException if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        try {
            return XmlObject.Factory.parse(node, xmlOptions);
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    /**
     * Abstract template method for unmarshalling from a given <code>InputStream</code>.
     *
     * @param inputStream the <code>InputStreamStream</code> to read from
     * @throws org.springframework.oxm.XmlMappingException
     *                             if the given object cannot be mapped to an object
     * @throws java.io.IOException if an I/O exception occurs
     * @throws ClassCastException  if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        try {
            return XmlObject.Factory.parse(inputStream, xmlOptions);
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
    }

    /**
     * Abstract template method for unmarshalling from a given <code>Reader</code>.
     *
     * @param reader the <code>Reader</code> to read from
     * @throws org.springframework.oxm.XmlMappingException
     *                             if the given object cannot be mapped to an object
     * @throws java.io.IOException if an I/O exception occurs
     * @throws ClassCastException  if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlObject
     */
    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        try {
            return XmlObject.Factory.parse(reader, xmlOptions);
        }
        catch (XmlException ex) {
            throw convertXmlBeansException(ex, false);
        }
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
     * @throws ClassCastException if <code>graph</code> does not implement <code>XmlObject</code>
     * @see XmlBeansUtils#convertXmlBeansException(Exception, boolean)
     * @see XmlObject
     */
    public XmlMappingException convertXmlBeansException(Exception ex, boolean marshalling) {
        return XmlBeansUtils.convertXmlBeansException(ex, marshalling);
    }
}
