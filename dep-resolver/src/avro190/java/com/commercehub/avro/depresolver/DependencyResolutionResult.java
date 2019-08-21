package com.commercehub.avro.depresolver;

import org.apache.avro.Schema;

import java.io.File;
import java.util.*;

class DependencyResolutionResult {
    private SortedSet<Schema> schemas = new TreeSet<>(Comparator.comparing(Schema::getFullName));
    private SortedMap<File, String> fileErrors = new TreeMap<>();

    void addSchema(Schema schema) {
        schemas.add(schema);
    }

    void addFileError(File file, String errorMessage) {
        fileErrors.put(file, errorMessage);
    }

    SortedSet<Schema> getSchemas() {
        return Collections.unmodifiableSortedSet(schemas);
    }

    SortedMap<File, String> getFileErrors() {
        return Collections.unmodifiableSortedMap(fileErrors);
    }
}
