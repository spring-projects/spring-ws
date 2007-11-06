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

package org.springframework.ws.transport.mail.support;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Service;
import javax.mail.Store;
import javax.mail.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** @author Arjen Poutsma */
public abstract class MailUtils {

    private static final Log logger = LogFactory.getLog(MailUtils.class);

    /**
     * Close the given JavaMail Service and ignore any thrown exception. This is useful for typical <code>finally</code>
     * blocks in manual JavaMail code.
     *
     * @param service the JavaMail Service to close (may be <code>null</code>)
     * @see Transport
     * @see Store
     */
    public static void closeService(Service service) {
        if (service != null) {
            try {
                service.close();
            }
            catch (MessagingException ex) {
                logger.debug("Could not close JavaMail Transport", ex);
            }
        }
    }

    /**
     * Close the given JavaMail Folder and ignore any thrown exception. This is useful for typical <code>finally</code>
     * blocks in manual JavaMail code.
     *
     * @param folder the JavaMail Folder to close (may be <code>null</code>)
     */

    public static void closeFolder(Folder folder) {
        closeFolder(folder, false);
    }

    /**
     * Close the given JavaMail Folder and ignore any thrown exception. This is useful for typical <code>finally</code>
     * blocks in manual JavaMail code.
     *
     * @param folder  the JavaMail Folder to close (may be <code>null</code>)
     * @param expunge whether all deleted messages should be expunged from the folder
     */
    public static void closeFolder(Folder folder, boolean expunge) {
        if (folder != null) {
            try {
                folder.close(expunge);
            }
            catch (MessagingException ex) {
                logger.debug("Could not close JavaMail Transport", ex);
            }
        }
    }


}
