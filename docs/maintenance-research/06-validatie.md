# 06 Validatie

## SOLID-vervolgmeting

De cijfers hieronder vergelijken de eerste registry-splitsing (`0c796f5`) met de huidige interface- en injectierefactor. Ze zijn gegenereerd met `scripts/measure-registry-architecture.ps1`.

| Metric | Eerste splitsing | SOLID-vervolg | Interpretatie |
|---|---:|---:|---|
| LOC `RestServiceImpl` | 197 | 159 | Dependencybeheer en lazy concrete constructie zijn uit de facade verwijderd. |
| Import fan-out `RestServiceImpl` | 16 | 16 | Geen verdere fan-outwinst in de facade. |
| Publieke registry-interface-operaties | 0 | 8 | Bewuste API-uitbreiding voor DIP/OCP/reuse. |
| Totale fysieke LOC gemeten subsysteem | 741 | 793 | +52 door interfaces, contracts en fail-fast constructor. Geen LOC-reductie claimen. |
| Totale nonblank LOC | 618 | 667 | +49; architectuurwinst kost extra expliciete code. |
| Unieke imports subsysteem | 32 | 34 | Totale dependencyset is licht gegroeid. |
| Rough control-flow tokens | 85 | 87 | Praktisch gelijk; deze refactor verlaagt complexiteit niet aantoonbaar. |

De tokenmeting is een regex-indicator en geen AST-gebaseerde cyclomatic complexity. Evidence: `evidence/registry-solid-metrics.md`.

## Tests

| Scope | Resultaat |
|---|---|
| Directe registry-, dependency- en OCP-tests | 19 groen |
| Bestaande `RestServiceImplTest` characterization suite | 53 groen |
| `mvn -pl omod-common test` | 149 groen |
| `mvn clean verify` | groen: `omod-common` 149 en `omod` 1803 tests, 0 failures/errors, 14 skipped |

## Conclusie

DIP, registry-level OCP, ISP, directe testability en reusability zijn nu met code en tests aantoonbaar. LOC, totale fan-out en control-flow complexity zijn niet verbeterd en worden daarom niet als winst geclaimd. OCP geldt voor het vervangen van een gehele registry; wijzigingen aan het interne resource- of selectiemechanisme vereisen nog steeds aanpassing van de default implementation.
