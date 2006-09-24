package org.springframework.webflow.samples.itemlist;

import org.springframework.binding.mapping.DefaultAttributeMapper;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.webflow.support.DefaultExpressionParserFactory;

public class DataMapper extends DefaultAttributeMapper {
	public DataMapper() {
		MappingBuilder mapping = new MappingBuilder(new DefaultExpressionParserFactory().getExpressionParser());
		addMapping(mapping.source("requestParameters.data").target("flowScope.item").value());
	}
}