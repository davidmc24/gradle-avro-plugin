package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.base.BaseAvroProtocolToSchemaTransformer;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;

import java.io.File;
import java.io.IOException;

class AvroProtocolToSchemaTransformerImpl extends BaseAvroProtocolToSchemaTransformer {
    @Override
    protected void transformProtocolToSchema(File inputFile, File outputDir) throws IOException {
        try {
            Protocol protocol = Protocol.parse(inputFile);
            for (Schema schema : protocol.getTypes()) {
                String schemaJson = schema.toString(true);
                File schemaFile = determineSchemaFile(outputDir, schema.getNamespace(), schema.getName());
                writeSchemaFile(schemaFile, schemaJson);
            }
        } catch (IOException ex) {
            handleException(inputFile, ex);
        }
    }
}
