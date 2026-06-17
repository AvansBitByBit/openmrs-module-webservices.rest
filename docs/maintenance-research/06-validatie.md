# 06 Validatie

## After commands

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot'
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
mvn --batch-mode --no-transfer-progress -pl omod-common test
mvn --batch-mode --no-transfer-progress -pl omod-common test -Dtest=ConversionUtilTest jacoco:report
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
```

## Before/after validatie

| Metric | Baseline commit | Na PoC working tree | Verschil | Bewijsbestand | Interpretatie |
|---|---:|---:|---:|---|---|
| LOC `convert(Object, Type)` | 118 | 39 | -79 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | De hoofdroute is veel kleiner en beter analyseerbaar. |
| CC `convert(Object, Type)` | 23 | 12 | -11 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | Minder beslispaden in de public methode. |
| Class LOC `ConversionUtil` | 593 | 628 | +35 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | Class werd groter door helpers, maar complexiteit is uit de hoofdroute gehaald. |
| Rough decision tokens `ConversionUtil` | 100 | 96 | -4 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | Kleine daling op classniveau; winst zit vooral in methode-niveau. |
| `ConversionUtilTest` tests | 22 | 26 | +4 | `evidence/baseline-conversionutil-test.txt`, `evidence/after-conversionutil-test-final.txt` | Extra regressiepaden zijn toegevoegd. |
| Line coverage `ConversionUtil` | 59.27% | 64.98% | +5.71 pp | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` | Meer regels van de hotspot geraakt. |
| Branch coverage `ConversionUtil` | 50.58% | 56.82% | +6.24 pp | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` | Meer beslissingen getest. |
| Focused `ConversionUtilTest` | 22 groen | 26 groen | +4 groen | `evidence/after-conversionutil-test-final.txt` | Hardste regressiebewijs voor de PoC. |
| `omod-common` tests | 117 groen binnen baseline reactor | 121 groen standalone | +4 tests | `evidence/baseline-mvn-test.txt`, `evidence/after-omod-common-test.txt` | Module-scope is groen na PoC. |
| `mvn clean test` | Failure in `webservices.rest-omod` dependency plugin | Zelfde failure in `webservices.rest-omod` dependency plugin | Geen bewezen nieuwe failure in deze command | `evidence/baseline-mvn-test.txt`, `evidence/after-mvn-test.txt` | Full reactor blijft rood, maar deze failure bestond al. |
| `mvn clean verify` | Niet bewaard als baseline | Failure in `omod`: 1797 tests, 1 failure, 14 skipped | Niet vergelijkbaar | `evidence/after-mvn-verify.txt` | Open risico, niet gebruiken als regressievrij bewijs. |
| PMD CPD duplicatie | Niet voor/na vergeleken | 15 duplicate blocks gemeten | n.v.t. | `evidence/duplication.md` | Contextmetric, geen PoC-validatiemetric. |

## Regressieconclusie

Geen nieuwe regressie aangetoond binnen de uitgevoerde `omod-common`/`ConversionUtil` scope. De focused `ConversionUtilTest` is groen met 26 tests en de standalone `omod-common` testscope is groen met 121 tests.

De full reactor is niet groen. `mvn clean test` faalt baseline en after op dezelfde bestaande `maven-dependency-plugin:unpack-dependencies` failure in `webservices.rest-omod`. `mvn clean verify` is alleen na de PoC bewaard en faalt later in `omod` op `ClearDbCacheController2_0Test`; omdat er geen baseline verify-run is, kan daar geen regressieconclusie uit worden getrokken.

## Onderhoudbaarheidsconclusie

Voor de gekozen hotspot is onderhoudbaarheid lokaal verbeterd:

- analyzability: `convert(Object, Type)` daalt van 118 naar 39 LOC;
- testability: `ConversionUtilTest` groeit van 22 naar 26 tests en coverage stijgt;
- modifiability: collection, string, date/class/valueOf en number logica zitten niet meer door elkaar;
- regressierisico: public API blijft gelijk en de relevante scope is groen.

De verbetering is bewust lokaal. De repo als geheel heeft nog open onderhoudbaarheidsrisico's in `RestUtil`, `RestServiceImpl`, `BaseDelegatingResource`, Swagger en de full reactor build/teststatus.
