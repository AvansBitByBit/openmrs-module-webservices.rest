# Auditrapport NEN-7510 8.15 en NEN 7513 auditlogging

## 1. Doel en scope

Dit document beschrijft de toegevoegde Java auditlogging voor beveiligingsgebeurtenissen in de OpenMRS REST Web Services module. De implementatie richt zich op NEN-7510 beheersmaatregel 8.15 en NEN 7513: herleidbare logging van toegang tot medische gegevens en autorisatiehandelingen.

De loggingcode is eerst toegevoegd in `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/` en daarna gekoppeld aan bestaande REST-paden.

## 2. Implementatie

| Onderdeel | Bestand | Functie |
| --- | --- | --- |
| Auditformatter en logger | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/SecurityAuditLogger.java` | Legt security-events vast met de 5 W's: wie, wat, wanneer, waar, waarom. |
| Persistente writer | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/FileAuditLogWriter.java` | Schrijft auditregels append-only naar een bestand. Standaard: `${user.home}/openmrs-webservices-rest-audit.log`. Configureerbaar via `openmrs.webservices.rest.audit.log.path`. |
| Writer interface | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/audit/AuditLogWriter.java` | Maakt persistente opslag testbaar met Mockito. |
| Loginlogging | `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/filter/AuthorizationFilter.java` | Logt succesvolle en mislukte Basic-authentication events. |
| Patiëntdossierinzage | `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/resource/openmrs1_8/PatientResource1_8.java` | Logt iedere geslaagde retrieve van een patiëntdossier. |
| Rol/rechtenwijziging | `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/resource/openmrs1_8/RoleResource1_8.java` en `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/resource/openmrs2_0/UserResource2_0.java` | Logt rolcreatie, rolupdates en user-role assignment updates. |
| Exportlogging | `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_8/ObsComplexValueController1_8.java` en `omod/src/main/java/org/openmrs/module/webservices/rest/web/v1_0/controller/openmrs1_9/FormResourceController1_9.java` | Logt export/download van complexe observatiebestanden en form resources. |

## 3. Logformaat

Elke auditregel gebruikt een vaste structuur:

```text
audit_event wie="<actor>" wat="<actie>" wanneer="<UTC ISO-8601 timestamp>" waar="<bron/ip + HTTP context>" waarom="<reden>"
```

Voorbeeld:

```text
audit_event wie="arts.jansen" wat="PATIENT_RECORD_VIEW patientUuid=patient-uuid-123" wanneer="2026-06-19T08:15:30Z" waar="10.0.0.5 GET /ws/rest/v1/patient/patient-uuid-123" waarom="REST patient dossier inzage"
```

Security-events worden niet op `DEBUG` gelogd. Succesvolle auditwaardige acties gebruiken `WARN`; mislukte of geweigerde security-acties gebruiken `ERROR`.

## 4. Privacy en testdekking

### 4.1 Gevoelige gegevens

De logger schrijft geen wachtwoorden, BSN's, diagnoses, medicatie, autorisatieheaders, cookies, sessietokens of bearer/basic tokens weg. De sanitizer redigeert bekende patronen naar `[REDACTED]` voordat de regel naar de applicatielog en de persistente auditwriter gaat.

De implementatie logt bij patiëntdossiers en export alleen technische identifiers zoals UUID's. Patiëntnamen, identifiers/display strings, diagnoses, medicatie, observatiewaarden, bestandsinhoud en sessietokens worden niet bewust aan auditregels toegevoegd.

### 4.2 Gedekte gebeurtenissen

| Gebeurtenis | Status |
| --- | --- |
| Inloggen succesvol | Gedekt via `SecurityAuditLogger.loginSucceeded` en `AuthorizationFilter`. |
| Inloggen fout | Gedekt via `SecurityAuditLogger.loginFailed` en `AuthorizationFilter`. |
| Inzage patiëntdossier | Gedekt via `PatientResource1_8.retrieve`. |
| Wijziging rechten/rol | Gedekt via `RoleResource1_8.create`, `RoleResource1_8.update` en `UserResource2_0.save`. |
| Export van gegevens | Gedekt via observatie- en form-resource downloadcontrollers. |
| Mislukte/geweigerde acties | Gedekt in de auditlogger-API met `loginFailed`, `patientRecordAccessDenied` en `dataExportDenied`. |

### 4.3 Persistentie

Auditregels worden niet alleen in memory vastgelegd. `SecurityAuditLogger` schrijft iedere regel ook naar `AuditLogWriter`. De standaardimplementatie `FileAuditLogWriter` appendt naar een bestand. Het pad kan per omgeving worden gezet met:

```bash
-Dopenmrs.webservices.rest.audit.log.path=/var/log/openmrs/webservices-rest-audit.log
```

### 4.4 Testklassen

De auditlogging wordt getest in:

`omod-common/src/test/java/org/openmrs/module/webservices/rest/web/audit/SecurityAuditLoggerTest.java`

Deze testklasse bevat:

| Test | Bewijs |
| --- | --- |
| `shouldLogSuccessfulSecurityActionsWithFiveWs` | Succesvolle login, patiëntdossierinzage, rol/rechtenwijziging en export worden op `WARN` gelogd met alle 5 W's. |
| `shouldLogFailedSecurityActionsAtErrorLevel` | Fout wachtwoord, geweigerde patiënttoegang en geweigerde export worden op `ERROR` gelogd met alle 5 W's. |
| `shouldNotWriteSensitiveDataToLogOrPersistentWriter` | Wachtwoord, BSN, diagnose, medicatie, bearer token en session token ontbreken in zowel ListAppender-output als persistente writer-output. |
| `shouldPersistAuditLinesToFile` | Auditregels worden daadwerkelijk naar een bestand geschreven. |

De tests gebruiken Logback `ListAppender` om logregels te vangen en Mockito om te verifiëren dat veilige auditregels naar de writer worden doorgezet.

## 5. Verificatie

Uitgevoerd op 19 juni 2026:

```bash
mvn -pl omod-common -Dtest=SecurityAuditLoggerTest test
mvn -pl omod -am -DskipTests compile
mvn verify
```

Resultaat:

| Commando | Resultaat |
| --- | --- |
| `mvn -pl omod-common -Dtest=SecurityAuditLoggerTest test` | Groen, 4 tests, 0 failures, 0 errors. |
| `mvn -pl omod -am -DskipTests compile` | Groen, reactor compile succesvol voor parent, `omod-common` en `omod`. |
| `mvn verify` | Groen, volledige reactor succesvol voor parent, `omod-common`, `omod` en `integration-tests`. |

## 6. PR-review

De code is voorbereid voor review via Pull Request. Voor volledige afronding van het criterium "Code is gereviewed via PR" moet een PR worden aangemaakt en door minimaal een tweede reviewer worden goedgekeurd. Reviewpunten:

| Reviewpunt | Verwachte controle |
| --- | --- |
| 5 W's | Iedere auditregel bevat `wie`, `wat`, `wanneer`, `waar`, `waarom`. |
| Dataminimalisatie | Geen wachtwoorden, BSN's, diagnoses, medicatie of tokens in logregels. |
| Persistentie | Auditregels worden via `FileAuditLogWriter` weggeschreven. |
| Logniveau | Security-events gebruiken `WARN` of `ERROR`, niet `DEBUG`. |
| Testbewijs | `SecurityAuditLoggerTest` slaagt en gebruikt `ListAppender`. |
