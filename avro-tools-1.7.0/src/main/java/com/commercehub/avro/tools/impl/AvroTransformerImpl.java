package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.base.*;

public class AvroTransformerImpl extends BaseAvroTransformer {
    @Override
    protected BaseAvroProtocolToJavaSourceTransformer provideProtocolToJavaSourceTransformer() {
        return new AvroProtocolToJavaSourceTransformerImpl();
    }

    @Override
    protected BaseAvroSchemaToJavaSourceTransformer provideSchemaToJavaSourceTransformer() {
        return new AvroSchemaToJavaSourceTransformerImpl();
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
