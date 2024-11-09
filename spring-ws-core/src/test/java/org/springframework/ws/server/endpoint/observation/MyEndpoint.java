package org.springframework.ws.server.endpoint.observation;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class MyEndpoint {

    private static final String NAMESPACE_URI = "http://springframework.org/spring-ws";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "request")
    @ResponsePayload
    public MyResponse handleRequest(@RequestPayload MyRequest request) {
        MyResponse myResponse = new MyResponse();
        myResponse.setMessage("Hello " + request.getName());
        return myResponse;
    }



    @XmlRootElement(namespace = NAMESPACE_URI, name = "request")
    static class MyRequest {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @XmlRootElement(namespace = NAMESPACE_URI, name = "response")
    static class MyResponse {

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}


