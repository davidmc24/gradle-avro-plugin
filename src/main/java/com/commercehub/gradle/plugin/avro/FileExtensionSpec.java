/**
 * Copyright © 2013-2015 Commerce Technologies, LLC.
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

import org.gradle.api.specs.Spec;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

class FileExtensionSpec implements Spec<File> {
    private final boolean contains;
    private final Set<String> extensions;

    FileExtensionSpec(boolean contains, Collection<String> extensions) {
        this.contains = contains;
        this.extensions = new LinkedHashSet<String>(extensions);
    }


    FileExtensionSpec(boolean contains, String... extensions) {
        this(contains, Arrays.asList(extensions));
    }


    FileExtensionSpec(String... extensions) {
        this(true, extensions);
    }

    @Override
    public boolean isSatisfiedBy(File file) {
        return contains == extensions.contains(FilenameUtils.getExtension(file.getName()));
    }
}
