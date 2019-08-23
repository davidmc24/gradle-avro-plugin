package com.commercehub.avro.depresolver.avro190;

import com.commercehub.avro.depresolver.AvroSchemaParseException;
import com.commercehub.avro.depresolver.DuplicateAvroTypeException;
import com.commercehub.avro.depresolver.NullPointerAvroException;
import com.commercehub.avro.depresolver.SchemaParserWrapper;
import com.commercehub.avro.depresolver.SchemaWrapper;
import com.commercehub.avro.depresolver.UnknownAvroTypeException;
import com.commercehub.avro.depresolver.WrapperFactory;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SchemaParserWrapperImpl extends SchemaParserWrapper<Schema> {
    private static Pattern ERROR_UNKNOWN_TYPE = Pattern.compile("(?i).*(undefined name|not a defined name).*");
    private static Pattern ERROR_DUPLICATE_TYPE = Pattern.compile("Can't redefine: (.*)");

    private final Schema.Parser parser = new Schema.Parser();

    SchemaParserWrapperImpl(WrapperFactory<Schema> wrapperFactory) {
        super(wrapperFactory);
    }

    @Override
    protected void addTypes(Map<String, SchemaWrapper<Schema>> types) {
        parser.addTypes(unwrapMap(types));
    }

    @Override
    protected SchemaWrapper<Schema> parse(File sourceFile) throws AvroSchemaParseException {
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

    @Override
    protected Map<String, SchemaWrapper<Schema>> getTypes() {
        return wrapMap(parser.getTypes());
    }
}
