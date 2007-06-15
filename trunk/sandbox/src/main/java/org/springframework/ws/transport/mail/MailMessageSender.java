/*
 * Copyright 2007 the original author or authors.
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
import java.util.Properties;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.beans.factory.InitializingBean;

/** @author Arjen Poutsma */
public class MailMessageSender implements WebServiceMessageSender, InitializingBean {

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private URLName transportUri;

    private InternetAddress from;

    public void setFrom(String from) throws AddressException {
        this.from = new InternetAddress(from);
    }

    /**
     * Set JavaMail properties for the {@link Session}.
     * <p/>
     * A new {@link Session} will be created with those properties. Use either this method or {@link #setSession}, but
     * not both.
     * <p/>
     * Non-default properties in this instance will override given JavaMail properties.
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        session = Session.getInstance(javaMailProperties, null);
    }

    /**
     * Set the JavaMail <code>Session</code>, possibly pulled from JNDI.
     * <p/>
     * Default is a new <code>Session</code> without defaults, that is completely configured via this instance's
     * properties.
     * <p/>
     * If using a pre-configured <code>Session</code>, non-default properties in this instance will override the
     * settings in the <code>Session</code>.
     *
     * @see #setJavaMailProperties
     */
    public void setSession(Session session) {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    public void setTransportUri(String transportUri) {
        this.transportUri = new URLName(transportUri);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(from, "Property 'from' is required");
    }

    public WebServiceConnection createConnection(String uri) throws IOException {
        MailtoUri mailtoUri = new MailtoUri(uri);
        MailSenderConnection connection = new MailSenderConnection(mailtoUri, session, from);
        if (transportUri != null) {
            connection.setTransportUri(transportUri);
        }
        if (storeUri != null) {
            connection.setStoreUri(storeUri);
        }
        return connection;
    }

    public boolean supports(String uri) {
        return StringUtils.hasLength(uri) && uri.startsWith(MailtoUri.MAILTO_SCHEME);
    }
}
