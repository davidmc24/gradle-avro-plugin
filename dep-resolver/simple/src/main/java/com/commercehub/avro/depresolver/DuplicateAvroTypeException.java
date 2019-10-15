package com.commercehub.avro.depresolver;

class DuplicateAvroTypeException extends AvroSchemaParseException {
    private final String typeName;

    DuplicateAvroTypeException(String message, Throwable cause, String typeName) {
        super(message, cause);
        this.typeName = typeName;
    }

    String getTypeName() {
        return typeName;
    }
}
