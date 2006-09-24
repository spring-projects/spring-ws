package org.springframework.binding.mapping;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.util.Assert;

/**
 * A stateful builder that builds {@link Mapping} objects. Designed for
 * convenience to build mappings in a clear, readable manner.
 * <p>
 * Example usage:
 * 
 * <pre>
 * MappingBuilder mapping = new MappingBuilder();
 * 
 * Mapping result = mapping.source(&quot;foo&quot;).target(&quot;bar&quot;).from(String.class).to(Long.class).value();
 * </pre>
 * 
 * Calling the {@link #value()} result method clears out this builder's state so
 * it can be reused to build another mapping.
 * @author Keith Donald
 */
public class MappingBuilder {

	/**
	 * The source mapping expression.
	 */
	private Expression sourceExpression;

	/**
	 * The target mapping property expression.
	 */
	private PropertyExpression targetExpression;

	/**
	 * The type of the object returned by evaluating the source expression.
	 */
	private Class sourceType;

	/**
	 * The type of the property settable by the target expression.
	 */
	private Class targetType;

	/**
	 * The expression string parser.
	 */
	private ExpressionParser expressionParser;

	/**
	 * The conversion service for applying type conversions.
	 */
	private ConversionService conversionService = new DefaultConversionService();

	/**
	 * Creates a mapping builder that uses the expression parser to parse
	 * attribute mapping expressions.
	 * @param expressionParser the expression parser.
	 */
	public MappingBuilder(ExpressionParser expressionParser) {
		Assert.notNull(expressionParser, "The expression parser is required");
		this.expressionParser = expressionParser;
	}

	/**
	 * Sets the conversion service that will convert the object returned by
	 * evaluating the source expression to the {@link #to(Class)} type if
	 * necessary.
	 * @param conversionService the conversion service
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Sets the source expression of the mapping built by this builder.
	 * @param expressionString the expression string
	 * @return this, to support call-chaining
	 */
	public MappingBuilder source(String expressionString) {
		sourceExpression = expressionParser.parseExpression(expressionString);
		return this;
	}

	/**
	 * Sets the target property expression of the mapping built by this builder.
	 * @param expressionString the expression string
	 * @return this, to support call-chaining
	 */
	public MappingBuilder target(String expressionString) {
		targetExpression = (PropertyExpression)expressionParser.parseExpression(expressionString);
		return this;
	}

	/**
	 * Sets the expected type of the object returned by evaluating the source
	 * expression. Used in conjunction with {@link #to(Class)} to perform a type
	 * conversion during the mapping process.
	 * @param sourceType the source type
	 * @return this, to support call-chaining
	 */
	public MappingBuilder from(Class sourceType) {
		this.sourceType = sourceType;
		return this;
	}

	/**
	 * Sets the target type of the property writeable by the target expression.
	 * @param targetType the target type
	 * @return this, to support call-chaining
	 */
	public MappingBuilder to(Class targetType) {
		this.targetType = targetType;
		return this;
	}

	/**
	 * The logical GOF builder getResult method, returning a fully constructed
	 * Mapping from the configured pieces. Once called, the state of this
	 * builder is nulled out to support building a new mapping object again.
	 * @return the mapping result
	 */
	public Mapping value() {
		Assert.notNull(sourceExpression, "The source expression must be set at a minimum");
		if (targetExpression == null) {
			targetExpression = (PropertyExpression)sourceExpression;
		}
		ConversionExecutor typeConverter = null;
		if (sourceType != null) {
			Assert.notNull(targetType, "The target type is required when the source type is specified");
			typeConverter = conversionService.getConversionExecutor(sourceType, targetType);
		}
		Mapping result = new Mapping(sourceExpression, targetExpression, typeConverter);
		sourceExpression = null;
		targetExpression = null;
		sourceType = null;
		targetType = null;
		return result;
	}
}