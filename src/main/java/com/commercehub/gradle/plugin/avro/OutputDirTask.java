package com.commercehub.gradle.plugin.avro;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.ArrayList;

import static com.commercehub.gradle.plugin.avro.Constants.JAR_EXTENSION;
import static com.commercehub.gradle.plugin.avro.Constants.ZIP_EXTENSION;

class OutputDirTask extends SourceTask {
    private File outputDir;
    protected final PatternFilterable archivePattern = new PatternSet()
        .include("*." + ZIP_EXTENSION)
        .include("*." + JAR_EXTENSION);

    public void setOutputDir(Object outputDir) {
        if (outputDir != null) {
            this.outputDir = getProject().file(outputDir);
            getOutputs().dir(this.outputDir);
        }
    }

    protected File getOutputDir() {
        return outputDir;
    }

    @Override
    public FileTree getSource() {
        FileTree src = getProject().files(new ArrayList<>(this.source)).getAsFileTree();
        FileTree filteredSrc = super.getSource();
        PatternSet patternSet = new PatternSet().setIncludes(getIncludes()).setExcludes(getExcludes());
        // Search inside any top-level archives, such as those appearing in the avro configuration
        for (File archiveFile : src.matching(archivePattern)) {
            FileTree zipTree = getProject().zipTree(archiveFile).getAsFileTree();
            // we really ought to be able to just pass 'this', but code in PatternSet.copyFrom wants to downcast
            // its PatternFilterable argument to a PatternSet for no reason!
            filteredSrc = filteredSrc.plus(zipTree.matching(patternSet));
        }
        return filteredSrc;
    }

    protected FileCollection filterSources(Spec<? super File> spec) {
        return getInputs().getSourceFiles().filter(spec);
    }
}
