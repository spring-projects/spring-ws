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

package org.springframework.ws.transport.jms;

import org.springframework.ws.transport.TransportConstants;

/**
 * Declares JMS-specific transport constants.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public interface JmsTransportConstants extends TransportConstants {

    /** The "jms" URI scheme" */
    String JMS_URI_SCHEME = "jms";

    String PARAM_DELIVERY_MODE = "deliveryMode";

    String PARAM_CONNECTION_FACTORY_NAME = "connectionFactoryName";

    String PARAM_INITIAL_CONTEXT_FACTORY = "initialContextFactory";

    String PARAM_JNDI_URL = "jndiURL";

    String PARAM_TIME_TO_LIVE = "timeToLive";

    String PARAM_PRIORITY = "priority";

    String PARAM_DESTINATION_TYPE = "destinationType";

    String PARAM_REPLY_TO_NAME = "replyToName";

    String DESTINATION_TYPE_QUEUE = "queue";

    String DESTINATION_TYPE_TOPIC = "topic";

    String PROPERTY_PREFIX = "SOAPJMS_";

    String PROPERTY_IS_FAULT = PROPERTY_PREFIX + "isFault";

    String PROPERTY_SOAP_ACTION = PROPERTY_PREFIX + "soapAction";

    String PROPERTY_CONTENT_LENGTH = PROPERTY_PREFIX + "contentLength";

    String PROPERTY_CONTENT_TYPE = PROPERTY_PREFIX + "contentType";

    String PROPERTY_BINDING_VERSION = PROPERTY_PREFIX + "bindingVersion";

    String PROPERTY_TARGET_SERVICE = PROPERTY_PREFIX + "targetService";

    String PROPERTY_REQUEST_IRI = PROPERTY_PREFIX + "requestIRI";

    String PROPERTY_SOAP_MEP = PROPERTY_PREFIX + "soapMEP";
}
