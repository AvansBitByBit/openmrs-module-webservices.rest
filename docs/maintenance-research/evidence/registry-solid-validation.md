# Registry SOLID-refactor validation

Datum: 2026-07-02

| Command/scope | Resultaat |
|---|---|
| `mvn -pl omod-common -Dtest=DefaultResourceRegistryTest,DefaultSearchHandlerRegistryTest,RestServiceImplDependencyTest test` | 19 tests groen |
| Focused reactor `RestServiceImplTest` | 53 tests groen |
| `mvn -pl omod-common test` | 149 tests groen |
| `mvn clean verify` | BUILD SUCCESS; `omod-common` 149 en `omod` 1803 tests, 0 failures/errors, 14 skipped |

De directe tests omvatten resource lookup, version filtering, duplicate order, superclassselectie, scanner- en instantiatiefouten, handlers, refresh, search matching, ambiguity, duplicate IDs, delegatie, async refresh en injectie van alternatieve registry-implementaties.

De integration-tests-module bevatte in deze Maven-run geen Surefire XML-resultaten. `clean verify` bewijst daarom de unit/component-reactor en Spring-context, niet een live HTTP/OpenMRS-integratietest.
