# Hibernate Query Parser

Experimental new parser for HQL and JP-QL queries, to convert these into SQL and other different targets such as Lucene queries,
Map/Reduce queries for NoSQL stores, make it possible to perform more sophisticated SQL transformations.

### Help

There is much to do and many tests to be ported from the existing parser or added from scratch; anyone is welcome to help
by sending pull requests, commenting the code on github directly or by by using the mailing list or IRC chat channels:

* [Mailing list](http://hibernate.org/community/mailinglists)
* [IRC](http://hibernate.org/community/irc)

### Bug Reports:

* [Hibernate JIRA](https://hibernate.onjira.com) (preferred)
* [Mailing list](http://hibernate.org/community/mailinglists)

### Building

This project used Gradle as build tool. To build the complete project, run the following command from the directory containing
this README file:

    ./gradlew clean build

You don't need to have Gradle installed; When executed for the first time, _gradlew_ (the "Gradle wrapper") will download Gradle
in the required version.

In order to install the JARs created by this project into your local Maven repository, run the following command:

    ./gradlew clean install

To deploy the modules to the JBoss Maven repository, run:

    ./gradlew clean publish

Depending on the version of the project (as given via `ext.projectVersion`) this will perform a deployment either into the JBoss
snapshot repository or the JBoss release staging repository (both requires your Nexus credentials to be configured in
_~/.m2/settings.xml_).

### Releasing

Perform the following steps to do a release of this project:

* delete any stale files and directories from your working copy: `git clean -d -x -f`
* verify that everything builds as expected: `./gradlew clean build`
* set project version in _build.gradle_ (property _ext.projectVersion_) to the release version, e.g. "1.0.0.Alpha3" and commit
* tag the release in Git: `git tag -a 1.0.0.Alpha3 -m 'my version 1.0.0.Alpha3'`
* perform the release: `./gradlew clean publish`
* examine the staged release on the JBoss Nexus server; either close and release or drop the staging repository
* set project version in _build.gradle_ to the next development version, e.g. "1.0.0-SNAPSHOT" and commit
* push the `master` branches and the new tag to the upstream repo
* release the version in JIRA and transition all involved issues to "Closed"

### Source and credits

The grammar and parser code is based on the _antlr3_ branch previously developed for Hibernate 3; the original code by
Steve Ebersole and Alexandre Porcelli can be found on the
[archived Hibernate subversion repository](http://anonsvn.jboss.org/repos/hibernate/core/branches/antlr3).

## License

This software and its documentation are distributed under the terms of the FSF Lesser GNU Public License (see license.txt).
