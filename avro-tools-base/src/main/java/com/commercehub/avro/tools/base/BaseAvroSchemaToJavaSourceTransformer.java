package com.commercehub.avro.tools.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseAvroSchemaToJavaSourceTransformer implements AvroSchemaToJavaSourceTransformer {
    private static Pattern ERROR_UNKNOWN_TYPE = Pattern.compile("(?i).*(undefined name|not a defined name).*");
    private static Pattern ERROR_DUPLICATE_TYPE = Pattern.compile("Can't redefine: (.*)");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected <T> Map<String, T> determineExistingParserTypes(ProcessingState<T> processingState, FileState fileState) {
        Set<String> duplicateTypeNames = fileState.getDuplicateTypeNames();
        Map<String, T> existingParserTypes = new HashMap<>();
        for (TypeState<T> typeState : processingState.getTypeStates()) {
            String typeName1 = typeState.getName();
            if (!duplicateTypeNames.contains(typeName1)) {
                existingParserTypes.put(typeState.getName(), typeState.getSchema());
            }
        }
        return existingParserTypes;
    }

    private <T> void processEachTypeDefinedInFile(ProcessingState<T> processingState, String path, Map<String, T> typesDefinedInFile) throws IOException {
        for (Map.Entry<String, T> entry : typesDefinedInFile.entrySet()) {
            String typeName = entry.getKey();
            T schema = entry.getValue();
            processingState.getTypeState(typeName).processTypeDefinition(path, schema);
        }
    }

    protected <T> void handleSuccessfulCompilation(ProcessingState<T> processingState, FileState fileState, String path, Map<String, T> typesDefinedInFile) throws IOException {
        processEachTypeDefinedInFile(processingState, path, typesDefinedInFile);
        fileState.clearError();
        processingState.incrementalProcessedTotal();
        processingState.queueDelayedFilesForProcessing();
        if (logger.isDebugEnabled()) {
            logger.debug("Processed {}; contained types {}", path, typesDefinedInFile.keySet());
        } else {
            logger.info("Processed {}", path);
        }
    }

    protected <T> void handleSchemaParseException(ProcessingState<T> processingState, FileState fileState, String path, Exception ex) throws IOException {
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
    }

    protected <T> void handleNullPointerException(ProcessingState<T> processingState, FileState fileState, String path, Exception ex) {
        fileState.setError(ex);
        processingState.queueForDelayedProcessing(fileState);
        logger.debug("Encountered null reference while parsing {} (possibly due to unresolved dependency); will try again", path);
    }

    protected void handleIOException(String path, Exception ex) throws IOException {
        throw new IOException(String.format("Failed to compile schema definition file %s", path), ex);
    }

    protected <T> void handleFailedFiles(ProcessingState<T> processingState) throws IOException {
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
    }

    protected void logFileProcessing(FileState fileState) {
        logger.debug("Processing {}, excluding types {}", fileState.getPath(), fileState.getDuplicateTypeNames());
    }
}
