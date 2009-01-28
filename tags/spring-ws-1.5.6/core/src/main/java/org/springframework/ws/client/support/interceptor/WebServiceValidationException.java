package org.springframework.ws.client.support.interceptor;

import org.xml.sax.SAXParseException;

import org.springframework.ws.client.WebServiceClientException;

/**
 * Exception thrown whenever a validation error occurs on the client-side.
 *
 * @author Stefan Schmidt
 * @author Arjen Poutsma
 * @since 1.5.4
 */
public class WebServiceValidationException extends WebServiceClientException {

    private SAXParseException[] validationErrors;

    /**
     * Create a new instance of the <code>WebServiceValidationException</code> class.
     *
     * @param msg the detail message
     */
    public WebServiceValidationException(SAXParseException[] validationErrors) {
        super(createMessage(validationErrors));
        this.validationErrors = validationErrors;
    }

    private static String createMessage(SAXParseException[] validationErrors) {
        StringBuffer buffer = new StringBuffer("XML validation error on response: ");

        for (int i = 0; i < validationErrors.length; i++) {
            buffer.append(validationErrors[i].getMessage());
        }
        return buffer.toString();
    }

    /** Returns the validation errors. */
    public SAXParseException[] getValidationErrors() {
        return validationErrors;
    }
}
