package com.commercehub.avro.depresolver.avro153;

import com.commercehub.avro.depresolver.SchemaParserWrapper;
import com.commercehub.avro.depresolver.SchemaWrapper;
import com.commercehub.avro.depresolver.WrapperFactory;
import org.apache.avro.Schema;

public class WrapperFactoryImpl implements WrapperFactory<Schema> {
    @Override
    public SchemaParserWrapper<Schema> createSchemaParser() {
        return new SchemaParserWrapperImpl(this);
    }

    @Override
    public SchemaWrapper<Schema> createSchemaWrapper(Schema schema) {
        return new SchemaWrapperImpl(schema);
    }
}
