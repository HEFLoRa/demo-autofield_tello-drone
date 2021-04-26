# Tello

[![pipeline status](https://gitlab.lrz.de/digital-agriculture/tello/badges/main/pipeline.svg)](https://gitlab.lrz.de/digital-agriculture/tello/-/commits/main)
[![docs](https://img.shields.io/badge/docs-javadoc-blue)](https://digital-agriculture.pages.gitlab.lrz.de/tello/)
[![coverage report](https://gitlab.lrz.de/digital-agriculture/tello/badges/main/coverage.svg)](https://gitlab.lrz.de/digital-agriculture/tello/-/commits/main)

Code to control the DJI Tello drone of the IDP.

## Requirements
- [JDK Version 16](https://jdk.java.net/16/)
- [Maven](https://maven.apache.org/)

You will need to create a `toolchains.xml` in `$HOME/.m2` (Linux) or your maven conf path with the following content:
```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
    <!-- JDK toolchains -->
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>16</version>
            <vendor>${YOUR VENDOR HERE}</vendor>
        </provides>
        <configuration>
            <jdkHome>${YOUR JAVA PATH HERE}</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```
It should contain the path to the JDK 16.

## Test
To run tests execute the following command in the project's root directory:
```bash
mvn test 
```

## Build
To build a `jar` run in the project's root directory:
```bash
mvn package
```
`tello-${project.version}.jar` will be created in the `target` directory. The `jar` will be executable with:
```bash
java -jar tello-${project.version}.jar 
```

## Run
To run the application execute the following command in the project's root directory:
```bash
mvn compile exec:exec
```
