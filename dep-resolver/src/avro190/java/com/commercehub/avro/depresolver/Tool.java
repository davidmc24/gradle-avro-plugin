package com.commercehub.avro.depresolver;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Tool {
    private static final Logger logger = LoggerFactory.getLogger(Tool.class);

    public static void main(String... args) {
        // TODO: argument processing
        File destinationDir = new File(args[0]);
        List<File> sourceFiles = new ArrayList<>();
        for (int i=1; i<args.length; i++) {
            sourceFiles.add(new File(args[i]));
        }
        try {
            DependencyResolver task = new DependencyResolver();
            DependencyResolutionResult result = task.resolveSchemas(sourceFiles);
            SortedMap<File, String> fileErrors = result.getFileErrors();
            if (!fileErrors.isEmpty()) {
                fileErrors.forEach((File file, String errorMessage) -> {
                    logger.error("Failed to resolve schema file '{}'; {}", file, errorMessage);
                });
                System.exit(1);
            }
            for (Schema schema : result.getSchemas()) {
                File destinationFile = new File(destinationDir, determineDestinationFileName(schema));
                try {
                    FileUtils.writeJsonFile(destinationFile, schema.toString(true));
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

    private static String determineDestinationFileName(Schema schema) {
        return schema.getFullName().replaceAll("\\.", "/") + ".avsc";
    }
}
