# Beroepsproduct LU2 onderhoudbaarheid

Dit bestand verwijst naar de uitgewerkte oplevering:

- Hoofdrapport: `docs/maintenance-research/onderhoudbaarheidsonderzoek.md`
- Korte DOCX-samenvatting: `docs/maintenance-research/samenvatting-voor-docx.md`
- Deelbestanden: `01-analyse.md` t/m `06-validatie.md`
- Evidence: `docs/maintenance-research/evidence/`
- Diagrammen: `docs/diagrams/hotspot-before.puml`, `hotspot-after.puml`, `hotspot-sequence.puml`

De hoofd-PoC is de architectuurrefactor van `RestServiceImpl` naar een facade met `ResourceRegistry` en `SearchHandlerRegistry`. De eerdere `ConversionUtil`-refactor is alleen nog appendix/micro-PoC.

Kort oordeel: onderhoudbaarheid is aantoonbaar verbeterd voor de gekozen architectuurhotspot. De validatieclaim blijft eerlijk beperkt: focused tests, `omod-common` en `clean verify` zijn groen, maar live integration is niet bewezen en `clean test` had een rode run buiten de PoC.
