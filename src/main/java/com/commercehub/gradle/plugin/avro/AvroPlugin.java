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
import java.io.FileFilter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.plugins.ide.idea.GenerateIdeaModule;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import static com.commercehub.gradle.plugin.avro.Constants.GROUP_SOURCE_GENERATION;
import static com.commercehub.gradle.plugin.avro.Constants.IDL_EXTENSION;
import static com.commercehub.gradle.plugin.avro.Constants.JAVA_EXTENSION;
import static com.commercehub.gradle.plugin.avro.Constants.PROTOCOL_EXTENSION;
import static com.commercehub.gradle.plugin.avro.Constants.SCHEMA_EXTENSION;
import static com.commercehub.gradle.plugin.avro.GradleCompatibility.configurePropertyConvention;

public class AvroPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(AvroBasePlugin.class);
        configureTasks(project);
        configureIntelliJ(project);
    }

    private static void configureTasks(final Project project) {
        getSourceSets(project).all(sourceSet -> {
            GenerateAvroProtocolTask generateProtoTask = configureProtocolGenerationTask(project, sourceSet);
            CompileAvroFromProtocolTask compileProtocolTask = configureCompileProtocolTask(project, sourceSet, generateProtoTask);
            ResolveAvroSchemaTask resolveSchemaTask = configureResolveSchemaTask(project, sourceSet);
            CompileAvroFromSchemaTask compileSchemaTask = configureCompileSchemaTask(project, sourceSet, resolveSchemaTask);
            configureTaskDependencies(project, sourceSet, compileProtocolTask, compileSchemaTask);
        });
    }

    private static void configureIntelliJ(final Project project) {
        project.getPlugins().withType(IdeaPlugin.class).all(ideaPlugin -> {
            SourceSet mainSourceSet = getMainSourceSet(project);
            SourceSet testSourceSet = getTestSourceSet(project);
            project.getTasks().withType(GenerateIdeaModule.class).all(generateIdeaModule ->
                project.getTasks().withType(BaseCompileAvroTask.class).all(generateAvroJavaTask ->
                    generateIdeaModule.doFirst(task -> project.mkdir(generateAvroJavaTask.getOutputDir()))));
            IdeaModule module = ideaPlugin.getModel().getModule();
            module.setSourceDirs(new SetBuilder<File>()
                .addAll(module.getSourceDirs())
                .add(getAvroSourceDir(project, mainSourceSet))
                .build());
            module.setTestSourceDirs(new SetBuilder<File>()
                .addAll(module.getTestSourceDirs())
                .add(getAvroSourceDir(project, testSourceSet))
                .build());
            // IntelliJ doesn't allow source directories beneath an excluded directory.
            // Thus, we remove the build directory exclude and add all non-generated sub-directories as excludes.
            SetBuilder<File> excludeDirs = new SetBuilder<>();
            excludeDirs.addAll(module.getExcludeDirs()).remove(project.getBuildDir());
            File buildDir = project.getBuildDir();
            if (buildDir.isDirectory()) {
                excludeDirs.addAll(project.getBuildDir().listFiles(new NonGeneratedDirectoryFileFilter()));
            }
            module.setExcludeDirs(excludeDirs.build());
        });
    }

    private static GenerateAvroProtocolTask configureProtocolGenerationTask(final Project project, final SourceSet sourceSet) {
        String taskName = sourceSet.getTaskName("generate", "avroProtocol");
        GenerateAvroProtocolTask task = project.getTasks().create(taskName, GenerateAvroProtocolTask.class);
        task.setDescription(
            String.format("Generates %s Avro protocol definition files from IDL files.", sourceSet.getName()));
        task.setGroup(GROUP_SOURCE_GENERATION);
        task.source(getAvroSourceDir(project, sourceSet));
        task.include("**/*." + IDL_EXTENSION);
        configurePropertyConvention(task.getOutputDir(), getGeneratedOutputDir(project, sourceSet, PROTOCOL_EXTENSION));
        return task;
    }

    private static ResolveAvroSchemaTask configureResolveSchemaTask(final Project project, final SourceSet sourceSet) {
        String taskName = sourceSet.getTaskName("resolve", "avroSchema");
        ResolveAvroSchemaTask task = project.getTasks().create(taskName, ResolveAvroSchemaTask.class);
        task.setDescription(
            String.format("Resolves dependencies between %s Avro schema files.", sourceSet.getName()));
        task.setGroup(GROUP_SOURCE_GENERATION);
        task.source(getAvroSourceDir(project, sourceSet));
        task.include("**/*." + SCHEMA_EXTENSION);
        configurePropertyConvention(task.getOutputDir(), getGeneratedOutputDir(project, sourceSet, SCHEMA_EXTENSION));
        return task;
    }

    private static CompileAvroFromProtocolTask configureCompileProtocolTask(final Project project, final SourceSet sourceSet,
                                                                    GenerateAvroProtocolTask protoTask) {
        String taskName = sourceSet.getTaskName("compile", "avroProtocol");
        CompileAvroFromProtocolTask task = project.getTasks().create(taskName, CompileAvroFromProtocolTask.class);
        task.setDescription(String.format("Generates %s Avro Java source files from protocol definition files.",
            sourceSet.getName()));
        task.setGroup(GROUP_SOURCE_GENERATION);
        task.source(getAvroSourceDir(project, sourceSet));
        task.source(protoTask.getOutputDir());
        task.source(protoTask.getOutputs());
        task.include("**/*." + PROTOCOL_EXTENSION);
        configurePropertyConvention(task.getOutputDir(), getGeneratedOutputDir(project, sourceSet, JAVA_EXTENSION));

        sourceSet.getJava().srcDir(task.getOutputDir());

        final JavaCompile compileJavaTask = getCompileJavaTask(project, sourceSet);
        compileJavaTask.source(task.getOutputDir());
        compileJavaTask.source(task.getOutputs());

        final AvroExtension avroExtension = project.getExtensions().findByType(AvroExtension.class);

        configurePropertyConvention(task.getOutputCharacterEncoding(), project.provider(() -> {
            String compilationEncoding = compileJavaTask.getOptions().getEncoding();
            String extensionEncoding = avroExtension.getOutputCharacterEncoding().getOrNull();
            return compilationEncoding != null ? compilationEncoding : extensionEncoding;
        }));
        return task;
    }

    private static CompileAvroFromSchemaTask configureCompileSchemaTask(final Project project, final SourceSet sourceSet,
                                                                    ResolveAvroSchemaTask resolveSchemaTask) {
        String taskName = sourceSet.getTaskName("compile", "avroSchema");
        CompileAvroFromSchemaTask task = project.getTasks().create(taskName, CompileAvroFromSchemaTask.class);
        task.setDescription(String.format("Generates %s Avro Java source files from schema definition files.",
            sourceSet.getName()));
        task.setGroup(GROUP_SOURCE_GENERATION);
        task.source(resolveSchemaTask.getOutputDir());
        task.source(resolveSchemaTask.getOutputs());
        task.include("**/*." + SCHEMA_EXTENSION);
        configurePropertyConvention(task.getOutputDir(), getGeneratedOutputDir(project, sourceSet, JAVA_EXTENSION));

        sourceSet.getJava().srcDir(task.getOutputDir());

        final JavaCompile compileJavaTask = getCompileJavaTask(project, sourceSet);
        compileJavaTask.source(task.getOutputDir());
        compileJavaTask.source(task.getOutputs());

        final AvroExtension avroExtension = project.getExtensions().findByType(AvroExtension.class);

        configurePropertyConvention(task.getOutputCharacterEncoding(), project.provider(() -> {
            String compilationEncoding = compileJavaTask.getOptions().getEncoding();
            String extensionEncoding = avroExtension.getOutputCharacterEncoding().getOrNull();
            return compilationEncoding != null ? compilationEncoding : extensionEncoding;
        }));
        return task;
    }

    private static void configureTaskDependencies(final Project project, final SourceSet sourceSet, final OutputDirTask... outputDirTasks) {
        project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin ->
            project.getTasks().matching(task -> {
                String compilationTaskName = sourceSet.getCompileTaskName("kotlin");
                return compilationTaskName.equals(task.getName());
            })
                .all(task -> {
                    if (task instanceof SourceTask) {
                        for (OutputDirTask outputDirTask : outputDirTasks) {
                            ((SourceTask) task).source(outputDirTask.getOutputs());
                        }
                    } else {
                        for (OutputDirTask outputDirTask : outputDirTasks) {
                            task.dependsOn(outputDirTask);
                        }
                    }
                }));
    }

    private static File getAvroSourceDir(Project project, SourceSet sourceSet) {
        return project.file(String.format("src/%s/avro", sourceSet.getName()));
    }

    private static Provider<Directory> getGeneratedOutputDir(Project project, SourceSet sourceSet, String extension) {
        String generatedOutputDirName = String.format("generated-%s-avro-%s", sourceSet.getName(), extension);
        return project.getLayout().getBuildDirectory().dir(generatedOutputDirName);
    }

    private static JavaCompile getCompileJavaTask(Project project, SourceSet sourceSet) {
        return (JavaCompile) project.getTasks().getByName(sourceSet.getCompileJavaTaskName());
    }

    private static SourceSetContainer getSourceSets(Project project) {
        return project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
    }

    private static SourceSet getMainSourceSet(Project project) {
        return getSourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    private static SourceSet getTestSourceSet(Project project) {
        return getSourceSets(project).getByName(SourceSet.TEST_SOURCE_SET_NAME);
    }

    private static class NonGeneratedDirectoryFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && !file.getName().startsWith("generated-");
        }
    }
}
