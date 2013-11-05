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

package org.springframework.ws.test.client.integration;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.test.integration.CustomerCountRequest;
import org.springframework.ws.test.integration.CustomerCountResponse;


public class CustomerClient extends WebServiceGatewaySupport {

    public int getCustomerCount() {
        CustomerCountRequest request = new CustomerCountRequest();
        request.setCustomerName("John Doe");

        CustomerCountResponse response = (CustomerCountResponse) getWebServiceTemplate().marshalSendAndReceive(request);
        return response.getCustomerCount();
    }

}
