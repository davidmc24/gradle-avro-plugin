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
package com.commercehub.gradle.plugin.avro;

import com.commercehub.avro.tools.api.AvroTransformer;
import com.commercehub.avro.tools.api.CompilerOptions;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import static com.commercehub.gradle.plugin.avro.Constants.PROTOCOL_EXTENSION;
import static com.commercehub.gradle.plugin.avro.Constants.SCHEMA_EXTENSION;

/**
 * Task to generate Java source files based on Avro protocol files and Avro schema files using {@code org.apache.avro.Protocol} and
 * {@code org.apache.avro.compiler.specific.SpecificCompiler}.
 */
@CacheableTask
public class GenerateAvroJavaTask extends OutputDirTask {
    private String outputCharacterEncoding;
    private String stringType;
    private String fieldVisibility;
    private String templateDirectory;
    private boolean createSetters;
    private boolean enableDecimalLogicalType;
    private boolean validateDefaults;

    // TODO: consider if any defaults are needed
//    private String outputCharacterEncoding;
//    private String stringType = DEFAULT_STRING_TYPE;
//    private String fieldVisibility = DEFAULT_FIELD_VISIBILITY;
//    private String templateDirectory;
//    private boolean createSetters = DEFAULT_CREATE_SETTERS;
//    private boolean enableDecimalLogicalType = DEFAULT_ENABLE_DECIMAL_LOGICAL_TYPE;
//    private boolean validateDefaults = DEFAULT_VALIDATE_DEFAULTS;

    @Optional
    @Input
    public String getOutputCharacterEncoding() {
        return outputCharacterEncoding;
    }

    public void setOutputCharacterEncoding(String outputCharacterEncoding) {
        this.outputCharacterEncoding = outputCharacterEncoding;
    }

    public void setOutputCharacterEncoding(Charset outputCharacterEncoding) {
        setOutputCharacterEncoding(outputCharacterEncoding.name());
    }

    @Input
    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    @Input
    public String getFieldVisibility() {
        return fieldVisibility;
    }

    public void setFieldVisibility(String fieldVisibility) {
        this.fieldVisibility = fieldVisibility;
    }

    @Optional
    @Input
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Input
    public boolean isCreateSetters() {
        return createSetters;
    }

    public void setCreateSetters(String createSetters) {
        this.createSetters = Boolean.parseBoolean(createSetters);
    }

    @Input
    public boolean isEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    public void setEnableDecimalLogicalType(String enableDecimalLogicalType) {
        this.enableDecimalLogicalType = Boolean.parseBoolean(enableDecimalLogicalType);
    }

    @Input
    public boolean isValidateDefaults() {
        return validateDefaults;
    }

    public void setValidateDefaults(boolean validateDefaults) {
        this.validateDefaults = validateDefaults;
    }

    @TaskAction
    protected void process() {
        CompilerOptions options = new CompilerOptions();
        options.setCreateSetters(isCreateSetters());
        options.setEnableDecimalLogicalType(isEnableDecimalLogicalType());
        options.setFieldVisibility(getFieldVisibility());
        options.setOutputCharacterEncoding(getOutputCharacterEncoding());
        options.setStringType(getStringType());
        options.setTemplateDirectory(getTemplateDirectory());
        options.setValidateDefaults(isValidateDefaults());

        getLogger().debug("Using outputCharacterEncoding {}", options.getOutputCharacterEncoding());
        getLogger().debug("Using stringType {}", options.getStringType());
        getLogger().debug("Using fieldVisibility {}", options.getFieldVisibility());
        getLogger().debug("Using templateDirectory '{}'", options.getTemplateDirectory());
        getLogger().debug("Using createSetters {}", options.isCreateSetters());
        getLogger().debug("Using enableDecimalLogicalType {}", options.isEnableDecimalLogicalType());
        getLogger().debug("Using validateDefaults {}", options.isValidateDefaults());

        AvroTransformer transformer = TransformerUtil.getTransfomer();
        try {
            checkForUnsupportedFiles();
            Set<File> protocolFiles = filterSources(new FileExtensionSpec(PROTOCOL_EXTENSION)).getFiles();
            Set<File> schemaFiles = filterSources(new FileExtensionSpec(SCHEMA_EXTENSION)).getFiles();
            getLogger().info("Found {} files", protocolFiles.size() + schemaFiles.size());
            File outputDir = getOutputDir();
            File baseFile = getProject().getProjectDir();
            int processedFileCount = 0;
            processedFileCount += transformer.transformProtocolToJavaSource(protocolFiles, outputDir, baseFile, options);
            processedFileCount += transformer.transformSchemaToJavaSource(schemaFiles, outputDir, baseFile, options);
            setDidWork(processedFileCount > 0);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }

    private void checkForUnsupportedFiles() throws IOException {
        FileCollection unsupportedFiles = filterSources(new FileExtensionSpec(false, PROTOCOL_EXTENSION, SCHEMA_EXTENSION));
        if (!unsupportedFiles.isEmpty()) {
            throw new IOException(
                String.format("Unsupported file extension for the following files: %s", unsupportedFiles));
        }
    }
}
