package com.commercehub.avro.depresolver;

public abstract class SchemaWrapper<T> implements Comparable<SchemaWrapper<T>> {
    public abstract String getFullName();
    protected abstract String toJson();
    public abstract T unwrap();

    @Override
    public int compareTo(SchemaWrapper<T> o) {
        return getFullName().compareTo(o.getFullName());
    }

    @Override
    public int hashCode() {
        return unwrap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SchemaWrapper && unwrap().equals(((SchemaWrapper)obj).unwrap());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
