# 03 Verbeterplan

## Scoremethode

Schaal 1 tot 5. Bij regressierisico betekent 5 laag risico en 1 hoog risico. Formule:

`Totaal = (impact * 2) + (bewijssterkte * 2) + regressierisico + effort + testbaarheid + onderwijswaarde`

| Optie | Hotspot | Probleem | Impact | Bewijssterkte | Regressierisico | Effort | Testbaarheid | Onderwijswaarde | Totaal | Keuze |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|---|
| V1 | `ConversionUtil.convert` | 118 LOC/CC 23 in een centrale conversiemethode. | 4 | 5 | 5 | 5 | 5 | 5 | 38 | Ja |
| V2 | `RestUtil` | 951 LOC utility class met 148 rough decision tokens. | 4 | 4 | 3 | 3 | 4 | 4 | 30 | Nee |
| V3 | `RestServiceImpl` | Resource/search discovery en lookup zitten strak gekoppeld. | 5 | 5 | 2 | 2 | 3 | 5 | 32 | Nee |
| V4 | `SwaggerSpecificationCreator` | 1241 LOC en 158 rough tokens in doc/schema generator. | 3 | 5 | 3 | 2 | 2 | 3 | 26 | Nee |
| V5 | Versioned resources/search handlers | PMD CPD vindt 13 duplicate blocks in `omod`. | 3 | 4 | 2 | 2 | 3 | 4 | 25 | Nee |

## Uitleg per optie

V1 scoort hoog omdat de metricdata sterk is, de scope klein blijft en de tests direct beschikbaar zijn. De public API kan gelijk blijven, dus callers hoeven niet mee te veranderen.

V2 is nuttig, maar `RestUtil` raakt request/webgedrag. Daarvoor wil je eerst meer characterization tests, anders is het regressierisico hoger dan bij `ConversionUtil`.

V3 heeft de hoogste architectuurimpact, maar ook veel risico. `RestServiceImpl` raakt resource discovery en search handlers. Dat is een groter ontwerptraject dan een LU2 PoC.

V4 is aantoonbaar groot en complex, maar de testbaarheid is minder direct. Swagger-output kan subtiel wijzigen, dus je hebt snapshot-achtige tests nodig.

V5 is onderbouwd door PMD CPD, maar duplicatie in resources is verspreid over OpenMRS-versies. Dat vraagt meer domeinkennis en waarschijnlijk meerdere files.

## Gekozen scope

De gekozen verbetering is V1: `ConversionUtil.convert(Object, Type)` refactoren met Extract Method. Dit volgt logisch uit de data: hoge methodecomplexiteit, bestaande tests, beperkte public impact en duidelijke onderwijswaarde omdat before/after meetbaar is.
