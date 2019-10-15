/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.avro.depresolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * General file manipulation utilities.
 */
class FileUtils {
    /**
     * Writes a file in a manner appropriate for a JSON file.  UTF-8 will be used, as it is the default encoding for JSON, and should be
     * maximally interoperable.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7159#section-8.1">JSON Character Encoding</a>
     */
    static void writeJsonFile(File file, String data) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        try (OutputStream outputStream = new FileOutputStream(file)) {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                if (data != null) {
                    writer.write(data);
                }
            }
        }
    }
}
