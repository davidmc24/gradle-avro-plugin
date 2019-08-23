package com.commercehub.avro.depresolver;

public class UnknownAvroTypeException extends AvroSchemaParseException {
    public UnknownAvroTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
