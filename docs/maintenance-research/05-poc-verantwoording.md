# 05 PoC-verantwoording

## Commit

`0c796f5 refactor: split rest service registries`

## Gewijzigde bestanden

- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java`
- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/ResourceRegistry.java`
- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/SearchHandlerRegistry.java`

## Traceerbaarheid ontwerp naar code

| Ontwerpkeuze | Code |
|---|---|
| Facade/coordinator | `RestServiceImpl` delegeert public methods naar registries. |
| Resource responsibility apart | `ResourceRegistry` bevat scanner, metadata, orderconflict en lookup. |
| Search responsibility apart | `SearchHandlerRegistry` bevat indexes, parameter matching en ambiguity handling. |
| Geen public API widening | Beide registries zijn package-private. |

## AI/tooling

Codex is gebruikt voor implementatie, maar de output is gecontroleerd met tests, metrics, `git diff --check` en handmatige inspectie. Een belangrijk leerpunt is dat de eerste ConversionUtil-PoC te klein was voor de hoofdvraag. Deze tweede PoC is daarom bewust architectuurgerichter.
