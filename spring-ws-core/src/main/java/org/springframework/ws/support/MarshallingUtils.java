/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.support;

import java.io.IOException;

import javax.xml.transform.Source;

import jakarta.activation.DataHandler;
import org.jspecify.annotations.Nullable;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.oxm.mime.MimeMarshaller;
import org.springframework.oxm.mime.MimeUnmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.MimeMessage;

/**
 * Helper class for endpoints and endpoint mappings that use marshalling.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class MarshallingUtils {

	private MarshallingUtils() {
	}

	/**
	 * Unmarshals the payload of the given message using the provided
	 * {@link Unmarshaller}.
	 * <p>
	 * If the request message has no payload (i.e.
	 * {@link WebServiceMessage#getPayloadSource()} returns {@code null}), this method
	 * will return {@code null}.
	 * @param unmarshaller the unmarshaller
	 * @param message the message of which the payload is to be unmarshalled
	 * @return the unmarshalled object
	 * @throws IOException in case of I/O errors
	 */
	public static @Nullable Object unmarshal(Unmarshaller unmarshaller, WebServiceMessage message) throws IOException {
		Source payload = message.getPayloadSource();
		if (payload == null) {
			return null;
		}
		else if (unmarshaller instanceof MimeUnmarshaller mimeUnmarshaller && message instanceof MimeMessage) {
			MimeMessageContainer container = new MimeMessageContainer((MimeMessage) message);
			return mimeUnmarshaller.unmarshal(payload, container);
		}
		else {
			return unmarshaller.unmarshal(payload);
		}
	}

	/**
	 * Marshals the given object to the payload of the given message using the provided
	 * {@link Marshaller}.
	 * @param marshaller the marshaller
	 * @param graph the root of the object graph to marshal
	 * @param message the message of which the payload is to be unmarshalled
	 * @throws IOException in case of I/O errors
	 */
	public static void marshal(Marshaller marshaller, Object graph, WebServiceMessage message) throws IOException {
		if (marshaller instanceof MimeMarshaller mimeMarshaller && message instanceof MimeMessage) {
			MimeMessageContainer container = new MimeMessageContainer((MimeMessage) message);
			mimeMarshaller.marshal(graph, message.getPayloadResult(), container);
		}
		else {
			marshaller.marshal(graph, message.getPayloadResult());
		}
	}

	private static final class MimeMessageContainer implements MimeContainer {

		private final MimeMessage mimeMessage;

		MimeMessageContainer(MimeMessage mimeMessage) {
			this.mimeMessage = mimeMessage;
		}

		@Override
		public boolean isXopPackage() {
			return this.mimeMessage.isXopPackage();
		}

		@Override
		public boolean convertToXopPackage() {
			return this.mimeMessage.convertToXopPackage();
		}

		@Override
		public void addAttachment(String contentId, DataHandler dataHandler) {
			this.mimeMessage.addAttachment(contentId, dataHandler);
		}

		@Override
		public @Nullable DataHandler getAttachment(String contentId) {
			Attachment attachment = this.mimeMessage.getAttachment(contentId);
			return (attachment != null) ? attachment.getDataHandler() : null;
		}

	}

}
