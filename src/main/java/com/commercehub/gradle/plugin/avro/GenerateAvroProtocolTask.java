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

import com.commercehub.avro.tools.api.Transformation;
import com.commercehub.avro.tools.impl.TransformationImpl;
import org.apache.avro.compiler.idl.Idl;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Task to convert Avro IDL files into Avro protocol files using {@link Idl}.
 */
@CacheableTask
public class GenerateAvroProtocolTask extends OutputDirTask {
    @TaskAction
    protected void process() {
        try {
            Transformation transformation = new TransformationImpl();
            Collection<File> inputs = getSource().getFiles();
            File outputDir = getOutputDir();
            ClassLoader resourceLoader = getRuntimeClassLoader(getProject());
            int processedFileCount = transformation.generateProtocolFiles(inputs, outputDir, resourceLoader);
            setDidWork(processedFileCount > 0);
        } catch (IOException ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }

    private ClassLoader getRuntimeClassLoader(Project project) {
        List<URL> urls = new LinkedList<>();
        String configurationName = getRuntimeConfigurationName();
        try {
            Configuration configuration = project.getConfigurations().getByName(configurationName);
            for (File file : configuration) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    getLogger().debug(e.getMessage());
                }
            }
        } catch (UnknownConfigurationException ex) {
            getLogger().debug("No configuration found with name {}; defaulting to system classloader", configurationName);
        }
        return urls.isEmpty() ? ClassLoader.getSystemClassLoader()
                : new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
    }

    /**
     * Backwards-compatible logic to return the appropriate configuration name for resolving the runtime classpath
     */
    private static String getRuntimeConfigurationName() {
        return GradleVersion.current().compareTo(GradleVersion.version("3.5")) >= 0
            ? Constants.RUNTIME_CLASSPATH_CONFIGURATION_NAME : Constants.RUNTIME_CONFIGURATION_NAME;
    }
}
