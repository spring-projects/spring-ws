/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.ws.samples.mtom.client.sws;

import java.io.IOException;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Simple client that demonstartes MTOM by invoking <code>StoreImage</code> and <code>LoadImage</code> using a
 * WebServiceTemplate and Axiom.
 *
 * @author Arjen Poutsma
 */
public class AxiomMtomClient extends WebServiceGatewaySupport {

    private StopWatch stopWatch = new StopWatch(ClassUtils.getShortName(getClass()));

    public AxiomMtomClient(AxiomSoapMessageFactory messageFactory) {
        super(messageFactory);
    }

    public void doIt(String path) {
        try {
            store(path);
            load(path);
            System.out.println(stopWatch.prettyPrint());
        }
        catch (SoapFaultClientException ex) {
            System.err.format("SOAP Fault Code    %1s%n", ex.getFaultCode());
            System.err.format("SOAP Fault String: %1s%n", ex.getFaultStringOrReason());
        }

    }

    private void store(final String path) {
        stopWatch.start("store");
        getWebServiceTemplate().sendAndReceive(new WebServiceMessageCallback() {
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                AxiomSoapMessage soapMessage = (AxiomSoapMessage) message;
                SOAPMessage axiomMessage = soapMessage.getAxiomMessage();
                SOAPFactory factory = (SOAPFactory) axiomMessage.getOMFactory();
                SOAPBody body = axiomMessage.getSOAPEnvelope().getBody();

                OMNamespace ns =
                        factory.createOMNamespace("http://www.springframework.org/spring-ws/samples/mtom", "tns");

                OMElement storeImageRequestElement = factory.createOMElement("StoreImageRequest", ns);
                body.addChild(storeImageRequestElement);
                OMElement nameElement = factory.createOMElement("name", ns);
                storeImageRequestElement.addChild(nameElement);
                nameElement.setText(StringUtils.getFilename(path));
                OMElement imageElement = factory.createOMElement("image", ns);
                storeImageRequestElement.addChild(imageElement);
                DataSource dataSource = new FileDataSource(path);
                DataHandler dataHandler = new DataHandler(dataSource);
                OMText text = factory.createOMText(dataHandler, true);
                imageElement.addChild(text);

                OMOutputFormat outputFormat = new OMOutputFormat();
                outputFormat.setSOAP11(true);
                outputFormat.setDoOptimize(true);
                soapMessage.setOutputFormat(outputFormat);
            }
        }, new WebServiceMessageExtractor() {
            public Object extractData(WebServiceMessage message) throws IOException, TransformerException {
                return null;
            }
        });
        stopWatch.stop();
    }

    private void load(final String path) {
        final StringBuilder name = new StringBuilder();
        stopWatch.start("load");
        java.awt.Image image = (java.awt.Image) getWebServiceTemplate().sendAndReceive(new WebServiceMessageCallback() {
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                SOAPMessage axiomMessage = ((AxiomSoapMessage) message).getAxiomMessage();
                SOAPFactory factory = (SOAPFactory) axiomMessage.getOMFactory();
                SOAPBody body = axiomMessage.getSOAPEnvelope().getBody();

                OMNamespace ns =
                        factory.createOMNamespace("http://www.springframework.org/spring-ws/samples/mtom", "tns");

                OMElement loadImageRequestElement = factory.createOMElement("LoadImageRequest", ns);
                loadImageRequestElement.setText(StringUtils.getFilename(path));
                body.addChild(loadImageRequestElement);
            }
        }, new WebServiceMessageExtractor() {
            public Object extractData(WebServiceMessage message) throws IOException, TransformerException {
                SOAPMessage axiomMessage = ((AxiomSoapMessage) message).getAxiomMessage();
                SOAPBody body = axiomMessage.getSOAPEnvelope().getBody();
                OMElement loadImageResponseElement = (OMElement) body.getChildElements().next();
                Assert.isTrue("LoadImageResponse".equals(loadImageResponseElement.getLocalName()));
                Iterator childElements = loadImageResponseElement.getChildElements();
                OMElement nameElement = (OMElement) childElements.next();
                Assert.isTrue("name".equals(nameElement.getLocalName()));
                name.append(nameElement.getText());
                OMElement imageElement = (OMElement) childElements.next();
                Assert.isTrue("image".equals(imageElement.getLocalName()));
                OMText text = (OMText) imageElement.getFirstOMChild();

                DataHandler dataHandler = (DataHandler) text.getDataHandler();
                return ImageIO.read(dataHandler.getInputStream());
            }
        });
        stopWatch.stop();
        logger.info("Received image " + name + " [" + image.getWidth(null) + "x" + image.getHeight(null) + "]");
    }


}
