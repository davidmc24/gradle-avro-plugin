package com.commercehub.avro.tools.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface Transformation {
    int generateJavaSourceFiles(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException; // TODO: other options
    int generateProtocolFiles(Collection<File> inputs, File outputDir, ClassLoader resourceLoader) throws IOException; // TODO: other options
    int generateSchemaFiles(Collection<File> inputs, File outputDir) throws IOException; // TODO: other options
}
