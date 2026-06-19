[![Build Status](https://github.com/openmrs/openmrs-module-webservices.rest/actions/workflows/maven.yml/badge.svg)](https://github.com/openmrs/openmrs-module-webservices.rest/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/openmrs/openmrs-module-webservices.rest/badge.svg?branch=master)](https://coveralls.io/github/openmrs/openmrs-module-webservices.rest?branch=master)

<img src="https://talk.openmrs.org/uploads/default/original/2X/f/f1ec579b0398cb04c80a54c56da219b2440fe249.jpg" alt="OpenMRS"/>

# OpenMRS REST Web Services Module

> REST API for [OpenMRS](http://openmrs.org)

<a href="https://ci.openmrs.org/browse/RESTWS-RESTWS"><img src="https://omrs-shields.psbrandt.io/build/RESTWS/RESTWS" alt="Build"/></a>
<a href="https://modules.openmrs.org/#/show/153/webservices-rest"><img src="https://omrs-shields.psbrandt.io/version/153" alt="Version"/></a>
<a href="https://modules.openmrs.org/#/show/153/webservices-rest"><img src="https://omrs-shields.psbrandt.io/omrsversion/153" alt="OpenMRS Version"/></a>

The module exposes the OpenMRS API as REST web services. If an OpenMRS instance is running the `webservice.rest` module, other applications can retrieve and post certain information to an OpenMRS database.

## Download

If you are not a developer, or just want to install the REST Web Services module into your
system, visit [the module download page](https://modules.openmrs.org/#/show/153/webservices-rest) instead.

> The required OpenMRS version to run the REST Web Services Module is `1.8.4+` or `1.9.0+`

## Build

To build the module from source, clone this repo:

```
git clone https://github.com/openmrs/openmrs-module-webservices.rest
```

Then navigate into the `openmrs-module-webservices.rest` directory and compile the module using Maven:

```
cd openmrs-module-webservices.rest && mvn clean install
```

:pushpin: You will need Maven and Java 8 installed to successfully build and run
the tests.

## Developer Documentation

### OTAP Docker Compose

This repository can run the REST module inside a fixed OpenMRS Reference Application runtime. The OTAP Docker Compose files use the official OpenMRS Reference Application `3.6.0` images and build a small backend overlay that replaces the bundled REST module with this repository's locally built `.omod`.

For a complete local setup guide for team members, see [`docs/setup.md`](docs/setup.md).

| Environment | Command | URL |
| --- | --- | --- |
| Dev | `docker compose -f docker-compose.dev.yml up --build -d` | `http://localhost:8080/openmrs/spa` |
| Test | `docker compose -f docker-compose.test.yml up --build -d` | `http://localhost:8081/openmrs/spa` |
| Prod | `docker compose -f docker-compose.prod.yml up --build -d` | `http://localhost:8082/openmrs/spa` |

Build the local REST module before starting an environment:

```bash
docker compose -f docker-compose.dev.yml --profile build-module run --rm module-builder
```

Prod requires explicit database secrets before startup:

```bash
export OMRS_DB_PASSWORD="replace-me"
export MYSQL_ROOT_PASSWORD="replace-me-root"
export OMRS_REST_ALLOWED_IPS="127.0.0.1 ::1 10.0.0.0/8"
docker compose -f docker-compose.prod.yml up --build
```

Prod enables REST security hardening by default: explicit IP allowlist, secure transport required for Basic authentication, and authentication rate limiting. Dev/test compose files explicitly disable secure-transport enforcement so local HTTP remains usable.

How it works:

1. `module-builder` builds this repository's `.omod` into `docker/modules/`.
2. The backend overlay starts from `openmrs/openmrs-reference-application-3-backend:3.6.0`.
3. The overlay removes the bundled `webservices.rest` OMOD and copies in the local one.
4. The overlay keeps the normal RefApp modules but removes the OCL startup import config because that import path makes local OTAP first boot unreliable.
5. OpenMRS starts with the normal RefApp backend modules, `referencedemodata`, and a separate database volume per environment.
6. The REST API is available under `/openmrs/ws/rest`.

Readiness check:

```bash
curl http://localhost:8080/openmrs/ws/rest/v1/session
```

CI still builds and validates this module from source.

For a teacher demo script, expected questions and security/compliance talking points, see
[`docs/otap-demo-guide.md`](docs/otap-demo-guide.md).
For a step-by-step explanation of how the module is used inside OpenMRS, see
[`docs/module-gebruiken-in-openmrs.md`](docs/module-gebruiken-in-openmrs.md).

The GitHub Actions workflow also has an optional manual smoke test. Run **CI/CD environments** with
`run_compose_smoke_test=true` to build the Dev stack in CI and verify
`/openmrs/ws/rest/v1/session`.

### Integration Tests

Integration tests can be found in the integration-tests directory. They are written with JUnit and Rest-Assured.
Before you can run integration tests you need to start up a server and install the module.
You can run integration tests with:
```
mvn clean verify -Pintegration-tests -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```
You can skip the testUrl parameter, if it is the same for your server.

### Wiki Pages

| Page | Description |
| ---- | ----------- |
| [REST Module](https://wiki.openmrs.org/display/docs/REST+Module) | The main module page with a description of the configuration options. |
| [Technical Documentation](https://wiki.openmrs.org/display/docs/REST+Web+Services+Technical+Documentation) | Technical information about the Web Services implementation. |
| [Core Developer Guide](https://wiki.openmrs.org/display/docs/Adding+a+Web+Service+Step+by+Step+Guide+for+Core+Developers) | Description of how to add REST resources to OpenMRS core. |
| [Module Developer Guide](https://wiki.openmrs.org/display/docs/Adding+a+Web+Service+Step+by+Step+Guide+for+Module+Developers) | Description of how to add REST resources to OpenMRS modules. |

### API Documentation

The API documentation is available inside the OpenMRS application and is linked
to the advanced administration screen. The URL should be something like:
> [http://localhost:8080/openmrs/module/webservices/rest/apiDocs.htm](http://localhost:8080/openmrs/module/webservices/rest/apiDocs.htm)

### Example Client code
  * Quick java swing client that displays patients and encounters: http://svn.openmrs.org/openmrs-contrib/examples/webservices/hackyswingexample/
  * You can download a client java application that allows add/edit a person (any resource) by making a query to the webservices.rest module - https://project-development-software-victor-aravena.googlecode.com/svn/trunk/ClientOpenMRSRest/

### Contributing to the API Documentation

The OpenMRS API documentation is built automatically using [Swagger UI](http://swagger.io/swagger-ui/). For details on how to customize the documentation see the [`swagger-ui` branch](https://github.com/psbrandt/openmrs-contrib-apidocs/tree/swagger-ui) in the [`openmrs-contrib-apidocs` repo](https://github.com/psbrandt/openmrs-contrib-apidocs).

## License

[MPL-2.0 w/ HD](http://openmrs.org/license/)
