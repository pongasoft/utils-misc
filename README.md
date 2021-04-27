Introduction
============
The project represents a set of miscellaneous utility classes that are used by other open
source projects.

Compilation
===========
In order to compile the code you need

* java 1.8+

At the top simply run

    ./gradlew test

which should compile and run all the tests.

IDE Support
===========
You can issue the command (at the top)

    ./gradlew cleanIdea idea

which will use the gradle IDEA plugin to create the right set of modules in order to open the
project in IntelliJ IDEA.

Directory structure
===================

* `org.linkedin.util-core`
  * Contains a set of java utilities with no external dependencies (except slf4j
for logging).

* `org.linkedin.util-groovy`
  * Contains a set of groovy utilities

Build configuration
===================
The project uses the [`org.linkedin.userConfig`](https://github.com/pongasoft/gradle-plugins/blob/master/README.md) plugin and as such can be configured

    Example:
    ~/.userConfig.properties
    top.build.dir="/Volumes/Disk2/deployment/${userConfig.project.name}"
    top.install.dir="/export/content/${userConfig.project.name}"
    top.release.dir="/export/content/repositories/release"
    top.publish.dir="/export/content/repositories/publish"