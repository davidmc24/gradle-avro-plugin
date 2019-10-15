/**
 * Copyright © 2013-2019 Commerce Technologies, LLC.
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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.inject.Inject;
import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.CacheableTask;

import static com.commercehub.gradle.plugin.avro.Constants.SCHEMA_EXTENSION;

/**
 * Task to generate Java source files based on Avro schema files using {@link SpecificCompiler}.
 */
@CacheableTask
public class CompileAvroFromSchemaTask extends BaseCompileAvroTask {
    private static Set<String> SUPPORTED_EXTENSIONS = new SetBuilder<String>().add(SCHEMA_EXTENSION).build();

    @Inject
    public CompileAvroFromSchemaTask(ObjectFactory objects) {
        super(objects);
    }

    @Override
    Set<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    void processFile(File sourceFile) {
        getLogger().info("Processing {}", sourceFile);
        try {
            compile(createSpecificCompiler(new Schema.Parser().parse(sourceFile)), sourceFile);
        } catch (IOException ex) {
            throw new GradleException(String.format("Failed to compile protocol definition file %s", sourceFile), ex);
        }
    }
}
