package org.springframework.ws.soap.security.xwss.callback;

import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import junit.framework.TestCase;

public class DefaultTimestampValidatorTest extends TestCase {

    private DefaultTimestampValidator validator;

    protected void setUp() throws Exception {
        validator = new DefaultTimestampValidator();
    }

    public void testValidate() throws Exception {
        TimestampValidationCallback.Request request = new TimestampValidationCallback.UTCTimestampRequest(
                "2006-09-25T20:42:50Z", "2107-09-25T20:42:50Z", 100, Long.MAX_VALUE);
        validator.validate(request);
    }

    public void testValidateNoExpired() throws Exception {
        TimestampValidationCallback.Request request =
                new TimestampValidationCallback.UTCTimestampRequest("2006-09-25T20:42:50Z", null, 100, Long.MAX_VALUE);
        validator.validate(request);
    }
}