package com.commercehub.avro.depresolver;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class SchemaParserWrapperImpl {
    private static Pattern ERROR_UNKNOWN_TYPE = Pattern.compile("(?i).*(undefined name|not a defined name).*");
    private static Pattern ERROR_DUPLICATE_TYPE = Pattern.compile("Can't redefine: (.*)");

    private final Schema.Parser parser = new Schema.Parser();

    void addTypes(Map<String, SchemaWrapperImpl> types) {
        parser.addTypes(unwrapMap(types));
    }

    SchemaWrapperImpl parse(File sourceFile) throws AvroSchemaParseException {
        try {
            return wrap(parser.parse(sourceFile));
        } catch (SchemaParseException ex) {
            String errorMessage = ex.getMessage();
            Matcher unknownTypeMatcher = ERROR_UNKNOWN_TYPE.matcher(errorMessage);
            Matcher duplicateTypeMatcher = ERROR_DUPLICATE_TYPE.matcher(errorMessage);
            if (unknownTypeMatcher.matches()) {
                throw new UnknownAvroTypeException(String.format("Found undefined name in %s (%s))", sourceFile, errorMessage), ex);
            } else if (duplicateTypeMatcher.matches()) {
                String typeName = duplicateTypeMatcher.group(1);
                throw new DuplicateAvroTypeException(String.format("Identified duplicate type %s in %s", typeName, sourceFile), ex, typeName);
            } else {
                throw new AvroSchemaParseException(String.format("Failed to parse schema definition file %s", sourceFile), ex);
            }
        } catch (NullPointerException ex) {
            throw new NullPointerAvroException(String.format("Encountered null reference while parsing %s (possibly due to unresolved dependency)", sourceFile), ex);
        } catch (IOException ex) {
            throw new AvroSchemaParseException(String.format("Failed to compile schema definition file %s", sourceFile), ex);
        }
    }

    Map<String, SchemaWrapperImpl> getTypes() {
        return wrapMap(parser.getTypes());
    }

    private SchemaWrapperImpl wrap(Schema schema) {
        return new SchemaWrapperImpl(schema);
    }

    private Map<String, Schema> unwrapMap(Map<String, SchemaWrapperImpl> wrappedMap) {
        return wrappedMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().unwrap()
        ));
    }

    private Map<String, SchemaWrapperImpl> wrapMap(Map<String, Schema> unwrappedMap) {
        return unwrappedMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> wrap(entry.getValue())
        ));
    }
}
