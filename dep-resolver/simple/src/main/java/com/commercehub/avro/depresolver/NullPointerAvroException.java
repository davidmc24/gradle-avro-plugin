package com.commercehub.avro.depresolver;

class NullPointerAvroException extends AvroSchemaParseException {
    NullPointerAvroException(String message, Throwable cause) {
        super(message, cause);
    }
}
