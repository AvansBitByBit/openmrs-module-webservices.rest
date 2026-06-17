# 03 Verbeterplan

## Prioritering

Scoreformule:

```text
Totaal = impact*2 + bewijs*2 + regressierisico + effort + testbaarheid + onderwijswaarde
```

| Optie | Hotspot | Verbetering | Impact | Bewijs | Risico | Effort | Testbaar | Onderwijs | Totaal | Keuze |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|---|
| V1 | `RestServiceImpl` | Split facade in `ResourceRegistry` en `SearchHandlerRegistry` | 5 | 5 | 4 | 3 | 5 | 5 | 42 | Ja |
| V2 | `ConversionUtil` | Extract Method in `convert` | 2 | 4 | 5 | 5 | 4 | 3 | 31 | Appendix |
| V3 | `RestUtil` | Utility splitsen per verantwoordelijkheid | 4 | 4 | 2 | 2 | 3 | 4 | 31 | Nee |
| V4 | `BaseDelegatingResource` | Base class opdelen | 5 | 4 | 1 | 1 | 2 | 5 | 29 | Nee |

## Gekozen verbetering

De gekozen PoC is V1. Reden: `RestServiceImpl` is architectonisch centraler dan `ConversionUtil`, heeft goede bestaande tests en de verbetering is meetbaar zonder het publieke `RestService` API te wijzigen.

## Afbakening

Wel:

- `RestServiceImpl` kleiner maken als facade/coordinator;
- `ResourceRegistry` en `SearchHandlerRegistry` package-private toevoegen;
- public API en exceptionteksten gelijk houden.

Niet:

- grote rewrite van resources/controllers;
- nieuwe public registry interfaces;
- live OpenMRS gedrag aanpassen.
