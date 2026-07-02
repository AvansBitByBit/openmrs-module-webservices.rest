# 04 Aangepast ontwerp

## Uitgangssituatie

Voor de eerste PoC deed `RestServiceImpl` servicecoordinatie, resource discovery en search-handlerselectie. De eerste splitsing bracht deze verantwoordelijkheden onder in twee package-private registries, maar `RestServiceImpl` construeerde de concrete classes nog zelf. Daardoor waren SRP en modularity verbeterd, maar Dependency Inversion, Open/Closed, testability en reusability slechts beperkt.

## Huidige situatie

- `RestServiceImpl` blijft de publieke facade voor het ongewijzigde `RestService`-contract.
- De facade is uitsluitend afhankelijk van de publieke interfaces `ResourceRegistry` en `SearchHandlerRegistry` en van `ExecutorService`.
- `DefaultResourceRegistry` en `DefaultSearchHandlerRegistry` bevatten de bestaande standaardalgoritmen.
- Alle dependencies zijn verplicht via constructor-injectie; de facade maakt geen registries meer met `new` aan.
- Spring publiceert de beans `resourceRegistry` en `searchHandlerRegistry`, zodat een andere implementatie kan worden geinjecteerd.
- `refresh()` bouwt de caches opnieuw op; querymethoden initialiseren alleen wanneer nog geen cache bestaat.

## Ontwerpprincipes

| Principe | Toepassing en grens |
|---|---|
| SRP | Facade, resource discovery en search selection hebben afzonderlijke verantwoordelijkheden. |
| DIP | `RestServiceImpl` is alleen afhankelijk van interfaces; concrete implementaties worden extern aangeleverd. |
| OCP | Een registry kan worden vervangen zonder `RestServiceImpl` te wijzigen. De interne algoritmen van de standaardimplementaties zijn niet volledig open voor uitbreiding. |
| ISP | Resource- en searchgedrag hebben elk een klein interface met vier operaties. |
| LSP | Alternatieve implementaties moeten dezelfde lookup-, exception- en refreshcontracten respecteren; volledige LSP voor toekomstige implementaties is niet bewezen. |
| Facade | `RestServiceImpl` blijft het bestaande toegangspunt voor callers. |

## API-trade-off

Het functionele `RestService`-contract is gelijk gebleven, maar er zijn bewust twee publieke extension interfaces toegevoegd. De no-arg constructor en dependency-setters van de concrete `RestServiceImpl` zijn verwijderd. Directe callers van die implementatie moeten constructor-injectie gebruiken; normale callers via `RestService` en Spring behouden hetzelfde contract.

Diagrammen:

- `docs/diagrams/hotspot-before.puml`
- `docs/diagrams/hotspot-after.puml`
- `docs/diagrams/hotspot-sequence.puml`
