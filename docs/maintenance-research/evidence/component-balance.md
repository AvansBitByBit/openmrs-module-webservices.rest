# Component balance

Command: PowerShell scan over `**/src/main/java/**/*.java` and `**/src/test/java/**/*.java`. LOC = non-empty lines, comments included.

## Java files en LOC per module/scope
| Module | Scope | Java files | LOC |
|---|---|---:|---:|
| omod-common | main | 124 | 11783 |
| omod-common | test | 35 | 3167 |
| omod | main | 221 | 27166 |
| omod | test | 291 | 29067 |
| integration-tests | main | 0 | 0 |
| integration-tests | test | 5 | 264 |

## Top productiepackages op aantal bestanden
| Package | Files | LOC |
|---|---:|---:|
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8` | 47 | 10115 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9` | 37 | 4573 |
| `org.openmrs.module.webservices.docs.swagger` | 23 | 2181 |
| `org.openmrs.module.webservices.rest.web.resource.api` | 20 | 953 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0` | 19 | 2288 |
| `org.openmrs.module.webservices.rest.web.resource.impl` | 17 | 2755 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10` | 15 | 1971 |
| `org.openmrs.module.webservices.rest.web.response` | 12 | 359 |
| `org.openmrs.module.webservices.rest.web` | 11 | 2167 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2` | 11 | 957 |
| `org.openmrs.module.webservices.helper` | 7 | 508 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11` | 7 | 563 |
| `org.openmrs.module.webservices.rest.web.annotation` | 7 | 196 |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0` | 7 | 537 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12` | 6 | 585 |

## Interpretatie

De code is niet gelijk verdeeld. `omod` bevat veel concrete resources en controllers, terwijl `omod-common` minder files heeft maar juist de gedeelde frameworkklassen bevat. Dat betekent dat kleine wijzigingen in `omod-common` relatief veel impact kunnen hebben.
