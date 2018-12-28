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

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.api.Transformation;
import com.commercehub.avro.tools.impl.TransformationImpl;
import org.apache.avro.Protocol;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

import static com.commercehub.gradle.plugin.avro.Constants.*;

/**
 * Task to generate Java source files based on Avro protocol files and Avro schema files using {@link Protocol} and
 * {@link SpecificCompiler}.
 */
@CacheableTask
public class GenerateAvroJavaTask extends OutputDirTask {
    private String outputCharacterEncoding;
    private String stringType = DEFAULT_STRING_TYPE;
    private String fieldVisibility = DEFAULT_FIELD_VISIBILITY;
    private String templateDirectory;
    private boolean createSetters = DEFAULT_CREATE_SETTERS;
    private boolean enableDecimalLogicalType = DEFAULT_ENABLE_DECIMAL_LOGICAL_TYPE;
    private boolean validateDefaults = DEFAULT_VALIDATE_DEFAULTS;

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

    public void setStringType(GenericData.StringType stringType) {
        setStringType(stringType.name());
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

    public void setFieldVisibility(SpecificCompiler.FieldVisibility fieldVisibility) {
        setFieldVisibility(fieldVisibility.name());
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

        try {
            Transformation transformation = new TransformationImpl();
            Collection<File> inputs = getSource().getFiles();
            File outputDir = getOutputDir();
            File baseFile = getProject().getProjectDir();
            int processedFileCount = transformation.generateJavaSourceFiles(inputs, outputDir, baseFile, options);
            setDidWork(processedFileCount > 0);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }
}
