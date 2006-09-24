/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;

import org.springframework.util.FileCopyUtils;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A continuation implementation that is based on standard java serialization, 
 * created by a {@link SerializedFlowExecutionContinuationFactory}.
 * 
 * @see SerializedFlowExecutionContinuationFactory.
 * 
 * @author Keith Donald
 */
class SerializedFlowExecutionContinuation extends FlowExecutionContinuation {

	/**
	 * The serialized flow execution.
	 */
	private byte[] data;

	/**
	 * Whether or not this flow execution array is compressed.
	 */
	private boolean compressed;

	/**
	 * Creates a new serialized flow execution continuation.
	 * @param the flow execution in byte form
	 * @param whether or not the execution compressed.
	 */
	public SerializedFlowExecutionContinuation(byte[] data, boolean compressed) {
		this.data = data;
		this.compressed = compressed;
	}

	/**
	 * Returns whether or not this byte array is compressed.
	 */
	public boolean isCompressed() {
		return compressed;
	}
	
	public FlowExecution restore() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(getData(true)));
			try {
				return (FlowExecution)ois.readObject();
			}
			finally {
				ois.close();
			}
		}
		catch (IOException e) {
			throw new FlowExecutionContinuationDeserializationException(
					"IOException thrown deserializing the flow execution stored in this continuation -- this should not happen!",
					e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionContinuationDeserializationException(
					"ClassNotFoundException thrown deserializing the flow execution stored in this continuation -- "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length + 128);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(this);
				oos.flush();
			}
			finally {
				oos.close();
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Return the flow execution in its raw byte[] form. Will decompress if
	 * requested.
	 * @param decompress whether or not to decompress the byte[] array before
	 * returning
	 * @return the byte array
	 * @throws IOException a problem occured with decompression
	 */
	public byte[] getData(boolean decompress) throws IOException {
		if (isCompressed() && decompress) {
			return decompress(data);
		}
		else {
			return data;
		}
	}

	/**
	 * Internal helper method to decompress given data using GZIP decompression.
	 */
	private byte[] decompress(byte[] dataToDecompress) throws IOException {
		GZIPInputStream gzipin = new GZIPInputStream(new ByteArrayInputStream(dataToDecompress));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileCopyUtils.copy(gzipin, baos);
		}
		finally {
			gzipin.close();
		}
		return baos.toByteArray();
	}
}