package com.commercehub.avro.depresolver;

import java.io.File;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

class DependencyResolutionResult<T> {
    private SortedSet<SchemaWrapper<T>> schemas = new TreeSet<>();
    private SortedMap<File, String> fileErrors = new TreeMap<>();

    void addSchema(SchemaWrapper<T> schema) {
        schemas.add(schema);
    }

    void addFileError(File file, String errorMessage) {
        fileErrors.put(file, errorMessage);
    }

    SortedSet<SchemaWrapper<T>> getSchemas() {
        return Collections.unmodifiableSortedSet(schemas);
    }

    SortedMap<File, String> getFileErrors() {
        return Collections.unmodifiableSortedMap(fileErrors);
    }
}
