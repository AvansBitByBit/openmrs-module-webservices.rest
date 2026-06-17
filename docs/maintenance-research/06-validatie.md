# 06 Validatie

## Metrics

| Metric | Baseline | Na PoC | Interpretatie |
|---|---:|---:|---|
| LOC `RestServiceImpl` | 737 | 197 | Facade veel kleiner. |
| Rough decision tokens `RestServiceImpl` | 170 | 19 | Veel minder beslislogica in facade. |
| Import fan-out `RestServiceImpl` | 32 | 16 | Minder directe afhankelijkheden. |
| `ResourceRegistry` LOC | n.v.t. | 244 | Cohesive class, kleiner dan oude god-class. |
| `SearchHandlerRegistry` LOC | n.v.t. | 300 | Cohesive class, kleiner dan oude god-class. |
| Nieuwe package cycles | n.v.t. | 0 | Registries blijven in bestaande impl package. |

Evidence:

- `restservice-baseline-metrics.md`
- `restservice-after-metrics.md`

## Tests

| Command | Resultaat |
|---|---|
| focused reactor `RestServiceImplTest` | groen, 53 tests |
| `mvn -pl omod-common test` | groen, 121 tests |
| `mvn clean test` | rood, 1 failure in `ClearDbCacheController2_0Test` |
| `mvn clean verify` | groen |
| `git diff --check` | groen |

## Regressieconclusie

Binnen de gekozen hotspot is geen regressie aangetoond: de bestaande 53 characterization tests blijven groen. Ook `omod-common` en `clean verify` zijn groen.

Ik claim niet dat de hele repo altijd groen is, want `clean test` had een rode run op een bestaande/flaky cache-test buiten `RestServiceImpl`. Live integration is geprobeerd, maar Docker Desktop draaide niet, dus dat is niet bewezen.
