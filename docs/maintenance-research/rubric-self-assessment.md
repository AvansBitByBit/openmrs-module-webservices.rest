# Rubric self-assessment

| Rubriccriterium | Max | Eigen score | Bewijs | Zwakke plek | Laatste verbetering |
|---|---:|---:|---|---|---|
| Analyse diepgaand en gestructureerd | 20 | 17 | `01-analyse.md`, extra evidence voor CPD, coupling, balance, interfacing en package edges | Metrieken zijn deels custom/indicatief, niet allemaal met enterprise tooling. | Extra metrieken toegevoegd en claims gekoppeld aan evidence. |
| Teststrategie reproduceerbaar | 20 | 16 | `02-teststrategie.md`, Maven logs in `evidence/` | Full reactor is niet groen en `clean verify` heeft geen baselinevergelijking. | Scope-conclusie aangescherpt en full reactor failures eerlijk beschreven. |
| Verbeterplan en prioritering | 15 | 13 | `03-verbeterplan.md` | Scores blijven deels beoordelend, ook al zijn ze onderbouwd. | Losse scores en uitleg per optie toegevoegd. |
| Ontwerpkwaliteit | 15 | 13 | `04-ontwerp.md`, diagrammen in `docs/diagrams/` | Extract Class/Strategy zijn alleen besproken, niet uitgewerkt in code. | Kwaliteitseisen, alternatieven en traceability toegevoegd. |
| PoC past bij ontwerp | 15 | 14 | `05-poc-verantwoording.md`, code in `ConversionUtil.java`, tests | PoC is lokaal, dus repo-brede onderhoudbaarheid verbetert beperkt. | Ontwerpkeuzes gekoppeld aan implementatie, tests en metrics. |
| Validatie hard en eerlijk | 10 | 8 | `06-validatie.md`, coverage/test logs | Geen groene full reactor. Verify baseline ontbreekt. | Regressieconclusie beperkt tot bewezen scope. |
| AI/toolingreflectie kritisch | 5 | 4 | `promptlogboek.md`, `05-poc-verantwoording.md` | Reflectie blijft kort. | Concrete AI-risico's toegevoegd. |

## Totaal

Eigen inschatting: 85/100. Niet perfect, vooral door de niet-groene full reactor en omdat enkele structurele metrieken custom scans zijn. Wel is het onderzoek nu bewijsgericht, reproduceerbaar en eerlijk over scope.
