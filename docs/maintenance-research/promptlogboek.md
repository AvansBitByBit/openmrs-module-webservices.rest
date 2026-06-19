# Promptlogboek en toolingreflectie

| Moment | Tool | Doel | Controle | Reflectie |
|---|---|---|---|---|
| Eerste analyse | Codex + PowerShell/Maven | Onderhoudbaarheidsrapport en kleine `ConversionUtil` PoC maken | `ConversionUtilTest`, `omod-common` tests, metrics | Te kleine PoC voor de hoofdvraag. Waardevol als appendix, niet als hoofdresultaat. |
| Kritiek verwerken | Codex | Plan herzien naar architectuur-PoC | Repo inspectie met `rg`, testclass paden gecontroleerd | Belangrijk: niet aannemen dat tests bestaan. |
| Buildfase fix | Maven + Codex | `maven-dependency-plugin` phaseprobleem isoleren | `clean test`, `clean package`, `clean verify`, resource-inspectie | Buildfix apart gehouden van refactor. |
| Characterization | Maven | Gedrag van `RestServiceImpl` vastleggen | `RestServiceImplTest` 53 groen | Geen dubbele tests toegevoegd omdat gedrag al goed gedekt was. |
| Refactor | Codex + Maven | `RestServiceImpl` splitsen naar `ResourceRegistry` en `SearchHandlerRegistry` | Focused reactor-test, `omod-common`, `clean verify`, metrics | AI-output is pas geaccepteerd na tests en `git diff --check`. |
| Live integration | Docker/curl | Proberen tegen echte OpenMRS server te valideren | Docker daemon check en curl | Niet bewezen, omdat Docker Desktop daemon niet draaide. |

Risico's van AI-gebruik:

- te snel een kleine lokale verbetering kiezen;
- te sterke regressieclaims maken;
- niet-bestaande tests of bestanden aannemen;
- rapportage laten klinken alsof de hele repo bewezen groen is.

Daarom is de tweede ronde strikter gefaseerd en zijn claims beperkt tot wat de evidence echt ondersteunt.
