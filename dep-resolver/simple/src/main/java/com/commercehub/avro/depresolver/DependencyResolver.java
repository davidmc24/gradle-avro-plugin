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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.commercehub.avro.depresolver.MapUtils.asymmetricDifference;

class DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);

    DependencyResolutionResult resolveSchemas(Collection<File> sourceFiles) {
        DependencyResolutionResult result = new DependencyResolutionResult();
        ProcessingState processingState = new ProcessingState(sourceFiles);
        while (processingState.isWorkRemaining()) {
            SchemaWrapperImpl schema = processSchemaFile(processingState, processingState.nextFileState());
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

    private SchemaWrapperImpl processSchemaFile(ProcessingState processingState, FileState fileState) {
        SchemaWrapperImpl schema = null;
        String path = fileState.getPath();
        logger.debug("Processing {}, excluding types {}", path, fileState.getDuplicateTypeNames());
        File sourceFile = fileState.getSourceFile();
        Map<String, SchemaWrapperImpl> parserTypes = processingState.determineParserTypes(fileState);
        SchemaParserWrapperImpl parser = new SchemaParserWrapperImpl();
        try {
            parser.addTypes(parserTypes);
            schema = parser.parse(sourceFile);
            Map<String, SchemaWrapperImpl> typesDefinedInFile = asymmetricDifference(parser.getTypes(), parserTypes);
            processingState.processTypeDefinitions(fileState, typesDefinedInFile);
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved {}; contained types {}", path, typesDefinedInFile.keySet());
            } else {
                logger.info("Resolved {}", path);
            }
        } catch (UnknownAvroTypeException | NullPointerAvroException ex) {
            fileState.setError(ex);
            processingState.queueForDelayedProcessing(fileState);
            logger.debug("{}; will try again", ex.getMessage());
        } catch (DuplicateAvroTypeException ex) {
            String typeName = ex.getTypeName();
            if (fileState.containsDuplicateTypeName(typeName)) {
                throw new RuntimeException(String.format("Failed to resolve schema definition file %s; contains duplicate type definition %s", path, typeName), ex);
            } else {
                fileState.setError(ex);
                fileState.addDuplicateTypeName(typeName);
                processingState.queueForProcessing(fileState);
                logger.debug("{}; will re-process excluding it", ex.getMessage());
            }
        } catch (AvroSchemaParseException ex) {
            throw new RuntimeException(String.format("Failed to resolve schema definition file %s", path), ex);
        }
        return schema;
    }
}
