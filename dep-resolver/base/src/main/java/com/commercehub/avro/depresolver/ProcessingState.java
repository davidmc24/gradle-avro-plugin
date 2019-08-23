/**
 * Copyright © 2015 Commerce Technologies, LLC.
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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

class ProcessingState<T> {
    private final Map<String, TypeState<T>> typeStates = new HashMap<>();
    private final Set<FileState> delayedFiles = new LinkedHashSet<>();
    private final Queue<FileState> filesToProcess;
    
    ProcessingState(Collection<File> sourceFiles) {
        filesToProcess = sourceFiles.stream().map(FileState::new).collect(Collectors.toCollection(LinkedList::new));
    }

    Map<String, SchemaWrapper<T>> determineParserTypes(FileState fileState) {
        Set<String> duplicateTypeNames = fileState.getDuplicateTypeNames();
        Map<String, SchemaWrapper<T>> types = new HashMap<>();
        for (TypeState<T> typeState : typeStates.values()) {
            String typeName = typeState.getName();
            if (!duplicateTypeNames.contains(typeName)) {
                types.put(typeState.getName(), typeState.getSchema());
            }
        }
        return types;
    }

    void processTypeDefinitions(FileState fileState, Map<String, SchemaWrapper<T>> newTypes) {
        File sourceFile = fileState.getSourceFile();
        newTypes.forEach((String typeName, SchemaWrapper schema) -> {
            typeStates.computeIfAbsent(typeName, TypeState::new).processTypeDefinition(sourceFile, schema);
        });
        fileState.clearError();
        queueDelayedFilesForProcessing();
    }

    Set<FileState> getFailedFiles() {
        return delayedFiles;
    }

    void queueForProcessing(FileState fileState) {
        filesToProcess.add(fileState);
    }

    void queueForDelayedProcessing(FileState fileState) {
        delayedFiles.add(fileState);
    }

    private void queueDelayedFilesForProcessing() {
        filesToProcess.addAll(delayedFiles);
        delayedFiles.clear();
    }

    FileState nextFileState() {
        return filesToProcess.poll();
    }

    boolean isWorkRemaining() {
        return !filesToProcess.isEmpty();
    }
}
