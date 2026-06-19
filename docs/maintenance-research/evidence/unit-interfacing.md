# Unit interfacing hotspots

Command: PowerShell regex scan over hotspot files. De telling is indicatief: public/protected method signatures in source, inclusief overrides; constructors kunnen door de regex meegeteld worden als ze op een methodesignature lijken. LOC = non-empty lines, comments included.

| Hotspot | Public/protected methods | Max parameters | Avg parameters | Methods met >=4 parameters | LOC |
|---|---:|---:|---:|---:|---:|
| `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\ConversionUtil.java` | 17 | 3 | 1.94 | 0 | 546 |
| `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\RestUtil.java` | 25 | 3 | 1.24 | 0 | 861 |
| `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\api\impl\RestServiceImpl.java` | 21 | 3 | 0.52 | 0 | 631 |
| `omod-common\src\main\java\org\openmrs\module\webservices\rest\web\resource\impl\BaseDelegatingResource.java` | 37 | 5 | 1.35 | 1 | 816 |
| `omod-common\src\main\java\org\openmrs\module\webservices\docs\swagger\SwaggerSpecificationCreator.java` | 12 | 2 | 0.75 | 0 | 1029 |

## Interpretatie

Unit interfacing zegt iets over hoeveel oppervlak een class aanbiedt. Grote public/protected oppervlakken verhogen testlast en wijzigingsrisico. Voor de PoC is `ConversionUtil` gunstig: de public API blijft klein, dus intern refactoren kan zonder callers aan te passen.
