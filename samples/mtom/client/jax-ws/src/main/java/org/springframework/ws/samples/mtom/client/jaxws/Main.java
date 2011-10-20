/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.samples.mtom.client.jaxws;

import java.io.File;
import java.io.IOException;
import java.awt.image.RenderedImage;
import java.awt.Toolkit;
import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.springframework.util.StopWatch;

/**
 * Simple client that calls the <code>GetFlights</code> and <code>BookFlight</code> operations using JAX-WS.
 *
 * @author Arjen Poutsma
 */
public class Main {

    public static void main(String[] args) throws IOException {
        try {

            String fileName = "../spring-ws-logo.png";
            if (args.length > 0) {
                fileName = args[0];
            }

            ImageRepositoryService service = new ImageRepositoryService();
            ImageRepository imageRepository = service.getImageRepositorySoap11();
            SOAPBinding binding = (SOAPBinding) ((BindingProvider) imageRepository).getBinding();
            binding.setMTOMEnabled(true);

            Image request = new Image();
            File file = new File(fileName);
            if (!file.exists()) {
                System.err.println("File [" + fileName + "] does not exist");
                System.exit(-1);
            }
            request.setName(file.getName());
            request.setImage(Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath()));
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("store");
            imageRepository.storeImage(request);
            stopWatch.stop();

            stopWatch.start("load");
            Image response = imageRepository.loadImage(file.getName());
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        }
        catch (SOAPFaultException ex) {
            System.err.format("SOAP Fault Code    %1s%n", ex.getFault().getFaultCodeAsQName());
            System.err.format("SOAP Fault String: %1s%n", ex.getFault().getFaultString());
        }

    }


}
