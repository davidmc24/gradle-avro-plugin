package com.commercehub.gradle.plugin.avro;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.commercehub.gradle.plugin.avro.Constants.*;

public class AvroDocumentationTask extends OutputDirTask {
    @TaskAction
    protected void process() {
        createOutputDir();

        // TODO: index or sidebar?
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.addProperty("resource.loader", "class");
        velocityEngine.addProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.setProperty("runtime.references.strict", true);
        velocityEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        Template indexTemplate = velocityEngine.getTemplate("index_doc.vm");
        Template recordTemplate = velocityEngine.getTemplate("record_doc.vm");
        Template enumTemplate = velocityEngine.getTemplate("enum_doc.vm");
        Set<Schema> schemas = loadAllSchemas();
        for (Schema schema : schemas) {
            Context context = new VelocityContext();
            context.put("schema", schema);
            File file = new File(getOutputDir(), String.format("%s.html", schema.getFullName()));
            try (Writer writer = new FileWriter(file)) {
                switch(schema.getType()) {
                    // TODO: render complex types as links?
                    case RECORD:
                        recordTemplate.merge(context, writer);
                        break;
                    case ENUM:
                        enumTemplate.merge(context, writer);
                        break;
                    default:
                        throw new GradleException(String.format("Unsupported type: %s", schema.getType()));
                }
            } catch (IOException ex) {
                throw new GradleException(
                        String.format("Failed to write documentation for schema %s", schema.getFullName()), ex);
            }
        }
        Context context = new VelocityContext();
        context.put("schemas", schemas);
        File file = new File(getOutputDir(), "index.html");
        try (Writer writer = new FileWriter(file)) {
            indexTemplate.merge(context, writer);
        } catch (IOException ex) {
            throw new GradleException("Failed to write documentation index", ex);
        }
    }

    private void createOutputDir() {
        try {
            Files.createDirectories(getOutputDir().toPath());
        } catch (IOException ex) {
            throw new GradleException("Could not create output directory", ex);
        }
    }

    private Set<Schema> loadAllSchemas() {
        int processedThisPass = -1;
        Map<String, Schema> types = Maps.newHashMap();
        Queue<File> nextPass = Lists.newLinkedList(filterSources(new FileExtensionSpec(SCHEMA_EXTENSION)).getFiles());
        Queue<File> thisPass = Lists.newLinkedList();
        while (processedThisPass != 0) {
            processedThisPass = 0;
            thisPass.addAll(nextPass);
            nextPass.clear();
            File sourceFile = thisPass.poll();
            while (sourceFile != null) {
                getLogger().debug("Processing {}", sourceFile);
                try {
                    Schema.Parser parser = new Schema.Parser();
                    parser.addTypes(types);
                    parser.parse(sourceFile);
                    types = parser.getTypes();
                    getLogger().info("Processed {}", sourceFile);
                    processedThisPass++;
                    // TODO: cleanup
                } catch (SchemaParseException ex) {
                    if (ex.getMessage().matches("(?i).*(undefined name|not a defined name).*")) {
                        getLogger().debug("Found undefined name in {}; will try again later", sourceFile);
                        nextPass.add(sourceFile);
                    } else {
                        throw new GradleException(String.format("Failed to compile schema definition file %s", sourceFile), ex);
                    }
                } catch (NullPointerException ex) {
                    getLogger().debug("Encountered null reference while parsing {} (possibly due to unresolved dependency); will try again later", sourceFile);
                    nextPass.add(sourceFile);
                } catch (IOException ex) {
                    throw new GradleException(String.format("Failed to compile schema definition file %s", sourceFile), ex);
                }
                sourceFile = thisPass.poll();
            }
        }
        if (!nextPass.isEmpty()) {
            throw new GradleException(String.format("Failed to compile schema definition files due to undefined names: %s", nextPass));
        }
        return new HashSet<>(types.values());
    }
}
