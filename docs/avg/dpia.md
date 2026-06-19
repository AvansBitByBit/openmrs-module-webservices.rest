# DPIA - OpenMRS REST Web Services

## Aanleiding

Deze module kan via REST toegang geven tot gezondheidsgegevens. Verwerking van gezondheidsgegevens en brede dossierinzage kan een hoog privacyrisico opleveren. Daarom moet de verwerkingsverantwoordelijke een DPIA uitvoeren of schriftelijk onderbouwen waarom die niet nodig is.

## Scope

In scope:

- REST API onder `/openmrs/ws/rest`.
- Authenticatie, autorisatie, auditlogging en export via de module.
- Docker OTAP-configuratie in deze repo.

Niet automatisch in scope:

- Volledige OpenMRS-core configuratie.
- Werkprocessen van zorgorganisatie.
- Externe integraties buiten deze repository.

## Gegevens en betrokkenen

Zie `verwerkingsregister.md`.

## Risicoanalyse

| Risico | Impact | Kans | Maatregel | Restrisico |
| --- | --- | --- | --- | --- |
| Onbeveiligde overdracht van Basic Auth of medische data | Hoog | Midden | `webservices.rest.requireSecureTransport=true` in productie; insecure Basic Auth wordt geweigerd. | Laag als TLS correct is ingericht. |
| Brute-force op accounts | Hoog | Midden | In-memory rate limiting en auditlogging van lockouts. | Midden bij multi-node zonder gedeelde limiter. |
| Ongeautoriseerde toegang door brede privileges | Hoog | Midden | OpenMRS privileges, afgeschermde diagnostics/settings, IP-allowlist. | Midden; periodieke autorisatiereview nodig. |
| Lekken van secrets of medische details in logs | Hoog | Laag | Sanitizer redigeert bekende gevoelige patronen; auditregels loggen technische identifiers. | Laag tot midden; logreview nodig. |
| Informatielek via foutresponses | Midden | Midden | Java class names, regelnummers en stacktraces worden niet standaard naar clients gestuurd. | Laag. |
| Onvoldoende afhandeling rechten betrokkenen | Hoog | Midden | Procedure in `rechten-van-betrokkenen.md`. | Afhankelijk van organisatie-uitvoering. |
| Onvoldoende datalekrespons | Hoog | Midden | Procedure in `datalekprocedure.md`; auditlogs ondersteunen onderzoek. | Afhankelijk van organisatie-uitvoering. |

## Besluiten

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Minimaal vastleggen:

- Verwerkingsgrondslag en art. 9 uitzondering.
- Toegangsmodel en autorisatiereview-frequentie.
- TLS-terminatie en netwerksegmentatie.
- Logretentie en wie auditlogs mag bekijken.
- Incidentrespons en meldtermijnen.

## Goedkeuring

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Datum, eigenaar, Functionaris Gegevensbescherming of privacy officer, en herzieningsdatum opnemen.
