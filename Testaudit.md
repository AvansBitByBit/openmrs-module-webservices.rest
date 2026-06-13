# Test Audit

## Summary

This audit documents the expanded automated test coverage for the OpenMRS REST Web Services module. The implementation follows the repository's existing test style: JUnit 4, OpenMRS test base classes, Maven Surefire/Failsafe, Rest Assured for live integration tests, GitHub Actions for CI/CD, and Newman for the requested Postman automation.

No production REST API behavior was intentionally changed. The added coverage focuses on risk-based core behavior rather than attempting to duplicate every inherited controller/resource test already present in the repository.

## What Was Added

| Layer | Location | What it covers | Why it exists |
| --- | --- | --- | --- |
| Unit tests | `omod-common/src/test/java` | `SimpleObject`, conversion, request context, REST utility parsing and fallbacks | Validates low-level behavior used by most resources without needing a running web server. |
| Component/controller tests | `omod/src/test/java` | Base exception handling, Location, Patient, Session, Swagger specification controller | Validates Spring/OpenMRS controller behavior, response shapes, and failure paths using existing test helpers. |
| Live integration tests | `integration-tests/src/test/java` | Session auth, metadata CRUD, patient read/search, error responses | Validates the deployed module through real HTTP calls against an OpenMRS server. |
| Postman tests | `postman/` | Session, patient search, metadata lifecycle, invalid UUID, malformed JSON | Provides a portable API test collection for local use and Newman execution in CI. |
| CI automation | `.github/workflows/ci-cd-environments.yml` | Maven tests, report artifacts, conditional live API and Newman gates | Ensures tests run consistently and live tests only run when environment credentials are configured. |
| Build and fixture fixes | `pom.xml`, `omod/src/test/resources/customTestDataset.xml`, `CreatePatientIdentifierResource1_9Test.java` | Dependency resolution and stale validator-sensitive fixtures | Keeps the existing suite runnable against the current OpenMRS/Jackson dependency set. |

## Why These Tests Were Chosen

The module exposes a broad REST surface, and many resources already inherit coverage from existing base controller/resource tests. The new tests target areas with high regression risk:

- Authentication and session response shape, because API clients rely on stable `authenticated`, `user`, locale, provider, and session location fields.
- Metadata CRUD on `location`, because it safely exercises create/read/update/search/delete without touching patient clinical data.
- Patient read/search, because patient access is a core API use case and should be verified without mutating patient/person records.
- Error handling, because clients need predictable status codes and JSON error bodies for invalid input, unknown resources, malformed JSON, and unsupported methods.
- Request context and conversion utilities, because paging, representation, dates, classes, arrays, and conversion failures are shared across many resources.
- Swagger generation, because client tooling depends on correct host, scheme, base path, and proxy-aware protocol handling.
- Postman/Newman tests, because they make a runnable API contract available outside the Java test harness.

## Where The Tests Are

| File or directory | Purpose |
| --- | --- |
| `omod-common/src/test/java/org/openmrs/module/webservices/rest/SimpleObjectTest.java` | Unit tests for fluent map behavior and JSON parsing success/failure. |
| `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java` | Expanded conversion happy paths and failure paths. |
| `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/RequestContextTest.java` | Paging links, encoded query parameters, and absolute-limit validation. |
| `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/RestUtilTest.java` | Request-context parsing and malformed global-property fallbacks. |
| `omod/src/test/java/org/openmrs/module/webservices/rest/web/v1_0/controller/BaseRestControllerTest.java` | HTTP status, auth headers, nested auth exceptions, and error body shape. |
| `omod/src/test/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_9/LocationController1_9Test.java` | Location success and failure paths. |
| `omod/src/test/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_9/PatientController1_9Test.java` | Patient invalid identifier failure path plus existing CRUD/search behavior. |
| `omod/src/test/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_9/SessionController1_9Test.java` | Explicit authenticated and unauthenticated session response shapes. |
| `omod/src/test/java/org/openmrs/module/webservices/rest/web/controller/SwaggerSpecificationControllerTest.java` | Swagger host/basePath/scheme and forwarded protocol behavior. |
| `integration-tests/src/test/java/org/openmrs/module/webservices/rest/LocationCrudIT.java` | End-to-end metadata lifecycle through HTTP. |
| `integration-tests/src/test/java/org/openmrs/module/webservices/rest/PatientReadIT.java` | End-to-end read-only patient search and representation checks. |
| `integration-tests/src/test/java/org/openmrs/module/webservices/rest/ErrorHandlingIT.java` | End-to-end API failure/status checks. |
| `postman/webservices-rest.postman_collection.json` | Newman-ready API collection. |
| `postman/webservices-rest.local.postman_environment.json` | Local Postman environment template. |

## How To Run

Run unit and component tests:

```bash
mvn --batch-mode --no-transfer-progress clean verify
```

Run live Rest Assured integration tests against a local OpenMRS server:

```bash
mvn --batch-mode --no-transfer-progress -pl integration-tests -Pintegration-tests verify \
  -DtestBaseUrl=http://localhost:8080/openmrs \
  -DtestUsername=admin \
  -DtestPassword=Admin123
```

The old URL-with-credentials form still works:

```bash
mvn --batch-mode --no-transfer-progress -pl integration-tests -Pintegration-tests verify \
  -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```

Run the Postman collection with Newman:

```bash
npx --yes newman@6 run postman/webservices-rest.postman_collection.json \
  --env-var baseUrl=http://localhost:8080/openmrs \
  --env-var username=admin \
  --env-var password=Admin123
```

## Verification Performed

Local Maven was installed as a portable Apache Maven 3.9.11 runtime under the user profile and run with Java 8.

Verified locally on June 10, 2026:

- `mvn --batch-mode --no-transfer-progress clean verify` passed.
- `omod-common` ran 117 tests with 0 failures and 0 errors.
- `omod` ran 1797 tests with 0 failures, 0 errors, and 14 existing skipped tests.
- `integration-tests` compiled and packaged its Rest Assured test classes during the default reactor build.
- Postman collection and environment JSON both parse successfully.
- `npx --yes newman@6 --version` resolved Newman 6.2.2.
- Live Rest Assured and Newman execution were not run locally because no OpenMRS REST endpoint was listening at `http://localhost:8080/openmrs`.

The Maven run also exposed two pre-existing build/test hygiene issues that were fixed:

- `jacksonVersion` was changed from unresolved `2.21.1` to `2.19.4`, the latest Maven Central version shared by `jackson-core`, `jackson-databind`, and `jackson-annotations`.
- Stale test fixtures were updated so current OpenMRS validators accept existing encounter/order data and patient identifier creation tests no longer reuse an already assigned identifier.

## CI/CD Automation

The GitHub Actions workflow now has two testing levels:

- Pull requests and normal builds run Maven unit/component tests through the existing build job.
- Live API and Postman tests run only for the `test` environment when `OPENMRS_BASE_URL`, `OPENMRS_TEST_USERNAME`, and `OPENMRS_TEST_PASSWORD` are configured.

CI uploads Maven Surefire/Failsafe reports, JaCoCo execution/report artifacts when present, and Newman JUnit output for live Postman runs.

## Coverage And Residual Risk

This test expansion improves behavioral coverage across shared utilities, controllers, live HTTP behavior, and API contract checks. It does not claim exhaustive coverage of every REST resource, because the repository already has a large inherited resource/controller test suite and exhaustive live CRUD against every resource would be slow, brittle, and unsafe for patient data.

The live tests intentionally mutate metadata only. Patient tests are read-only. This protects test and production-like environments from accidental clinical data changes.

## Risk Matrix

| Risk | Impact | Likelihood | Coverage added | CI gate | Residual risk | Mitigation |
| --- | --- | --- | --- | --- | --- | --- |
| Broken authentication/session response | High | Medium | Unit/component session tests, Rest Assured session tests, Postman session tests | Maven and conditional live gate | Low | Keep response-shape assertions on authenticated and unauthenticated sessions. |
| REST error responses lose JSON shape or status code | High | Medium | Base controller tests, ErrorHandlingIT, Postman invalid UUID/malformed JSON tests | Maven and conditional live gate | Low | Assert status codes and `error` body presence at both component and live layers. |
| Metadata CRUD regressions | High | Medium | Location controller tests, LocationCrudIT, Postman location lifecycle | Maven and conditional live gate | Medium | Live tests create unique metadata and purge it; monitor cleanup failures in reports. |
| Patient read/search regressions | High | Medium | Patient controller tests, PatientReadIT, Postman patient search tests | Maven and conditional live gate | Medium | Live read-by-UUID depends on a known test patient and is skipped if absent; maintain seed data in test environment. |
| Pagination and request parameter regressions | Medium | Medium | RequestContextTest and RestUtilTest | Maven build | Low | Assert limit, startIndex, includeAll, link generation, and malformed input behavior. |
| Conversion regressions for shared data types | Medium | Medium | ConversionUtilTest expanded happy/failure paths | Maven build | Low | Cover dates, classes, arrays, collections, locales, enums, and numeric conversions. |
| Swagger docs generated with wrong host/base path/scheme | Medium | Medium | SwaggerSpecificationControllerTest | Maven build | Low | Verify direct request scheme and `X-Forwarded-Proto` proxy behavior. |
| Test data leaks into deployable artifacts | High | Low | Existing artifact inspection remains in CI | Build job | Low | Continue failing builds if known test fixture patterns appear in `.omod` artifacts. |
| Dependency drift breaks the test/build pipeline | High | Medium | Maven verification and Jackson version alignment | Maven build | Low | Keep dependency versions resolvable across all declared artifacts and avoid non-existent patch versions. |
| Stale fixtures fail newer OpenMRS validators | Medium | Medium | Corrected encounter/order and identifier fixture data | Maven build | Low | Keep fixture patient/order/date/type data consistent with validator rules when OpenMRS is upgraded. |
| Live test environment unavailable or unconfigured | Medium | Medium | Conditional CI configuration gate | Live job | Medium | Live job skips with an explicit message when URL/credentials are missing; configure GitHub Environment secrets to enforce it. |
| Newman/Postman drift from Java tests | Medium | Medium | Shared endpoint scope and CI Newman JUnit report | Conditional live gate | Medium | Keep Postman collection focused on stable API smoke-contract behavior. |
| Broad untested resource-specific edge cases | Medium | Medium | Risk-based core coverage plus existing inherited tests | Maven build | Medium | Add resource-specific tests when changing individual resources or after production incidents. |

## Acceptance Criteria

- Maven unit/component tests cover happy and failure paths for shared utilities and selected high-risk controllers.
- Live integration tests cover success and failure paths through real HTTP calls when a test server is configured.
- Newman runs the Postman collection in CI when live environment variables/secrets are present.
- Test reports are uploaded as GitHub Actions artifacts.
- This audit explains what was added, why it exists, where it lives, how to run it, and what risk remains.
