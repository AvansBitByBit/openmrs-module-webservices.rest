# Verwerkingsregister - OpenMRS REST Web Services

## Verwerkingsactiviteit

REST API voor het raadplegen, aanmaken, wijzigen en exporteren van gegevens in een OpenMRS-installatie.

## Verwerkingsverantwoordelijke

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

## Verwerker(s)

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Denk aan hostingpartij, beheerpartij, supportpartij en eventuele centrale logging/monitoringdienst.

## Doeleinden

- Zorgverlening en dossierbeheer.
- Autorisatiebeheer voor OpenMRS-gebruikers en rollen.
- Technisch beheer, beveiligingslogging en incidentonderzoek.
- Gegevensuitwisseling met geautoriseerde gekoppelde systemen.

## Categorieen betrokkenen

- Patienten.
- Zorgverleners en andere OpenMRS-gebruikers.
- Beheerders.

## Categorieen persoonsgegevens

- Identificerende gegevens van patienten.
- Gezondheidsgegevens in OpenMRS-resources zoals patient, encounter, obs, diagnosis, condition, allergy, medication/order en exports.
- Gebruikersnamen, rollen, privileges en auditgegevens van gebruikers.
- Technische gegevens zoals IP-adres, requestpad, tijdstip en authenticatie-uitkomst.

## Rechtsgrond en uitzondering bijzondere gegevens

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Voor gezondheidsgegevens moet naast een AVG art. 6 grondslag ook een art. 9 uitzondering worden vastgesteld.

## Ontvangers

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

Minimaal beoordelen: interne zorgverleners, beheerders, gekoppelde systemen, hosting/support en auditors.

## Doorgifte buiten EER

IN TE VULLEN DOOR VERWERKINGSVERANTWOORDELIJKE.

## Bewaartermijnen

Zie `bewaartermijnenbeleid.md`. Organisatie-specifieke medische bewaartermijnen blijven leidend.

## Technische en organisatorische maatregelen

- IP-allowlist is deny-by-default als leeg.
- Productieconfiguratie vereist expliciete REST allowlist.
- Basic Auth over onbeveiligd transport wordt standaard geweigerd.
- Mislukte authenticatie stopt direct met HTTP 401.
- In-memory rate limiting blokkeert herhaalde foutieve authenticatiepogingen.
- Auditlogging registreert login, lockout, transportweigering, patientinzage, rechtenwijziging en export.
- Auditlogging redigeert wachtwoorden, tokens, cookies, BSN-patronen, diagnoses en medicatie.
- Standaard foutresponses bevatten geen Java class names, regelnummers of stacktraces.

## Status

Organisatorisch verplicht: dit register moet per deployment worden ingevuld, goedgekeurd en periodiek herzien.
