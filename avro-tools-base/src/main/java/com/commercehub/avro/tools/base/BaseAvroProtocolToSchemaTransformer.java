package com.commercehub.avro.tools.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.commercehub.avro.tools.base.Constants.SCHEMA_EXTENSION;

public abstract class BaseAvroProtocolToSchemaTransformer implements AvroProtocolToSchemaTransformer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int transform(Collection<File> inputs, File outputDir) throws IOException {
        int processedFileCount = 0;
        for (File sourceFile : inputs) {
            logger.info("Processing {}", sourceFile);
            transformProtocolToSchema(sourceFile, outputDir);
            processedFileCount++;
        }
        return processedFileCount;
    }

    protected abstract void transformProtocolToSchema(File inputFile, File outputDir) throws IOException;

    protected File determineSchemaFile(File outputDir, String schemaNamespace, String schemaName) {
        String path = schemaNamespace.replaceAll(Pattern.quote("."), "/");
        return new File(outputDir, path + "/" + schemaName + "." + SCHEMA_EXTENSION);
    }

    protected void writeSchemaFile(File schemaFile, String schemaJson) throws IOException {
        FileUtils.writeJsonFile(schemaFile, schemaJson);
        logger.debug("Wrote {}", schemaFile.getPath());
    }

    protected void handleException(File inputFile, Exception ex) throws IOException {
        throw new IOException(String.format("Failed to process protocol definition file %s", inputFile), ex);
    }
}
