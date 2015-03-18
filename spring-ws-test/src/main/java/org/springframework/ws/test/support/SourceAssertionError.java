/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.test.support;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Subclass of {@link AssertionError} that also contains a {@link Source} for more context.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0.1 
 */
@SuppressWarnings("serial")
public class SourceAssertionError extends AssertionError {

	private final String sourceLabel;

	private final Source source;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	/**
	 * Creates a new instance of the {@code SourceAssertionError} class with the given parameters.
	 */
	public SourceAssertionError(String detailMessage, String sourceLabel, Source source) {
		super(detailMessage);
		this.sourceLabel = sourceLabel;
		this.source = source;
	}

	/**
	 * Returns the source context of this error.
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.getMessage());
		String sourceString = getSourceString();
		if (sourceString != null) {
			String newLine = System.getProperty("line.separator");
			builder.append(newLine);
			String label = sourceLabel != null ? sourceLabel : "Source";
			builder.append(label);
			builder.append(": ");
			builder.append(sourceString);
		}
		return builder.toString();
	}

	private String getSourceString() {
		if (source != null) {
			try {
				StringResult result = new StringResult();
				Transformer transformer = createNonIndentingTransformer();
				transformer.transform(source, result);
				return result.toString();
			}
			catch (TransformerException ex) {
				// Ignore
			}
		}
		return null;
	}

	private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
		Transformer transformer = transformerHelper.createTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		return transformer;
	}


}
