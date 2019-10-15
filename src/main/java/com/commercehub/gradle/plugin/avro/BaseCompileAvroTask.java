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
import java.nio.charset.Charset;
import java.util.Set;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility;
import org.apache.avro.generic.GenericData;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.NotSpec;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import static com.commercehub.gradle.plugin.avro.Constants.DEFAULT_CREATE_SETTERS;
import static com.commercehub.gradle.plugin.avro.Constants.DEFAULT_DATE_TIME_LOGICAL_TYPE;
import static com.commercehub.gradle.plugin.avro.Constants.DEFAULT_ENABLE_DECIMAL_LOGICAL_TYPE;
import static com.commercehub.gradle.plugin.avro.Constants.DEFAULT_FIELD_VISIBILITY;
import static com.commercehub.gradle.plugin.avro.Constants.DEFAULT_STRING_TYPE;
import static com.commercehub.gradle.plugin.avro.Constants.OPTION_DATE_TIME_LOGICAL_TYPE;
import static com.commercehub.gradle.plugin.avro.Constants.OPTION_FIELD_VISIBILITY;
import static com.commercehub.gradle.plugin.avro.Constants.OPTION_STRING_TYPE;
import static com.commercehub.gradle.plugin.avro.GradleCompatibility.configurePropertyConvention;

/**
 * Task to generate Java source files based on Avro files {@link SpecificCompiler}.
 */
@CacheableTask
abstract class BaseCompileAvroTask extends OutputDirTask {
    private final Property<String> outputCharacterEncoding;
    private final Property<String> stringType;
    private final Property<String> fieldVisibility;
    private final Property<String> templateDirectory;
    private final Property<Boolean> createSetters;
    private final Property<Boolean> enableDecimalLogicalType;
    private final Property<String> dateTimeLogicalType;

    private final Provider<GenericData.StringType> stringTypeProvider;
    private final Provider<FieldVisibility> fieldVisibilityProvider;
    private final Provider<SpecificCompiler.DateTimeLogicalTypeImplementation> dateTimeLogicalTypeImplementationProvider;

    BaseCompileAvroTask(ObjectFactory objects) {
        super();
        this.outputCharacterEncoding = objects.property(String.class);
        this.stringType = configurePropertyConvention(objects.property(String.class), DEFAULT_STRING_TYPE);
        this.fieldVisibility = configurePropertyConvention(objects.property(String.class), DEFAULT_FIELD_VISIBILITY);
        this.templateDirectory = objects.property(String.class);
        this.createSetters = configurePropertyConvention(objects.property(Boolean.class), DEFAULT_CREATE_SETTERS);
        this.enableDecimalLogicalType = configurePropertyConvention(objects.property(Boolean.class), DEFAULT_ENABLE_DECIMAL_LOGICAL_TYPE);
        this.dateTimeLogicalType = configurePropertyConvention(objects.property(String.class), DEFAULT_DATE_TIME_LOGICAL_TYPE);
        this.stringTypeProvider = getStringType()
            .map(input -> Enums.parseCaseInsensitive(OPTION_STRING_TYPE, GenericData.StringType.values(), input));
        this.fieldVisibilityProvider = getFieldVisibility()
            .map(input -> Enums.parseCaseInsensitive(OPTION_FIELD_VISIBILITY, FieldVisibility.values(), input));
        this.dateTimeLogicalTypeImplementationProvider = getDateTimeLogicalType()
            .map(input -> Enums.parseCaseInsensitive(OPTION_DATE_TIME_LOGICAL_TYPE,
                SpecificCompiler.DateTimeLogicalTypeImplementation.values(), input));
    }

    abstract Set<String> getSupportedExtensions();

    abstract void processFile(File sourceFile);
    @Optional
    @Input
    public Property<String> getOutputCharacterEncoding() {
        return outputCharacterEncoding;
    }

    public void setOutputCharacterEncoding(String outputCharacterEncoding) {
        this.outputCharacterEncoding.set(outputCharacterEncoding);
    }

    public void setOutputCharacterEncoding(Charset outputCharacterEncoding) {
        setOutputCharacterEncoding(outputCharacterEncoding.name());
    }

    @Input
    public Property<String> getStringType() {
        return stringType;
    }

    public void setStringType(GenericData.StringType stringType) {
        setStringType(stringType.name());
    }

    public void setStringType(String stringType) {
        this.stringType.set(stringType);
    }

    @Input
    public Property<String> getFieldVisibility() {
        return fieldVisibility;
    }

    public void setFieldVisibility(String fieldVisibility) {
        this.fieldVisibility.set(fieldVisibility);
    }

    public void setFieldVisibility(FieldVisibility fieldVisibility) {
        setFieldVisibility(fieldVisibility.name());
    }

    @Optional
    @Input
    public Property<String> getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory.set(templateDirectory);
    }

    public Property<Boolean> isCreateSetters() {
        return createSetters;
    }

    @Input
    public Property<Boolean> getCreateSetters() {
        return createSetters;
    }

    public void setCreateSetters(String createSetters) {
        this.createSetters.set(Boolean.parseBoolean(createSetters));
    }

    public Property<Boolean> isEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    @Input
    public Property<Boolean> getEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    public void setEnableDecimalLogicalType(String enableDecimalLogicalType) {
        this.enableDecimalLogicalType.set(Boolean.parseBoolean(enableDecimalLogicalType));
    }

    @Optional
    @Input
    public Property<String> getDateTimeLogicalType() {
        return dateTimeLogicalType;
    }

    public void setDateTimeLogicalType(String dateTimeLogicalType) {
        this.dateTimeLogicalType.set(dateTimeLogicalType);
    }

    public void setDateTimeLogicalType(SpecificCompiler.DateTimeLogicalTypeImplementation dateTimeLogicalType) {
        setDateTimeLogicalType(dateTimeLogicalType.name());
    }

    @TaskAction
    protected void process() {
        getLogger().debug("Using outputCharacterEncoding {}", getOutputCharacterEncoding().getOrNull());
        getLogger().debug("Using stringType {}", stringTypeProvider.get().name());
        getLogger().debug("Using fieldVisibility {}", fieldVisibilityProvider.get().name());
        getLogger().debug("Using templateDirectory '{}'", getTemplateDirectory().getOrNull());
        getLogger().debug("Using createSetters {}", isCreateSetters().get());
        getLogger().debug("Using enableDecimalLogicalType {}", isEnableDecimalLogicalType().get());
        getLogger().debug("Using dateTimeLogicalType {}", dateTimeLogicalTypeImplementationProvider.get().name());
        getLogger().info("Found {} files", getInputs().getSourceFiles().getFiles().size());
        failOnUnsupportedFiles();
        processFiles();
    }

    private void failOnUnsupportedFiles() {
        FileCollection unsupportedFiles = filterSources(new NotSpec<>(new FileExtensionSpec(getSupportedExtensions())));
        if (!unsupportedFiles.isEmpty()) {
            throw new GradleException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }
    }

    private void processFiles() {
        int processedFileCount = 0;
        for (File sourceFile : filterSources(new FileExtensionSpec(getSupportedExtensions()))) {
            processFile(sourceFile);
            processedFileCount++;
        }
        setDidWork(processedFileCount > 0);
    }

    protected SpecificCompiler createSpecificCompiler(Schema schema) {
        return new SpecificCompiler(schema, dateTimeLogicalTypeImplementationProvider.get());
    }

    protected SpecificCompiler createSpecificCompiler(Protocol protocol) {
        return new SpecificCompiler(protocol, dateTimeLogicalTypeImplementationProvider.get());
    }

    protected void compile(SpecificCompiler compiler, File sourceFile) throws IOException {
        compiler.setOutputCharacterEncoding(getOutputCharacterEncoding().getOrNull());
        compiler.setStringType(stringTypeProvider.get());
        compiler.setFieldVisibility(fieldVisibilityProvider.get());
        if (getTemplateDirectory().isPresent()) {
            compiler.setTemplateDir(getTemplateDirectory().get());
        }
        compiler.setCreateSetters(isCreateSetters().get());
        compiler.setEnableDecimalLogicalType(isEnableDecimalLogicalType().get());

        compiler.compileToDestination(sourceFile, getOutputDir().get().getAsFile());
    }
}
