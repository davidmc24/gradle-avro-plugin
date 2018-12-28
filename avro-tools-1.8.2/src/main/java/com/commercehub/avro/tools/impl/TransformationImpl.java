package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.api.Transformation;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.compiler.idl.Idl;
import org.apache.avro.compiler.idl.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.commercehub.avro.tools.impl.Constants.*;

public class TransformationImpl implements Transformation {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int generateJavaSourceFiles(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException {
        return new JavaSourceGenerator(outputDir, baseFile, options).generate(inputs);
    }

    @Override
    public int generateProtocolFiles(Collection<File> inputs, File outputDir, ClassLoader resourceLoader) throws IOException {
        logger.info("Found {} files", inputs.size());
        Map<Boolean, List<File>> inputsBySupported = inputs.stream().collect(Collectors.partitioningBy(hasExtension(IDL_EXTENSION)));
        List<File> unsupportedFiles = inputsBySupported.getOrDefault(Boolean.FALSE, Collections.emptyList());
        if (!unsupportedFiles.isEmpty()) {
            throw new IOException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }
        List<File> supportedFiles = inputsBySupported.getOrDefault(Boolean.TRUE, Collections.emptyList());
        int processedFileCount = 0;
        for (File idlFile : supportedFiles) {
            logger.info("Processing {}", idlFile);
            File protoFile = new File(outputDir,
                FilenameUtils.getBaseName(idlFile.getName()) + "." + PROTOCOL_EXTENSION);
            Idl idl = null;
            try {
                idl = new Idl(idlFile, resourceLoader);
                String protoJson = idl.CompilationUnit().toString(true);
                FileUtils.writeJsonFile(protoFile, protoJson);
                logger.debug("Wrote {}", protoFile.getPath());
            } catch (IOException | ParseException ex) {
                throw new IOException(String.format("Failed to compile IDL file %s", idlFile), ex);
            } finally {
                if (idl != null) {
                    try {
                        idl.close();
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }
            processedFileCount++;
        }
        return processedFileCount;
    }

    @Override
    public int generateSchemaFiles(Collection<File> inputs, File outputDir) throws IOException {
        logger.info("Found {} files", inputs.size());
        Map<Boolean, List<File>> inputsBySupported = inputs.stream().collect(Collectors.partitioningBy(hasExtension(PROTOCOL_EXTENSION)));
        List<File> unsupportedFiles = inputsBySupported.getOrDefault(Boolean.FALSE, Collections.emptyList());
        if (!unsupportedFiles.isEmpty()) {
            throw new IOException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }
        List<File> supportedFiles = inputsBySupported.getOrDefault(Boolean.TRUE, Collections.emptyList());
        int processedFileCount = 0;
        for (File sourceFile : supportedFiles) {
            logger.info("Processing {}", sourceFile);
            try {
                Protocol protocol = Protocol.parse(sourceFile);
                for (Schema schema : protocol.getTypes()) {
                    String path = schema.getNamespace().replaceAll(Pattern.quote("."), "/");
                    File schemaFile = new File(outputDir, path + "/" + schema.getName() + "." + SCHEMA_EXTENSION);
                    String schemaJson = schema.toString(true);
                    FileUtils.writeJsonFile(schemaFile, schemaJson);
                    logger.debug("Wrote {}", schemaFile.getPath());
                }
            } catch (IOException ex) {
                throw new IOException(String.format("Failed to process protocol definition file %s", sourceFile), ex);
            }
            processedFileCount++;
        }
        return processedFileCount;
    }

    private static Predicate<File> hasExtension(String... targetExtensions) {
        return (File file) -> {
            String actualExtension = FilenameUtils.getExtension(file.getName());
            for (String targetExtension : targetExtensions) {
                if (actualExtension.equalsIgnoreCase(targetExtension)) {
                    return true;
                }
            }
            return false;
        };
    }
}
