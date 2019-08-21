/**
 * Copyright © 2013-2015 Commerce Technologies, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.avro.depresolver;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.commercehub.avro.depresolver.MapUtils.asymmetricDifference;

class DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);

    private static Pattern ERROR_UNKNOWN_TYPE = Pattern.compile("(?i).*(undefined name|not a defined name).*");
    private static Pattern ERROR_DUPLICATE_TYPE = Pattern.compile("Can't redefine: (.*)");

    DependencyResolutionResult resolveSchemas(Collection<File> sourceFiles) {
        DependencyResolutionResult result = new DependencyResolutionResult();
        ProcessingState processingState = new ProcessingState(sourceFiles);
        while (processingState.isWorkRemaining()) {
            Schema schema = processSchemaFile(processingState, processingState.nextFileState());
            if (schema != null) {
                result.addSchema(schema);
            }
        }
        Set<FileState> failedFiles = processingState.getFailedFiles();
        for (FileState fileState : failedFiles) {
            result.addFileError(fileState.getSourceFile(), fileState.getErrorMessage());
        }
        return result;
    }

    private Schema processSchemaFile(ProcessingState processingState, FileState fileState) {
        Schema schema = null;
        String path = fileState.getPath();
        logger.debug("Processing {}, excluding types {}", path, fileState.getDuplicateTypeNames());
        File sourceFile = fileState.getSourceFile();
        Map<String, Schema> parserTypes = processingState.determineParserTypes(fileState);
        try {
            Schema.Parser parser = new Schema.Parser();
            parser.addTypes(parserTypes);
            schema = parser.parse(sourceFile);
            Map<String, Schema> typesDefinedInFile = asymmetricDifference(parser.getTypes(), parserTypes);
            processingState.processTypeDefinitions(fileState, typesDefinedInFile);
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved {}; contained types {}", path, typesDefinedInFile.keySet());
            } else {
                logger.info("Resolved {}", path);
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
                    throw new RuntimeException(
                        String.format("Failed to compile schema definition file %s; contains duplicate type definition %s", path, typeName),
                        ex);
                } else {
                    fileState.setError(ex);
                    fileState.addDuplicateTypeName(typeName);
                    processingState.queueForProcessing(fileState);
                    logger.debug("Identified duplicate type {} in {}; will re-process excluding it", typeName, path);
                }
            } else {
                throw new RuntimeException(String.format("Failed to compile schema definition file %s", path), ex);
            }
        } catch (NullPointerException ex) {
            fileState.setError(ex);
            processingState.queueForDelayedProcessing(fileState);
            logger.debug("Encountered null reference while parsing {} (possibly due to unresolved dependency); will try again", path);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Failed to compile schema definition file %s", path), ex);
        }
        return schema;
    }
}
