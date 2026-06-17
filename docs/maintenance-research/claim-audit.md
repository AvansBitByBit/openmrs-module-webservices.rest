# Claim audit

| Claim | Status | Evidence |
|---|---|---|
| `RestServiceImpl` was een maintainability hotspot | Onderbouwd | `restservice-baseline-metrics.md`: 737 LOC, 32 imports, 170 rough decision tokens. |
| Maven buildfaseprobleem is geisoleerd en opgelost | Onderbouwd | `buildfix-clean-test.txt`, `buildfix-clean-verify.txt`, commit `5129d2d`. |
| Public `RestService` API is niet gewijzigd | Onderbouwd | Code diff: alleen interne impl en package-private registries. |
| Focused RestService gedrag is behouden | Onderbouwd | `refactor-focused-restserviceimpl-reactor-test-final.txt`: 53 tests groen. |
| `omod-common` blijft groen | Onderbouwd | `refactor-omod-common-test.txt`: 121 tests groen. |
| Full reactor is altijd groen | Niet claimen | `refactor-clean-test.txt` was rood op `ClearDbCacheController2_0Test`; `refactor-clean-verify.txt` was wel groen. |
| Live OpenMRS integration is bewezen | Niet claimen | Docker daemon draaide niet; zie `docker-build-module.txt` en `live-session-curl.txt`. |
| Onderhoudbaarheid is repo-breed volledig opgelost | Niet claimen | Alleen de gekozen hotspot is verbeterd; andere hotspots blijven vervolgwerk. |
