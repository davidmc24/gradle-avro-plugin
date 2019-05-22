package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.base.Enums;
import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;

import static com.commercehub.avro.tools.base.Constants.OPTION_STRING_TYPE;

class CompilerOptionsUtils {
    static void configure(SpecificCompiler compiler, CompilerOptions options) throws IllegalArgumentException {
        if (options.getOutputCharacterEncoding() != null) {
            throw new IllegalArgumentException("outputCharacterEncoding is not supported in this version of Avro");
        }
        if (options.getStringType() != null) {
            compiler.setStringType(Enums.parseCaseInsensitive(OPTION_STRING_TYPE, GenericData.StringType.values(), options.getStringType()));
        }
        if (options.getFieldVisibility() != null) {
            throw new IllegalArgumentException("fieldVisibility is not supported in this version of Avro");
        }
        if (options.getTemplateDirectory() != null) {
            compiler.setTemplateDir(options.getTemplateDirectory());
        }
        if (options.isCreateSetters() != null) {
            throw new IllegalArgumentException("createSetters is not supported in this version of Avro");
        }
        if (options.isEnableDecimalLogicalType() != null) {
            throw new IllegalArgumentException("enableDecimalLogicalType is not supported in this version of Avro");
        }
    }

    static void configure(Schema.Parser parser, CompilerOptions options) throws IllegalArgumentException {
        if (options.isValidateDefaults() != null) {
            throw new IllegalArgumentException("validateDefaults is not supported in this version of Avro");
        }
    }
}
