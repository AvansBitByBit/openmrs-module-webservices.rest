# 01 Analyse onderhoudbaarheid

## Scope en baseline

Dit onderzoek gaat over `openmrs-module-webservices.rest`, een Maven multi-module project voor de OpenMRS REST Web Services module. De baseline is commit `84fdc113631bd193c6cb4502552cd8709e80d463` op branch `Onderhoudbaarheidsonderzoek`. De PoC staat nog niet als aparte commit in git; de after-state is de huidige working tree bovenop die baseline. Dat is vastgelegd in `docs/maintenance-research/evidence/git-evidence.md`.

Modules:

| Module | Rol |
|---|---|
| `omod-common` | Gedeeld REST-framework: converters, controllers, `RestService`, Swagger generatie en utilities. |
| `omod` | Concrete REST resources/controllers per OpenMRS versie. |
| `integration-tests` | Rest-Assured tests tegen een draaiende OpenMRS server. |

Architectuurdiagram: `docs/diagrams/baseline-architecture.puml`.

## Metrieken

| Metric | Command/evidence | Resultaat | ISO 25010 interpretatie |
|---|---|---|---|
| Volume en unit size | `evidence/baseline-metrics.md` | 676 Java files totaal, 345 production files. Grootste class: `SwaggerSpecificationCreator.java` met 1241 LOC. | Grote units drukken vooral op analyzability. Je moet meer context lezen voordat je veilig wijzigt. |
| Rough complexity | `evidence/baseline-metrics.md` | Top: Swagger 158, `RestServiceImpl` 151, `RestUtil` 148, `BaseDelegatingResource` 134, `ConversionUtil` 100 rough decision tokens. | Veel beslissingen in centrale classes maakt testen en wijzigen lastiger. |
| Component balance | `evidence/component-balance.md` | `omod-common/main`: 124 files/11783 LOC. `omod/main`: 221 files/27166 LOC. `omod/test`: 291 files/29067 LOC. | `omod` heeft de meeste concrete surface, maar `omod-common` bevat de gedeelde frameworkcode met hogere impact per wijziging. |
| Package/import coupling | `evidence/import-coupling.md` | `SwaggerSpecificationCreator` heeft 24 unieke importpackages, `BaseDelegatingResource` 23, `RestUtil` 21, `ConversionUtil` 20. | Veel fan-out betekent meer afhankelijkheden om te controleren. Dit raakt modularity en analyzability. |
| Unit interfacing | `evidence/unit-interfacing.md` | `BaseDelegatingResource` heeft 37 public/protected methods, `RestUtil` 25, `RestServiceImpl` 21, `ConversionUtil` 17. | Een groter public/protected oppervlak vraagt meer regressietests en maakt wijzigen risicovoller. |
| Package cycles/layer scan | `evidence/package-cycles.md` | 11 directe tweerichtingsafhankelijkheden tussen packages gevonden met import-scan. | Packagegrenzen zijn niet overal strak. Dit is context voor modularity, niet de gekozen PoC-driver. |
| Duplicatie | `evidence/duplication.md`, raw output `evidence/duplication-pmd-cpd.txt` | PMD CPD vindt 15 duplicate blocks: 2 in `omod-common`, 13 in `omod`. | Duplicatie zit vooral in resources/search handlers. Voor deze PoC is complexiteit belangrijker dan duplicatie. |
| Coverage gekozen hotspot | `evidence/coverage-baseline.txt` | `ConversionUtil` baseline: 59.27% line coverage, 50.58% branch coverage. | Bij een centrale conversieclass is branch coverage belangrijk voor testability. |

## Hotspotanalyse

| Hotspot | File/regels | Verantwoordelijkheid | Metrics | Design smell | ISO 25010 impact | Risico bij wijziging | Testbaarheid | Verbeteroptie |
|---|---|---|---|---|---|---|---|---|
| `ConversionUtil.convert(Object, Type)` | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java:178` after, baseline rond `:176` | Runtime conversie tussen strings, collections, arrays, maps, dates, classes, enums en numbers. | Baseline methode 118 LOC/CC 23. Class baseline 593 LOC/100 rough tokens. Coverage 59.27% line/50.58% branch. | Complexity, opacity, low cohesion binnen de methode. | Analyzability, testability, modifiability. | Middel: shared code, maar public API kan gelijk blijven. | Goed: bestaande `ConversionUtilTest` en extra regressietests mogelijk. | Extract Method binnen dezelfde class. |
| `RestUtil.java` | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/RestUtil.java:60` | Web/request helpers, paging, URI en response helpers. | 951 LOC baseline, 148 rough decision tokens, 21 unieke importpackages. | Complexity, low cohesion. | Analyzability, modifiability. | Hoger: raakt webgedrag en limit/security-achtige defaults. | Redelijk, maar breder dan PoC. | Opsplitsen per helpergebied na characterization tests. |
| `RestServiceImpl.java` | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java:49` | Resource/search-handler discovery en lookup. | 738 LOC baseline, 151 rough decision tokens, 16 unieke importpackages. | Rigidity, complexity, high coupling. | Modularity, testability. | Hoog: resource discovery raakt veel REST endpoints. | Er zijn tests, maar regressie-impact is breed. | Lookup/initialisatie scheiden in kleinere collaborators. |
| `BaseDelegatingResource.java` | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/resource/impl/BaseDelegatingResource.java:75` | Basisgedrag voor delegated resources. | 902 LOC baseline, 134 rough tokens, 37 public/protected methods, 23 unieke importpackages. | Fragility, high coupling, complexity. | Reusability, testability, analyzability. | Hoog: base class wijziging raakt veel subclasses. | Moeilijker, omdat veel gedrag via subclass-contracten loopt. | Eerst contracttests toevoegen, daarna kleinere template-method refactors. |
| `SwaggerSpecificationCreator.java` | `omod-common/src/main/java/org/openmrs/module/webservices/docs/swagger/SwaggerSpecificationCreator.java:76` | Swagger documentatie genereren vanuit resources. | 1241 LOC baseline, 158 rough tokens, 24 unieke importpackages. | Complexity, opacity. | Analyzability, testability. | Middel/hoog: vooral docs-output, maar veel paden. | Minder direct voor deze PoC. | Extract Class rond schema/path generation. |

## Waarom `ConversionUtil` is gekozen

`ConversionUtil.convert(Object, Type)` is niet de grootste class, maar wel de beste PoC-keuze. De methode heeft harde baseline-metrics, bestaande tests, een duidelijke interne ontwerpverbetering en een beperkt wijzigingsvlak. `RestUtil`, `RestServiceImpl`, `BaseDelegatingResource` en Swagger zijn ook echte onderhoudbaarheidsproblemen, maar die vragen meer characterization tests en hebben een grotere kans op regressie.
