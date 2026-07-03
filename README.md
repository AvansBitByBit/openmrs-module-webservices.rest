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

---

# Engineering Improvements

<p align="center">
  <img alt="Security hardened" src="https://img.shields.io/badge/security-hardened-2ea44f?style=for-the-badge&logo=owasp" />
  <img alt="CycloneDX SBOM" src="https://img.shields.io/badge/SBOM-CycloneDX%201.6-005f9e?style=for-the-badge" />
  <img alt="OTAP environments" src="https://img.shields.io/badge/OTAP-Dev%20%7C%20Test%20%7C%20Prod-6f42c1?style=for-the-badge&logo=docker" />
  <img alt="Maintainability" src="https://img.shields.io/badge/maintainability-ISO%2FIEC%2025010-blue?style=for-the-badge" />
</p>

> [!IMPORTANT]
> This section documents the repository's improvement programme and its evidence. It distinguishes implemented module controls from deployment responsibilities and future work. The changes improve the security and maintainability baseline; they do **not** by themselves certify an OpenMRS deployment as fully AVG/GDPR-, NEN 7510-, or NEN 7513-compliant.

## Contents

- [At a glance](#at-a-glance)
- [What changed](#what-changed)
- [Security improvements](#security-improvements)
  - [Application security](#application-security)
  - [Audit logging and privacy](#audit-logging-and-privacy)
  - [GitHub and software-supply-chain security](#github-and-software-supply-chain-security)
  - [OTAP and deployment security](#otap-and-deployment-security)
  - [Security verification and limitations](#security-verification-and-limitations)
- [Maintainability improvements](#maintainability-improvements)
  - [Architecture refactor](#architecture-refactor)
  - [Testing and build quality](#testing-and-build-quality)
  - [Documentation and reproducibility](#documentation-and-reproducibility)
- [Future changes](#future-changes)
- [Evidence and references](#evidence-and-references)

## At a glance

| Area | Before | Improved state | Evidence |
| --- | --- | --- | --- |
| REST authentication | Invalid Basic Auth could continue too far into request handling; no brute-force lockout | Invalid credentials stop with `401`; insecure Basic Auth can be rejected with `426`; repeated failures return `429` | [`AuthorizationFilter.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java), [`AuthorizationFilterTest.java`](omod-common/src/test/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilterTest.java) |
| Network access | Empty REST IP allowlist could leave access open | Empty allowlist is deny-by-default; Prod requires an explicit allowlist | [`RestUtil.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestUtil.java), [`docker-compose.prod.yml`](docker-compose.prod.yml) |
| Error disclosure | Error responses could expose Java implementation details | Stack-trace class names and line numbers are disabled by default | [`RestUtil.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestUtil.java), [`config.xml`](omod/src/main/resources/config.xml) |
| Security events | Security-relevant activity was not consistently persisted | Structured 5-W audit events cover login, lockout, record access, rights changes, and export | [NEN audit-logging report](docs/auditrapport/nen7510-nen7513-audit-logging.md) |
| Dependency risk | No repository-owned SCA/SBOM gate or scheduled update policy | CycloneDX SBOM, Trivy gate/SARIF, and weekly Dependabot updates | [`SecureSca.yaml`](.github/workflows/SecureSca.yaml), [`SecureSbom.yaml`](.github/workflows/SecureSbom.yaml), [`dependabot.yml`](.github/dependabot.yml) |
| Environments | No reproducible repository-owned OTAP runtime | Isolated Dev, Test, and Prod Compose stacks on a fixed OpenMRS `3.6.0` runtime | [`docker-compose.dev.yml`](docker-compose.dev.yml), [`docker-compose.test.yml`](docker-compose.test.yml), [`docker-compose.prod.yml`](docker-compose.prod.yml) |
| Central service design | `RestServiceImpl` combined coordination, resource discovery, and search selection in 737 LOC | A 159-LOC facade delegates through two focused registry interfaces | [Maintainability research](docs/maintenance-research/onderhoudbaarheidsonderzoek.md), [measured metrics](docs/maintenance-research/evidence/registry-solid-metrics.md) |
| Test evidence | Important behavior was spread across existing tests and manual checks | Focused registry tests, characterization tests, full Maven verification, JaCoCo artifacts, Postman, and optional live smoke testing | [Validation evidence](docs/maintenance-research/evidence/registry-solid-validation.md), [`ci-cd-environments.yml`](.github/workflows/ci-cd-environments.yml) |

## What changed

The improvement work covers more than a single code refactor. It adds a complete local environment, hardens REST request handling, introduces auditability and privacy controls, automates dependency and vulnerability checks, and separates a central architectural hotspot into testable components.

```mermaid
flowchart LR
    PR["Pull request / branch"] --> POLICY["Policy and secret-file check"]
    POLICY --> BUILD["Java 8 Maven clean verify"]
    POLICY --> COMPOSE["Validate Dev / Test / Prod Compose"]
    BUILD --> OMOD["Deployable OMOD artifact"]
    BUILD --> COVERAGE["JaCoCo evidence"]
    OMOD --> SCAN["Trivy repository and image reports"]
    COMPOSE --> SCAN
    OMOD --> DEV["Dev gate"]
    OMOD --> TEST["Test gate"]
    OMOD --> PROD["Prod gate: master only"]
```

The major delivered changes are:

- three isolated Docker Compose environments with separate ports, databases, volumes, and configuration;
- a backend overlay that replaces the bundled REST OMOD with the module built from this repository;
- hardened authentication, IP filtering, diagnostics, settings access, error handling, and server-banner behavior;
- persistent, privacy-aware security audit logging;
- CI build, Compose validation, artifact inspection, coverage upload, Trivy reporting, and an optional live REST smoke test;
- a blocking module-level SCA gate and validated CycloneDX 1.6 SBOM;
- weekly Maven and GitHub Actions dependency update automation;
- new integration and unit/component tests around CRUD, patient reads, error behavior, registries, dependency injection, authorization, and auditing;
- an ISO/IEC 25010-oriented architecture refactor and a reproducible evidence set.

<details>
<summary><strong>View the C4 container overview</strong></summary>

<br />

<p align="center">
  <img src="docs/auditrapport/03-C4%20Level%201.drawio.png" alt="C4 container diagram of the OpenMRS REST deployment" width="820" />
</p>

The diagram shows the main runtime trust path: users enter through the gateway and SPA, REST traffic is handled by the OpenMRS backend, and clinical data is stored in MariaDB. The complete context and container sources are available under [`docs/auditrapport/`](docs/auditrapport/) and [`docs/diagrams/`](docs/diagrams/).

</details>

## Security improvements

### Application security

| Control | Implemented behavior | Security effect |
| --- | --- | --- |
| Basic Auth termination | Invalid credentials return `401 Unauthorized` and do not invoke the remaining filter chain | Prevents unauthenticated requests from reaching API handling |
| Secure transport | `webservices.rest.requireSecureTransport` can reject Basic Auth on an insecure request with `426 Upgrade Required`; enabled in Prod | Reduces credential exposure over plaintext transport |
| Authentication rate limiting | Per-client failed attempts are tracked in a bounded time window; lockout returns `429 Too Many Requests` | Reduces online brute-force attempts |
| IP allowlisting | Empty or missing allowlists deny REST access; Prod cannot start without `OMRS_REST_ALLOWED_IPS` | Removes fail-open network configuration |
| Diagnostic authorization | `/ws/rest/v1/session/diag` requires `View RESTWS` and no longer exposes user roles or privileges | Reduces reconnaissance and authorization-data leakage |
| Settings authorization | Settings views and searches require `Manage RESTWS`; autocomplete returns names, not values | Protects global properties and secret-like values |
| Safe errors | Stack-trace details are disabled by default and REST errors omit Java class and source-line details | Reduces implementation disclosure |
| Server hardening | nginx hides upstream server headers and version tokens; Tomcat error reports suppress server and report details | Reduces runtime fingerprinting |

The corresponding configuration keys are defined in [`RestConstants.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestConstants.java) and documented in [`config.xml`](omod/src/main/resources/config.xml). Detailed threat findings and retest outcomes are recorded in the [pentest report](docs/auditrapport/02-pentesting.md) and [AVG technical evidence](docs/auditrapport/avg-conformiteitsbewijs.md).

> [!NOTE]
> Dev and Test explicitly allow local HTTP for developer usability. Prod enables secure-transport enforcement, but the Compose gateway currently publishes HTTP. A real production deployment must terminate TLS at a trusted reverse proxy/load balancer and forward the secure-request state correctly.

### Audit logging and privacy

The new audit subsystem records the **who, what, when, where, and why** of security-relevant events. Events include:

- successful and failed login attempts;
- authentication lockouts and requests denied during lockout;
- denial of Basic Auth over insecure transport;
- patient-record views and denied access attempts;
- role, privilege, and user-role changes;
- complex observation and form-resource exports, including denied exports.

[`SecurityAuditLogger.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/SecurityAuditLogger.java) sanitizes line breaks and redacts authorization values, passwords, tokens, nine-digit identifiers, diagnoses, and medication data. [`FileAuditLogWriter.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/FileAuditLogWriter.java) appends events to `${user.home}/openmrs-webservices-rest-audit.log` by default; deployments can override this with `openmrs.webservices.rest.audit.log.path`.

The writer is behind [`AuditLogWriter.java`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/AuditLogWriter.java), so persistence can be replaced and tested independently. Tests verify the 5-W format, severity, redaction, and file persistence. See the complete [NEN 7510/NEN 7513 audit-logging report](docs/auditrapport/nen7510-nen7513-audit-logging.md).

The repository also contains governance templates for the organizational work that code cannot complete:

- [processing register](docs/avg/verwerkingsregister.md);
- [DPIA](docs/avg/dpia.md);
- [privacy statement template](docs/avg/privacyverklaring-template.md);
- [processor-agreement checklist](docs/avg/verwerkersovereenkomst-checklist.md);
- [retention policy](docs/avg/bewaartermijnenbeleid.md);
- [data-breach procedure](docs/avg/datalekprocedure.md);
- [data-subject rights procedure](docs/avg/rechten-van-betrokkenen.md).

These files are templates and require an accountable organization to complete, approve, operate, and review them.

### GitHub and software-supply-chain security

Repository-owned GitHub configuration now provides layered checks:

| Configuration | What it does | Enforcement |
| --- | --- | --- |
| [`ci-cd-environments.yml`](.github/workflows/ci-cd-environments.yml) | Builds with Temurin Java 8, runs `clean verify`, uploads JaCoCo and OMOD artifacts, validates all Compose files, checks the OMOD for test fixtures, scans source/images, and applies environment gates | Build and policy failures block the workflow; broad container/repository Trivy results are currently report-only |
| [`SecureSca.yaml`](.github/workflows/SecureSca.yaml) | Generates a CycloneDX 1.6 aggregate SBOM, uploads HIGH/CRITICAL findings to GitHub code scanning as SARIF, and runs a separate blocking Trivy gate | HIGH or CRITICAL findings in shipped module dependencies fail the job |
| [`SecureSbom.yaml`](.github/workflows/SecureSbom.yaml) | Generates and validates CycloneDX JSON and publishes it as an immutable workflow artifact | Invalid SBOMs fail validation; the workflow does not push directly to protected branches |
| [`dependabot.yml`](.github/dependabot.yml) | Checks Maven and GitHub Actions weekly; groups minor/patch updates while keeping breaking major updates separate | Update PRs still require build and review |

The SCA scope deliberately excludes Maven `provided` and `test` dependencies. Those dependencies are not shipped inside this module. This makes the module release gate accurate while leaving OpenMRS host-platform and container risks visible in the broader image scans. The CI/CD audit documents this correction, the Jackson security backport, and the resulting 24-component SBOM with no locally detected HIGH/CRITICAL module findings: [CI/CD audit report](docs/ci-cd-auditrapport-2026-07-03.md).

GitHub Environment gates map branches to stages:

| Stage | Normal branch | Required repository configuration | Current deployment status |
| --- | --- | --- | --- |
| Dev | `dev` | `Dev` environment, optional `OPENMRS_BASE_URL` and `DEPLOY_TOKEN` | Gate and artifact flow implemented; server deploy command is a placeholder |
| Test | `acceptance` / `devprodomgeving` | `Test` environment | Gate and artifact flow implemented; server deploy command is a placeholder |
| Prod | `master` only | Protected `Prod` environment, reviewers/secrets as configured in GitHub | Branch restriction implemented; server deploy command is a placeholder |

> [!WARNING]
> The workflow models deployment policy but does not yet perform a remote deployment. Setting `DEPLOY_ENABLED=true` without implementing the environment-specific deploy command intentionally fails to provide a false impression of delivery.

### OTAP and deployment security

Each OTAP stage runs the same fixed OpenMRS Reference Application `3.6.0` topology:

```text
gateway -> frontend
       \-> backend overlay -> MariaDB
```

The backend overlay removes the bundled `webservices.rest` module and installs the locally built OMOD. Dev, Test, and Prod have separate database and application-data volumes, reducing accidental cross-environment contamination. Health checks are configured for the frontend, backend, and database.

Prod differs intentionally from Dev/Test:

- requires `OMRS_DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, and `OMRS_REST_ALLOWED_IPS` at startup;
- disables the web administration module;
- requires secure transport for REST Basic Auth;
- enables rate limiting with five failures in 900 seconds and a 900-second lockout;
- uses restart policies for runtime services;
- keeps database and application data in Prod-specific volumes.

The CI policy check rejects committed `.env` and secret-like files and directs operators to GitHub Environment variables and secrets. The exact setup, reset, readiness, and module-presence checks are in [`docs/setup.md`](docs/setup.md); the demonstration and control checklist is in [`docs/otap-demo-guide.md`](docs/otap-demo-guide.md).

### Security verification and limitations

Implemented controls have focused tests and documented retests. The repository includes unit/component verification for the authorization filter, IP matching, error wrapping, diagnostics, settings access, and audit redaction/persistence. It also contains manual pentest results for authentication, access control, injection, XSS, information disclosure, IP filtering, logging, and the assessed deserialization route.

Known limits are explicit:

- full AVG/GDPR and NEN compliance requires organizational controls and deployment evidence outside this repository;
- the HTTP `Server` header may still depend on the selected gateway image/modules, even though version tokens and upstream headers are suppressed;
- OpenMRS platform dependencies and base-container vulnerabilities are not erased by narrowing the module SBOM; they must be upgraded, mitigated, or formally accepted separately;
- broad Trivy filesystem/container scans are report-only and need an agreed release-blocking policy;
- the checked-in live-integration evidence records that Docker Desktop was unavailable during one validation run; a successful `clean verify` is not proof of a live HTTP deployment.

## Maintainability improvements

### Architecture refactor

The main maintainability hotspot was [`RestServiceImpl`](omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java). It previously coordinated dependencies, discovered resources, indexed search handlers, resolved version/order conflicts, and selected handlers. The refactor preserves the public `RestService` behavior while separating those responsibilities:

```mermaid
classDiagram
    class RestServiceImpl {
      +initialize()
      +getResourceByName()
      +getSearchHandler()
    }
    class ResourceRegistry {
      <<interface>>
      +refresh()
      +getResourceByName()
      +getResourceBySupportedClass()
    }
    class SearchHandlerRegistry {
      <<interface>>
      +refresh()
      +getSearchHandler()
      +getSearchHandlers()
    }
    class DefaultResourceRegistry
    class DefaultSearchHandlerRegistry

    RestServiceImpl --> ResourceRegistry : constructor injection
    RestServiceImpl --> SearchHandlerRegistry : constructor injection
    DefaultResourceRegistry ..|> ResourceRegistry
    DefaultSearchHandlerRegistry ..|> SearchHandlerRegistry
```

This applies Single Responsibility, Facade, Dependency Inversion, Interface Segregation, and program-to-interface principles. Spring wiring in [`webModuleApplicationContext.xml`](omod-common/src/main/resources/webModuleApplicationContext.xml) provides the default implementations. Alternative registry implementations can be injected without changing the facade.

Measured result:

| Metric | Baseline `RestServiceImpl` | Current facade | Change |
| --- | ---: | ---: | ---: |
| Physical LOC | 737 | 159 | **-578** |
| Import fan-out | 32 | 16 | **-16** |
| Rough control-flow tokens | 170 | 18 | **-152** |

The extracted subsystem is not smaller in total: the facade, two interfaces, and two default implementations contain 793 physical LOC and 87 rough control-flow tokens. That is an intentional modularity and dependency-direction tradeoff, not a claim of total code-volume reduction. Measurements are reproducible with [`measure-registry-architecture.ps1`](docs/maintenance-research/scripts/measure-registry-architecture.ps1) and are documented in [registry metrics](docs/maintenance-research/evidence/registry-solid-metrics.md).

### Testing and build quality

The refactor and hardening work added direct tests rather than relying only on end-to-end behavior:

- registry lookup, version filtering, duplicate-order handling, superclass selection, scanner/instantiation errors, refresh, search matching, defaults, ambiguity, and duplicate IDs;
- facade delegation, asynchronous refresh, constructor injection, and alternative registry implementations;
- authentication outcomes, lockouts, insecure transport, allowlist behavior, error disclosure, audit events, and sensitive-data redaction;
- Swagger, wrapper, controller, settings, patient, location CRUD, and error-handling behavior;
- Rest-Assured integration-test scenarios and a Postman collection for live API checks.

Recorded validation for the final registry refactor:

| Scope | Recorded result |
| --- | --- |
| Direct registry/dependency tests | 19 passed |
| Existing `RestServiceImpl` characterization suite | 53 passed |
| `omod-common` suite | 149 passed |
| Full `mvn clean verify` | 149 common + 1,803 OMOD tests; 0 failures/errors; 14 skipped |

The full evidence is in [registry validation](docs/maintenance-research/evidence/registry-solid-validation.md). That report also states an important boundary: the integration-tests module produced no Surefire XML in that run, so the result proves the unit/component reactor and Spring context, not a live OpenMRS HTTP instance.

The CI workflow additionally:

- builds the complete Maven reactor on the module's Java 8 target;
- generates and retains JaCoCo reports for 30 days;
- verifies that deployable OMOD artifacts do not contain known test fixtures;
- retains deployable OMOD and Trivy evidence as workflow artifacts;
- validates every Compose model before a stage can proceed;
- optionally starts Dev and polls `/openmrs/ws/rest/v1/session` for a live smoke test.

### Documentation and reproducibility

The repository now includes:

- a setup guide and module integration explanation;
- C4 context/container diagrams and PlantUML before/after/sequence diagrams;
- an ISO/IEC 25010-oriented maintainability study with baseline, design, decision, validation, and claim-audit documents;
- raw Maven, coverage, architecture, duplication, coupling, Docker, and live-integration evidence;
- a gap analysis, pentest results, technical AVG evidence, NEN audit-logging analysis, and CI/CD audit;
- a versioned CycloneDX SBOM and generated security reports;
- a teacher/demo guide describing expected checks and limitations.

This makes the design decisions and their validation repeatable instead of depending on undocumented team knowledge. Start with the [maintainability research](docs/maintenance-research/onderhoudbaarheidsonderzoek.md), [security gap analysis](docs/auditrapport/01-gap-analyse.md), and [local OTAP setup](docs/setup.md).

## Future changes

The following roadmap is ordered by risk reduction and delivery value:

- [ ] **Connect real deployment commands.** Replace the Dev/Test/Prod placeholders with an authenticated, auditable deployment mechanism, rollback procedure, and post-deploy health check.
- [ ] **Terminate and verify TLS in Prod.** Configure the production ingress/reverse proxy, trusted forwarded-header handling, certificate lifecycle, HSTS, and an automated test proving Basic Auth cannot traverse plaintext transport.
- [ ] **Run live integration continuously.** Make the Compose REST smoke test mandatory for release candidates and execute the Rest-Assured/Postman suites against the running image.
- [ ] **Define vulnerability gates per scope.** Keep the module SBOM gate blocking; establish separate severity, exploitability, exception-expiry, and ownership rules for OpenMRS platform and container findings.
- [ ] **Upgrade the host platform and base images.** Track vulnerable libraries supplied by OpenMRS/runtime containers independently from this module's shipped dependencies.
- [ ] **Centralize tamper-resistant audit storage.** Forward append-only events to a protected logging platform/SIEM; define access control, retention, integrity monitoring, clock synchronization, alerting, and tested recovery.
- [ ] **Complete privacy governance.** Assign owners and approval dates to the processing register, DPIA, retention schedule, processor agreements, breach procedure, and data-subject request process.
- [ ] **Finish information-disclosure hardening.** Verify or remove the remaining HTTP `Server` header in the real production gateway and add a regression test at the ingress boundary.
- [ ] **Refactor the next measured hotspots.** Evaluate splitting `RestUtil` by HTTP/paging/date responsibilities, reducing `BaseDelegatingResource` surface area, and isolating Swagger generation—each behind characterization tests.
- [ ] **Stabilize the full test lifecycle.** Investigate the previously observed `ClearDbCacheController2_0Test` instability and make `clean test`, `clean verify`, and live integration consistently reproducible.
- [ ] **Add maintainability quality gates.** Track coverage, duplication, dependency cycles, architectural rules, and hotspot complexity over time instead of treating the current measurements as a one-off audit.
- [ ] **Exercise operational recovery.** Test database backup/restore, environment reset, OMOD rollback, secret rotation, audit-log recovery, and incident-response runbooks.

## Evidence and references

### Repository evidence

| Topic | Primary reference |
| --- | --- |
| Security gap analysis | [`docs/auditrapport/01-gap-analyse.md`](docs/auditrapport/01-gap-analyse.md) |
| Penetration tests and mitigations | [`docs/auditrapport/02-pentesting.md`](docs/auditrapport/02-pentesting.md) |
| Technical AVG/GDPR mapping | [`docs/auditrapport/avg-conformiteitsbewijs.md`](docs/auditrapport/avg-conformiteitsbewijs.md) |
| NEN 7510/NEN 7513 audit logging | [`docs/auditrapport/nen7510-nen7513-audit-logging.md`](docs/auditrapport/nen7510-nen7513-audit-logging.md) |
| CI/CD and GitHub security audit | [`docs/ci-cd-auditrapport-2026-07-03.md`](docs/ci-cd-auditrapport-2026-07-03.md) |
| Maintainability study | [`docs/maintenance-research/onderhoudbaarheidsonderzoek.md`](docs/maintenance-research/onderhoudbaarheidsonderzoek.md) |
| Maintainability claim audit | [`docs/maintenance-research/claim-audit.md`](docs/maintenance-research/claim-audit.md) |
| Registry metrics and validation | [`registry-solid-metrics.md`](docs/maintenance-research/evidence/registry-solid-metrics.md), [`registry-solid-validation.md`](docs/maintenance-research/evidence/registry-solid-validation.md) |
| Local OTAP setup | [`docs/setup.md`](docs/setup.md) |
| OTAP demonstration guide | [`docs/otap-demo-guide.md`](docs/otap-demo-guide.md) |
| Current CycloneDX SBOM | [`docs/sbom.cdx.json`](docs/sbom.cdx.json) |
| AVG/GDPR governance templates | [`docs/avg/README.md`](docs/avg/README.md) |

### Standards and platform references

- [ISO/IEC 25010:2023 — Product quality model](https://www.iso.org/standard/78176.html)
- [EU Regulation 2016/679 (GDPR/AVG)](https://eur-lex.europa.eu/eli/reg/2016/679/oj)
- [GitHub Docs — Configuring Dependabot version updates](https://docs.github.com/en/code-security/how-tos/secure-your-supply-chain/secure-your-dependencies/configuring-dependabot-version-updates)
- [GitHub Docs — About Dependabot security updates](https://docs.github.com/code-security/supply-chain-security/managing-vulnerabilities-in-your-projects-dependencies/about-dependabot-security-updates)
- [CycloneDX specification](https://cyclonedx.org/specification/overview/)
- [Trivy documentation](https://trivy.dev/latest/docs/)

<p align="right"><a href="#openmrs-rest-web-services-module">Back to top</a></p>
