# AVG conformiteitsbewijs - technische maatregelen

## Scope

Dit bewijsdocument gaat over de technische AVG-relevante maatregelen in de OpenMRS REST Web Services module. Het beoordeelt met name beveiliging van verwerking, dataminimalisatie in logs, toegangscontrole en aantoonbaarheid via tests.

Volledige juridische AVG-conformiteit vereist daarnaast organisatorische bewijsstukken buiten de codebase, zoals een verwerkingsregister, DPIA, privacyverklaring, verwerkersovereenkomsten, bewaartermijnenbeleid en datalekprocedure.

## Opgeloste risico's

| AVG-risico | Maatregel | Bewijs |
| --- | --- | --- |
| Ongeautoriseerde toegang tot diagnostiek | `/ws/rest/v1/session/diag` vereist nu `View RESTWS`. | `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_9/SessionController1_9.java` |
| Lekken van rollen en privileges via diagnostiek | Het diagnostic endpoint retourneert geen `userRoles` of `userPrivileges` meer. | `SessionController1_9Test#getDiagnostics_shouldNotExposeRolesOrPrivileges` |
| Ongeautoriseerde toegang tot global properties | `settings.form`, `settings.form/search` en het model vereisen nu `Manage RESTWS`. | `omod/src/main/java/org/openmrs/module/webservices/rest/web/controller/SettingsFormController.java` |
| Lekken van secrets in settings autocomplete | Search response bevat alleen propertynamen, geen propertywaarden. | `SettingsFormControllerTest#searchProperties_shouldReturnPropertyNamesWithoutValues` |
| Open REST toegang als IP-allowlist leeg is | Een lege allowlist is nu deny-by-default. | `RestUtilTest#ipMatches_shouldReturnFalseIfListIsEmpty` |
| Productie zonder expliciete REST allowlist | `docker-compose.prod.yml` vereist `OMRS_REST_ALLOWED_IPS` voor startup. | `docker-compose.prod.yml` |
| Basic Auth over onbeveiligd transport | Productie weigert Basic Auth zonder secure request met HTTP 426 en audit-event. | `AuthorizationFilterTest#doFilter_shouldDenyBasicAuthOverInsecureTransportIfRequired` |
| Brute-force op REST-authenticatie | In-memory rate limiting blokkeert herhaalde mislukte pogingen met HTTP 429. | `AuthorizationFilterTest#doFilter_shouldLockOutAfterConfiguredFailures` |
| Foutieve credentials bereiken API-laag | Foutieve Basic Auth stopt direct met HTTP 401 en roept de filter chain niet aan. | `AuthorizationFilterTest#doFilter_shouldStopInvalidBasicAuthWithUnauthorized` |
| Informatielek via foutresponses | Standaard error responses bevatten geen Java class name of regelnummer meer. | `RestUtilTest#wrapErrorResponse_shouldNotExposeStackTraceCodeIfAvailable` |
| Onveilige module-defaults | `allowedips` is deny-by-default beschreven en `enableStackTraceDetails` staat standaard op `false`. | `omod/src/main/resources/config.xml` |
| Serverversie-reconnaissance | Gateway gebruikt `server_tokens off` en backend Tomcat error reports tonen geen serverinfo/rapportdetails. | `docker/gateway/security-hardening.conf`, `docker/backend/Dockerfile` |
| Auditlogging van medische inzage en exports | Login, patiëntdossierinzage, rol/rechtenwijziging en export worden auditwaardig gelogd. | `docs/auditrapport/nen7510-nen7513-audit-logging.md` |
| Gevoelige data in auditlogs | Wachtwoorden, tokens, BSN, diagnose en medicatie worden geredigeerd. | `SecurityAuditLoggerTest#shouldNotWriteSensitiveDataToLogOrPersistentWriter` |

## Testbewijs

Uitgevoerd op 19 juni 2026:

```bash
mvn -pl omod-common -Dtest=RestUtilTest,SecurityAuditLoggerTest test
mvn -pl omod-common -Dtest=AuthorizationFilterTest,RestUtilTest,SecurityAuditLoggerTest test
mvn -pl omod -am -Dtest=SessionController1_9Test,SettingsFormControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Resultaat:

| Testset | Resultaat |
| --- | --- |
| `RestUtilTest`, `SecurityAuditLoggerTest` | Groen |
| `AuthorizationFilterTest`, `RestUtilTest`, `SecurityAuditLoggerTest` | Groen |
| `SessionController1_9Test`, `SettingsFormControllerTest` | Groen, 21 tests, 0 failures, 0 errors |

## AVG-mapping

| AVG-artikel | Technische invulling in deze module |
| --- | --- |
| Art. 9 - bijzondere persoonsgegevens | De module verwerkt gezondheidsgegevens; toegang tot patiëntdossier en exports wordt gelogd en afgeschermd via OpenMRS privileges. |
| Art. 25 - gegevensbescherming door ontwerp en standaardinstellingen | Lege IP-allowlist is deny-by-default; settings-search retourneert geen secret values. |
| Art. 25 - gegevensbescherming door ontwerp en standaardinstellingen | Productie weigert Basic Auth zonder secure transport; dev/test moeten afwijking expliciet configureren. |
| Art. 30 - aantoonbaarheid verwerkingen | Code bevat technische logging, maar het formele verwerkingsregister moet organisatorisch worden beheerd. |
| Art. 32 - beveiliging van verwerking | Toegangscontrole, IP-restrictie, secure transport enforcement, rate limiting, auditlogging, secret-redactie en verplichte productieconfiguratie reduceren risico op ongeautoriseerde toegang. |
| Art. 33 - datalekken | Auditlogs ondersteunen onderzoek naar incidenten; formele meldprocedure en datalekregister blijven organisatorisch nodig. |
| Art. 35 - DPIA | Omdat de module gezondheidsgegevens kan verwerken, moet de verwerkingsverantwoordelijke een DPIA uitvoeren of aantonen waarom die niet nodig is. |

## Configuratiebewijs

| Global property | Productie-default | Dev/test |
| --- | --- | --- |
| `webservices.rest.requireSecureTransport` | `true` | expliciet `false` in Docker Compose |
| `webservices.rest.auth.rateLimit.enabled` | `true` | `true` |
| `webservices.rest.auth.rateLimit.maxFailures` | `5` | `5` |
| `webservices.rest.auth.rateLimit.windowSeconds` | `900` | `900` |
| `webservices.rest.auth.rateLimit.lockoutSeconds` | `900` | `900` |
| `webservices.rest.enableStackTraceDetails` | `false` | alleen tijdelijk voor debug/test inschakelen |

## Resterende deployment-afhankelijkheid

De gatewayconfiguratie verwijdert het exacte nginx-versienummer met `server_tokens off` en verbergt upstream `Server` headers. Het volledig verwijderen of herschrijven van de HTTP `Server` header kan afhankelijk zijn van de gebruikte gateway-image en beschikbare nginx-modules. Dit blijft daarom een deployment-hardeningpunt voor echte productie.

## Governance-documenten

De repo bevat nu invulbare AVG-documenten onder `docs/avg/`:

- verwerkingsregister
- DPIA
- privacyverklaring-template
- verwerkersovereenkomst-checklist
- bewaartermijnenbeleid
- datalekprocedure
- procedure rechten van betrokkenen

## Eindoordeel

De eerder gevonden technische AVG-blockers zijn opgelost of verder verkleind en met tests afgedekt. De app is technisch sterker in lijn met AVG-beveiligingseisen, maar volledige AVG-conformiteit kan pas definitief worden geclaimd wanneer de organisatie de governance-documenten invult, goedkeurt, uitvoert en periodiek herziet.
