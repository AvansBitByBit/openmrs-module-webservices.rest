# Beroepsproduct LU2 onderhoudbaarheid

Dit beroepsproduct is de compacte samenvoeging van het onderhoudbaarheidsonderzoek. De detailbestanden `01` t/m `06`, de evidence en de diagrammen staan in dezelfde map.

## Hoofdvraag

> In hoeverre is `openmrs-module-webservices.rest` onderhoudbaar volgens ISO 25010 Maintainability, en welke kleine ontwerpverbetering kan dat aantoonbaar verbeteren zonder regressie binnen de uitgevoerde scope?

## Baseline

Baseline commit: `84fdc113631bd193c6cb4502552cd8709e80d463` op branch `Onderhoudbaarheidsonderzoek`. De PoC is de huidige working tree en is nog niet gecommit. Zie `evidence/git-evidence.md`.

De repo heeft drie Maven modules: `omod-common`, `omod` en `integration-tests`. `omod-common` is het meest interessant voor deze PoC, omdat daar gedeelde frameworkcode staat.

## Analyse

Gemeten onderhoudbaarheidsdata:

| Metric | Waarde | Evidence |
|---|---|---|
| Volume | 676 Java files, 345 production files | `evidence/baseline-metrics.md` |
| Component balance | `omod/main` 221 files/27166 LOC, `omod-common/main` 124 files/11783 LOC | `evidence/component-balance.md` |
| Import coupling | Swagger 24, `BaseDelegatingResource` 23, `RestUtil` 21, `ConversionUtil` 20 unieke importpackages | `evidence/import-coupling.md` |
| Duplicatie | PMD CPD vindt 15 duplicate blocks | `evidence/duplication.md` |
| Package coupling | 11 directe tweerichtingsafhankelijkheden tussen packages | `evidence/package-cycles.md` |

Belangrijkste hotspots zijn `SwaggerSpecificationCreator`, `RestUtil`, `BaseDelegatingResource`, `RestServiceImpl` en `ConversionUtil`. De gekozen PoC is `ConversionUtil.convert(Object, Type)`, omdat die methode in de baseline 118 LOC en CC 23 had, terwijl er al tests bestonden.

## Verbeterplan en ontwerp

De gekozen optie is Extract Method binnen `ConversionUtil`. Strategy/Factory of Extract Class is niet gekozen omdat dat meer wijzigingsvlak geeft dan nodig is. De public API blijft gelijk:

```java
public static Object convert(Object object, Type toType) throws ConversionException
```

Ontwerpdiagrammen:

- `docs/diagrams/hotspot-before.puml`
- `docs/diagrams/hotspot-after.puml`
- `docs/diagrams/hotspot-sequence.puml`

## PoC

Gewijzigde code:

- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java`
- `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java`

Nieuwe regressietests:

- `convert_shouldConvertParameterizedListElements`
- `convert_shouldConvertStringUsingValueOfMethod`
- `convert_shouldConvertDoubleToFloat`
- `convert_shouldConvertBooleanToString`

## Validatie

| Metric/test | Baseline | Na PoC | Evidence |
|---|---:|---:|---|
| LOC `convert(Object, Type)` | 118 | 39 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` |
| CC `convert(Object, Type)` | 23 | 12 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` |
| `ConversionUtilTest` | 22 groen | 26 groen | `evidence/baseline-conversionutil-test.txt`, `evidence/after-conversionutil-test-final.txt` |
| `omod-common` tests | 117 groen binnen baseline reactor | 121 groen standalone | `evidence/baseline-mvn-test.txt`, `evidence/after-omod-common-test.txt` |
| Line coverage `ConversionUtil` | 59.27% | 64.98% | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` |
| Branch coverage `ConversionUtil` | 50.58% | 56.82% | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` |

Full reactor: `mvn clean test` faalt baseline en after op dezelfde bestaande Maven dependency-plugin failure. `mvn clean verify` is after gedraaid en faalt in `omod` met 1 testfailure, maar er is geen baseline verify om dat eerlijk te vergelijken.

## Conclusie

De PoC verbetert de gekozen hotspot aantoonbaar binnen de uitgevoerde scope. De hoofdroute is kleiner, de CC is lager, extra regressietests zijn groen en coverage stijgt. De repo als geheel heeft nog grotere onderhoudbaarheidsproblemen in centrale classes en de full reactor build/teststatus, dus de conclusie blijft bewust afgebakend.
