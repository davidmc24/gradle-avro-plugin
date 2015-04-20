package com.commercehub.gradle.plugin.avro

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class AvroPluginTest {
    Project project

    @Before
    public void setUpProject() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply(AvroPlugin.class)
    }

    @Test
    public void testPluginWithDependencies() {
        GenerateAvroJavaTask genJavaTask = project.getTasks().findByName("generateAvroJava")
        GenerateAvroProtocolTask genProtoTask = project.getTasks().findByName("generateAvroProtocol")
        project.dependencies {
            avro project.fileTree(resource("/avro-dep"))
        }
        GenerateAvroProtocolTask protoDepTask = project.getTasks().getByName("generateAvroProtocolDependencies")
        GenerateAvroJavaTask javaDepTask = project.getTasks().getByName("resolveAvroDependencies")
        protoDepTask.execute()
        javaDepTask.execute()
        genJavaTask.source = resource("/avro")
        genProtoTask.execute()
        genJavaTask.execute()
    }

    private String resource(String name) {
        getClass().getResource(name)
    }
}
