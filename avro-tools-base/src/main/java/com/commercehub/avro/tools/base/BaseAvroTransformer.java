package com.commercehub.avro.tools.base;

import com.commercehub.avro.tools.api.AvroTransformer;
import com.commercehub.avro.tools.api.CompilerOptions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public abstract class BaseAvroTransformer implements AvroTransformer {
    @Override
    public int transformProtocolToJavaSource(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException, UnsupportedOperationException {
        return provideProtocolToJavaSourceTransformer().transform(inputs, outputDir, baseFile, options);
    }

    @Override
    public int transformSchemaToJavaSource(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException, UnsupportedOperationException {
        return provideSchemaToJavaSourceTransformer().transform(inputs, outputDir, baseFile, options);
    }

    @Override
    public int transformIDLToProtocol(Collection<File> inputs, File outputDir, ClassLoader resourceLoader) throws IOException, UnsupportedOperationException {
        return provideIDLToProtocolTransformer().transform(inputs, outputDir, resourceLoader);
    }

    @Override
    public int transformProtocolToSchema(Collection<File> inputs, File outputDir) throws IOException, UnsupportedOperationException {
        return provideProtocolToSchemaTransformer().transform(inputs, outputDir);
    }

    protected abstract AvroProtocolToJavaSourceTransformer provideProtocolToJavaSourceTransformer() throws UnsupportedOperationException;
    protected abstract AvroSchemaToJavaSourceTransformer provideSchemaToJavaSourceTransformer() throws UnsupportedOperationException;
    protected abstract AvroIDLToProtocolTransformer provideIDLToProtocolTransformer() throws UnsupportedOperationException;
    protected abstract AvroProtocolToSchemaTransformer provideProtocolToSchemaTransformer() throws UnsupportedOperationException;
}
