# Promptlogboek en tooling

| Moment | Tool | Doel | Output gebruikt? | Controle | Reflectie |
|---|---|---|---|---|---|
| Opdracht lezen | Codex | Eisen uit opdracht en follow-up vertalen naar onderzoek. | Ja | Vergeleken met `lu2_onderhoudbaarheid_codex_opdracht.md` en `followup.md`. | Belangrijkste correctie was: geen claims zonder evidence. |
| Repo verkennen | PowerShell/rg | Modules, hotspots en files vinden. | Ja | `pom.xml`, `rg`, LOC/complexity scans. | Grote classes vielen direct op, maar extra metrieken waren nodig. |
| Baseline tests | Maven/JaCoCo | Teststatus en coverage voor baseline. | Ja | Logs in `evidence/`. | Full reactor failure is niet weggepoetst. |
| Extra metrieken | PMD CPD/PowerShell | Duplicatie, coupling, component balance, unit interfacing, package edges. | Ja | Raw output in `evidence/`. | Sommige metrieken zijn indicatief, dus ook zo opgeschreven. |
| PoC ontwerpen | Codex + handmatige keuze | Kleine refactor voor `ConversionUtil`. | Ja | Scope beperkt tot private helpers. | Geen Strategy/Factory gekozen omdat dat pattern-forcing zou zijn. |
| Tests uitbreiden | Maven/Surefire | Branchgerichte regressietests toevoegen. | Ja | `ConversionUtilTest` groen met 26 tests. | Tests bewijzen gedrag beter dan alleen "code leest fijner". |
| Validatie | Maven/JaCoCo/git diff | Before/after vergelijken. | Ja | Focused test, `omod-common`, coverage, full reactor logs. | Conclusie is bewust beperkt tot bewezen scope. |

Kritische punten:

- AI kan niet-bestaande methodes of bestanden verzinnen; daarom is met `rg` gecontroleerd.
- AI kan te brede refactors voorstellen; daarom bleef de PoC bij private helpers.
- AI kan OpenMRS-gedrag verkeerd aannemen; daarom zijn bestaande tests leidend.
- Coverage kan te sterk worden geinterpreteerd; daarom is coverage ondersteunend bewijs, niet de enige conclusie.
- Full reactor is niet groen; daarom staat er nergens dat de hele repo regressievrij is.
