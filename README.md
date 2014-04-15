# Spring Web Services
Spring Web Services is a product of the Spring community focused on creating
document-driven Web services. Spring Web Services aims to facilitate
contract-first SOAP service development, allowing for the creation of flexible
web services using one of the many ways to manipulate XML payloads.

## Installation

Releases of Spring Web Services are available for download from Maven Central,
as well as our own repository, http://repo.springsource.org/release.

For Maven users:

    <repository>
        <id>repository.spring.release</id>
        <name>Spring GA Repository</name>
        <url>http://repo.spring.io/release</url>
    </repository>
    ...
    <dependency>
        <groupId>org.springframework.ws</groupId>
        <artifactId>spring-ws-core</artifactId>
        <version>2.1.4</version>
    </dependency>
    
## Snapshots

Nightly snapshots of Spring Web Services are available for download from our
snapshot repository, http://repo.springsource.org/snapshot.

For Maven users:

    <repository>
        <id>repository.spring.snapshot</id>
        <name>Spring Snapshot Repository</name>
        <url>http://repo.spring.io/snapshot</url>
    </repository>
    ...
    <dependency>
        <groupId>org.springframework.ws</groupId>
        <artifactId>spring-ws-core</artifactId>
        <version>2.2.0.BUILD-SNAPSHOT</version>
    </dependency>

## Building from Source

Spring Web Services uses a [Gradle](http://gradle.org)-based build system. In
the instructions below, [`./gradlew`](http://vimeo.com/34436402) is invoked
from the root of the source tree and serves as a cross-platform, self-contained
bootstrap mechanism for the build. The only prerequisites are
[Git](http://help.github.com/set-up-git-redirect) and JDK 1.7+.

### check out sources
`git clone git://github.com/spring-projects/spring-ws.git`

### compile and test, build all jars, distribution zips and docs
`./gradlew build`

### install all spring-\* jars into your local Maven cache
`./gradlew install`

... and discover more commands with `./gradlew tasks`. See also the [Gradle build and release FAQ](https://github.com/spring-projects/spring-framework/wiki/Gradle-build-and-release-FAQ).

## Documentation

See the current
[Javadoc](http://static.springsource.org/spring-ws/docs/current/javadoc-api)
and [reference
docs](http://static.springsource.org/spring-ws/docs/current/spring-ws-reference
).

## Issue Tracking

Spring Web Services uses [JIRA](https://jira.springsource.org/browse/SWS) for issue tracking purposes

## License

Spring Web Services is [Apache 2.0 licensed](http://www.apache.org/licenses/LICENSE-2.0.html).