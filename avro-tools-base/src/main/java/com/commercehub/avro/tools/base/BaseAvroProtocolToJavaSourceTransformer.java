package com.commercehub.avro.tools.base;

import com.commercehub.avro.tools.api.CompilerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public abstract class BaseAvroProtocolToJavaSourceTransformer implements AvroProtocolToJavaSourceTransformer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int transform(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException {
        int processedFileCount = 0;
        for (File sourceFile : inputs) {
            logger.info("Processing {}", sourceFile);
            try {
                transformProtocolToJavaSource(sourceFile, outputDir, options);
            } catch (IOException ex) {
                throw new IOException(String.format("Failed to compile protocol definition file %s", sourceFile), ex);
            }
            processedFileCount++;
        }
        return processedFileCount;
    }

    protected abstract void transformProtocolToJavaSource(File inputFile, File outputDir, CompilerOptions options) throws IOException;
}
