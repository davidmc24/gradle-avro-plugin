package com.commercehub.avro.depresolver;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SchemaParserWrapper<T> {
    private final WrapperFactory<T> wrapperFactory;

    public SchemaParserWrapper(WrapperFactory<T> wrapperFactory) {
        this.wrapperFactory = wrapperFactory;
    }

    protected SchemaWrapper<T> wrap(T schema) {
        return wrapperFactory.createSchemaWrapper(schema);
    }

    protected Map<String, T> unwrapMap(Map<String, SchemaWrapper<T>> wrappedMap) {
        return wrappedMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().unwrap()
        ));
    }

    protected Map<String, SchemaWrapper<T>> wrapMap(Map<String, T> unwrappedMap) {
        return unwrappedMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> wrap(entry.getValue())
        ));
    }

    protected abstract void addTypes(Map<String, SchemaWrapper<T>> parserTypes);
    protected abstract SchemaWrapper<T> parse(File sourceFile) throws AvroSchemaParseException;
    protected abstract Map<String, SchemaWrapper<T>> getTypes();
}
