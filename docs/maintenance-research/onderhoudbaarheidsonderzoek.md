# Verbeteronderzoek onderhoudbaarheid - openmrs-module-webservices.rest

## 1. Inleiding

Dit onderzoek gaat over de onderhoudbaarheid van `openmrs-module-webservices.rest`, een Java/Maven module voor OpenMRS REST Web Services. De repo is niet klein: er zijn meerdere Maven-modules, veel versioned REST resources en een aantal centrale frameworkclasses waar veel gedrag samenkomt.

Hoofdvraag:

> In hoeverre is `openmrs-module-webservices.rest` onderhoudbaar volgens ISO 25010 Maintainability, en welke onderbouwde ontwerpverbetering kan deze onderhoudbaarheid aantoonbaar verbeteren zonder regressie te introduceren?

De eerste poging in deze repo was een kleine refactor van `ConversionUtil`. Die staat nog in de geschiedenis als micro-PoC, maar is te lokaal voor de hoofdvraag. Daarom is de hoofd-PoC in dit verslag verlegd naar `RestServiceImpl`: een centrale serviceclass voor resource discovery en search handler selectie. Dat past beter bij architectuur, modularity en modifiability.

## 2. Theoretisch kader

Ik gebruik ISO 25010 Maintainability als hoofdkader:

| Subkwaliteit | Wat betekent dit in deze repo? |
|---|---|
| Modularity | Resource discovery, search handler lookup, controllers en util-code moeten niet allemaal in dezelfde centrale class vastzitten. |
| Reusability | Centrale registratielogica moet bruikbaar zijn zonder onnodige web/controllerdetails. |
| Analyzability | Een ontwikkelaar moet snel kunnen zien waar resource lookup of search matching gebeurt. |
| Modifiability | Nieuwe resources of search handlers moeten toegevoegd kunnen worden zonder een god-class nog groter te maken. |
| Testability | Gedrag moet met gerichte unit/characterization tests te controleren zijn, niet alleen via live OpenMRS. |

De gebruikte design smells zijn vooral:

- **Complexity**: veel beslislogica in een class of methode.
- **Opacity**: moeilijk te zien welke verantwoordelijkheid waar zit.
- **Rigidity**: wijziging in resource discovery raakt ook search handler code omdat het in dezelfde class zit.

Voor het ontwerp gebruik ik vooral SRP en Facade/Coordinator. `RestServiceImpl` blijft het publieke gezicht van `RestService`, maar de interne registratielogica wordt opgesplitst in twee cohesive package-private classes. Geen nieuw publiek API-contract dus, en geen pattern-forcing.

## 3. Onderzoeksaanpak

De aanpak was gefaseerd:

1. Huidige ConversionUtil-poging vastgelegd met checkpointcommit `6a36c8d`.
2. Baseline evidence verzameld voor `RestServiceImpl`.
3. Maven buildfaseprobleem geisoleerd en apart gefixt in commit `5129d2d`.
4. Bestaande characterization tests voor `RestServiceImpl` gecontroleerd.
5. Architecture PoC uitgevoerd in commit `0c796f5`.
6. Tests, metrics en live-integration poging opnieuw vastgelegd.

Belangrijke evidence staat in `docs/maintenance-research/evidence/`. De belangrijkste bestanden:

| Evidence | Inhoud |
|---|---|
| `restservice-baseline-metrics.md` | Baseline metrics voor `RestServiceImpl`. |
| `restservice-baseline-clean-test.txt` | Baseline `mvn clean test`, rood op Maven unpack fase. |
| `buildfix-clean-verify.txt` | Buildfix-validatie, `clean verify` groen. |
| `characterization-restserviceimpl-test.txt` | Bestaande characterization tests: 53 groen. |
| `refactor-focused-restserviceimpl-reactor-test-final.txt` | Focused reactor-test na refactor: groen. |
| `refactor-omod-common-test.txt` | `omod-common` na refactor: 121 groen. |
| `refactor-clean-test.txt` | Full `clean test` na refactor: rood op bestaande/flaky cache-test. |
| `refactor-clean-verify.txt` | Full `clean verify` na refactor: groen. |
| `restservice-after-metrics.md` | After metrics voor facade + registries. |
| `docker-build-module.txt` | Live integration poging; Docker daemon draaide niet. |

## 4. Project- en architectuuranalyse

De Maven-structuur is logisch:

| Module | Rol |
|---|---|
| `omod-common` | Shared REST frameworkcode: service interfaces, utilities, representations, resource API. |
| `omod` | Concrete REST resources, controllers en OpenMRS-versiespecifieke implementaties. |
| `integration-tests` | Rest-Assured tests tegen een draaiende OpenMRS server. |

Architectonisch zit de meeste onderhoudsdruk in centrale shared classes. `RestServiceImpl` staat in `omod-common` en wordt gebruikt als service/facade voor resource lookup en search handler lookup. Voor de PoC had deze class drie soorten verantwoordelijkheden:

- dependency/coordinatie (`RestHelperService`, `OpenmrsClassScanner`, async initialize);
- resource discovery, annotation metadata, version filtering, orderconflicten en class/name lookup;
- search handler indexing, id lookup, parameter matching, default handler keuze en ambiguity errors.

Dat is een SRP-probleem: drie redenen om te wijzigen in een centrale class.

## 5. Onderhoudbaarheidsanalyse

Baseline voor `RestServiceImpl`:

| Metric | Waarde | Interpretatie |
|---|---:|---|
| LOC | 737 | Te groot voor een serviceclass die ook public facade is. |
| Nonblank LOC | 631 | Veel echte code, niet alleen comments. |
| Import fan-out | 32 | Veel directe afhankelijkheden, dus hogere analysekosten. |
| Public/protected methods | 21 | Brede surface voor een centrale class. |
| Rough decision tokens | 170 | Veel branches/loops/condities, dus testbaarheid en analyzability onder druk. |

Hotspots uit de bredere repo-analyse:

| Hotspot | Probleem | ISO-impact |
|---|---|---|
| `SwaggerSpecificationCreator` | Grootste generatorachtige class met veel interpretatielogica. | Analyzability, testability. |
| `RestUtil` | Veel web/HTTP utilitygedrag bij elkaar. | Modifiability, opacity. |
| `BaseDelegatingResource` | Brede base class met veel public/protected surface. | Reusability, fragility. |
| `RestServiceImpl` | Resource en search discovery zitten in een god-class. | Modularity, analyzability, modifiability. |
| `ConversionUtil` | Complexe conversieutility; lokaal verbeterd, maar repo-impact beperkt. | Testability, analyzability. |

`RestServiceImpl` is gekozen omdat deze hotspot niet alleen complex is, maar ook architectonisch centraal staat. Nieuwe REST resources en search handlers zijn normale uitbreidingspunten van deze module. Juist daarom moet discovery/lookup begrijpelijk blijven.

## 6. Testopzet en baseline testresultaten

Voor `RestServiceImpl` bestonden al echte tests:

- `omod/src/test/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImplTest.java`
- `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/api/RestServiceTest.java`

Er is dus niet gedaan alsof `RestServiceImplTest` nog gemaakt moest worden. De bestaande testclass dekt precies de belangrijkste characterization-cases:

| Gedrag | Bestaande testdekking |
|---|---|
| Duplicate resource order geeft `IllegalStateException` | aanwezig voor name lookup, class lookup en initialize. |
| Superclass resource lookup kiest meest specifieke resource | aanwezig met `MockingBird`/`BirdResource`. |
| Duplicate search handler id geeft `IllegalStateException` | aanwezig via `getSearchHandler`, `getSearchHandlers`, `initialize`. |
| Ambiguous search handler kiest `default` als die bestaat | aanwezig. |
| Ambiguous search handler zonder `default` geeft `InvalidSearchException` | aanwezig. |

Daarom zijn geen dubbele tests toegevoegd. De characterization-baseline was:

```text
mvn --batch-mode --no-transfer-progress -pl omod -Dtest=RestServiceImplTest test
Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
```

Belangrijke testresultaten:

| Command | Resultaat | Opmerking |
|---|---|---|
| Baseline focused `RestServiceImplTest` | groen, 53 tests | Gedrag vastgelegd voor refactor. |
| Baseline `-pl omod-common test` | groen, 121 tests | Shared module werkt. |
| Baseline `clean test` | rood | Maven unpack fase faalde met MDEP-98. |
| Na buildfix `clean verify` | groen | Buildfaseprobleem opgelost. |
| Na refactor focused reactor-test | groen, 53 tests | Refactor behoudt gedrag binnen hotspot. |
| Na refactor `-pl omod-common test` | groen, 121 tests | Shared module blijft groen. |
| Na refactor `clean test` | rood | 1 failure in `ClearDbCacheController2_0Test`, ook eerder/flaky gezien. |
| Na refactor `clean verify` | groen | Full reactor kan groen draaien, maar `clean test` was niet stabiel. |

## 7. Verbeteropties en prioritering

Scoreformule:

```text
Totaal = impact*2 + bewijs*2 + regressierisico + effort + testbaarheid + onderwijswaarde
```

| Optie | Hotspot | Verbetering | Impact | Bewijs | Risico | Effort | Testbaar | Onderwijs | Totaal | Keuze |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|---|
| V1 | `RestServiceImpl` | Split facade in `ResourceRegistry` en `SearchHandlerRegistry` | 5 | 5 | 4 | 3 | 5 | 5 | 42 | Ja |
| V2 | `ConversionUtil` | Extract Method in `convert` | 2 | 4 | 5 | 5 | 4 | 3 | 31 | Alleen appendix |
| V3 | `RestUtil` | Utility splitsen per HTTP/paging/date responsibility | 4 | 4 | 2 | 2 | 3 | 4 | 31 | Nee, meer regressierisico |
| V4 | `BaseDelegatingResource` | Base class opdelen / hooks explicieter maken | 5 | 4 | 1 | 1 | 2 | 5 | 29 | Nee, te breed |

V1 wint omdat de class centraal is, de bestaande tests sterk zijn en de verbetering architectonisch meetbaar is zonder public API te wijzigen.

## 8. Aangepast ontwerp

Voor de PoC:

```text
RestServiceImpl
  - public RestService facade
  - resource discovery/cache
  - search handler discovery/cache
  - async initialize
```

Na de PoC:

```text
RestServiceImpl
  - beheert dependencies en lazy initialization
  - public RestService API blijft hetzelfde
  - delegeert naar ResourceRegistry en SearchHandlerRegistry

ResourceRegistry
  - resource classes scannen
  - annotations interpreteren
  - OpenMRS version filtering
  - duplicate order conflict
  - name/class/resource handler lookup

SearchHandlerRegistry
  - search handlers indexeren
  - id lookup
  - parameter matching
  - required parameter filtering
  - default/ambiguous handler selection
```

Toegepaste principes:

- **SRP**: resource registry en search registry hebben gescheiden redenen om te wijzigen.
- **Facade**: `RestServiceImpl` blijft het bestaande servicegezicht.
- **Encapsulate what varies**: resource discovery en search selection zijn aparte variatiepunten.
- **No public API widening**: nieuwe classes zijn package-private in `org.openmrs.module.webservices.rest.web.api.impl`.

Alternatief dat niet gekozen is:

| Alternatief | Voordeel | Nadeel |
|---|---|---|
| Alleen Extract Method binnen `RestServiceImpl` | Lage effort | Class blijft god-class; modularity verbetert nauwelijks. |
| Nieuwe public registry interfaces | Testbaar en uitbreidbaar | Wijdt API onnodig uit voor een interne refactor. |
| Grote rewrite naar Strategy/Factory | Theoretisch netjes | Te veel regressierisico en pattern-forcing. |

Diagrammen:

- `docs/diagrams/hotspot-before.puml`
- `docs/diagrams/hotspot-after.puml`
- `docs/diagrams/hotspot-sequence.puml`

## 9. Realisatie PoC

Commit: `0c796f5 refactor: split rest service registries`

Gewijzigde code:

- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java`
- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/ResourceRegistry.java`
- `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/SearchHandlerRegistry.java`

Belangrijkste codepunten:

- `RestServiceImpl` delegeert public API calls naar registries rond regels 112-153.
- `RestServiceImpl.initialize()` reset beide registries en initialiseert ze opnieuw rond regels 160-165.
- `ResourceRegistry` doet resource discovery en lookup vanaf regel 46.
- `SearchHandlerRegistry` doet search indexing en selectie vanaf regel 48.

Er zijn geen nieuwe public classes toegevoegd. Beide registries zijn package-private. Het publieke `RestService` contract is niet aangepast.

## 10. Validatie

Before/after metrics:

| Metric | Baseline `RestServiceImpl` | Na PoC | Verschil | Interpretatie |
|---|---:|---:|---:|---|
| LOC `RestServiceImpl` | 737 | 197 | -540 | Facade is veel kleiner en sneller te begrijpen. |
| Rough decision tokens `RestServiceImpl` | 170 | 19 | -151 | Public facade bevat bijna geen discovery/selection branches meer. |
| Import fan-out `RestServiceImpl` | 32 | 16 | -16 | Minder directe coupling in facade. |
| Nieuwe `ResourceRegistry` LOC | n.v.t. | 244 | +244 | Cohesive resource responsibility, kleiner dan oude god-class. |
| Nieuwe `SearchHandlerRegistry` LOC | n.v.t. | 300 | +300 | Cohesive search responsibility, kleiner dan oude god-class. |
| Package cycles | 11 directe cycles baseline | geen nieuwe cycle | 0 nieuw | Registries blijven in bestaande impl package. |

Testvalidatie:

| Command | Resultaat |
|---|---|
| `mvn --batch-mode --no-transfer-progress -pl omod-common,omod "-Dtest=RestServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" test` | groen, 53 tests |
| `mvn --batch-mode --no-transfer-progress -pl omod-common test` | groen, 121 tests |
| `mvn --batch-mode --no-transfer-progress clean test` | rood, 1 failure in `ClearDbCacheController2_0Test` |
| `mvn --batch-mode --no-transfer-progress clean verify` | groen |
| `git diff --check` | groen |

Regressieclaim:

Ik claim niet botweg "geen regressie in de hele repo", omdat `clean test` na de refactor rood was door `ClearDbCacheController2_0Test`. Wel is de after-state sterker dan de eerdere ConversionUtil-versie, want:

- de gekozen hotspot heeft 53 focused characterization tests groen;
- `omod-common` draait groen;
- `clean verify` draait groen;
- de full `clean test` failure zit buiten `RestServiceImpl` en is vergelijkbaar met eerder waargenomen instabiliteit rond dezelfde cache-test.

Live integration:

Docker was beschikbaar als CLI, maar Docker Desktop/daemon draaide niet:

```text
open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
```

Daarom is live OpenMRS integration niet bewezen. `curl http://localhost:8080/openmrs/ws/rest/v1/session` faalde ook omdat er geen server draaide.

## 11. AI- en toolingreflectie

Codex is gebruikt voor analyse, planning, refactor en documentatie. De grootste AI-risico's waren precies wat bij de eerste poging misging:

- te snel een kleine lokale PoC kiezen;
- te sterke validatieclaim maken op basis van een beperkte testscope;
- aannemen dat tests bestaan zonder te inspecteren;
- rapportage breder laten klinken dan de codewijziging echt is.

Daarom is de tweede aanpak gefaseerd uitgevoerd: eerst testclasses zoeken, daarna buildfix commit, daarna pas architectuurrefactor, en pas daarna rapport herschrijven. Claims zijn gecontroleerd met Maven, `git diff --check`, PowerShell-metrics, Docker poging en handmatige inspectie.

## 12. Conclusie

De repo is redelijk onderhoudbaar qua Maven-modules en bestaande testbasis, maar centrale frameworkclasses zijn duidelijke onderhoudbaarheidsrisico's. Vooral `RestServiceImpl`, `RestUtil`, `BaseDelegatingResource` en Swagger-generatie drukken op analyzability en modifiability.

De hoofd-PoC verbetert de onderhoudbaarheid van `RestServiceImpl` aantoonbaar: de public facade gaat van 737 naar 197 LOC en van 170 naar 19 rough decision tokens. Resource discovery en search handler selection zijn nu aparte package-private registries met duidelijke verantwoordelijkheden. Dit verbetert vooral modularity, analyzability en modifiability.

De validatie is eerlijk afgebakend: focused tests en `omod-common` zijn groen, `clean verify` is groen, maar `clean test` was in een run rood door een bestaande/flaky cache-test buiten de PoC. Live integration is geprobeerd maar niet bewezen omdat Docker Desktop niet draaide.

Mijn strenge zelfinschatting: dit is duidelijk sterker dan de eerdere ConversionUtil-PoC, vooral door architectuurfocus en meetbare class-splitsing. Overblijvende zwakte is dat er geen live OpenMRS integrationbewijs is en dat de full reactor teststatus niet 100% stabiel is.

## Bijlage: ConversionUtil micro-PoC

De eerdere `ConversionUtil` refactor blijft nuttig als kleine appendix: `convert(Object, Type)` werd opgesplitst en `ConversionUtilTest` ging van 22 naar 26 tests. Maar de verbetering was vooral lokaal en loste de repo-brede architectuurvraag niet genoeg op. Daarom is deze niet meer de hoofd-PoC.
