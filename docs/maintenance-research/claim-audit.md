# Claim audit

| Claim | Status | Evidence |
|---|---|---|
| `RestServiceImpl` was een maintainability hotspot | Onderbouwd | `restservice-baseline-metrics.md`: 737 LOC, 32 imports, 170 rough decision tokens. |
| Maven buildfaseprobleem is geisoleerd en opgelost | Onderbouwd | `buildfix-clean-test.txt`, `buildfix-clean-verify.txt`, commit `5129d2d`. |
| Functioneel `RestService` API is niet gewijzigd | Onderbouwd | `RestService` is gelijk gebleven en de 53 characterization tests zijn groen. |
| `RestServiceImpl` volgt Dependency Inversion | Onderbouwd | Alleen `ResourceRegistry`, `SearchHandlerRegistry` en `ExecutorService` worden via de constructor ontvangen. |
| Registries zijn vervangbaar zonder facadewijziging | Onderbouwd | `RestServiceImplDependencyTest` injecteert alternatieve implementations. |
| Registries zijn direct getest | Onderbouwd | 19 tests voor registries, dependency-injectie en OCP zijn groen. |
| Geen publieke API-uitbreiding | Niet claimen | Twee publieke registry-interfaces met samen acht operaties en twee default implementations zijn toegevoegd. |
| SOLID-vervolg verlaagt totale LOC/complexiteit | Niet claimen | LOC 741 naar 793 en rough control-flow tokens 85 naar 87. |
| Focused RestService gedrag is behouden | Onderbouwd | `refactor-focused-restserviceimpl-reactor-test-final.txt`: 53 tests groen. |
| `omod-common` blijft groen | Onderbouwd | `refactor-omod-common-test.txt`: 121 tests groen. |
| Full reactor is altijd groen | Niet claimen | `refactor-clean-test.txt` was rood op `ClearDbCacheController2_0Test`; `refactor-clean-verify.txt` was wel groen. |
| Live OpenMRS integration is bewezen | Niet claimen | Docker daemon draaide niet; zie `docker-build-module.txt` en `live-session-curl.txt`. |
| Onderhoudbaarheid is repo-breed volledig opgelost | Niet claimen | Alleen de gekozen hotspot is verbeterd; andere hotspots blijven vervolgwerk. |
