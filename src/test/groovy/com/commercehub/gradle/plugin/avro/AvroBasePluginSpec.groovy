/*
 * Copyright © 2021 David M. Carr
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
package com.commercehub.gradle.plugin.avro

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.LogEvent
import org.gradle.internal.logging.events.OutputEvent
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.logging.slf4j.OutputEventListenerBackedLoggerContext
import org.gradle.testfixtures.ProjectBuilder
import org.slf4j.LoggerFactory
import spock.lang.Specification

class AvroBasePluginSpec extends Specification {
    private static final Date BEFORE_VERSION_CUTOFF = new Date(0L)
    private static final Date AFTER_VERSION_CUTOFF = new Date(Long.MAX_VALUE)

    Project project = ProjectBuilder.builder().build()
    OutputEventListenerBackedLoggerContext loggerContext = LoggerFactory.ILoggerFactory as OutputEventListenerBackedLoggerContext
    OutputEventListener outputEventListener = Mock(OutputEventListener)

    def setup() {
        loggerContext.outputEventListener = outputEventListener
    }

    def cleanup() {
        loggerContext.reset()
    }

    def "Before version cutoff date, warns about upcoming cutoff"() {
        given:
        def plugin = new AvroBasePlugin()
        plugin.now = BEFORE_VERSION_CUTOFF

        when:
        plugin.apply(project)

        then:
        noExceptionThrown()
        1 * outputEventListener.onOutput({ it ->
            it instanceof LogEvent &&
                it.logLevel == LogLevel.WARN &&
                it.message ==
                "This version of gradle-avro-plugin is obsolete and will stop working on ${AvroBasePlugin.VERSION_CUTOFF_DATE}. " +
                "Please upgrade to a newer version from ${AvroBasePlugin.PROJECT_URL} at your earliest opportunity."
        } as OutputEvent)
    }

    def "After version cutoff date, throws exception"() {
        given:
        def plugin = new AvroBasePlugin()
        plugin.now = AFTER_VERSION_CUTOFF

        when:
        plugin.apply(project)

        then:
        GradleException ex = thrown(GradleException)
        ex.message == "This version of gradle-avro-plugin is obsolete and will no longer function. " +
            "Please upgrade to a newer version from ${AvroBasePlugin.PROJECT_URL} at your earliest opportunity."
    }
}
