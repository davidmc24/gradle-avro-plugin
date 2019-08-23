package com.commercehub.avro.depresolver;

public class DuplicateAvroTypeException extends AvroSchemaParseException {
    private final String typeName;

    public DuplicateAvroTypeException(String message, Throwable cause, String typeName) {
        super(message, cause);
        this.typeName = typeName;
    }

    String getTypeName() {
        return typeName;
    }
}
