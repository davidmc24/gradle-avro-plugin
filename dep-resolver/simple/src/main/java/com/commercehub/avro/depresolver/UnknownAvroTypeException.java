package com.commercehub.avro.depresolver;

class UnknownAvroTypeException extends AvroSchemaParseException {
    UnknownAvroTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
