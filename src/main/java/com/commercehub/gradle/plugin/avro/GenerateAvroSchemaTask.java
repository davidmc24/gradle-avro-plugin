/*
 * Copyright © 2018 Commerce Technologies, LLC.
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
package com.commercehub.gradle.plugin.avro;

import com.commercehub.avro.tools.api.AvroTransformer;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.commercehub.avro.tools.base.Constants.PROTOCOL_EXTENSION;

@CacheableTask
public class GenerateAvroSchemaTask extends OutputDirTask {
    @TaskAction
    protected void process() {
        AvroTransformer transformer = TransformerUtil.getTransfomer();
        try {
            checkForUnsupportedFiles();
            Set<File> supportedFiles = filterSources(new FileExtensionSpec(PROTOCOL_EXTENSION)).getFiles();
            getLogger().info("Found {} files", supportedFiles.size());
            File outputDir = getOutputDir();
            int processedFileCount = transformer.transformProtocolToSchema(supportedFiles, outputDir);
            setDidWork(processedFileCount > 0);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }

    private void checkForUnsupportedFiles() throws IOException {
        FileCollection unsupportedFiles = filterSources(new FileExtensionSpec(false, PROTOCOL_EXTENSION));
        if (!unsupportedFiles.isEmpty()) {
            throw new IOException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }
    }
}
