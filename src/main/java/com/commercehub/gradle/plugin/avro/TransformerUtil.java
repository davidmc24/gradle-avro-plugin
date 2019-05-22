/**
 * Copyright © 2019 Commerce Technologies, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.gradle.plugin.avro;

import com.commercehub.avro.tools.api.AvroTransformer;

import java.util.Iterator;
import java.util.ServiceLoader;

class TransformerUtil {
    static AvroTransformer getTransfomer() throws IllegalStateException {
        ServiceLoader<AvroTransformer> serviceLoader = ServiceLoader.load(AvroTransformer.class);
        Iterator<AvroTransformer> iterator = serviceLoader.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("No " + AvroTransformer.class.getName() + " service provider found");
        }
        return iterator.next();
    }
}
