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

import java.awt.Toolkit;
import javax.xml.bind.JAXBElement;

import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * Simple client that demonstartes MTOM by invoking <code>StoreImage</code> and <code>LoadImage</code> using a
 * WebServiceTemplate and SAAJ.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 */
public class SaajMtomClient extends WebServiceGatewaySupport {

    private ObjectFactory objectFactory = new ObjectFactory();

    private StopWatch stopWatch = new StopWatch(ClassUtils.getShortName(getClass()));

    public SaajMtomClient(SaajSoapMessageFactory messageFactory) {
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

    private void store(String path) {
        Image image = objectFactory.createImage();
        image.setName(StringUtils.getFilename(path));
        image.setImage(Toolkit.getDefaultToolkit().getImage(path));
        JAXBElement<Image> storeImageRequest = objectFactory.createStoreImageRequest(image);
        stopWatch.start("store");
        getWebServiceTemplate().marshalSendAndReceive(storeImageRequest);
        stopWatch.stop();
    }

    private void load(String path) {
        JAXBElement<String> loadImageRequest = objectFactory.createLoadImageRequest(StringUtils.getFilename(path));

        stopWatch.start("load");
        JAXBElement<Image> loadImageResponse =
                (JAXBElement<Image>) getWebServiceTemplate().marshalSendAndReceive(loadImageRequest);
        stopWatch.stop();
        Image image = loadImageResponse.getValue();
        logger.info("Received image " + image.getName() + " [" + image.getImage().getWidth(null) + "x" +
                image.getImage().getHeight(null) + "]");

    }

}
