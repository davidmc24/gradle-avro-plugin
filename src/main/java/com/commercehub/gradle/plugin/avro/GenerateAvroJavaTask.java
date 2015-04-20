package com.commercehub.gradle.plugin.avro;

import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.generic.GenericData;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.specs.NotSpec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.commercehub.gradle.plugin.avro.Constants.*;

public class GenerateAvroJavaTask extends OutputDirTask {
    protected final HashMap<String, Schema> parsedTypes = new HashMap<>();

    private static Set<String> SUPPORTED_EXTENSIONS = SetBuilder.build(PROTOCOL_EXTENSION, SCHEMA_EXTENSION);

    private String encoding = Constants.UTF8_ENCONDING;

    private String stringType;

    private boolean compile = true;

    public Map<String, Schema> getParsedTypes() {
        HashMap<String, Schema> typeMap = new HashMap<>();
        typeMap.putAll(parsedTypes);
        return typeMap;
    }

    /**
     * Add any dependency types to be passed to the Avro schema parser
     * @param typeMap Map of fully-qualified type names to Schema describing their type
     */
    public void addTypes(Map<String, Schema> typeMap) {
        parsedTypes.putAll(typeMap);
    }

    /**
     * Set whether we should actually generate Java files for parsed types. Useful when used to parse dependency types
     * where the types themselves do not need to be part of the auto-generated source.
     * @param bool
     */
    public void setCompile(boolean bool) {
        compile = bool;
    }

    public boolean getCompile() {
        return compile;
    }

    @Input
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Input
    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    private GenericData.StringType parseStringType() {
        String stringType = getStringType();
        for (GenericData.StringType type : GenericData.StringType.values()) {
            if (type.name().equalsIgnoreCase(stringType)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid stringType '%s'.  Valid values are: %s", stringType,
            Arrays.asList(GenericData.StringType.values())));
    }

    @TaskAction
    protected void process() {
        getLogger().debug("Using encoding {}", getEncoding());
        getLogger().info("Found {} files", filterSources(new FileExtensionSpec(SUPPORTED_EXTENSIONS)).getFiles().size());
        if (getOutputDir() == null) compile = false;
        preClean();
        processFiles();
    }

    /**
     * We need to remove all previously generated Java classes.  Otherwise, when we call
     * {@link SpecificCompiler#compileToDestination(java.io.File, java.io.File)}, it will skip generating classes for
     * any schema files where the generated class is newer than the schema file.  That seems like a useful performance
     * optimization, but it can cause problems in the case where the schema file for this class hasn't changed, but
     * the schema definition for one of the types it depends on has, resulting in some usages of a type now having
     * outdated schema.
     */
    private void preClean() {
        if (compile) getProject().delete(getOutputDir());
    }

    private void processFiles() {
        int processedFileCount = 0;
        processedFileCount += processProtoFiles();
        processedFileCount += processSchemaFiles();
        setDidWork(processedFileCount > 0);
    }

    private int processProtoFiles() {
        int processedFileCount = 0;
        for (File sourceFile : filterSources(new FileExtensionSpec(PROTOCOL_EXTENSION))) {
            processProtoFile(sourceFile);
            processedFileCount++;
        }
        return processedFileCount;
    }

    private void processProtoFile(File sourceFile) {
        getLogger().info("Processing {}", sourceFile);
        try {
            // We cannot provide parsed types to a protocol in the same way as we can to an ordinary schema file
            // protocols must therefore depend on other protocols explicitly with using imports
            Protocol protocol = Protocol.parse(sourceFile);
            for (Schema schema : protocol.getTypes()) {
                parsedTypes.put(schema.getFullName(), schema);
            }
            if (compile) {
                SpecificCompiler compiler = new SpecificCompiler(protocol);
                compiler.setStringType(parseStringType());
                compiler.setOutputCharacterEncoding(getEncoding());
                compiler.compileToDestination(sourceFile, getOutputDir());
            }
        }
        catch (IOException ex) {
            throw new GradleException(String.format("Failed to compile protocol definition file %s", sourceFile), ex);
        }
    }

    private int processSchemaFiles() {
        int processedTotal = 0;
        int processedThisPass = -1;
        Queue<File> nextPass = new LinkedList<>(filterSources(new FileExtensionSpec(SCHEMA_EXTENSION)).getFiles());
        Queue<File> thisPass = new LinkedList<>();
        while (processedThisPass != 0) {
            if (processedThisPass > 0) {
                processedTotal += processedThisPass;
            }
            processedThisPass = 0;
            thisPass.addAll(nextPass);
            nextPass.clear();
            File sourceFile = thisPass.poll();
            while (sourceFile != null) {
                getLogger().debug("Processing {}", sourceFile);
                try {
                    Schema.Parser parser = new Schema.Parser();
                    parser.addTypes(parsedTypes);
                    Schema schema = parser.parse(sourceFile);
                    parsedTypes.putAll(parser.getTypes());
                    if (compile) {
                        SpecificCompiler compiler = new SpecificCompiler(schema);
                        compiler.setStringType(parseStringType());
                        compiler.setOutputCharacterEncoding(getEncoding());
                        compiler.compileToDestination(sourceFile, getOutputDir());
                    }
                    getLogger().info("Processed {}", sourceFile);
                    processedThisPass++;
                }
                catch (SchemaParseException ex) {
                    if (ex.getMessage().matches("(?i).*(undefined name|not a defined name).*")) {
                        getLogger().debug("Found undefined name in {}; will try again later", sourceFile);
                        nextPass.add(sourceFile);
                    }
                    else {
                        throw new GradleException(String.format("Failed to compile schema definition file %s", sourceFile), ex);
                    }
                }
                catch (NullPointerException ex) {
                    getLogger().debug("Encountered null reference while parsing {} (possibly due to unresolved dependency); will try again later", sourceFile);
                    nextPass.add(sourceFile);
                }
                catch (IOException ex) {
                    throw new GradleException(String.format("Failed to compile schema definition file %s", sourceFile), ex);
                }
                sourceFile = thisPass.poll();
            }
        }
        if (!nextPass.isEmpty()) {
            throw new GradleException(String.format("Failed to compile schema definition files due to undefined names: %s", nextPass));
        }
        return processedTotal;
    }
}
