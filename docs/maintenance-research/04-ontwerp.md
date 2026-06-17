# 04 Aangepast ontwerp

## Huidige situatie

Voor de PoC deed `RestServiceImpl` drie dingen tegelijk:

- service facade en dependencybeheer;
- resource discovery en resource lookup;
- search handler indexing en selectie.

Dat is een SRP-probleem en maakt de class minder analyseerbaar.

## Nieuwe situatie

Na de PoC:

- `RestServiceImpl` blijft de public facade voor `RestService`;
- `ResourceRegistry` beheert resource discovery, annotation metadata, version filtering, orderconflicten en class/name lookup;
- `SearchHandlerRegistry` beheert search handler indexing, id lookup, parameter matching, required parameter filtering en default/ambiguous selectie.

## Principes

| Principe | Toepassing |
|---|---|
| SRP | Iedere registry heeft een eigen reden om te wijzigen. |
| Facade | `RestServiceImpl` blijft het bestaande toegangspunt. |
| Encapsulate what varies | Resource discovery en search matching zijn aparte variatiepunten. |
| No API widening | Nieuwe classes zijn package-private. |

## Alternatieven

| Alternatief | Waarom niet gekozen |
|---|---|
| Alleen Extract Method | De class blijft alsnog alles doen. |
| Nieuwe public interfaces | Onnodig breed voor interne registries. |
| Strategy/Factory rewrite | Te zwaar en te veel pattern-forcing voor deze scope. |

Diagrammen:

- `docs/diagrams/hotspot-before.puml`
- `docs/diagrams/hotspot-after.puml`
- `docs/diagrams/hotspot-sequence.puml`
