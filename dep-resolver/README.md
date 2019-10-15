Version compatibility notes:

* Avro 1.0.x-Avro 1.3.x don't have published Java libraries
* Avro 1.4.x-1.5.2 supports parsing schema, but doesn't expose an API for registering type names, making resolution impossible
* Avro 1.5.3 introduced `Schema.Parser`