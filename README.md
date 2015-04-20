# Overview

This is a [Gradle](http://www.gradle.org/) plugin to allow easily performing Java code generation for [Apache Avro](http://avro.apache.org/).  It supports JSON schema declaration files, JSON protocol declaration files, and Avro IDL files.

[![Build Status](https://travis-ci.org/commercehub-oss/gradle-avro-plugin.svg?branch=master)](https://travis-ci.org/commercehub-oss/gradle-avro-plugin)

# Usage

Add the following to your `build.gradle` file.  Substitute the desired version based on [CHANGES.md](https://github.com/commercehub-oss/gradle-avro-plugin/blob/master/CHANGES.md).

```groovy
// Gradle 2.1 and later
plugins {
  id "com.commercehub.gradle.plugin.avro" version "VERSION"
}

// Earlier versions of Gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.commercehub.gradle.plugin:gradle-avro-plugin:VERSION"
    }
}
apply plugin: "com.commercehub.gradle.plugin.avro"
```

Optionally, configure the string type to `charSequence`, `string` (the default), or `utf8`.

```groovy
avro {
    stringType = "string"
}
```

Optionally, you can also configure the output character encoding.

```groovy
avro {
    encoding = "UTF-8"
}
```

Additionally, ensure that you have a compile dependency on avro, such as:

```groovy
repositories {
    jcenter()
}
dependencies {
    compile "org.apache.avro:avro:1.7.6"
}
```

If you now run `gradle build`, Java classes will be compiled from Avro files in `src/main/avro`.  Actually, it will attempt to process an "avro" directory in every `SourceSet` (main, test, etc.)

# Depending on external Avro sources
Sometimes it may be desirable to depend on Avro types defined in other projects, such as a shared repository. The plugin provides a mechanism for this via a configuration that is added to the project called `avro`. It can be used as follows:

```groovy
dependencies {
    avro 'com.example:avro-schema-repo:1.0.0'
    avro fileTree('/tmp/avro')
}
```

When the avro configuration is resolved any avro files (*.avsc, *.avdl, *.avpr) will be treated as dependencies to be
processed in order to obtain types to supply to the avro `Schema.Parser`. Additionally if any top level (in the FileTree)
files are archives (*.zip, *.jar) they will be internally searched for avro files. This means that you can package
external avro dependencies in a jar and use the usual `<group:name:version>` notation to specify a dependency.

If you reference a type using fully qualified notation within an Avro schema file (*.avsc) then if that type is defined 
in an avro file in the avro configuration then that type will be available to the Avro compiler when compiling avsc files
local to the project.

## Dependent Avro Protocols
If you wish to define protocols that depend on other Avro files the only way to achieve this is to use the Avro
Interface Definition Language (IDL - file extension avdl)  that provides an import statement. This is because the protocol parser provides
no mechanism for providing pre-processed types. Furthermore the import statement relies on a file path resolved
relative to the location of the avdl file. To achieve this something of a workaround can be used:

```groovy
def collationDir = 'build/avro-collation'

task copyAvroDeps(type: Copy, dependsOn: resolveAvroDependencies) {
  delete collationDir
  from(generateAvroProtocolDependencies.source.files)
  into collationDir
  from 'src/main/avro'
  into collationDir
}

generateAvroProtocol {
  dependsOn copyAvroDeps
  setSource(collationDir)
}

generateAvroJava {
  dependsOn copyAvroDeps, generateAvroProtocol
  setSource(collationDir)
  source generateAvroProtocol.outputDir
}
```

Here we copy all dependent avdl files to a collation directory along with all project-local avro files. If our avdl
files import dependent avdl files by their filename then the generated avpr files will be produced correctly with shared
types collated. Any project-local avsc depending on types from the avro configuration will still compile since the 
Avro schema parser is passed all dependent types before being used to parse project-local files

# IntelliJ Integration

The plugin attempts to make IntelliJ play more smoothly with generated sources when using Gradle-generated project files.  However, there are still some rough edges.  It will work best if you first run `gradle build`, and _after_ that run `gradle idea`.  If you do it in the other order, IntelliJ may not properly exclude some directories within your `build` directory.

# Alternate Usage

If the defaults used by the plugin don't work for you, you can still use the tasks by themselves.  In this case, use the "com.commercehub.gradle.plugin.avro" plugin instead, and create tasks of type `GenerateAvroJavaTask` and/or `GenerateAvroProtocolTask`.

Here's a short example of what this might look like:

```groovy
apply plugin: "java"
apply plugin: "com.commercehub.gradle.plugin.avro-base"

dependencies {
    compile "org.apache.avro:avro:1.7.6"
}

task generateAvro(type: com.commercehub.gradle.plugin.avro.GenerateAvroJavaTask) {
    source("src/avro")
    outputDir = file("dest/avro")
}

compileJava.source(generateAvro.outputs)
```
