package com.commercehub.avro.depresolver;

import org.apache.avro.Schema;

class SchemaWrapperImpl implements Comparable<SchemaWrapperImpl> {
    private final Schema schema;

    SchemaWrapperImpl(Schema schema) {
        this.schema = schema;
    }

    String getFullName() {
        return schema.getFullName();
    }

    String toJson() {
        return schema.toString(true);
    }

    Schema unwrap() {
        return schema;
    }

    @Override
    public int compareTo(SchemaWrapperImpl o) {
        return getFullName().compareTo(o.getFullName());
    }

    @Override
    public int hashCode() {
        return unwrap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SchemaWrapperImpl && unwrap().equals(((SchemaWrapperImpl)obj).unwrap());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
