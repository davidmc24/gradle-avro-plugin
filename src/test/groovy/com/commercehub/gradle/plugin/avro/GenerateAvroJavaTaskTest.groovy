package com.commercehub.gradle.plugin.avro

import com.google.common.io.Files
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

class GenerateAvroJavaTaskTest {
    def project
    def projectDir

    @Before
    public void setUpProject() {
        projectDir = Files.createTempDir()
        project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .build()
        project.plugins.apply(AvroPlugin.class)
    }

    @Test
    public void testDependencies() {
        def dependencyTask = getJavaTask("dependencyTask",
                getClass().getResource("/avro-dep"))
        def dependentTask = getJavaTask("dependentTask",
                getClass().getResource("/avro/test-dependent.avsc"),
                'src')
        dependencyTask.process()
        dependentTask.addTypes(dependencyTask.parsedTypes)
        // this will throw an exception if the TestDependency schema has not been parsed
        dependentTask.process()
    }

    @Test
    public void testProtocolDeps() {
        def protoTask = getProtoTask("protoTask",
                getClass().getResource("/avro-dep"),
                project.file('proto'))
        protoTask.process()

        def dependentTask = getJavaTask("dependentTask",
                [getClass().getResource("/avro/test-dependent-on-proto.avsc"),
                 project.file('proto')],
                'avro')
        dependentTask.process()
    }

    GenerateAvroProtocolTask getProtoTask(String name,
                                          source,
                                          dest = null) {
        GenerateAvroProtocolTask task = project.task(name,
                type: GenerateAvroProtocolTask)
        task.source = source
        task.outputDir = dest != null ? project.file(dest) : null
        return task
    }

    GenerateAvroJavaTask getJavaTask(String name,
                                     source,
                                     dest = null) {
        GenerateAvroJavaTask task = project.task(name,
                type: GenerateAvroJavaTask)
        task.source = source
        task.compile = dest != null
        task.outputDir = dest != null ? project.file(dest) : null
        task.stringType = 'string'
        return task
    }

    @After
    public void tearDownProject() {
        Files.fileTreeTraverser().postOrderTraversal(projectDir).each {
            it.delete();
        }
        projectDir.delete()
    }
}
