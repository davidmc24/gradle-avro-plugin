package com.commercehub.avro.tools.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface AvroTransformer {
    int transformProtocolToJavaSource(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException;
    int transformSchemaToJavaSource(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException, UnsupportedOperationException;
    int transformIDLToProtocol(Collection<File> inputs, File outputDir, ClassLoader resourceLoader) throws IOException;
    int transformProtocolToSchema(Collection<File> inputs, File outputDir) throws IOException;
}
