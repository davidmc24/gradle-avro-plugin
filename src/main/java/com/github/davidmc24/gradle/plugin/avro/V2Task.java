package com.github.davidmc24.gradle.plugin.avro;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

public abstract class V2Task extends SourceTask {
    @Inject
    protected abstract ExecOperations getExecOperations();

    @InputFiles
    abstract ConfigurableFileCollection getClasspath();

    @OutputDirectory
    abstract DirectoryProperty getDestinationDirectory();

    @TaskAction
    void run() {
        ExecOperations exec = getExecOperations();
        for (File file : getSource().getFiles()) {
            getLogger().info("Processing source file {}", file);
            ExecResult result = exec.javaexec(jvm -> {
                jvm.classpath(getClasspath());
                // TODO: abstract
                jvm.setMain("com.github.davidmc24.avro.supplement.impl.CompileSchemaCommand");
                jvm.args("--file=" + file.getAbsolutePath(), "--output=" + getDestinationDirectory().get().getAsFile().getPath());
            });
            result.rethrowFailure().assertNormalExitValue();
        }
    }
}
