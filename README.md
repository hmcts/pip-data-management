# Publication and Information - Data Management

[![Build Status](https://travis-ci.org/hmcts/spring-boot-template.svg?branch=master)](https://travis-ci.org/hmcts/spring-boot-template)

## Purpose

The purpose of this service is to provide the ability to manage data based on data governance, retention and
security policies. Including providing Usage Data & MI to SDP

## What's inside

The template is a working application with a minimal setup. It contains:
 * common plugins and libraries
 * docker setup
 * swagger configuration for api documentation ([see how to publish your api documentation to shared repository](https://github.com/hmcts/reform-api-docs#publish-swagger-docs))
 * code quality tools already set up
 * Hystrix circuit breaker enabled
 * MIT license and contribution information.
 * Helm chart using chart-java.

The application exposes health endpoint (http://localhost:8090/health) and metrics endpoint
(http://localhost:8090/metrics).

## Plugins

The template contains the following plugins:

  * checkstyle

    https://docs.gradle.org/current/userguide/checkstyle_plugin.html

    Performs code style checks on Java source files using Checkstyle and generates reports from these checks.
    The checks are included in gradle's *check* task (you can run them by executing `./gradlew check` command).

  * pmd

    https://docs.gradle.org/current/userguide/pmd_plugin.html

    Performs static code analysis to finds common programming flaws. Included in gradle `check` task.


  * jacoco

    https://docs.gradle.org/current/userguide/jacoco_plugin.html

    Provides code coverage metrics for Java code via integration with JaCoCo.
    You can create the report by running the following command:

    ```bash
      ./gradlew jacocoTestReport
    ```

    The report will be created in build/reports subdirectory in your project directory.

  * io.spring.dependency-management

    https://github.com/spring-gradle-plugins/dependency-management-plugin

    Provides Maven-like dependency management. Allows you to declare dependency management
    using `dependency 'groupId:artifactId:version'`
    or `dependency group:'group', name:'name', version:version'`.

  * org.springframework.boot

    http://projects.spring.io/spring-boot/

    Reduces the amount of work needed to create a Spring application

  * org.owasp.dependencycheck

    https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html

    Provides monitoring of the project's dependent libraries and creating a report
    of known vulnerable components that are included in the build. To run it
    execute `gradle dependencyCheck` command.

  * com.github.ben-manes.versions

    https://github.com/ben-manes/gradle-versions-plugin

    Provides a task to determine which dependencies have updates. Usage:

    ```bash
      ./gradlew dependencyUpdates -Drevision=release
    ```


## Notes

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.

JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Integration Testing

To run integration tests (After PUB-1000 is merged), [Docker](https://www.docker.com) is now required to run integration tests.
This is due to the use of [TestContainers](https://www.testcontainers.org) to stand up a local Postgres instance for testing.
Previously we used H2 for this purpose, which did not require docker.
You do not have to install any particular containers manually, as testcontainers will do all the necessary work for you, as long as docker is running.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Environment Variables

The following environment variables are required to run the application

Name | Value
--- | ---
CONNECTION_STRING | This is used to communicate with Azure
DB_USER | The username to connect to the postgres DB
DB_PASS | The password to connect to the postgres DB
DB_PORT | The port to connect to the postgres DB
DB_NAME | (Azure Only) The name of the postgres DB to connect to
SPRING_PROFILES_ACTIVE | Set this to 'dev' in dev mode. This will automatically connect to the local docker image

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/spring-boot-template` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8090` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8090/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Hystrix

[Hystrix](https://github.com/Netflix/Hystrix/wiki) is a library that helps you control the interactions
between your application and other services by adding latency tolerance and fault tolerance logic. It does this
by isolating points of access between the services, stopping cascading failures across them,
and providing fallback options. We recommend you to use Hystrix in your application if it calls any services.

### Hystrix circuit breaker

This template API has [Hystrix Circuit Breaker](https://github.com/Netflix/Hystrix/wiki/How-it-Works#circuit-breaker)
already enabled. It monitors and manages all the`@HystrixCommand` or `HystrixObservableCommand` annotated methods
inside `@Component` or `@Service` annotated classes.

### Other

Hystrix offers much more than Circuit Breaker pattern implementation or command monitoring.
Here are some other functionalities it provides:
 * [Separate, per-dependency thread pools](https://github.com/Netflix/Hystrix/wiki/How-it-Works#isolation)
 * [Semaphores](https://github.com/Netflix/Hystrix/wiki/How-it-Works#semaphores), which you can use to limit
 the number of concurrent calls to any given dependency
 * [Request caching](https://github.com/Netflix/Hystrix/wiki/How-it-Works#request-caching), allowing
 different code paths to execute Hystrix Commands without worrying about duplicating work

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

# API

Numerous apis are set up to handle the data passing through P&I and will be built upon as the project progresses.

 - [Retrieving Courts and Hearings (Deprecating)](#retrieving-courts-and-hearings)
 - [Retrieving and uploading artefacts](#uploading-and-retrieving-artefacts)

## Retrieving Courts and Hearings
Api set up under the url `{root-url}/courts` to retrieve courts with their assosiated hearings, or `{root-url}
/hearings`. The /courts and /hearings API's are on their way to deprecation to be replaced by [publications](#uploading-and-retrieving-artefacts)
to get purely hearings

 - /courts - Will return an array of all courts
 - /courts/{courtId} - Will search courts for matching court Id and return full object
 - /courts/find/{courtName} - will search all courts for a matching court with that name and return its full court
   object
 - /courts/filter - Takes in a body of an array of `filters` and `values` to search the court list for and return
   matching courts that satisfy all params provided, can return empty list if none match

example filter request:
```
{
  "filters": ["location", "jurisdiction"],
  "values": ["london", "manchester", "crown court"]
}
```

- /hearings/{courtId} - returns all hearings for particular court id
- /hearings/case-name/{caseName} returns all matched and partial match hearings
- /hearings/case-number/{caseNumber} returns single hearing for matched case number
- /hearings/urn/{urnNumber} returns single hearing for matched urn number
NOTE: searching or filtering is not case-sensitive but requires exact match otherwise.

## Uploading and retrieving artefacts
Artefacts are created by uploading blobs of raw data or flat files.
These are defined by the [schemas](src/main/resources/schemas).

- POST `/publication` used to upload a blob and create an artefact in the P&I database, must include series of
  headers defined in the [upload headers section](#upload-headers). NOTE: providing the content type as json and
  including raw json in the body will upload blob as raw data to be processed by P&I, providing the content type as
  multipart form data and uploading a file as the body will upload a flat file to the artefact where no P&I
  processing will be done on the contents of the file.

- GET `/publication/courtId/{courtId}` used to get a series of publications matching the courtId.
- GET `publication/search/{searchTerm}/{searchValue}` used to get a series of publications matching a given case
  search value, eg. (CASE_URN/CASE_ID/CASE_NAME)

- GET `/publication/{artefactId}` used to get the metadata for the artefact.
- GET `/publication/{artefactId}/payload` used to get the payload for the artefact.
- GET `/publication/{artefactId}/file` used to get payload file for the artefact.
- DELETE  `/publication/{artefactId}` used to delete an artefact and its payload from P&I.

## Headers
### Upload headers
headers for uploading an artefact:
```json
{
  "x-provenance":  "String of the provenance the upload is coming from",
  "x-source-artefact-id": "String of the artefact id as labelled in the source system",
  "x-type":  "ENUM of the type of artefact",
  "x-sensitivity": "ENUM of the sensitvity of the artefact",
  "x-language":  "ENUM of the language the data is in",
  "x-display-from": "Local date of when the list can be displayed from",
  "x-display-to":  "Local date of when the list should be displayed to",
  "x-list-type": "ENUM of the different list types available",
  "x-court-id":  "String of the court id the list is linked to",
  "x-content-date": "Local date of when the earliest case in the list refers to"
}
```
