package com.commercehub.avro.depresolver;

public interface WrapperFactory<T> {
    SchemaParserWrapper<T> createSchemaParser();
    SchemaWrapper<T> createSchemaWrapper(T schema);
}
