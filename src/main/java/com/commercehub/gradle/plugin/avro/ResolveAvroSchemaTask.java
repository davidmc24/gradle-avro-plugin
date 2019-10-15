/**
 * Copyright © 2019 Commerce Technologies, LLC.
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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import static com.commercehub.gradle.plugin.avro.Constants.SCHEMA_EXTENSION;

@CacheableTask
public class ResolveAvroSchemaTask extends OutputDirTask {
    private final Property<String> mainClassName;
    private FileCollection classpath;

    @Inject
    public ResolveAvroSchemaTask(ObjectFactory objects) {
        super();
        this.mainClassName = objects.property(String.class).convention("com.commercehub.avro.depresolver.Tool");
        this.classpath = objects.fileCollection(); // TODO: convention?
        this.classpath = objects.fileCollection().from("/Users/dcarr/repos/gradle-avro-plugin/dep-resolver/build/libs/avro-dependency-resolver.jar"); // TODO: remove
    }

    @TaskAction
    private void run() {
        getProject().javaexec(spec -> spec.setMain(getMainClassName().get()).setArgs(assembleArgs()).setClasspath(getClasspath()));
    }

    private List<String> assembleArgs() {
        List<String> args = new ArrayList<>();
        args.add(getOutputDir().getAsFile().get().getPath());
        filterSources(new FileExtensionSpec(SCHEMA_EXTENSION)).getFiles().stream().map(File::getPath).forEach(args::add);
        return args;
    }

    @Input
    public Property<String> getMainClassName() {
        return this.mainClassName;
    }

    public void setMainClassName(String mainClassName) {
        this.mainClassName.set(mainClassName);
    }

    @Input
    public FileCollection getClasspath() {
        return this.classpath;
    }

    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }
}
