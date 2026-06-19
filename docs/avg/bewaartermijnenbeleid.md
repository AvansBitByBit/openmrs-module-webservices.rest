# Bewaartermijnenbeleid

## Medische gegevens

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

De wettelijke medische bewaartermijnen van de betreffende jurisdictie en zorgcontext zijn leidend.

## Gebruikers- en autorisatiegegevens

Bewaar zolang nodig voor toegang, beheer, verantwoording en wettelijke verplichtingen.

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

## Auditlogs

Aanbevolen operationele default:

- Bewaar security-auditlogs minimaal 12 maanden.
- Verleng naar 24 maanden of langer als wet, contract, NEN 7513-beleid of risicobeoordeling dat vereist.
- Beperk toegang tot auditlogs tot geautoriseerde beheerders/auditors.
- Bescherm auditlogs tegen wijziging en verwijdering.
- Richt logrotatie in op de deploymentlaag.

De module schrijft auditregels append-only via `FileAuditLogWriter`. Het pad is configureerbaar met:

```bash
-Dopenmrs.webservices.rest.audit.log.path=/var/log/openmrs/webservices-rest-audit.log
```

## Verwijdering en anonimisering

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Beschrijf wie verwijdering autoriseert, hoe medische bewaarplichten worden getoetst en hoe verwijdering of beperking wordt gelogd.
