package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.base.BaseAvroIDLToProtocolTransformer;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.commercehub.avro.tools.base.Constants.UTF8_ENCODING;

class AvroIDLToProtocolTransformerImpl extends BaseAvroIDLToProtocolTransformer {
    @Override
    protected void transformIDLToProtocol(File inputFile, File outputFile, ClassLoader resourceLoader) throws IOException {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            try {
                Idl idl = new Idl(inputStream, UTF8_ENCODING);
                String protoJson = idl.CompilationUnit().toString(true);
                writeProtocolFile(outputFile, protoJson);
            } catch (IOException | ParseException ex) {
                handleException(inputFile, ex);
            }
        }
    }
}
