package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import org.apache.avro.compiler.specific.SpecificCompiler;

class CompilerOptionsUtils {
    static void configure(SpecificCompiler compiler, CompilerOptions options) throws IllegalArgumentException {
        if (options.getOutputCharacterEncoding() != null) {
            throw new IllegalArgumentException("outputCharacterEncoding is not supported in this version of Avro");
        }
        if (options.getStringType() != null) {
            throw new IllegalArgumentException("stringType is not supported in this version of Avro");
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
}
