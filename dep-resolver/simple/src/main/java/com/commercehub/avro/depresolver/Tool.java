package com.commercehub.avro.depresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class Tool {
    private static final Logger logger = LoggerFactory.getLogger(Tool.class);
    private final File destinationDir;
    private final List<File> sourceFiles;

    private Tool(String... args) {
        // TODO: argument processing
        this.destinationDir = new File(args[0]);
        this.sourceFiles = new ArrayList<>();
        for (int i = 1; i< args.length; i++) {
            Tool.this.sourceFiles.add(new File(args[i]));
        }
    }

    public static void main(String... args) {
        new Tool(args).run();
    }

    private static String determineDestinationFileName(SchemaWrapperImpl schema) {
        return schema.getFullName().replaceAll("\\.", "/") + ".avsc";
    }

    private void run() {
        try {
            DependencyResolver dependencyResolver = new DependencyResolver();
            DependencyResolutionResult result = dependencyResolver.resolveSchemas(sourceFiles);
            SortedMap<File, String> fileErrors = result.getFileErrors();
            if (!fileErrors.isEmpty()) {
                fileErrors.forEach((File file, String errorMessage) -> logger.error("Failed to resolve schema file '{}'; {}", file, errorMessage));
                System.exit(1);
            }
            for (SchemaWrapperImpl schema : result.getSchemas()) {
                File destinationFile = new File(destinationDir, determineDestinationFileName(schema));
                try {
                    FileUtils.writeJsonFile(destinationFile, schema.toJson());
                    logger.info("Wrote schema file '{}'", destinationFile);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write file '" + destinationFile + "'", ex);
                }
            }
            System.exit(0);
        } catch (Exception ex) {
            logger.error("Encountered error", ex);
            System.exit(2);
        }
    }
}
