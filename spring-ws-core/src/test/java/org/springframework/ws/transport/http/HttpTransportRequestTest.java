package org.springframework.ws.transport.http;

import java.util.Iterator;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

public class HttpTransportRequestTest extends TestCase {

    private HttpTransportRequest transportRequest;

    private MockHttpServletRequest mockRequest;

    protected void setUp() throws Exception {
        mockRequest = new MockHttpServletRequest();
        transportRequest = new HttpTransportRequest(mockRequest);
    }

    public void testGetHttpServletRequest() throws Exception {
        assertEquals("Invalid request", mockRequest, transportRequest.getHttpServletRequest());
    }

    public void testGetHeaderNames() throws Exception {
        mockRequest.addHeader("header1", "value11");
        mockRequest.addHeader("header1", "value12");
        mockRequest.addHeader("header2", "value2");
        Iterator headers = transportRequest.getHeaderNames();
        assertTrue("Invalid amount of header names", headers.hasNext());
        String header = (String) headers.next();
        assertTrue("Invalid header", "header1".equals(header) || "header2".equals(header));
        assertTrue("Invalid amount of header names", headers.hasNext());
        header = (String) headers.next();
        assertTrue("Invalid header", "header1".equals(header) || "header2".equals(header));
        assertFalse("Invalid amount of header names", headers.hasNext());
    }

    public void testGetHeaders() throws Exception {
        mockRequest.addHeader("header", "value1");
        mockRequest.addHeader("header", "value2");
        Iterator values = transportRequest.getHeaders("header");
        assertTrue("Invalid amount of header names", values.hasNext());
        String value = (String) values.next();
        assertTrue("Invalid value", "value1".equals(value) || "value2".equals(value));
        assertTrue("Invalid amount of header names", values.hasNext());
        value = (String) values.next();
        assertTrue("Invalid value", "value1".equals(value) || "value2".equals(value));
        assertFalse("Invalid amount of header names", values.hasNext());
    }

    public void testGetURL() throws Exception {
        mockRequest.setScheme("http");
        mockRequest.setServerName("www.example.com");
        mockRequest.setServerPort(8080);
        mockRequest.setRequestURI("/services/Service");
        String url = transportRequest.getUrl();
        assertNotNull("No url returned", url);
        assertEquals("Invalid url returned", "http://www.example.com:8080/services/Service", url);
    }
}