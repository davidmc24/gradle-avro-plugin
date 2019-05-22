package com.commercehub.avro.tools.base;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface AvroProtocolToSchemaTransformer {
    int transform(Collection<File> inputs, File outputDir) throws IOException;
}
