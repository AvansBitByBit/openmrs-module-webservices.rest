# 02 Teststrategie

## Doel

De teststrategie moet aantonen wat wel en niet bewezen is. Voor deze PoC is de harde scope `omod-common` en vooral `ConversionUtil`. De full reactor is ook geprobeerd, maar die is geen groen bewijs.

## Commands

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-8.0.482.8-hotspot'
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress -pl omod-common test
mvn --batch-mode --no-transfer-progress -pl omod-common test -Dtest=ConversionUtilTest jacoco:report
mvn --batch-mode --no-transfer-progress clean verify
```

## Baseline en after resultaten

| Command | Baseline | Na PoC | Evidence | Conclusie |
|---|---|---|---|---|
| `mvn clean test` | `omod-common` groen met 117 tests, daarna failure in `webservices.rest-omod` bij `maven-dependency-plugin:unpack-dependencies`. | `omod-common` groen met 121 tests, daarna dezelfde plugin failure. | `evidence/baseline-mvn-test.txt`, `evidence/after-mvn-test.txt` | Geen nieuwe regressie aangetoond binnen het deel dat draaide; full reactor blijft rood. |
| `mvn -pl omod-common test` | Niet los als baseline-run bewaard. In baseline full reactor draaide `omod-common` wel groen met 117 tests. | BUILD SUCCESS, 121 tests, 0 failures, 0 errors. | `evidence/baseline-mvn-test.txt`, `evidence/after-omod-common-test.txt` | After-scope voor `omod-common` is groen. |
| `mvn -pl omod-common test -Dtest=ConversionUtilTest jacoco:report` | BUILD SUCCESS, 22 tests, 0 failures, 0 errors. | BUILD SUCCESS, 26 tests, 0 failures, 0 errors. | `evidence/baseline-conversionutil-test.txt`, `evidence/after-conversionutil-test-final.txt` | Hardste regressiebewijs voor de PoC. |
| `mvn clean verify` | Geen baseline-run bewaard. | BUILD FAILURE in `omod` door 1 testfailure: `ClearDbCacheController2_0Test.clearDbCache_shouldEvictTheEntityFromTheCaches`. | `evidence/after-mvn-verify.txt` | Niet gebruiken als "geen regressie" bewijs, want baseline ontbreekt. Wel eerlijk opnemen als open risico. |

Belangrijk: de juiste conclusie is dus niet "alles is regressievrij". De juiste conclusie is: geen nieuwe regressie aangetoond binnen de uitgevoerde `omod-common`/`ConversionUtil` scope; de full reactor is niet groen en apart gedocumenteerd.

## Testtypen

| Testtype | Toepassing |
|---|---|
| Unit/component tests | `ConversionUtilTest` en de `omod-common` tests draaien in Maven/Surefire tegen OpenMRS test support. |
| Characterization tests | Bestaande `ConversionUtilTest` legt gedrag vast voor dates, locales, enums, arrays, classes, maps en custom representations. |
| Regression tests | Vier nieuwe tests leggen refactorgevoelige paden vast. |
| Coverage analyse | JaCoCo op `ConversionUtil`, evidence in `coverage-baseline.txt` en `coverage-after.txt`. |
| Integration tests | Niet live uitgevoerd tegen een draaiende OpenMRS server. Er was geen aparte OpenMRS server op `localhost:8080/openmrs` als testdoel in deze opdracht. Compensatie: Maven unit/component scope en full reactor pogingen zijn vastgelegd. |

## Extra regressietests

| Testcase | Type | Branch/pad | Testdata | Verwachte uitkomst | Resultaat | Waarom relevant |
|---|---|---|---|---|---|---|
| `convert_shouldConvertParameterizedListElements` | Regression | `ParameterizedType` collection branch | `["5", "8"]` naar `List<Integer>` | `[5, 8]` | Groen | Bewijst dat collection elementconversie na Extract Method blijft werken. |
| `convert_shouldConvertStringUsingValueOfMethod` | Regression | `valueOf(String)` fallback | `"true"` naar `Boolean.class` | `Boolean.TRUE` | Groen | Dekt fallback die snel kapot kan gaan bij refactor. |
| `convert_shouldConvertDoubleToFloat` | Regression | Number branch | `5d` naar `Float.class` | `5f` | Groen | Dekt numerieke conversie buiten string-pad. |
| `convert_shouldConvertBooleanToString` | Regression | Boolean naar string branch | `true` naar `String.class` | `"true"` | Groen | Dekt apart eindpad in `convert`. |

## Coverage

| Moment | Line coverage `ConversionUtil` | Branch coverage `ConversionUtil` | Evidence |
|---|---:|---:|---|
| Baseline | 147/248 = 59.27% | 87/172 = 50.58% | `evidence/coverage-baseline.txt` |
| Na PoC | 167/257 = 64.98% | 100/176 = 56.82% | `evidence/coverage-after.txt` |

Coverage bewijst niet automatisch kwaliteit, maar hier ondersteunt het de testability-claim: er zijn meer branches van de gekozen hotspot geraakt.
