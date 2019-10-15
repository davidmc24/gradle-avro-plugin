package com.commercehub.avro.depresolver.avro140;

import com.commercehub.avro.depresolver.BaseTool;

public class Tool extends BaseTool {
    private Tool(String... args) {
        super(args);
    }

    public static void main(String... args) {
        new Tool(args).run(new WrapperFactoryImpl());
    }
}
