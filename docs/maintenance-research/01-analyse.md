# 01 Analyse onderhoudbaarheid

## Scope

De repo is onderzocht op ISO 25010 Maintainability: modularity, reusability, analyzability, modifiability en testability. De hele repo is bekeken, maar de hoofd-PoC richt zich op `RestServiceImpl`, omdat deze class centraal staat in resource discovery en search handler selectie.

## Architectuur

| Module | Verantwoordelijkheid |
|---|---|
| `omod-common` | Shared REST frameworkcode, service API, resource abstractions, representations, utils. |
| `omod` | Concrete controllers/resources/search handlers per OpenMRS-versie. |
| `integration-tests` | Rest-Assured tests tegen een draaiende OpenMRS server. |

De architectuur is op hoofdlijnen logisch, maar centrale frameworkclasses trekken veel verantwoordelijkheden naar zich toe.

## Metrics en interpretatie

| Hotspot | Baseline metrics | Onderhoudbaarheidsrisico |
|---|---|---|
| `RestServiceImpl` | 737 LOC, 32 imports, 170 rough decision tokens | Resource discovery, search handler lookup en servicecoordinatie zitten bij elkaar. |
| `RestUtil` | Groot utilbestand, veel web/paging/date gedrag | Lage analyzability; wijziging kan meerdere webflows raken. |
| `BaseDelegatingResource` | Brede base class met veel public/protected surface | Fragility voor subclasses. |
| `SwaggerSpecificationCreator` | Grootste generatorachtige hotspot | Moeilijk te testen en te wijzigen. |
| `ConversionUtil` | Complexe conversieutility | Lokaal nuttig, maar geen sterke architectuur-PoC. |

## Gekozen hotspot

`RestServiceImpl` is gekozen omdat de class meerdere redenen heeft om te wijzigen:

- nieuwe resource class of annotationregel;
- nieuwe search handler of search parameterregel;
- initialize/cache gedrag;
- async init/coordinatie.

Dat schaadt vooral modularity, analyzability en modifiability. Evidence: `docs/maintenance-research/evidence/restservice-baseline-metrics.md`.
