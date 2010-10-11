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

package org.springframework.ws.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StopWatch;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.stroap.StroapMessageFactory;
import org.springframework.ws.stream.StreamingPayload;
import org.springframework.ws.stream.StreamingWebServiceMessage;

public class PerformanceTest {

    private static final int ITERATIONS = 1000;

    private static final int ELEMENTS = 500;

    private SoapMessageFactory messageFactory;

    private Marshaller marshaller;

    private StopWatch stopWatch;

    private MyRootElement jaxbElement;

    private OutputStream os;

    private boolean streaming = true;

    private static final QName NAME = new QName("http://springframework.org", "root");

    private Transformer transformer;

    public PerformanceTest(SoapMessageFactory messageFactory, StopWatch stopWatch) throws Exception {
        if (messageFactory instanceof InitializingBean) {
            ((InitializingBean) messageFactory).afterPropertiesSet();
        }
        this.messageFactory = messageFactory;
        JAXBContext jaxbContext = JAXBContext.newInstance(MyRootElement.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        jaxbElement = new MyRootElement();
        for (int i = 0; i < ELEMENTS; i++) {
            jaxbElement.getStrings().add(String.valueOf(i));
        }

        os = new NullOutputSteam();

        this.stopWatch = stopWatch;

        this.transformer = TransformerFactory.newInstance().newTransformer();
    }

    public void test() throws Exception {
        stopWatch.start(messageFactory.toString());
        for (int i = 0; i < ITERATIONS; i++) {
            SoapMessage message = (SoapMessage) messageFactory.createWebServiceMessage();

            marshal(message);

            transformer.transform(message.getPayloadSource(), new StreamResult(os));

            message.writeTo(os);


        }
        stopWatch.stop();
    }

    private void marshal(SoapMessage message) throws JAXBException {
        if (streaming && message instanceof StreamingWebServiceMessage) {
            StreamingWebServiceMessage streamingMessage = (StreamingWebServiceMessage) message;
            StreamingPayload payload = new JaxbStreamingPayload(jaxbElement, NAME, marshaller);

            streamingMessage.setStreamingPayload(payload);
        }
        else {
            marshaller.marshal(jaxbElement, message.getPayloadResult());
        }
    }

    public static void main(String[] args) throws Exception {
        StopWatch stopWatch = new StopWatch();

        try {
            saaj(stopWatch);
            axiomCaching(stopWatch);
            axiomNoCaching(stopWatch);
            stroapCaching(stopWatch);
            stroapNoCaching(stopWatch);

        }
        finally {
            System.out.println(stopWatch.prettyPrint());
        }
    }

    private static void saaj(StopWatch stopWatch) throws Exception {
        SaajSoapMessageFactory ssmf = new SaajSoapMessageFactory();
        PerformanceTest performanceTest = new PerformanceTest(ssmf, stopWatch);
        performanceTest.test();
    }

    private static void axiomCaching(StopWatch stopWatch) throws Exception {
        axiom(stopWatch, true);
    }

    private static void axiomNoCaching(StopWatch stopWatch) throws Exception {
        axiom(stopWatch, false);
    }

    private static void stroapCaching(StopWatch stopWatch) throws Exception {
        stroap(stopWatch, true);
    }

    private static void stroapNoCaching(StopWatch stopWatch) throws Exception {
        stroap(stopWatch, false);
    }

    private static void axiom(StopWatch stopWatch, boolean caching) throws Exception {
        AxiomSoapMessageFactory axmf = new AxiomSoapMessageFactory();
        axmf.setPayloadCaching(caching);
        PerformanceTest performanceTest = new PerformanceTest(axmf, stopWatch);
        performanceTest.test();
    }

    private static void stroap(StopWatch stopWatch, boolean caching) throws Exception {
        StroapMessageFactory smf = new StroapMessageFactory();
        smf.setPayloadCaching(caching);
        PerformanceTest performanceTest = new PerformanceTest(smf, stopWatch);
        performanceTest.test();
    }

    @XmlRootElement(name = "root", namespace = "http://springframework.org")
    public static class MyRootElement {

        private List<String> strings;

        @XmlElement(name = "string", namespace = "http://springframework.org")
        public List<String> getStrings() {
            if (strings == null) {
                strings = new ArrayList<String>();
            }
            return strings;
        }

    }

    private static class NullOutputSteam extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static class JaxbStreamingPayload implements StreamingPayload {

        private final Object jaxbElement;

        private final QName name;

        private final Marshaller marshaller;

        private JaxbStreamingPayload(Object jaxbElement, QName name, Marshaller marshaller) {
            this.jaxbElement = jaxbElement;
            this.name = name;
            this.marshaller = marshaller;
        }

        public QName getName() {
            return name;
        }

        public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
            try {
                marshaller.marshal(jaxbElement, streamWriter);
            }
            catch (JAXBException ex) {
                throw new XMLStreamException(ex);
            }
        }
    }


}
