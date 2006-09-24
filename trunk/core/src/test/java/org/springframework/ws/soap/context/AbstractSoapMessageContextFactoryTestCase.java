package org.springframework.ws.soap.context;

import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.mock.MockTransportRequest;

public abstract class AbstractSoapMessageContextFactoryTestCase extends TestCase {

    protected MessageContextFactory contextFactory;

    protected final void setUp() throws Exception {
        contextFactory = createSoapMessageContextFactory();
        if (contextFactory instanceof InitializingBean) {
            ((InitializingBean) contextFactory).afterPropertiesSet();
        }
    }

    protected MockTransportContext createTransportContext(Properties headers, String requestFile) throws IOException {
        byte[] contents = FileCopyUtils
                .copyToByteArray(AbstractSoapMessageContextFactoryTestCase.class.getResourceAsStream(requestFile));
        return new MockTransportContext(new MockTransportRequest(headers, contents));
    }

    protected abstract MessageContextFactory createSoapMessageContextFactory() throws Exception;

}
