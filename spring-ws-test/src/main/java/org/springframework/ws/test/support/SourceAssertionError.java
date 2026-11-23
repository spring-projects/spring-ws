/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerHelper;

/**
 * Subclass of {@link AssertionError} that also contains a {@link Source} for more
 * context.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0.1
 */
@SuppressWarnings("serial")
public class SourceAssertionError extends AssertionError {

	private final @Nullable String sourceLabel;

	private final @Nullable Source source;

	private final TransformerHelper transformerHelper = new TransformerHelper();

	/**
	 * Creates a new instance of the {@code SourceAssertionError} class with the given
	 * parameters.
	 */
	public SourceAssertionError(String detailMessage, @Nullable String sourceLabel, @Nullable Source source) {
		super(detailMessage);
		this.sourceLabel = sourceLabel;
		this.source = source;
	}

	/**
	 * Returns the source context of this error.
	 * @return the source
	 */
	public @Nullable Source getSource() {
		return this.source;
	}

	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.getMessage());
		String sourceString = getSourceString();
		if (sourceString != null) {
			String newLine = System.lineSeparator();
			builder.append(newLine);
			String label = (this.sourceLabel != null) ? this.sourceLabel : "Source";
			builder.append(label);
			builder.append(": ");
			builder.append(sourceString);
		}
		return builder.toString();
	}

	private @Nullable String getSourceString() {
		if (this.source != null) {
			try {
				StringResult result = new StringResult();
				Transformer transformer = createNonIndentingTransformer();
				transformer.transform(this.source, result);
				return result.toString();
			}
			catch (TransformerException ex) {
				// Ignore
			}
		}
		return null;
	}

	private Transformer createNonIndentingTransformer() throws TransformerConfigurationException {
		Transformer transformer = this.transformerHelper.createTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		return transformer;
	}

}
