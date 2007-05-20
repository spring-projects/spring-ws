/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.server;

import org.springframework.ws.server.EndpointMapping;

/**
 * SOAP-specific sub-interface of the <code>EndpointMapping</code>.  Adds associated actors (SOAP 1.1) or roles (SOAP
 * 1.2). Used by the <code>SoapMessageDispatcher</code> to determine the MustUnderstand headers for particular
 * endpoint.
 * <p/>
 * The main purpose for this interface is to add consitency between all SOAP-specific <code>EndpointMappings</code>. The
 * <code>SoapMessageDispatcher</code> does not require all endpoint mappings to implement this interface.
 *
 * @author Arjen Poutsma
 */
public interface SoapEndpointMapping extends EndpointMapping {

    /** Sets a single SOAP actor/actorOrRole to apply to all endpoints mapped by the delegate endpoint mapping. */
    void setActorOrRole(String actorOrRole);

    /** Sets the array of SOAP actors/actorsOrRoles to apply to all endpoints mapped by the delegate endpoint mapping. */
    void setActorsOrRoles(String[] actorsOrRoles);

}
