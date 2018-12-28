package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.commercehub.avro.tools.impl.Constants.*;
import static com.commercehub.avro.tools.impl.MapUtils.asymmetricDifference;

public class JavaSourceGenerator {
    private static Pattern ERROR_UNKNOWN_TYPE = Pattern.compile("(?i).*(undefined name|not a defined name).*");
    private static Pattern ERROR_DUPLICATE_TYPE = Pattern.compile("Can't redefine: (.*)");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File outputDir;
    private final File baseFile;
    private final GenericData.StringType parsedStringType;
    private final SpecificCompiler.FieldVisibility parsedFieldVisibility;
    private final CompilerOptions options;

    JavaSourceGenerator(File outputDir, File baseFile, CompilerOptions options) {
        this.outputDir = outputDir;
        this.baseFile = baseFile;
        this.options = options;
        parsedStringType = Enums.parseCaseInsensitive(OPTION_STRING_TYPE, GenericData.StringType.values(), options.getStringType());
        parsedFieldVisibility =
            Enums.parseCaseInsensitive(OPTION_FIELD_VISIBILITY, SpecificCompiler.FieldVisibility.values(), options.getFieldVisibility());
    }

    int generate(Collection<File> inputs) throws IOException {
        logger.debug("Using outputCharacterEncoding {}", options.getOutputCharacterEncoding());
        logger.debug("Using stringType {}", parsedStringType.name());
        logger.debug("Using fieldVisibility {}", parsedFieldVisibility.name());
        logger.debug("Using templateDirectory '{}'", options.getTemplateDirectory());
        logger.debug("Using createSetters {}", options.isCreateSetters());
        logger.debug("Using enableDecimalLogicalType {}", options.isEnableDecimalLogicalType());
        logger.debug("Using validateDefaults {}", options.isValidateDefaults());
        logger.info("Found {} files", inputs.size());
        Collection<String> supportedExtensions = Arrays.asList(PROTOCOL_EXTENSION, SCHEMA_EXTENSION);
        Map<String, List<File>> inputsByExtension = inputs.stream().collect(Collectors.groupingBy((File file) -> {
            String extension = FilenameUtils.getExtension(file.getName());
            if (supportedExtensions.contains(extension)) {
                return extension;
            }
            return null;
        }));

        List<File> unsupportedFiles = inputsByExtension.getOrDefault(null, Collections.emptyList());
        if (!unsupportedFiles.isEmpty()) {
            throw new IOException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }

        int processedFileCount = 0;

        List<File> protocolFiles = inputsByExtension.getOrDefault(PROTOCOL_EXTENSION, Collections.emptyList());
        for (File sourceFile : protocolFiles) {
            logger.info("Processing {}", sourceFile);
            try {
                compile(Protocol.parse(sourceFile), sourceFile, outputDir);
            } catch (IOException ex) {
                throw new IOException(String.format("Failed to compile protocol definition file %s", sourceFile), ex);
            }
            processedFileCount++;
        }

        Set<File> schemaFiles = new LinkedHashSet<>(inputsByExtension.getOrDefault(SCHEMA_EXTENSION, Collections.emptyList()));
        ProcessingState processingState = new ProcessingState(schemaFiles, baseFile);
        while (processingState.isWorkRemaining()) {
            processSchemaFile(processingState, processingState.nextFileState(), outputDir);
        }
        Set<FileState> failedFiles = processingState.getFailedFiles();
        if (!failedFiles.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Could not compile schema definition files:");
            for (FileState fileState : failedFiles) {
                String path = fileState.getPath();
                String fileErrorMessage = fileState.getErrorMessage();
                errorMessage.append(System.lineSeparator()).append("* ").append(path).append(": ").append(fileErrorMessage);
            }
            throw new IOException(errorMessage.toString());
        }
        processedFileCount += processingState.getProcessedTotal();

        return processedFileCount;
    }

    private void processSchemaFile(ProcessingState processingState, FileState fileState, File outputDir) throws IOException {
        String path = fileState.getPath();
        logger.debug("Processing {}, excluding types {}", path, fileState.getDuplicateTypeNames());
        File sourceFile = fileState.getFile();
        Map<String, Schema> parserTypes = processingState.determineParserTypes(fileState);
        try {
            Schema.Parser parser = new Schema.Parser();
            parser.addTypes(parserTypes);
            parser.setValidateDefaults(options.isValidateDefaults());

            compile(parser.parse(sourceFile), sourceFile, outputDir);
            Map<String, Schema> typesDefinedInFile = asymmetricDifference(parser.getTypes(), parserTypes);
            processingState.processTypeDefinitions(fileState, typesDefinedInFile);
            if (logger.isDebugEnabled()) {
                logger.debug("Processed {}; contained types {}", path, typesDefinedInFile.keySet());
            } else {
                logger.info("Processed {}", path);
            }
        } catch (SchemaParseException ex) {
            String errorMessage = ex.getMessage();
            Matcher unknownTypeMatcher = ERROR_UNKNOWN_TYPE.matcher(errorMessage);
            Matcher duplicateTypeMatcher = ERROR_DUPLICATE_TYPE.matcher(errorMessage);
            if (unknownTypeMatcher.matches()) {
                fileState.setError(ex);
                processingState.queueForDelayedProcessing(fileState);
                logger.debug("Found undefined name in {} ({}); will try again", path, errorMessage);
            } else if (duplicateTypeMatcher.matches()) {
                String typeName = duplicateTypeMatcher.group(1);
                if (fileState.containsDuplicateTypeName(typeName)) {
                    throw new IOException(
                        String.format("Failed to compile schema definition file %s; contains duplicate type definition %s", path, typeName),
                        ex);
                } else {
                    fileState.setError(ex);
                    fileState.addDuplicateTypeName(typeName);
                    processingState.queueForProcessing(fileState);
                    logger.debug("Identified duplicate type {} in {}; will re-process excluding it", typeName, path);
                }
            } else {
                throw new IOException(String.format("Failed to compile schema definition file %s", path), ex);
            }
        } catch (NullPointerException ex) {
            fileState.setError(ex);
            processingState.queueForDelayedProcessing(fileState);
            logger.debug("Encountered null reference while parsing {} (possibly due to unresolved dependency); will try again", path);
        } catch (IOException ex) {
            throw new IOException(String.format("Failed to compile schema definition file %s", path), ex);
        }
    }

    private void compile(Protocol protocol, File sourceFile, File outputDir) throws IOException {
        compile(new SpecificCompiler(protocol), sourceFile, outputDir);
    }

    private void compile(Schema schema, File sourceFile, File outputDir) throws IOException {
        compile(new SpecificCompiler(schema), sourceFile, outputDir);
    }

    private void compile(SpecificCompiler compiler, File sourceFile, File outputDir) throws IOException {
        String templateDirectory = options.getTemplateDirectory();
        compiler.setOutputCharacterEncoding(options.getOutputCharacterEncoding());
        compiler.setStringType(parsedStringType);
        compiler.setFieldVisibility(parsedFieldVisibility);
        if (templateDirectory != null) {
            compiler.setTemplateDir(templateDirectory);
        }
        compiler.setCreateSetters(options.isCreateSetters());
        compiler.setEnableDecimalLogicalType(options.isEnableDecimalLogicalType());

        compiler.compileToDestination(sourceFile, outputDir);
    }
}
