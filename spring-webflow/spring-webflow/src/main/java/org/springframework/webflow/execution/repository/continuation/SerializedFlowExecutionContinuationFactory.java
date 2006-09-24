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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A factory that creates new instances of flow execution continuations based on
 * standard java serialization.
 * 
 * @author Keith Donald
 */
public class SerializedFlowExecutionContinuationFactory implements FlowExecutionContinuationFactory {

	/**
	 * Flag to toggle continuation compression; compression is on by default.
	 */
	private boolean compress = true;

	/**
	 * Returns whether or not the continuations should be compressed.
	 */
	public boolean getCompress() {
		return compress;
	}

	/**
	 * Set whether or not the continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecutionContinuation createContinuation(FlowExecution flowExecution) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(384);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(flowExecution);
				oos.flush();
				byte[] data = baos.toByteArray();
				if (compress) {
					data = compress(data);
				}
				return new SerializedFlowExecutionContinuation(data, compress);
			}
			finally {
				oos.close();
			}
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionSerializationException(flowExecution,
					"Could not serialize flow execution; make sure all objects stored in flow scope are serializable",
					e);
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(flowExecution,
					"IOException thrown serializing flow execution -- this should not happen!", e);
		}
	}
	
	/**
	 * Internal helper method to compress given data using GZIP compression.
	 */
	private byte[] compress(byte[] dataToCompress) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipos = new GZIPOutputStream(baos);
		try {
			gzipos.write(dataToCompress);
			gzipos.flush();
		}
		finally {
			gzipos.close();
		}
		return baos.toByteArray();
	}
}