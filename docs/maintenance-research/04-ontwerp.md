# 04 Ontwerp

## Probleem

In de baseline deed `ConversionUtil.convert(Object, Type)` te veel in een methode: raw target type bepalen, collection/array conversie, string naar date/locale/enum/class/valueOf, map-conversie, number-conversie, boolean naar string en foutafhandeling. Baseline-metrics: 118 LOC en CC 23, vastgelegd in `evidence/baseline-metrics.md`.

## Kwaliteitseisen

| Eis | Waarom |
|---|---|
| Analyzability verbeteren | De hoofdroute moet sneller te lezen zijn dan een methode van 118 LOC. |
| Testability verbeteren | Branches moeten apart te begrijpen en gericht te testen zijn. |
| Regressierisico beperken | Geen wijziging aan public signatures of exception type. |
| Public API behouden | Callers in resources/controllers mogen niet aangepast hoeven worden. |
| Geen pattern-forcing | Alleen extra structuur toevoegen als het echte complexiteit verlaagt. |

## Before en after

Before diagram: `docs/diagrams/hotspot-before.puml`.

After diagram: `docs/diagrams/hotspot-after.puml`.

Flowdiagram: `docs/diagrams/hotspot-sequence.puml`.

De gekozen oplossing houdt deze public methode gelijk:

```java
public static Object convert(Object object, Type toType) throws ConversionException
```

Intern is de methode opgesplitst in helpers:

| Helper | Verantwoordelijkheid |
|---|---|
| `getRawClass` | Raw target class uit `Type` halen. |
| `convertToCollectionOrArray` | Collection/array-pad herkennen en coordineren. |
| `convertToArray` | Array aanmaken en elementen converteren. |
| `createCollection` | Concrete collection kiezen. |
| `convertFromString` | String-conversies routeren. |
| `convertStringToDate` | Date formats proberen. |
| `convertStringToClass` | Class loading via OpenMRS `Context`. |
| `convertUsingValueOf` | `valueOf(String)` fallback. |
| `convertNumber` | Number naar target number type. |

## Traceerbaarheid ontwerp naar code

| Ontwerpbeslissing | Codebestand/methode | Waarom |
|---|---|---|
| Public API gelijk houden | `ConversionUtil.convert(Object, Type)` | Beperkt regressierisico voor alle callers. |
| Collection/array apart zetten | `convertToCollectionOrArray`, `convertToArray`, `createCollection` | Elementconversie heeft eigen regels en foutpaden. |
| Stringvarianten apart zetten | `convertFromString`, `convertStringToDate`, `convertStringToClass`, `convertUsingValueOf` | Date/class/valueOf veranderen om andere redenen. |
| Sentinel gebruiken | `NO_CONVERSION` | Helpers kunnen "geen match" teruggeven zonder `null` te verwarren met geldige output. |
| Numberpad apart zetten | `convertNumber` | Numerieke conversie is klein maar anders dan string/map-conversie. |

## Principes

| Principe | Toepassing |
|---|---|
| SRP | Helpers hebben elk een kleiner conversiegebied. |
| Extract Method | De lange methode is opgesplitst zonder nieuw public contract. |
| Encapsulate what varies | Date parsing, collection handling, class loading en `valueOf` fallback zitten apart. |
| Geen pattern-forcing | Geen Strategy/Factory toegevoegd, omdat er nog geen echte runtime-uitbreidingsbehoefte is. |

## Alternatieven

| Alternatief | Voordeel | Nadeel | Waarom niet gekozen |
|---|---|---|---|
| Alleen tests toevoegen | Laag risico en snel. | Methode blijft 118 LOC/CC 23. | Verbetert testbaarheid iets, maar analyzability niet. |
| Strategy pattern/converter registry | Uitbreidbaar per conversietype. | Veel nieuwe classes en dispatch-regels voor een kleine PoC. | Te zwaar en verhoogt coupling voordat de behoefte bewezen is. |
| Extract Class | Kan `ConversionUtil` structureel kleiner maken. | Groter wijzigingsvlak en meer keuzes over package/API. | Interessant later, maar te breed voor deze PoC. |
| Extract Method binnen class | Klein, meetbaar, API blijft gelijk. | Class blijft nog steeds groot. | Beste balans voor deze opdracht. |
