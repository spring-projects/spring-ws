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

package org.springframework.ws.wsdl.wsdl11.visitor;

import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;

/**
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface PortTypeVisitor {

    void startPortType(PortType portType) throws WSDLException;

    void startOperation(Operation operation) throws WSDLException;

    void input(Input input) throws WSDLException;

    void output(Output output) throws WSDLException;

    void fault(Fault fault) throws WSDLException;

    void endOperation(Operation operation) throws WSDLException;

    void endPortType(PortType portType) throws WSDLException;

}
