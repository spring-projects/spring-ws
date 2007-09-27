package org.springframework.ws.server.endpoint.adapter;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

public class MarshallingMethodEndpointAdapterTest extends TestCase {

    private MarshallingMethodEndpointAdapter adapter;

    private boolean noResponseInvoked;

    private MockControl marshallerControl;

    private Marshaller marshallerMock;

    private MockControl unmarshallerControl;

    private Unmarshaller unmarshallerMock;

    private MessageContext messageContext;

    private boolean responseInvoked;

    protected void setUp() throws Exception {
        adapter = new MarshallingMethodEndpointAdapter();
        marshallerControl = MockControl.createControl(Marshaller.class);
        marshallerMock = (Marshaller) marshallerControl.getMock();
        adapter.setMarshaller(marshallerMock);
        unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        adapter.setUnmarshaller(unmarshallerMock);
        adapter.afterPropertiesSet();
        MockWebServiceMessage request = new MockWebServiceMessage("<request/>");
        messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
    }

    public void testNoResponse() throws Exception {
        Method noResponse = getClass().getMethod("noResponse", new Class[]{MyType.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        unmarshallerMock.unmarshal(messageContext.getRequest().getPayloadSource());
        unmarshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        unmarshallerControl.setReturnValue(new MyType());
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method invoked", noResponseInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", noResponseInvoked);
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testNoRequestPayload() throws Exception {
        MockWebServiceMessage request = new MockWebServiceMessage();
        messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        Method noResponse = getClass().getMethod("noResponse", new Class[]{MyType.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method invoked", noResponseInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", noResponseInvoked);
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testResponse() throws Exception {
        Method response = getClass().getMethod("response", new Class[]{MyType.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
        unmarshallerMock.unmarshal(messageContext.getRequest().getPayloadSource());
        unmarshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        unmarshallerControl.setReturnValue(new MyType());
        marshallerMock.marshal(new MyType(), messageContext.getResponse().getPayloadResult());
        marshallerControl.setMatcher(MockControl.ALWAYS_MATCHER);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method invoked", responseInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", responseInvoked);
        marshallerControl.verify();
        unmarshallerControl.verify();

    }

    public void testSupportedNoResponse() throws NoSuchMethodException {
        Method noResponse = getClass().getMethod("noResponse", new Class[]{MyType.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        unmarshallerControl.expectAndReturn(unmarshallerMock.supports(MyType.class), true);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testSupportedResponse() throws NoSuchMethodException {
        Method response = getClass().getMethod("response", new Class[]{MyType.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
        unmarshallerControl.expectAndReturn(unmarshallerMock.supports(MyType.class), true);
        marshallerControl.expectAndReturn(marshallerMock.supports(MyType.class), true);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedMultipleParams", new Class[]{String.class, String.class});
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[]{String.class});
        unmarshallerControl.expectAndReturn(unmarshallerMock.supports(String.class), false);
        marshallerControl.expectAndReturn(marshallerMock.supports(String.class), true);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[]{String.class});
        marshallerControl.expectAndReturn(marshallerMock.supports(String.class), false);
        marshallerControl.replay();
        unmarshallerControl.replay();
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void noResponse(MyType type) {
        noResponseInvoked = true;

    }

    public MyType response(MyType type) {
        responseInvoked = true;
        return new MyType();
    }

    public void unsupportedMultipleParams(String s1, String s2) {
    }

    public String unsupportedWrongParam(String s) {
        return s;
    }

    public static class MyType {

    }
}