# 05 PoC-verantwoording

## Gewijzigde code

Codewijzigingen:

- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java`
- `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java`

Git evidence: `docs/maintenance-research/evidence/git-evidence.md`.

## Ontwerp naar implementatie

| Ontwerpkeuze | Implementatie | Testbewijs | Metricbewijs |
|---|---|---|---|
| Public API gelijk houden | `public static Object convert(Object object, Type toType)` staat nog op `ConversionUtil.java:178`. | `ConversionUtilTest` groen met 26 tests. | `evidence/after-conversionutil-test-final.txt` |
| Collection/array pad apart | `convertToCollectionOrArray`, `convertToArray`, `createCollection`. | `convert_shouldConvertParameterizedListElements`. | LOC methode 118 naar 39, CC 23 naar 12. |
| Stringpad apart | `convertFromString`, `convertStringToDate`, `convertStringToClass`, `convertUsingValueOf`. | `convert_shouldConvertStringUsingValueOfMethod` plus bestaande date/class/enum tests. | Branch coverage 50.58% naar 56.82%. |
| Numberpad apart | `convertNumber`. | `convert_shouldConvertDoubleToFloat`. | Rough decision tokens class 100 naar 96. |
| `null` en "geen conversie" scheiden | `NO_CONVERSION` sentinel. | Focused tests groen, geen exception-regressie gezien. | Evidence in `rg` output en tests. |

## Codecontrole

| Controlepunt | Resultaat |
|---|---|
| Helper methods bestaan | Ja: `getRawClass`, `convertToCollectionOrArray`, `convertToArray`, `createCollection`, `convertFromString`, `convertStringToDate`, `convertStringToClass`, `convertUsingValueOf`, `convertNumber`. |
| Public API blijft gelijk | Ja, de public `convert(Object, Type)` signature is behouden. |
| Exceptiongedrag | `ConversionException` blijft het public fouttype voor mislukte conversies. |
| Geen brede cleanup | Codewijziging blijft bij `ConversionUtil` plus tests. Docs/evidence zijn apart. |
| Testdekking | `ConversionUtilTest` 22 naar 26 tests, groen. `omod-common` na PoC 121 tests groen. |

## Kritische AI/toolingreflectie

| Moment | Tool | Output | Wat is overgenomen | Wat is aangepast/verworpen | Controle | Risico |
|---|---|---|---|---|---|---|
| Opdracht vertalen | Codex | Structuur voor rapport en PoC-keuze. | Indeling analyse/test/ontwerp/validatie. | Te formele formuleringen zijn simpeler gemaakt. | Vergeleken met opdracht-MD en follow-up. | AI kan rubric-eisen missen of te algemeen schrijven. |
| Metrieken | PowerShell, Maven, PMD CPD | LOC, rough complexity, import fan-out, CPD duplicatie. | Alleen reproduceerbare waarden in rapport. | Claims zonder bewijs zijn verzwakt of verwijderd. | Raw output in `evidence/`. | Coverage of complexity verkeerd interpreteren. |
| Refactor | Codex + handmatige inspectie | Extract Method voorstel. | Kleine private helpers. | Geen Strategy/Factory, want te breed. | `git diff`, tests, public signature check. | AI kan OpenMRS-gedrag verkeerd aannemen. |
| Tests | Maven/Surefire/JaCoCo | Testresultaten en coverage. | Focused en module evidence. | Full reactor niet als groen bewijs gebruikt. | Logs bewaard. | Te snel "geen regressie" claimen terwijl scope beperkt is. |
| Documentatie | Codex | Rapporttekst. | Tabellen en interpretatie. | Claims gekoppeld aan evidencebestanden. | Claim audit en rubric self-assessment. | Niet-bestaande methodes/bestanden verzinnen. |

Belangrijkste AI-risico was te breed refactoren of mooie claims schrijven zonder bewijs. Daarom is de PoC klein gehouden en zijn testresultaten, metricbestanden en git evidence leidend gemaakt.
