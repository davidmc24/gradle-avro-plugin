package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.base.BaseAvroProtocolToJavaSourceTransformer;
import org.apache.avro.Protocol;
import org.apache.avro.compiler.specific.SpecificCompiler;

import java.io.File;
import java.io.IOException;

class AvroProtocolToJavaSourceTransformerImpl extends BaseAvroProtocolToJavaSourceTransformer {
    @Override
    protected void transformProtocolToJavaSource(File inputFile, File outputDir, CompilerOptions options) throws IOException {
        SpecificCompiler compiler = new SpecificCompiler(Protocol.parse(inputFile));
        CompilerOptionsUtils.configure(compiler, options);
        compiler.compileToDestination(inputFile, outputDir);
    }
}
