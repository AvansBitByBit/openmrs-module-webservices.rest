# Registry SOLID-refactor metrics

Generated: 2026-07-02T21:19:06+02:00

Command:

```powershell
./docs/maintenance-research/scripts/measure-registry-architecture.ps1
```

## Huidige werkboom

| File | Lines | Nonblank | Import fan-out | Rough control-flow tokens | Public methods | Interface methods |
|---|---:|---:|---:|---:|---:|---:|
| `RestServiceImpl.java` | 159 | 140 | 16 | 18 | 10 | 0 |
| `ResourceRegistry.java` | 34 | 27 | 4 | 0 | 0 | 4 |
| `SearchHandlerRegistry.java` | 35 | 28 | 5 | 0 | 0 | 4 |
| `DefaultResourceRegistry.java` | 255 | 207 | 18 | 28 | 4 | 0 |
| `DefaultSearchHandlerRegistry.java` | 310 | 265 | 19 | 41 | 6 | 0 |

- Unique imports in het gemeten subsysteem: 34
- Totale fysieke LOC: 793
- Totale nonblank LOC: 667
- Totale rough control-flow tokens: 87

## Vergelijkingspunt `0c796f5`

| Metric | Waarde |
|---|---:|
| Totale fysieke LOC facade + twee package-private registries | 741 |
| Totale nonblank LOC | 618 |
| Unique imports | 32 |
| Rough control-flow tokens met hetzelfde script | 85 |

Interpretatie: de SOLID-vervolgstap verbetert dependencyrichting, vervangbaarheid, directe testbaarheid en herbruikbaarheid. Hij vermindert de totale codevolume, dependencyset of control-flow complexity niet. De regexmeting is indicatief en telt tokens in broncode; ze is geen formele cyclomatic-complexitymeting.
