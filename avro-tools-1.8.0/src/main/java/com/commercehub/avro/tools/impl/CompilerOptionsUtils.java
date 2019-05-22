package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.base.Enums;
import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;

import static com.commercehub.avro.tools.base.Constants.OPTION_FIELD_VISIBILITY;
import static com.commercehub.avro.tools.base.Constants.OPTION_STRING_TYPE;

class CompilerOptionsUtils {
    static void configure(SpecificCompiler compiler, CompilerOptions options) throws IllegalArgumentException {
        if (options.getOutputCharacterEncoding() != null) {
            compiler.setOutputCharacterEncoding(options.getOutputCharacterEncoding());
        }
        if (options.getStringType() != null) {
            compiler.setStringType(Enums.parseCaseInsensitive(OPTION_STRING_TYPE, GenericData.StringType.values(), options.getStringType()));
        }
        if (options.getFieldVisibility() != null) {
            compiler.setFieldVisibility(Enums.parseCaseInsensitive(OPTION_FIELD_VISIBILITY, SpecificCompiler.FieldVisibility.values(), options.getFieldVisibility()));
        }
        if (options.getTemplateDirectory() != null) {
            compiler.setTemplateDir(options.getTemplateDirectory());
        }
        if (options.isCreateSetters() != null) {
            compiler.setCreateSetters(options.isCreateSetters());
        }
        if (options.isEnableDecimalLogicalType() != null) {
            throw new IllegalArgumentException("enableDecimalLogicalType is not supported in this version of Avro");
        }
    }

    static void configure(Schema.Parser parser, CompilerOptions options) throws IllegalArgumentException {
        if (options.isValidateDefaults() != null) {
            parser.setValidateDefaults(options.isValidateDefaults());
        }
    }
}
