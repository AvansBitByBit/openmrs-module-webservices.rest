# Import coupling

Command: PowerShell import-scan over production Java files. Imports zijn gegroepeerd per package. Wildcard/static imports tellen mee als tekstuele import.

## Top files op fan-out imports
| Unique imports | Internal own imports | OpenMRS core imports | Third-party imports | File |
|---:|---:|---:|---:|---|
| 24 | 9 | 3 | 9 | `omod-common\src\main\java\org\openmrs\module\webservices\docs\swagger\SwaggerSpecificationCreator.java` |
| 23 | 9 | 4 | 7 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\BaseDelegatingResource.java` |
| 21 | 4 | 5 | 7 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\RestUtil.java` |
| 20 | 8 | 4 | 4 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\ConversionUtil.java` |
| 20 | 8 | 4 | 4 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ObsResource1_8.java` |
| 20 | 10 | 2 | 4 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ModuleActionResource1_8.java` |
| 19 | 6 | 2 | 9 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\controller\openmrs1_8\HL7MessageController1_8.java` |
| 17 | 5 | 6 | 4 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\controller\openmrs2_0\VisitConfigurationController2_0.java` |
| 17 | 5 | 4 | 6 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\controller\openmrs1_9\SessionController1_9.java` |
| 17 | 7 | 5 | 3 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs2_0\ConceptProposalResource2_0.java` |
| 17 | 8 | 4 | 3 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ConceptResource1_8.java` |
| 16 | 3 | 3 | 6 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\controller\openmrs1_8\ObsComplexValueController1_8.java` |
| 16 | 6 | 2 | 8 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\controller\openmrs2_0\ClearDbCacheController2_0.java` |
| 16 | 7 | 3 | 2 | `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\api\impl\RestServiceImpl.java` |
| 16 | 7 | 2 | 4 | `omod\src\main\java\org\openmrs\module\webservices\rest\web\v1_0\resource\openmrs1_8\ModuleResource1_8.java` |

## Top packages op gemiddelde fan-out
| Package | Files | Avg unique imports | Max unique imports |
|---|---:|---:|---:|
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8` | 3 | 16 | 19 |
| `org.openmrs.module.webservices.rest.web.api.impl` | 2 | 15 | 16 |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0` | 7 | 12.86 | 17 |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9` | 4 | 12.5 | 17 |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_2` | 1 | 12 | 12 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_0` | 1 | 12 | 12 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8` | 47 | 11.47 | 20 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_2` | 1 | 11 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs1_8` | 1 | 11 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_3` | 1 | 11 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs2_2` | 1 | 11 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_10` | 4 | 10.25 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_9` | 6 | 10.17 | 12 |
| `org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_8` | 6 | 9.83 | 11 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10` | 15 | 9.4 | 15 |

## Interpretatie

Fan-out laat zien welke bestanden veel andere packages kennen. Dat raakt ISO 25010 modularity en analyzability: hoe meer directe imports, hoe meer context je moet controleren bij een wijziging. Deze meting bewijst geen runtime-coupling, maar geeft wel een reproduceerbare structurele indicatie.
