package com.commercehub.avro.tools.api;

public class CompilerOptions {
    private String stringType;
    private String fieldVisibility;
    private String outputCharacterEncoding;
    private String templateDirectory;
    private Boolean createSetters;
    private Boolean enableDecimalLogicalType;
    private Boolean validateDefaults;

    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    public String getFieldVisibility() {
        return fieldVisibility;
    }

    public void setFieldVisibility(String fieldVisibility) {
        this.fieldVisibility = fieldVisibility;
    }

    public String getOutputCharacterEncoding() {
        return outputCharacterEncoding;
    }

    public void setOutputCharacterEncoding(String outputCharacterEncoding) {
        this.outputCharacterEncoding = outputCharacterEncoding;
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public Boolean getCreateSetters() {
        return createSetters;
    }

    public Boolean isCreateSetters() {
        return createSetters;
    }

    public void setCreateSetters(Boolean createSetters) {
        this.createSetters = createSetters;
    }

    public Boolean getEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    public Boolean isEnableDecimalLogicalType() {
        return enableDecimalLogicalType;
    }

    public void setEnableDecimalLogicalType(Boolean enableDecimalLogicalType) {
        this.enableDecimalLogicalType = enableDecimalLogicalType;
    }

    public Boolean getValidateDefaults() {
        return validateDefaults;
    }

    public Boolean isValidateDefaults() {
        return validateDefaults;
    }

    public void setValidateDefaults(Boolean validateDefaults) {
        this.validateDefaults = validateDefaults;
    }
}
