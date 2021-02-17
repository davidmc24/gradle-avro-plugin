package com.github.davidmc24.gradle.plugin.avro;

import com.google.common.io.Resources;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class V2TaskTest {
    @TempDir
    Path tempDir;

    @Test
    void supportsVersionArgument() throws Exception {
        String buildFileContent = """
            plugins { id "com.github.davidmc24.gradle.plugin.avro-base" }
            repositories {
                mavenCentral()
                maven { url = "https://oss.sonatype.org/content/repositories/snapshots/" }
            }
            
            configurations {
                avroSupplement
            }
            
            dependencies {
                avroSupplement "com.github.davidmc24.avro.supplement:1.10.1:0.0.1-SNAPSHOT"
            }
            
            tasks.register("v2", com.github.davidmc24.gradle.plugin.avro.V2Task) {
                classpath.from(configurations.avroSupplement)
                source("src/main/avro")
                include("**/*.avsc")
                destinationDirectory.set(file("build/generated-main-avro-java"))
            }
            """.stripIndent();
        String expectedJava = "moo"; // TODO: extract

        Path testProjectDir = tempDir.resolve("project");
        Path buildFile = testProjectDir.resolve("build.gradle");
        Path avroDir = testProjectDir.resolve("src").resolve("main").resolve("avro");
        Path avroFile = avroDir.resolve("Cat.avsc");
        Path javaFile = testProjectDir.resolve("build/generated-main-avro-java/example/Cat.java");
        Files.createDirectories(testProjectDir);
        Files.writeString(buildFile, buildFileContent, StandardCharsets.UTF_8);
        Files.createDirectories(avroDir);
        Files.write(avroFile, Resources.toByteArray(Resources.getResource("examples/inline/Cat.avsc")));

        String gradleVersion = "6.8.2"; // TODO: parameterize
        List<String> args = List.of(
//            "--info",
//            "--warning-mode", "all",
//            "--refresh-dependencies",
//            "dependencies",
            "v2");
        GradleRunner runner = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withArguments(args);
        BuildResult result = runner.build();
        System.out.println("Build output:\n" + result.getOutput());
        String actualJava = Files.readString(javaFile, StandardCharsets.UTF_8);
        Assertions.assertEquals(expectedJava, actualJava);
    }
}
