# Verbeteronderzoek onderhoudbaarheid - openmrs-module-webservices.rest

## 1. Inleiding

Dit onderzoek kijkt naar de onderhoudbaarheid van `openmrs-module-webservices.rest` met ISO 25010 Maintainability als bril. De hoofdvraag:

> In hoeverre is deze repo onderhoudbaar volgens ISO 25010 Maintainability, en welke kleine ontwerpverbetering kan dat aantoonbaar verbeteren zonder regressie binnen de uitgevoerde scope?

De hele repo is bekeken, maar de PoC is bewust klein: `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java`.

## 2. Baseline

| Onderdeel | Waarde | Evidence |
|---|---|---|
| Branch | `Onderhoudbaarheidsonderzoek` | `evidence/git-evidence.md` |
| Baseline commit | `84fdc113631bd193c6cb4502552cd8709e80d463` | `evidence/git-evidence.md` |
| PoC state | Working tree, nog niet gecommit | `evidence/git-evidence.md` |
| Java | Java 8 via Temurin | `evidence/baseline.md` |
| Maven | Maven 3.9.11 | `evidence/baseline.md` |
| Modules | `omod-common`, `omod`, `integration-tests` | `pom.xml`, `01-analyse.md` |

## 3. Architectuur en metrieken

De module-indeling is logisch: `omod-common` bevat shared frameworkcode, `omod` bevat de concrete versioned resources, en `integration-tests` bevat live API-tests. Het risico zit vooral in centrale shared classes.

Belangrijkste metrieken:

| Metric | Resultaat | Evidence |
|---|---|---|
| Volume | 676 Java files totaal, 345 production files | `evidence/baseline-metrics.md` |
| Component balance | `omod/main` 221 files/27166 LOC, `omod-common/main` 124 files/11783 LOC | `evidence/component-balance.md` |
| Import fan-out | Swagger 24, `BaseDelegatingResource` 23, `RestUtil` 21, `ConversionUtil` 20 unieke importpackages | `evidence/import-coupling.md` |
| Unit interfacing | `BaseDelegatingResource` 37 public/protected methods, `RestUtil` 25, `RestServiceImpl` 21 | `evidence/unit-interfacing.md` |
| Package coupling | 11 directe tweerichtingsafhankelijkheden tussen packages gevonden | `evidence/package-cycles.md` |
| Duplicatie | PMD CPD: 15 duplicate blocks, vooral in `omod` | `evidence/duplication.md` |

Hotspots:

| Hotspot | Metrics | Risico |
|---|---|---|
| `SwaggerSpecificationCreator.java` | 1241 LOC, 158 rough tokens, 24 importpackages | Groot en lastig te testen. |
| `RestUtil.java` | 951 LOC, 148 rough tokens, 21 importpackages | Veel web utility gedrag in een class. |
| `BaseDelegatingResource.java` | 902 LOC, 134 rough tokens, 37 public/protected methods | Base class met veel impact op subclasses. |
| `RestServiceImpl.java` | 738 LOC, 151 rough tokens | Resource/search discovery is complex. |
| `ConversionUtil.java` | 593 LOC baseline, 100 rough tokens, methode `convert` 118 LOC/CC 23 | Shared conversiegedrag met een complexe hoofdroute. |

## 4. Keuze verbeteroptie

De gekozen verbetering is `ConversionUtil.convert(Object, Type)` refactoren met Extract Method. Dit scoort het beste omdat de methode meetbaar complex is, bestaande tests heeft en de public API gelijk kan blijven. Grotere opties zoals `RestServiceImpl` of `BaseDelegatingResource` zijn relevanter voor een vervolgonderzoek, maar te breed voor een kleine PoC.

Prioritering staat uitgewerkt in `03-verbeterplan.md`.

## 5. Ontwerp

Kwaliteitseisen:

- analyzability verbeteren: hoofdroute kleiner en duidelijker;
- testability verbeteren: conversiepaden beter apart te testen;
- regressierisico beperken: public API en exceptiontype gelijk houden;
- geen pattern-forcing: geen Strategy/Factory toevoegen zonder echte noodzaak.

Voor/na diagrammen:

- `docs/diagrams/hotspot-before.puml`
- `docs/diagrams/hotspot-after.puml`
- `docs/diagrams/hotspot-sequence.puml`

De public methode blijft:

```java
public static Object convert(Object object, Type toType) throws ConversionException
```

Intern zijn helpers toegevoegd voor raw class bepaling, collections/arrays, strings, dates/classes, `valueOf` en numbers.

## 6. Realisatie PoC

Gewijzigde code:

- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java`
- `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java`

Toegevoegde regressietests:

- `convert_shouldConvertParameterizedListElements`
- `convert_shouldConvertStringUsingValueOfMethod`
- `convert_shouldConvertDoubleToFloat`
- `convert_shouldConvertBooleanToString`

## 7. Test en validatie

| Test/metric | Baseline | Na PoC | Evidence |
|---|---:|---:|---|
| `ConversionUtilTest` | 22 groen | 26 groen | `evidence/baseline-conversionutil-test.txt`, `evidence/after-conversionutil-test-final.txt` |
| `omod-common` tests | 117 groen binnen baseline reactor | 121 groen standalone | `evidence/baseline-mvn-test.txt`, `evidence/after-omod-common-test.txt` |
| LOC `convert(Object, Type)` | 118 | 39 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` |
| CC `convert(Object, Type)` | 23 | 12 | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` |
| Line coverage `ConversionUtil` | 59.27% | 64.98% | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` |
| Branch coverage `ConversionUtil` | 50.58% | 56.82% | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` |

Full reactor eerlijkheid:

- `mvn clean test` faalt baseline en after bij dezelfde bestaande `maven-dependency-plugin:unpack-dependencies` failure in `webservices.rest-omod`.
- `mvn clean verify` is alleen after bewaard en faalt in `omod` met 1 testfailure. Omdat baseline verify ontbreekt, gebruik ik dit niet als regressieclaim.

Conclusie over regressie: geen nieuwe regressie aangetoond binnen de uitgevoerde `omod-common`/`ConversionUtil` scope. Geen claim dat de volledige Maven reactor groen is.

## 8. AI en tooling

Codex is gebruikt om analyse en documentatie te structureren, maar de claims zijn gecontroleerd met Maven, JaCoCo, PMD CPD, PowerShell scans, `rg` en `git diff`. Risico's waren: niet-bestaande code verzinnen, te breed refactoren, OpenMRS-gedrag verkeerd aannemen en coverage te sterk interpreteren. Daarom staan raw logs in `docs/maintenance-research/evidence/` en is de conclusie bewust afgebakend.

## 9. Conclusie

De repo is redelijk onderhoudbaar qua module-indeling en testaanwezigheid, maar centrale frameworkclasses drukken op analyzability, modularity en testability. De PoC verbetert lokaal en aantoonbaar de gekozen hotspot: `convert(Object, Type)` daalt van 118 naar 39 LOC en van CC 23 naar CC 12, terwijl `ConversionUtilTest` en `omod-common` groen draaien na de wijziging.

De verbetering is dus echt, maar lokaal. De full reactor blijft een apart open probleem en moet niet worden weggepoetst.
