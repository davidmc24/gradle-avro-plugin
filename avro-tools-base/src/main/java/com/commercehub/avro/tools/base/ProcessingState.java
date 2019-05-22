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
package com.commercehub.avro.tools.base;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class ProcessingState<T> {
    private final Map<String, TypeState<T>> typeStates = new HashMap<>();
    private final Set<FileState> delayedFiles = new LinkedHashSet<>();
    private final Queue<FileState> filesToProcess = new LinkedList<>();
    private int processedTotal = 0;

    public ProcessingState(Set<File> files, File baseFile) {
        Path basePath = baseFile.toPath();
        for (File file : files) {
            filesToProcess.add(new FileState(file, basePath.relativize(file.toPath()).toString() )); //TODO: is this right?
        }
    }

    public Set<FileState> getFailedFiles() {
        return delayedFiles;
    }

    public TypeState<T> getTypeState(String typeName) {
        TypeState<T> typeState = typeStates.get(typeName);
        if (typeState == null) {
            typeState = new TypeState<>(typeName);
            typeStates.put(typeName, typeState);
        }
        return typeState;
    }

    public void queueForProcessing(FileState fileState) {
        filesToProcess.add(fileState);
    }

    public void queueForDelayedProcessing(FileState fileState) {
        delayedFiles.add(fileState);
    }

    public void queueDelayedFilesForProcessing() {
        filesToProcess.addAll(delayedFiles);
        delayedFiles.clear();
    }

    public FileState nextFileState() {
        return filesToProcess.poll();
    }

    public boolean isWorkRemaining() {
        return !filesToProcess.isEmpty();
    }

    public int getProcessedTotal() {
        return processedTotal;
    }

    public Collection<TypeState<T>> getTypeStates() {
        return typeStates.values();
    }

    public void incrementalProcessedTotal() {
        processedTotal++;
    }
}
