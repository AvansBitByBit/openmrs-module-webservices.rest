# CI/CD-auditrapport OpenMRS REST-module

**Datum:** 3 juli 2026  
**Repository:** `AvansBitByBit/openmrs-module-webservices.rest`  
**Onderzochte default branch:** `master`  
**Fixbranch:** `fix/ci-cd-audit`

## Managementsamenvatting

De reguliere build, unit- en integratietests, OTAP Compose-validatie, CodeQL en SonarCloud waren succesvol. Twee aanvullende securityworkflows op `master` faalden:

1. de SBOM-workflow probeerde wijzigingen naar GitHub te schrijven terwijl de repository dit niet toestaat;
2. de SCA-gate scande ook Maven-dependencies met scope `provided` en `test`, waardoor kwetsbaarheden uit het OpenMRS-hostplatform ten onrechte als meegeleverde dependencies van deze module werden geblokkeerd.

De workflowconfiguratie is gecorrigeerd. De module-SBOM bevat nu alleen dependencies die daadwerkelijk met de module worden uitgeleverd. Daarnaast is Jackson aangepast van 2.19.4 naar de beveiligde backportversie 2.18.8. De lokaal gegenereerde SBOM bevat 24 in plaats van 237 componenten en Trivy 0.70.0 vindt daarin geen HIGH- of CRITICAL-kwetsbaarheden.

De kwetsbaarheden in het OpenMRS-hostplatform zijn hiermee niet technisch opgelost. Ze vallen buiten het artefact van deze module en moeten via platform- en containerbeheer worden opgevolgd.

## Onderzoeksresultaten GitHub

| Onderdeel | Waarneming | Beoordeling |
| --- | --- | --- |
| CI/CD environments | Build, tests, Compose-validatie en rapporterende Trivy-scan waren succesvol. De `master`-run wacht op de handmatige Prod-environmentgate. | Normaal gedrag; geen technische fout. |
| Secure SBOM | Run `28624520815` faalde met `GH013` bij een directe push. Run `28673728961` genereerde en valideerde de SBOM wel, maar mocht geen automatische pull request maken. | Workflowfout; opgelost door de workflow read-only te maken. |
| Secure SCA | Run `28624520858` faalde op HIGH/CRITICAL-kwetsbaarheden. | Deels echte platformrisico's, deels verkeerde scanscope voor de modulegate. |
| Code scanning | 36 open Trivy-meldingen: 1 critical, 34 high en 1 medium. | Afkomstig uit de oude, te brede SBOM. Een nieuwe gerichte scan moet deze modulemeldingen sluiten. |
| Dependabot alerts | 5 open meldingen voor Jackson: 2 high en 3 medium. | Directe dependency; opgelost met Jackson 2.18.8. |
| Open Dependabot-PR's | 11 PR's; alle SCA-checks faalden door dezelfde te brede scan. Vijf PR's hadden daarnaast een echte buildfout door incompatibele major-upgrades. | Na deze workflowfix opnieuw laten draaien; major-upgrades afzonderlijk beoordelen. |

## Uitgevoerde wijzigingen

### 1. Correcte SCA-scope

In `SecureSca.yaml` en `SecureSbom.yaml` zijn de CycloneDX-opties toegevoegd:

```text
-DincludeProvidedScope=false
-DincludeTestScope=false
```

`provided`-dependencies worden door het OpenMRS-platform geleverd en zitten niet in het moduleartefact. Testdependencies worden evenmin naar productie uitgeleverd. De CI-gate beoordeelt daardoor nu het leverbare moduleartefact in plaats van de volledige ontwikkel- en hostomgeving.

### 2. SBOM-publicatie als CI-artifact

De SBOM-workflow schrijft niet meer naar de repository en gebruikt alleen `contents: read`. Iedere run uploadt de gegenereerde en gevalideerde SBOM als CI-artifact. Het bijgehouden bestand `docs/sbom.cdx.json` wordt alleen via een normale ontwikkel-pull-request aangepast. Dit respecteert branch protection zonder de brede repository-instelling in te schakelen waarmee alle GitHub Actions-workflows pull requests kunnen maken.

### 3. Jackson-securityfix

`jacksonVersion` is gewijzigd van 2.19.4 naar 2.18.8. Versie 2.18.8 bevat de relevante securitybackports en valt buiten alle vijf momenteel open Dependabot-ranges. De nog niet gepubliceerde 2.21.5 kon niet worden gebruikt; de beschikbare major-upgrade naar 2.22.0 faalt in de bestaande Dependabot-PR en is daarom niet zonder migratie geschikt.

### 4. SBOM vernieuwd

`docs/sbom.cdx.json` is opnieuw gegenereerd als CycloneDX 1.6-JSON met alleen uitgeleverde dependencies.

## Verificatie

| Controle | Resultaat |
| --- | --- |
| `mvn -B -ntp clean verify` | Geslaagd voor alle vier Maven-reactormodules. |
| YAML-parse van alle drie workflows | Geslaagd. |
| CycloneDX 2.9.1 `makeAggregateBom` | Geslaagd; 24 componenten. |
| Trivy 0.70.0 op de nieuwe SBOM, gate HIGH/CRITICAL | Geslaagd; 0 kwetsbaarheden. |
| `git diff --check` op functionele wijzigingen | Geen patchfouten. |

De lokale build draaide met Java 17 en compileerde naar Java 8-bytecode. De GitHub-workflow gebruikt Temurin Java 8; daarom blijft een nieuwe Actions-run na push de definitieve Java 8-validatie.

## Restrisico's en vervolgacties

1. Push de fixbranch en laat de drie GitHub-workflows opnieuw draaien. Verwacht wordt dat Secure SBOM en Secure SCA groen worden en dat oude Trivy-code-scanningmeldingen door de nieuwe SARIF-run worden gesloten.
2. Behandel de hostplatformbevindingen afzonderlijk. De oude SBOM toonde onder meer kwetsbaarheden in Spring 5.3.30, Netty 4.1.118, PostgreSQL 42.7.7, protobuf 3.19.4, Struts 1.3.8 en Hibernate 5.6.15. Deze libraries worden door OpenMRS 2.8.6 of de runtime geleverd en vragen een platformupgrade, containerupdate of gedocumenteerde risicoacceptatie.
3. Houd de bestaande container-Trivy-scan actief. Deze is momenteel rapporterend en niet blokkerend; bepaal per omgeving welke HIGH/CRITICAL-bevindingen een release moeten blokkeren.
4. Beoordeel de vijf Dependabot-PR's met buildfouten niet automatisch. Het zijn major-upgrades die broncode- of buildmigraties vereisen.
5. Verwijder na controle de verweesde branch `automation/sbom-4a907a902024`; deze is door de mislukte run aangemaakt, maar er bestaat geen pull request voor.

## Conclusie

De gevonden CI/CD-fouten zijn lokaal hersteld en technisch gevalideerd. De modulepipeline krijgt hiermee een correcte securitygate en respecteert branch protection. Het resterende securitywerk betreft hoofdzakelijk het bredere OpenMRS-runtimeplatform en moet als afzonderlijk upgrade- of risicotraject worden behandeld.
