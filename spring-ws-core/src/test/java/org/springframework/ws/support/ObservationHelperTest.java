package org.springframework.ws.support;

import org.dom4j.Namespace;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlunit.builder.Input;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationHelperTest {

    private ObservationHelper helper;

    @BeforeEach
    void setUp() {
        helper = new ObservationHelper();
    }

    @Test
    void getRootElementStreamSource() {

        StringSource source = new StringSource("<root xmlns='http://springframework.org/spring-ws'><child/></root>");

        QName name = helper.getRootElement(source);
        assertThat(name.getLocalPart()).isEqualTo("root");
        assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
    }

    @Test
    void getRootElementDomSource() {

        DOMDocument document = new DOMDocument(
                new DOMElement(
                        new org.dom4j.QName("root",
                                new Namespace(null, "http://springframework.org/spring-ws"))));
        document.getRootElement().addElement("child");

        QName name = helper.getRootElement(Input.from(document).build());
        assertThat(name.getLocalPart()).isEqualTo("root");
        assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
    }

    @Test
    void getRootElementSaxSource() throws Exception {
        StringReader reader = new StringReader("<root xmlns='http://springframework.org/spring-ws'><child/></root>");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        SAXSource saxSource = new SAXSource(xmlReader, new InputSource(reader));
        QName name = helper.getRootElement(saxSource);
        assertThat(name.getLocalPart()).isEqualTo("root");
        assertThat(name.getNamespaceURI()).isEqualTo("http://springframework.org/spring-ws");
    }
}