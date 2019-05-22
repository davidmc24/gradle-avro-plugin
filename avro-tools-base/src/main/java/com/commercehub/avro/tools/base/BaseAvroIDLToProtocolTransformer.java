package com.commercehub.avro.tools.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.commercehub.avro.tools.base.Constants.PROTOCOL_EXTENSION;

public abstract class BaseAvroIDLToProtocolTransformer implements AvroIDLToProtocolTransformer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int transform(Collection<File> inputs, File outputDir, ClassLoader resourceLoader) throws IOException {
        int processedFileCount = 0;
        for (File idlFile : inputs) {
            logger.info("Processing {}", idlFile);
            File protoFile = new File(outputDir,
                FilenameUtils.getBaseName(idlFile.getName()) + "." + PROTOCOL_EXTENSION);
            transformIDLToProtocol(idlFile, protoFile, resourceLoader);
            processedFileCount++;
        }
        return processedFileCount;
    }

    protected abstract void transformIDLToProtocol(File inputFile, File outputFile, ClassLoader resourceLoader) throws IOException;

    protected void handleException(File inputFile, Exception ex) throws IOException {
        throw new IOException(String.format("Failed to compile IDL file %s", inputFile), ex);
    }

    protected void writeProtocolFile(File outputFile, String protoJson) throws IOException {
        FileUtils.writeJsonFile(outputFile, protoJson);
        logger.debug("Wrote {}", outputFile.getPath());
    }
}
