package com.commercehub.avro.tools.base;

import com.commercehub.avro.tools.api.CompilerOptions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface AvroSchemaToJavaSourceTransformer {
    int transform(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException;
}
