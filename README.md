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

## CI/CD, SBOM & Supply-Chain Security

[![CI/CD environments](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/ci-cd-environments.yml/badge.svg?branch=master)](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/ci-cd-environments.yml)
[![Secure SBOM](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/SecureSbom.yaml/badge.svg?branch=master)](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/SecureSbom.yaml)
[![Secure SCA](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/SecureSca.yaml/badge.svg?branch=master)](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions/workflows/SecureSca.yaml)
[![SBOM](https://img.shields.io/badge/SBOM-CycloneDX%201.6-6f42c1.svg)](docs/sbom.cdx.json)
[![Java](https://img.shields.io/badge/Java-8%20compatible-e76f00.svg)](pom.xml)
[![Security policy](https://img.shields.io/badge/security-least%20privilege-2ea44f.svg)](.github/workflows/SecureSbom.yaml)

> [!IMPORTANT]
> The pipeline deliberately separates **module risk** from **host-platform risk**. The blocking SCA gate scans dependencies shipped with this OMOD. OpenMRS-provided libraries and runtime container images are assessed separately, because fixing those requires an OpenMRS/platform or base-image upgrade rather than a module-only dependency override.

### Why this was improved

The CI/CD and software-supply-chain controls were audited on **3 July 2026**. The audit found that the normal Maven build, test suite, Compose validation, CodeQL and SonarCloud checks were healthy, but the SBOM and SCA workflows needed a clearer security boundary.

The complete evidence, findings and residual-risk assessment are available in the [CI/CD audit report](docs/ci-cd-auditrapport-2026-07-03.md).

| Area | Before | Improvement | Result / evidence |
| --- | --- | --- | --- |
| SBOM publication | The workflow attempted to write generated content back to protected `master`. A second attempt tried to create a PR, which repository policy does not allow GitHub Actions to do. | The SBOM job is now read-only and publishes its validated result as an Actions artifact. Tracked SBOM changes use the normal reviewed PR process. | Branch protection is respected and the workflow requires only `contents: read`. |
| SBOM scope | The aggregate BOM included `provided` and `test` dependencies from the OpenMRS host and development toolchain. | CycloneDX generation explicitly sets `includeProvidedScope=false` and `includeTestScope=false`. | The module SBOM was reduced from **237** to **24** delivered components. |
| SCA gate | Host-platform CVEs made every module and Dependabot PR fail, even where the OMOD did not ship the affected library. | The blocking gate now evaluates the deliverable module boundary. Platform and image risks remain visible in separate reports. | Local verification with Trivy 0.70.0 found **0 HIGH/CRITICAL** findings in the corrected module SBOM. |
| Jackson advisories | Jackson 2.19.4 matched five active Dependabot advisory ranges. | The managed Jackson line was moved to security-backported 2.18.8. | Existing tests pass; the version falls outside the five advisory ranges present during the audit. |
| Security evidence | Security output was spread across logs and failing checks. | The workflows retain SBOM, SARIF, JaCoCo and Trivy artifacts with concise job summaries. | Evidence can be downloaded per workflow run and linked from an audit or release record. |
| Pipeline permissions | SBOM automation requested repository write access. | The SBOM workflow follows least privilege; SARIF upload keeps only the dedicated `security-events: write` permission it needs. | Smaller token blast radius and clearer review boundaries. |

### Pipeline architecture

```mermaid
flowchart LR
    change["Push / pull request"] --> policy["Environment policy check"]
    policy --> build["Maven clean verify"]
    policy --> compose["Validate Dev / Test / Prod Compose"]
    build --> artifact["OMOD + JaCoCo artifacts"]
    compose --> runtime["Trivy repository and image reports"]
    artifact --> runtime
    artifact --> dev["Dev environment gate"]
    artifact --> test["Test environment gate"]
    artifact --> prod["Prod approval gate"]

    change --> bom["CycloneDX 1.6 module SBOM"]
    bom --> validate["CycloneDX validation"]
    validate --> sbomArtifact["Read-only SBOM artifact"]
    bom --> trivy["Trivy HIGH / CRITICAL gate"]
    trivy --> sarif["SARIF → GitHub Code Scanning"]
```

The implementation is split across three workflows:

| Workflow | Responsibility | Blocking behavior | Primary outputs |
| --- | --- | --- | --- |
| [`CI/CD environments`](.github/workflows/ci-cd-environments.yml) | Policy checks, Maven build/tests, JaCoCo, Compose validation, repository/image scans, optional smoke test and OTAP gates. | Build, test, artifact and Compose failures block. The broad runtime Trivy scan is currently **report-only**. | OMOD, JaCoCo report, repository and container Trivy reports. |
| [`Secure SBOM`](.github/workflows/SecureSbom.yaml) | Generates and validates a CycloneDX 1.6 JSON inventory for delivered module dependencies. | Invalid or missing SBOM blocks the job. The workflow does not write to the repository. | `sbom-cyclonedx-json` artifact and job summary. |
| [`Secure SCA`](.github/workflows/SecureSca.yaml) | Scans the same module SBOM and uploads machine-readable findings. | Any delivered dependency with a known HIGH or CRITICAL vulnerability blocks the job. | SARIF in GitHub Code Scanning and `sbom-cyclonedx-json-sca` artifact. |

### Security boundary and ownership

| Dependency class | Example | Included in blocking module SBOM? | Owner / remediation route |
| --- | --- | ---: | --- |
| Packaged compile/runtime dependency | Libraries embedded in the built OMOD | Yes | This repository; upgrade, replace or mitigate before merge. |
| Maven `provided` dependency | Spring, Hibernate or database drivers supplied by OpenMRS | No | OpenMRS runtime/platform upgrade; track in the platform risk register. |
| Test-only dependency | JUnit, Mockito, Rest-Assured | No | Development toolchain; Dependabot and build compatibility checks. |
| Base-image operating system package | Packages in OpenMRS, gateway, frontend or MariaDB images | No, but scanned separately | Container image owner; update image digest/tag or document acceptance. |
| Secret or Compose misconfiguration | Credential pattern or insecure container setting | Not an SBOM component | Repository/container configuration owner; the main Trivy job reports it. |

> [!WARNING]
> A green `Secure SCA` check means the **module deliverable** has passed its vulnerability gate. It does not close risks inherited from OpenMRS 2.8.6, legacy UI modules, the database or container base images. Review the `trivy-security-reports` artifact before a production release.

### Running the checks locally

#### 1. Build and test the complete Maven reactor

```bash
mvn --batch-mode --no-transfer-progress clean verify
```

The project targets Java 8 bytecode. Use Java 8 for parity with GitHub Actions; newer local JDKs are useful as an additional compatibility check but do not replace the Java 8 CI run.

#### 2. Generate the same module-scoped SBOM as CI

```bash
mvn -B -ntp org.cyclonedx:cyclonedx-maven-plugin:2.9.1:makeAggregateBom \
  -DschemaVersion=1.6 \
  -DoutputFormat=json \
  -DoutputName=sbom.cdx \
  -DincludeProvidedScope=false \
  -DincludeTestScope=false
```

Output: `target/sbom.cdx.json`.

#### 3. Reproduce the vulnerability gate

```bash
trivy sbom \
  --severity HIGH,CRITICAL \
  --ignore-unfixed=false \
  --exit-code 1 \
  target/sbom.cdx.json
```

#### 4. Validate the OTAP Compose definitions

```bash
docker compose -f docker-compose.dev.yml config
docker compose -f docker-compose.test.yml config

OMRS_DB_PASSWORD=ci-placeholder \
MYSQL_ROOT_PASSWORD=ci-placeholder-root \
OMRS_REST_ALLOWED_IPS=127.0.0.1 \
docker compose -f docker-compose.prod.yml config
```

### Finding CI evidence on GitHub

1. Open the repository's [Actions page](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/actions).
2. Select **CI/CD environments**, **Secure SBOM**, or **Secure SCA**.
3. Open the run for the relevant commit or pull request.
4. Review the job summary and download the artifacts at the bottom of the run.
5. Review uploaded SCA findings in [Code scanning](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/security/code-scanning) and dependency advisories in [Dependabot alerts](https://github.com/AvansBitByBit/openmrs-module-webservices.rest/security/dependabot).

Useful GitHub CLI commands:

```bash
# Recent workflow runs
gh run list --limit 15

# Inspect a failed run
gh run view <run-id> --log-failed

# Download all evidence for a run
gh run download <run-id> --dir evidence/<run-id>

# Show checks for the current pull request
gh pr checks
```

<details>
<summary><strong>Artifact inventory</strong></summary>

| Artifact | Produced by | Purpose |
| --- | --- | --- |
| `webservices-rest-omod` | CI/CD environments | Deployable OpenMRS module. |
| `jacoco-coverage` | CI/CD environments | Test coverage evidence. |
| `trivy-security-reports` | CI/CD environments | JSON, table and Markdown summaries for repository and runtime images. |
| `sbom-cyclonedx-json` | Secure SBOM | Validated CycloneDX 1.6 module inventory. |
| `sbom-cyclonedx-json-sca` | Secure SCA | Exact SBOM input used by the vulnerability gate. |
| Trivy SARIF | Secure SCA | Findings integrated into GitHub Code Scanning. |

</details>

<details>
<summary><strong>Why the committed SBOM and CI artifact can differ</strong></summary>

[`docs/sbom.cdx.json`](docs/sbom.cdx.json) is a reviewed snapshot that makes the dependency inventory visible in the repository. The workflow artifact is regenerated from the exact commit under test and is therefore the authoritative evidence for that run. CycloneDX metadata such as timestamps and serial numbers can change between generations even when the dependency graph is unchanged.

The workflow intentionally does not commit generated output or create pull requests. GitHub Actions is configured without that repository-wide capability, and retaining read-only workflow permissions is the safer default. When the dependency graph changes, regenerate the tracked snapshot locally and include it in the same reviewed pull request.

</details>

### Roadmap

The current setup establishes a reliable baseline. The next improvements should be implemented incrementally and backed by evidence:

- [ ] Upgrade or replace vulnerable dependencies supplied by the OpenMRS host platform; do not hide them through module-level overrides that the runtime may ignore.
- [ ] Introduce a reviewed vulnerability baseline for runtime images, then promote the broad Trivy image scan from report-only to a controlled release gate.
- [ ] Replace deployment placeholders with authenticated, auditable deployment commands and post-deployment health checks for Dev, Test and Prod.
- [ ] Pin every third-party GitHub Action to a full commit SHA and automate reviewed update PRs through Dependabot.
- [ ] Add artifact attestations and build provenance for the OMOD and SBOM.
- [ ] Sign release artifacts and publish checksums alongside each tagged release.
- [ ] Add VEX statements for investigated findings that are not exploitable in the deployed OpenMRS context.
- [ ] Add a scheduled SBOM/SCA run so newly published CVEs are detected even when no source code changes.
- [ ] Add an automated dependency-diff summary to pull requests (`added`, `removed`, `upgraded`, license changes and vulnerability delta).
- [ ] Define retention and archival rules for SBOM, SARIF, JaCoCo and deployment evidence.
- [ ] Modernize the Java baseline in coordination with the supported OpenMRS runtime; retain a compatibility matrix during migration.
- [ ] Add rollback verification and environment-specific smoke tests after real deployments are connected.

### Standards and official references

- [CycloneDX — Bill of Materials standard](https://cyclonedx.org/)
- [CycloneDX Maven plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin)
- [CycloneDX Authoritative Guide to SBOM](https://cyclonedx.org/guides/sbom/)
- [Trivy — SBOM scanning](https://trivy.dev/latest/docs/target/sbom/)
- [GitHub Actions workflow syntax](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax)
- [GitHub Actions security hardening](https://docs.github.com/en/actions/reference/security/secure-use)
- [GitHub Actions artifacts](https://docs.github.com/en/actions/using-workflows/storing-workflow-data-as-artifacts)
- [Uploading SARIF to GitHub Code Scanning](https://docs.github.com/en/code-security/code-scanning/integrating-with-code-scanning/uploading-a-sarif-file-to-github)
- [Managing repository Actions permissions](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/enabling-features-for-your-repository/managing-github-actions-settings-for-a-repository)
- [OpenMRS REST Web Services technical documentation](https://wiki.openmrs.org/display/docs/REST+Web+Services+Technical+Documentation)
- [OpenMRS O3 data access guidance](https://o3-docs.openmrs.org/en-US/docs/recipes/retrieve-and-post-data/)

### Project evidence and configuration

- [CI/CD audit report — 3 July 2026](docs/ci-cd-auditrapport-2026-07-03.md)
- [Tracked CycloneDX SBOM snapshot](docs/sbom.cdx.json)
- [Local setup guide](docs/setup.md)
- [OTAP demo guide](docs/otap-demo-guide.md)
- [Using the module in OpenMRS](docs/module-gebruiken-in-openmrs.md)
- [Dependabot configuration](.github/dependabot.yml)
- [Maven dependency and build configuration](pom.xml)

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
