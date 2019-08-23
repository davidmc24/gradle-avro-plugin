package com.commercehub.avro.depresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public abstract class BaseTool {
    private static final Logger logger = LoggerFactory.getLogger(BaseTool.class);

    private final File destinationDir;
    private final List<File> sourceFiles;

    protected BaseTool(String... args) {
        // TODO: argument processing
        this.destinationDir = new File(args[0]);
        this.sourceFiles = new ArrayList<>();
        for (int i=1; i<args.length; i++) {
            sourceFiles.add(new File(args[i]));
        }
    }

    protected <T> void run(WrapperFactory<T> wrapperFactory) {
        try {
            DependencyResolver dependencyResolver = new DependencyResolver();
            DependencyResolutionResult<T> result = dependencyResolver.resolveSchemas(wrapperFactory, sourceFiles);
            SortedMap<File, String> fileErrors = result.getFileErrors();
            if (!fileErrors.isEmpty()) {
                fileErrors.forEach((File file, String errorMessage) -> logger.error("Failed to resolve schema file '{}'; {}", file, errorMessage));
                System.exit(1);
            }
            for (SchemaWrapper<T> schema : result.getSchemas()) {
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

    private static String determineDestinationFileName(SchemaWrapper schema) {
        return schema.getFullName().replaceAll("\\.", "/") + ".avsc";
    }
}
