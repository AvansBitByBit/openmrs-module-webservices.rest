# After metrics

Datum/tijd: 2026-06-17 23:36:01 +02:00

## Volume

Java files totaal: 676
Production Java files: 345

| Module | Java files | Production files | Test files |
|---|---:|---:|---:|
| omod-common | 159 | 124 | 35 |
| omod | 512 | 221 | 291 |
| integration-tests | 5 | 0 | 5 |

## Top 20 grootste production Java files

| LOC | File |
|---:|---|
| 1241 | `omod-common\src\main\java\org\openmrs\module\webservices\docs\swagger\SwaggerSpecificationCreator.java` |
| 951 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\RestUtil.java` |
| 902 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\BaseDelegatingResource.java` |
| 738 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\api\impl\RestServiceImpl.java` |
| 717 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ConceptResource1_8.java` |
| 628 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\ConversionUtil.java` |
| 584 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ObsResource1_8.java` |
| 568 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PersonResource1_8.java` |
| 425 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\UserResource1_8.java` |
| 412 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs2_0\ConceptProposalResource2_0.java` |
| 408 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_9\VisitResource1_9.java` |
| 392 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PatientResource1_8.java` |
| 350 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_9\SystemSettingResource1_9.java` |
| 331 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_10\OrderResource1_10.java` |
| 328 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ModuleActionResource1_8.java` |
| 326 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PersonAttributeResource1_8.java` |
| 316 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\DelegatingCrudResource.java` |
| 316 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PersonAddressResource1_8.java` |
| 315 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs2_0\PatientAllergyResource2_0.java` |
| 302 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\LocationResource1_8.java` |

## Top 20 rough complexity hotspots per file

| Decision tokens | LOC | File |
|---:|---:|---|
| 158 | 1241 | `omod-common\src\main\java\org\openmrs\module\webservices\docs\swagger\SwaggerSpecificationCreator.java` |
| 151 | 738 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\api\impl\RestServiceImpl.java` |
| 148 | 951 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\RestUtil.java` |
| 134 | 902 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\BaseDelegatingResource.java` |
| 96 | 628 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\ConversionUtil.java` |
| 80 | 584 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ObsResource1_8.java` |
| 54 | 568 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PersonResource1_8.java` |
| 47 | 717 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ConceptResource1_8.java` |
| 46 | 328 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ModuleActionResource1_8.java` |
| 39 | 316 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\DelegatingCrudResource.java` |
| 34 | 244 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\api\SearchQuery.java` |
| 34 | 425 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\UserResource1_8.java` |
| 33 | 392 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\PatientResource1_8.java` |
| 30 | 412 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs2_0\ConceptProposalResource2_0.java` |
| 29 | 239 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ModuleResource1_8.java` |
| 29 | 315 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs2_0\PatientAllergyResource2_0.java` |
| 29 | 270 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_9\ObsTreeResource1_9.java` |
| 29 | 408 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_9\VisitResource1_9.java` |
| 28 | 190 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\search\openmrs1_8\ConceptSearchHandler1_8.java` |
| 27 | 219 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\TaskActionResource1_8.java` |

## ConversionUtil.convert(Object, Type) na PoC

Methode: `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java:178`

| Metric | Na PoC | Verschil |
|---|---:|---:|
| Methode LOC | 39 | -79 |
| Cyclomatic complexity volgens rubric-regels | 12 | -11 |
| Gerichte ConversionUtil tests | 26 | +4 |

Nieuwe helpers houden deelgedrag apart: collectie/array-conversie, string-conversie, date parsing, class loading, `valueOf(String)` en number conversion.

## Coverage after

ConversionUtil line coverage: 167/257 = 64.98%
ConversionUtil branch coverage: 100/176 = 56.82%

## Teststatus after

- `mvn --batch-mode --no-transfer-progress -pl omod-common test -Dtest=ConversionUtilTest jacoco:report`: BUILD SUCCESS, 26 tests, 0 failures, 0 errors.
- `mvn --batch-mode --no-transfer-progress clean test`: zelfde reactorprobleem als baseline bij `webservices.rest-omod`, namelijk `maven-dependency-plugin:unpack-dependencies` voordat `omod` tests starten. `omod-common` draait wel groen met 121 tests.
