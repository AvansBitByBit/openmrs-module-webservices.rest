# Package cycles and layer scan

Command: PowerShell import-edge scan over production Java imports starting with `org.openmrs.module.webservices`. Dit detecteert directe tweerichtingsafhankelijkheden tussen packages; langere cycles zijn hiermee niet volledig bewezen of uitgesloten.

## Directe tweerichtingsafhankelijkheden
| Package A | Package B |
|---|---|
| `org.openmrs.module.webservices.docs.swagger` | `org.openmrs.module.webservices.rest` |
| `org.openmrs.module.webservices.rest` | `org.openmrs.module.webservices.rest.util` |
| `org.openmrs.module.webservices.rest` | `org.openmrs.module.webservices.rest.web` |
| `org.openmrs.module.webservices.rest.util` | `org.openmrs.module.webservices.rest.web.resource.impl` |
| `org.openmrs.module.webservices.rest.web` | `org.openmrs.module.webservices.rest.web.representation` |
| `org.openmrs.module.webservices.rest.web` | `org.openmrs.module.webservices.rest.web.resource.api` |
| `org.openmrs.module.webservices.rest.web` | `org.openmrs.module.webservices.rest.web.resource.impl` |
| `org.openmrs.module.webservices.rest.web.annotation` | `org.openmrs.module.webservices.rest.web.representation` |
| `org.openmrs.module.webservices.rest.web.annotation` | `org.openmrs.module.webservices.rest.web.resource.impl` |
| `org.openmrs.module.webservices.rest.web.api` | `org.openmrs.module.webservices.rest.web.resource.impl` |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8` | `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8` |

## Meeste interne package edges
| Package | Internal outgoing packages |
|---|---:|
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0` | 15 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8` | 14 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2` | 14 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10` | 11 |
| `org.openmrs.module.webservices.rest.web.resource.impl` | 10 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_1` | 10 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_4` | 10 |
| `org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0` | 10 |
| `org.openmrs.module.webservices.docs.swagger` | 9 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9` | 9 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12` | 9 |
| `org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs2_2` | 8 |
| `org.openmrs.module.webservices.rest.web` | 8 |
| `org.openmrs.module.webservices.rest.web.v1_0.converter.openmrs1_8` | 8 |
| `org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_3` | 8 |

## Interpretatie

Deze eenvoudige scan vindt 11 directe tweerichtingsafhankelijkheden tussen packages. Dat is geen volledig cycle-rapport, maar het laat wel zien dat de packagegrenzen niet overal strak zijn. Voor de PoC is dit vooral context: `ConversionUtil` zelf is een centrale util in `rest.web`, maar de gekozen wijziging voegt geen nieuwe imports of package edges toe.
