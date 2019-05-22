package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.base.*;

public class AvroTransformerImpl extends BaseAvroTransformer {
    @Override
    protected BaseAvroProtocolToJavaSourceTransformer provideProtocolToJavaSourceTransformer() {
        return new AvroProtocolToJavaSourceTransformerImpl();
    }

    @Override
    protected BaseAvroSchemaToJavaSourceTransformer provideSchemaToJavaSourceTransformer() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("SchemaToJavaSource transformation is not supported in this version of Avro");
    }

    @Override
    protected BaseAvroIDLToProtocolTransformer provideIDLToProtocolTransformer() {
        return new AvroIDLToProtocolTransformerImpl();
    }

    @Override
    protected BaseAvroProtocolToSchemaTransformer provideProtocolToSchemaTransformer() {
        return new AvroProtocolToSchemaTransformerImpl();
    }
}
