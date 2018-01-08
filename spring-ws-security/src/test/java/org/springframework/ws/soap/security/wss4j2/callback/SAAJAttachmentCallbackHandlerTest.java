package org.springframework.ws.soap.security.wss4j2.callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.AttachmentRequestCallback;
import org.apache.wss4j.common.ext.AttachmentResultCallback;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class SAAJAttachmentCallbackHandlerTest {

    private static final String ATTACHMENTS = "Attachments";
    private static final String CONTENT_ID_TEST = "123456";
    private static final String CONTENT_TYPE_TEST = "application/xml";
    private static final String SAMPLE_XML_CONTENT = "<Invoice><CustomizationID></CustomizationID><PostalAddress/></Invoice>";

    @Test
    public void requestCallbackNoDeleteTest () {
        SaajSoapMessageFactory soapMessageFactory = createSAAJMessageFactory();
        SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage();
        soapMessage.setSoapAction(null);
        soapMessage.addAttachment(CONTENT_ID_TEST, new ByteArrayResource(SAMPLE_XML_CONTENT.getBytes()), CONTENT_TYPE_TEST);

        SAAJAttachmentCallbackHandler attachmentCallbackHandler = new SAAJAttachmentCallbackHandler(soapMessage);
        AttachmentRequestCallback attachmentRequestCallback = new AttachmentRequestCallback();

        attachmentRequestCallback.setRemoveAttachments(false);
        attachmentRequestCallback.setAttachmentId(ATTACHMENTS);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = attachmentRequestCallback;

        try {
            attachmentCallbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            fail(e.getMessage());
        }
        assertEquals("Callback must have an attachment element", 1, attachmentRequestCallback.getAttachments()
                .size());
        assertEquals("Callback attachment must have the same content-ID", CONTENT_ID_TEST, attachmentRequestCallback.getAttachments()
                .get(0)
                .getId());

        assertTrue("SoapMessage must conserve its attachment", soapMessage.getAttachments()
                .hasNext());

    }

    @Test
    public void requestCallbackDeleteTest () {
        SaajSoapMessageFactory soapMessageFactory = createSAAJMessageFactory();
        SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage();
        soapMessage.setSoapAction(null);
        soapMessage.addAttachment(CONTENT_ID_TEST, new ByteArrayResource(SAMPLE_XML_CONTENT.getBytes()), CONTENT_TYPE_TEST);

        SAAJAttachmentCallbackHandler attachmentCallbackHandler = new SAAJAttachmentCallbackHandler(soapMessage);
        AttachmentRequestCallback attachmentRequestCallback = new AttachmentRequestCallback();

        attachmentRequestCallback.setRemoveAttachments(true);
        attachmentRequestCallback.setAttachmentId(CONTENT_ID_TEST);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = attachmentRequestCallback;

        try {
            attachmentCallbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            fail(e.getMessage());
        }
        assertEquals("Callback must have an attachment element", 1, attachmentRequestCallback.getAttachments()
                .size());
        assertEquals("Callback attachment must have the same content-ID", CONTENT_ID_TEST, attachmentRequestCallback.getAttachments()
                .get(0)
                .getId());

        assertTrue("SoapMessage must not conserve its attachments", !soapMessage.getAttachments()
                .hasNext());
    }

    @Test
    public void responseCallbackTest () {
        SaajSoapMessageFactory soapMessageFactory = createSAAJMessageFactory();
        SoapMessage soapMessage = soapMessageFactory.createWebServiceMessage();
        soapMessage.setSoapAction(null);

        SAAJAttachmentCallbackHandler attachmentCallbackHandler = new SAAJAttachmentCallbackHandler(soapMessage);
        AttachmentResultCallback attachmentResultCallback = new AttachmentResultCallback();

        Attachment attachment = new Attachment();
        attachment.setId(CONTENT_ID_TEST);
        attachment.setMimeType(CONTENT_TYPE_TEST);
        attachment.setSourceStream(new ByteArrayInputStream(SAMPLE_XML_CONTENT.getBytes()));

        attachmentResultCallback.setAttachment(attachment);
        attachmentResultCallback.setAttachmentId(CONTENT_ID_TEST);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = attachmentResultCallback;

        assertTrue("SoapMessage must not have attachments at start", !soapMessage.getAttachments()
                .hasNext());

        try {
            attachmentCallbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            fail(e.getMessage());
        }

        assertTrue("SoapMessage must have attachments after result handle", soapMessage.getAttachments()
                .hasNext());

        org.springframework.ws.mime.Attachment resultAttachment = soapMessage.getAttachments()
                .next();

        assertEquals("SoapMessage attachment must have the same content-ID", CONTENT_ID_TEST, resultAttachment.getContentId());
        assertEquals("SoapMessage attachment must have the same content-type", CONTENT_TYPE_TEST, resultAttachment.getContentType());

    }

    private SaajSoapMessageFactory createSAAJMessageFactory () {
        SaajSoapMessageFactory soapMessageFactory = new SaajSoapMessageFactory();
        soapMessageFactory.setSoapVersion(org.springframework.ws.soap.SoapVersion.SOAP_12);
        soapMessageFactory.afterPropertiesSet();
        return soapMessageFactory;
    }

}
