package org.springframework.ws.mime;

import java.io.File;
import java.util.Iterator;
import javax.activation.DataHandler;

import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.WebServiceMessage;

/**
 * Represents a Web service message with MIME attachments. Attachments can be added as a file, an {@link
 * InputStreamSource}, or a {@link DataHandler}.
 *
 * @author Arjen Poutsma
 * @see Attachment
 * @since 1.0.0
 */
public interface MimeMessage extends WebServiceMessage {

    /**
     * Indicates whether this message is a XOP package.
     *
     * @return <code>true</code> when the constraints specified in <a href="http://www.w3.org/TR/2005/REC-xop10-20050125/#identifying_xop_documents">Identifying
     *         XOP Documents</a> are met.
     * @see <a href="http://www.w3.org/TR/2005/REC-xop10-20050125/#xop_packages">XOP Packages</a>
     */
    boolean isXopPackage();

    /**
     * Turns this message into a XOP package.
     *
     * @return <code>true</code> when the message is a XOP package
     * @see <a href="http://www.w3.org/TR/2005/REC-xop10-20050125/#xop_packages">XOP Packages</a>
     */
    boolean convertToXopPackage();

    /**
     * Returns the {@link Attachment} with the specified content Id.
     *
     * @return the attachment with the specified content id; or <code>null</code> if it cannot be found
     * @throws AttachmentException in case of errors
     */
    Attachment getAttachment(String contentId) throws AttachmentException;

    /**
     * Returns an <code>Iterator</code> over all {@link Attachment} objects that are part of this message.
     *
     * @return an iterator over all attachments
     * @throws AttachmentException in case of errors
     * @see Attachment
     */
    Iterator getAttachments() throws AttachmentException;

    /**
     * Add an attachment to the message, taking the content from a {@link File}.
     * <p/>
     * The content type will be determined by the name of the given content file. Do not use this for temporary files
     * with arbitrary filenames (possibly ending in ".tmp" or the like)!
     *
     * @param contentId the content Id of the attachment
     * @param file      the file  to take the content from
     * @return the added attachment
     * @throws AttachmentException in case of errors
     */
    Attachment addAttachment(String contentId, File file) throws AttachmentException;

    /**
     * Add an attachment to the message, taking the content from an {@link InputStreamSource}.
     * <p/>
     * Note that the stream returned by the source needs to be a <em>fresh one on each call</em>, as underlying
     * implementations can invoke {@link InputStreamSource#getInputStream()} multiple times.
     *
     * @param contentId         the content Id of the attachment
     * @param inputStreamSource the resource to take the content from (all of Spring's Resource implementations can be
     *                          passed in here)
     * @param contentType       the content type to use for the element
     * @return the added attachment
     * @throws AttachmentException in case of errors
     * @see org.springframework.core.io.Resource
     */
    Attachment addAttachment(String contentId, InputStreamSource inputStreamSource, String contentType);

    /**
     * Add an attachment to the message, taking the content from a {@link DataHandler}.
     *
     * @param dataHandler the data handler to take the content from
     * @return the added attachment
     * @throws AttachmentException in case of errors
     */
    Attachment addAttachment(String contentId, DataHandler dataHandler);
}
