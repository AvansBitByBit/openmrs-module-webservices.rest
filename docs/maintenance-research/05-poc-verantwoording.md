# 05 PoC-verantwoording

## Ontwikkelstappen

De oorspronkelijke classesplitsing staat in commit `0c796f5`. De SOLID-vervolgstap vervangt de interne registries door publieke interfaces en injecteerbare standaardimplementaties.

Belangrijkste productieonderdelen:

- `web.api.ResourceRegistry` en `web.api.SearchHandlerRegistry`: publieke extension contracts;
- `DefaultResourceRegistry` en `DefaultSearchHandlerRegistry`: standaardgedrag;
- `RestServiceImpl`: facade met verplichte constructor-injectie;
- `webModuleApplicationContext.xml`: beans `resourceRegistry` en `searchHandlerRegistry`.

## Traceerbaarheid ontwerp naar bewijs

| Ontwerpkeuze | Code/testbewijs |
|---|---|
| Program to interface / DIP | `RestServiceImpl` heeft alleen interfacevelden en een constructor met interfaceparameters. |
| Open/Closed | `RestServiceImplDependencyTest` injecteert alternatieve implementaties zonder de facade te wijzigen. |
| Directe testability | `DefaultResourceRegistryTest` en `DefaultSearchHandlerRegistryTest` testen de classes afzonderlijk. |
| Reusability | Interfaces en default implementations zijn public; Spring publiceert beide beans. |
| Gedragsbehoud | De bestaande 53 `RestServiceImplTest` characterization tests blijven groen. |

De API-uitbreiding is een bewuste trade-off: acht interface-operaties worden publiek onderhoudscontract. Dat verbetert hergebruik en vervangbaarheid, maar vergroot het te onderhouden publieke oppervlak.
