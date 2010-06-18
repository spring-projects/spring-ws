/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.mock.client;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Main entry point for client-side Web Service testing. Typically used in combination with a {@link
 * org.springframework.ws.client.core.WebServiceTemplate WebServiceTemplate}.
 * <p/>
 * The typical usage of this mock is similar to any other mocking library (such as EasyMock), that is:
 * <ol>
 * <li>Inject this mock into the {@link org.springframework.ws.client.core.WebServiceTemplate WebServiceTemplate}.
 * See {@link org.springframework.ws.client.core.WebServiceTemplate#setMessageSender(WebServiceMessageSender) WebServiceTemplate.setMessageSender()}.</li>
 * <li>Set up expectations about the URI to connect to, and about the outgoing request message.
 * See {@link #whenConnecting()}, {@link #whenConnectingTo(String)}, and {@link RequestExpectations}.</li>
 * <li>Indicate the desired response actions. See {@link ResponseActions}.</li>
 * <li>Call {@link #replay()}.
 * <li>Use the {@code WebServiceTemplate} as normal.
 * <li>Call {@link #verify()}.
 * </ol>
 * Note that because of the 'fluent' API used by this class, you can typically use the Code Completion features
 * offered by your IDE to set up the mocks.
 * <p/>
 * For example:
 * <blockquote><pre>
 * // set up
 * MockWebServiceMessageSender mockMessageSender = new MockWebServiceMessageSender();
 * AirlineClient client = new AirlineClient(); // AirlineClient extends WebServiceGatewaySupport
 * <strong>client.getWebServiceTemplate().setMessageSender(mockMessageSender);</strong>
 * // expectations
 * String uri = "http://example.com/airline";
 * String expectedRequest = "&lt;getFlightsRequest xmlns=\"http://example.com/\" &gt;";
 * String response = "&lt;getFlightsResponse xmlns=\"http://example.com/\"&gt;";
 * <strong>mockMessageSender.whenConnectingTo(uri).expectPayload(request).andRespondWithPayload(response);</strong>
 * <strong>mockMessageSender.replay();</strong>
 * // execution
 * StringResult result = new StringResult();
 * <strong>template.sendSourceAndReceiveToResult(uri, new StringSource(expectedRequest), stringResult)</strong>;
 * assertXMLEqual(response, result.toString()); // from XMLUnit
 * <strong>mockMessageSender.verify();</strong>
 * </pre></blockquote>
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @see org.springframework.ws.client.core.WebServiceTemplate#setMessageSender
 * @see #whenConnecting()
 * @see #whenConnectingTo
 * @see RequestExpectations
 * @see ResponseActions
 * @see #replay()
 * @see #verify()
 * @since 2.0
 */
public class MockWebServiceMessageSender implements WebServiceMessageSender {

    private final List<MockSenderConnection> expectedConnections = new LinkedList<MockSenderConnection>();

    private Iterator<MockSenderConnection> connectionIterator;

    /** Creates a new {@code MockWebServiceMessageSender}. */
    public MockWebServiceMessageSender() {
        reset();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation checks whether the given URI has been recorded as expected, and throws an {@code AssertionError} if not.
     *
     * @throws IllegalStateException if this mock is not in {@linkplain #replay() replay} state
     * @throws AssertionError        if the given URI is not expected
     */
    public MockSenderConnection createConnection(URI uri) throws IOException {
        Assert.notNull(uri, "'uri' must not be null");
        if (connectionIterator == null) {
            throw new IllegalStateException("Please call replay() after recording expected connections");
        }
        if (!connectionIterator.hasNext()) {
            throw new AssertionError("No further connections expected");
        }
        MockSenderConnection currentConnection = connectionIterator.next();
        if (!currentConnection.getUri().equals(uri) && !currentConnection.hasAnyUri()) {
            throw new AssertionError("Unexpected connection to \"" + uri + "\"");
        }
        return currentConnection;
    }

    /** Always returns {@code true}. */
    public boolean supports(URI uri) {
        return true;
    }

    /**
     * Sets up an expected connection to the specific URI. Returns a {@link RequestExpectations} object that allows for
     * further expectations on the request.
     *
     * @param uri the URI expected to connect to
     * @return the request expectations
     */
    public RequestExpectations whenConnectingTo(String uri) {
        Assert.hasLength(uri, "'uri' must not be empty");
        return whenConnectingTo(URI.create(uri));
    }

    /**
     * Sets up an expected connection to the specific URI. Returns a {@link RequestExpectations} object that allows for
     * further expectations on the request.
     *
     * @param uri the URI expected to connect to
     * @return the request expectations
     */
    public RequestExpectations whenConnectingTo(URI uri) {
        Assert.notNull(uri, "'uri' must not be null");
        MockSenderConnection connection = new MockSenderConnection(uri);
        expectedConnections.add(connection);
        return connection;
    }

    /**
     * Sets up an expected connection to <em>any</em> URI. Returns a {@link RequestExpectations} object that allows for
     * further expectations on the request.
     *
     * @return the request expectations
     */
    public RequestExpectations whenConnecting() {
        MockSenderConnection connection = new MockSenderConnection();
        expectedConnections.add(connection);
        return connection;
    }

    /** Resets the mock to the state directly after creation. */
    public void reset() {
        connectionIterator = null;
        expectedConnections.clear();
    }

    /**
     * Switches the mock from record state to replay state.
     *
     * @throws IllegalStateException if this mock already is in replay state.
     */
    public void replay() {
        Assert.state(connectionIterator == null, "Already in replay state");
        connectionIterator = expectedConnections.iterator();
    }

    /**
     * Verifies that all expectations have been met.
     *
     * @throws IllegalStateException if this mock is in record state
     * @throws AssertionError        if any expectation have not been met
     */
    public void verify() {
        Assert.state(connectionIterator != null, "Calling verify() is only allowed after replay()");
        if (connectionIterator.hasNext()) {
            StringBuilder builder = new StringBuilder("Expected connection(s) to [");
            while (connectionIterator.hasNext()) {
                MockSenderConnection connection = connectionIterator.next();
                builder.append(connection.getUri());
                if (connectionIterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(']');
            throw new AssertionError(builder.toString());
        }
    }


}
