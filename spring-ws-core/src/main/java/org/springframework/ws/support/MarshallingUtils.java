/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.activation.DataHandler;
import javax.xml.transform.Source;

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

	private MarshallingUtils() {}

	/**
	 * Unmarshals the payload of the given message using the provided {@link Unmarshaller}.
	 * <p>
	 * If the request message has no payload (i.e. {@link WebServiceMessage#getPayloadSource()} returns {@code null}),
	 * this method will return {@code null}.
	 *
	 * @param unmarshaller the unmarshaller
	 * @param message the message of which the payload is to be unmarshalled
	 * @return the unmarshalled object
	 * @throws IOException in case of I/O errors
	 */
	public static Object unmarshal(Unmarshaller unmarshaller, WebServiceMessage message) throws IOException {
		Source payload = message.getPayloadSource();
		if (payload == null) {
			return null;
		} else if (unmarshaller instanceof MimeUnmarshaller && message instanceof MimeMessage) {
			MimeUnmarshaller mimeUnmarshaller = (MimeUnmarshaller) unmarshaller;
			MimeMessageContainer container = new MimeMessageContainer((MimeMessage) message);
			return mimeUnmarshaller.unmarshal(payload, container);
		} else {
			return unmarshaller.unmarshal(payload);
		}
	}

	/**
	 * Marshals the given object to the payload of the given message using the provided {@link Marshaller}.
	 *
	 * @param marshaller the marshaller
	 * @param graph the root of the object graph to marshal
	 * @param message the message of which the payload is to be unmarshalled
	 * @throws IOException in case of I/O errors
	 */
	public static void marshal(Marshaller marshaller, Object graph, WebServiceMessage message) throws IOException {
		if (marshaller instanceof MimeMarshaller && message instanceof MimeMessage) {
			MimeMarshaller mimeMarshaller = (MimeMarshaller) marshaller;
			MimeMessageContainer container = new MimeMessageContainer((MimeMessage) message);
			mimeMarshaller.marshal(graph, message.getPayloadResult(), container);
		} else {
			marshaller.marshal(graph, message.getPayloadResult());
		}
	}

	private static class MimeMessageContainer implements MimeContainer {

		private final MimeMessage mimeMessage;

		public MimeMessageContainer(MimeMessage mimeMessage) {
			this.mimeMessage = mimeMessage;
		}

		@Override
		public boolean isXopPackage() {
			return mimeMessage.isXopPackage();
		}

		@Override
		public boolean convertToXopPackage() {
			return mimeMessage.convertToXopPackage();
		}

		@Override
		public void addAttachment(String contentId, DataHandler dataHandler) {
			mimeMessage.addAttachment(contentId, dataHandler);
		}

		@Override
		public DataHandler getAttachment(String contentId) {
			Attachment attachment = mimeMessage.getAttachment(contentId);
			return attachment != null ? attachment.getDataHandler() : null;
		}
	}

}
