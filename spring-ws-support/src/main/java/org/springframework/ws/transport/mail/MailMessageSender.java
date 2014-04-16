/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.transport.mail;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * {@link WebServiceMessageSender} implementation that uses Mail {@link MimeMessage}s. Requires a {@link
 * #setTransportUri(String) transport} and {@link #setStoreUri(String) store} URI to be set.
 *
 * <p>Calling {@link WebServiceConnection#receive(WebServiceMessageFactory)} on connections created by this message sender
 * will result in a blocking call, for the amount of milliseconds specified by the {@link #setReceiveSleepTime(long)
 * receiveSleepTime} property. This will give the server time to formulate a response message. By default, this propery
 * is set to 1 minute. For a proper request-response conversation to work, this property value must not be smaller the
 * {@link PollingMonitoringStrategy#setPollingInterval(long) pollingInterval} property of the server-side message
 * receiver polling strategy.
 *
 * <p>This message sender supports URI's of the following format: <blockquote> <tt><b>mailto:</b></tt><i>to</i>[<tt><b>?</b></tt><i>param-name</i><tt><b>=</b></tt><i>param-value</i>][<tt><b>&amp;</b></tt><i>param-name</i><tt><b>=</b></tt><i>param-value</i>]*
 * </blockquote> where the characters <tt><b>:</b></tt>, <tt><b>?</b></tt>, and <tt><b>&amp;</b></tt> stand for
 * themselves. The <i>to</i> represents a RFC 822 mailbox. Valid <i>param-name</i> include:
 *
 * <blockquote><table> <tr><th><i>param-name</i></th><th><i>Description</i></th></tr>
 * <tr><td><tt>subject</tt></td><td>The subject of the request message.</td></tr> </table></blockquote>
 *
 * <p>Some examples of email URIs are:
 *
 * <blockquote><tt>mailto:john@example.com</tt><br> <tt>mailto:john@example.com@?subject=SOAP%20Test</tt><br></blockquote>
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.ietf.org/rfc/rfc2368.txt">The mailto URL scheme</a>
 * @since 1.5.0
 */
public class MailMessageSender implements WebServiceMessageSender, InitializingBean {

    /**
     * Default timeout for receive operations. Set to 1000 * 60 milliseconds (i.e. 1 minute).
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60;

    private long receiveSleepTime = DEFAULT_RECEIVE_TIMEOUT;

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private URLName transportUri;

    private InternetAddress from;

    /**
     * Sets the from address to use when sending request messages.
     */
    public void setFrom(String from) throws AddressException {
        this.from = new InternetAddress(from);
    }

    /**
     * Set JavaMail properties for the {@link Session}.
     *
     * <p>A new {@link Session} will be created with those properties. Use either this method or {@link #setSession}, but
     * not both.
     *
     * <p>Non-default properties in this instance will override given JavaMail properties.
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        session = Session.getInstance(javaMailProperties, null);
    }

    /**
     * Set the sleep time to use for receive calls, <strong>in milliseconds</strong>. The default is 1000 * 60 ms, that
     * is 1 minute.
     */
    public void setReceiveSleepTime(long receiveSleepTime) {
        this.receiveSleepTime = receiveSleepTime;
    }

    /**
     * Set the JavaMail {@code Session}, possibly pulled from JNDI.
     *
     * <p>Default is a new {@code Session} without defaults, that is completely configured via this instance's
     * properties.
     *
     * <p>If using a pre-configured {@code Session}, non-default properties in this instance will override the
     * settings in the {@code Session}.
     *
     * @see #setJavaMailProperties
     */
    public void setSession(Session session) {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    /**
     * Sets the JavaMail Store URI to be used for retrieving response messages. Typically takes the form of
     * {@code [imap|pop3]://user:password@host:port/INBOX}. Setting this property is required.
     *
     * <p>For example, {@code imap://john:secret@imap.example.com/INBOX}
     *
     * @see Session#getStore(URLName)
     */
    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    /**
     * Sets the JavaMail Transport URI to be used for sending response messages. Typically takes the form of
     * {@code smtp://user:password@host:port}. Setting this property is required.
     *
     * <p>For example, {@code smtp://john:secret@smtp.example.com}
     *
     * @see Session#getTransport(URLName)
     */
    public void setTransportUri(String transportUri) {
        this.transportUri = new URLName(transportUri);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(transportUri, "'transportUri' is required");
        Assert.notNull(storeUri, "'storeUri' is required");
    }

    @Override
    public WebServiceConnection createConnection(URI uri) throws IOException {
        InternetAddress to = MailTransportUtils.getTo(uri);
        MailSenderConnection connection =
                new MailSenderConnection(session, transportUri, storeUri, to, receiveSleepTime);
        if (from != null) {
            connection.setFrom(from);
        }
        String subject = MailTransportUtils.getSubject(uri);
        if (subject != null) {
            connection.setSubject(subject);
        }
        return connection;
    }

    @Override
    public boolean supports(URI uri) {
        return uri.getScheme().equals(MailTransportConstants.MAIL_URI_SCHEME);
    }
}
