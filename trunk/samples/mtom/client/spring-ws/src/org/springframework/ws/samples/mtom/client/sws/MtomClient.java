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
import java.io.File;
import javax.xml.bind.JAXBElement;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;

/**
 * Simple client that demonstartes MTOM by invoking <code>StoreImage</code> and <code>LoadImage</code> using a
 * WebServiceTemplate.
 *
 * @author Tareq Abed Rabbo
 */
public class MtomClient extends WebServiceGatewaySupport {

    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext("applicationContext.xml", MtomClient.class);
            MtomClient client = (MtomClient) applicationContext.getBean("mtomClient", MtomClient.class);

            ObjectFactory objectFactory = new ObjectFactory();
            Image image = objectFactory.createImage();
            File file = new File(args[0]);
            image.setName(file.getName());
            image.setImage(Toolkit.getDefaultToolkit().getImage(args[0]));
            JAXBElement<Image> storeImageRequest = objectFactory.createStoreImageRequest(image);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("store");
            client.getWebServiceTemplate().marshalSendAndReceive(storeImageRequest);
            stopWatch.stop();
            JAXBElement<String> loadImageRequest = objectFactory.createLoadImageRequest(file.getName());

            stopWatch.start("load");
            Object result = client.getWebServiceTemplate().marshalSendAndReceive(loadImageRequest);
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        }
        catch (SoapFaultClientException ex) {
            System.err.format("SOAP Fault Code    %1s%n", ex.getFaultCode());
            System.err.format("SOAP Fault String: %1s%n", ex.getFaultStringOrReason());
        }
    }
}
