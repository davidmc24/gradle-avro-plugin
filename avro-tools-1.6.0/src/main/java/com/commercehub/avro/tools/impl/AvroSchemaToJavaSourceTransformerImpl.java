package com.commercehub.avro.tools.impl;

import com.commercehub.avro.tools.api.CompilerOptions;
import com.commercehub.avro.tools.base.BaseAvroSchemaToJavaSourceTransformer;
import com.commercehub.avro.tools.base.FileState;
import com.commercehub.avro.tools.base.ProcessingState;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.compiler.specific.SpecificCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.commercehub.avro.tools.base.MapUtils.asymmetricDifference;

class AvroSchemaToJavaSourceTransformerImpl extends BaseAvroSchemaToJavaSourceTransformer {
    @Override
    public int transform(Collection<File> inputs, File outputDir, File baseFile, CompilerOptions options) throws IOException {
        ProcessingState<Schema> processingState = new ProcessingState<>(new LinkedHashSet<>(inputs), baseFile);
        while (processingState.isWorkRemaining()) {
            processSchemaFile(processingState, processingState.nextFileState(), outputDir, options);
        }
        handleFailedFiles(processingState);
        return processingState.getProcessedTotal();
    }

    private void processSchemaFile(ProcessingState<Schema> processingState, FileState fileState, File outputDir, CompilerOptions options) throws IOException {
        logFileProcessing(fileState);
        String path = fileState.getPath();
        Map<String, Schema> existingParserTypes = determineExistingParserTypes(processingState, fileState);
        try {
            Map<String, Schema> typesDefinedInFile = compileSchemaToJavaSource(fileState, outputDir, options, existingParserTypes);
            handleSuccessfulCompilation(processingState, fileState, path, typesDefinedInFile);
        } catch (SchemaParseException ex) {
            handleSchemaParseException(processingState, fileState, path, ex);
        } catch (NullPointerException ex) {
            handleNullPointerException(processingState, fileState, path, ex);
        } catch (IOException ex) {
            handleIOException(path, ex);
        }
    }

    private Map<String, Schema> compileSchemaToJavaSource(FileState fileState, File outputDir, CompilerOptions options, Map<String, Schema> existingParserTypes) throws IOException {
        File sourceFile = fileState.getFile();
        Schema.Parser parser = createParser(existingParserTypes, options);
        Schema parsedSchema = parser.parse(sourceFile);
        Map<String, Schema> newParserTypes = parser.getTypes();
        compile(parsedSchema, sourceFile, outputDir, options);
        return asymmetricDifference(newParserTypes, existingParserTypes);
    }

    private Schema.Parser createParser(Map<String, Schema> existingParserTypes, CompilerOptions options) {
        Schema.Parser parser = new Schema.Parser();
        parser.addTypes(existingParserTypes);
        CompilerOptionsUtils.configure(parser, options);
        return parser;
    }

    private void compile(Schema parsedSchema, File sourceFile, File outputDir, CompilerOptions options) throws IOException {
        SpecificCompiler compiler = new SpecificCompiler(parsedSchema);
        CompilerOptionsUtils.configure(compiler, options);
        compiler.compileToDestination(sourceFile, outputDir);
    }
}
