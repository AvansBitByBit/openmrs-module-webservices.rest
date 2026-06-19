# Gap-analyse NEN-7510:2024-2

**Module:** openmrs-module-webservices.rest  
**Datum:** 2026-06-11

---

## A.8.3 – Toegangsbeveiliging

**Status:** Opgelost voor de REST-modulecode op 2026-06-19

**Bewijslast:**

Zie `avg-conformiteitsbewijs.md` voor het actuele bewijs. `RestUtil.ipMatches` retourneert nu `false` als de allowlist leeg is. `/session/diag` vereist nu `View RESTWS` en retourneert geen rollen of privileges.

**Huidige situatie:**  
De module gebruikt nu deny-by-default voor een lege IP-allowlist. Daarnaast is het diagnostics-endpoint (`/diag`) afgeschermd met een privilege-check.

**Gewenste situatie (NEN-7510):**  
Toegang wordt standaard geweigerd (deny-by-default). Elk endpoint dat interne of gevoelige gegevens ontsluit, vereist een autorisatiecontrole.

**Afhandeling:**
1. De code in `RestUtil.java` is aangepast zodat een lege IP-lijst resulteert in een blokkade (`return false`).
2. Het `/diag` endpoint is beveiligd met `Context.requirePrivilege(RestConstants.PRIV_VIEW_RESTWS)`.

---

## A.8.5 – Authenticatie

**Status:** Opgelost voor de REST-modulecode op 2026-06-19

**Bewijslast:**

```java
// AuthorizationFilter.java, regel 32-36
// Filter intended for all /ws/rest calls that allows the user to authenticate via Basic
// authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
// exceptions if an unauthenticated user tries to do something they are not allowed to do.)
```

```java
// AuthorizationFilter.java, regel 110-114
catch (Exception ex) {
    // This filter never stops execution. If the user failed to
    // authenticate, that will be caught later.
    log.debug("authentication exception ", ex);
}
```

**Huidige situatie:**  
Het authenticatiefilter stopt foutieve Basic Auth nu direct met HTTP 401. Daarnaast beperkt een in-memory rate limiter herhaalde mislukte pogingen per gebruikersnaam/IP-combinatie en retourneert tijdelijk HTTP 429 bij lockout.

**Gewenste situatie (NEN-7510):**  
Authenticatie moet robuust en afdwingbaar zijn. Een ongeldige inlogpoging mag nooit het applicatiedomein (de API) bereiken. Er moeten maatregelen zijn tegen brute-force aanvallen.

**Afhandeling:**
1. `AuthorizationFilter.java` retourneert direct HTTP 401 bij foutieve Basic Auth.
2. `AuthenticationRateLimiter` blokkeert standaard na 5 mislukte pogingen binnen 15 minuten voor 15 minuten.
3. Lockout en geweigerde lockout-verzoeken worden auditwaardig gelogd.

---

## A.8.15 – Logging

**Status:** Opgelost voor de REST-modulecode op 2026-06-19

**Bewijslast:**

```java
// AuthorizationFilter.java, regel 113
log.debug("authentication exception ", ex);
```

```java
// ServerLogActionWrapper1_8.java, regel 20
return (MemoryAppender) Logger.getRootLogger().getAppender("MEMORY_APPENDER");
```

**Huidige situatie:**  
Mislukte inlogpogingen worden alleen gelogd op `DEBUG`-niveau, wat op een productieserver normaal gesproken onzichtbaar is. Daarnaast worden logs alleen tijdelijk in het werkgeheugen (RAM) opgeslagen; bij een herstart is het volledige logboek leeg.

**Gewenste situatie (NEN-7510):**  
Relevante beveiligingsgebeurtenissen (zoals inloggen of geweigerde toegang) moeten altijd en permanent worden vastgelegd ten behoeve van forensisch onderzoek.

**De Gap (Wat moet er gebeuren):**  
1. Verhoog het log-niveau van authenticatiefouten naar `WARN` of `ERROR` zodat deze in productie opvallen.
2. Sla beveiligingslogs persistent op schijf op via een robuuste appender (bijv. een `FileAppender` of integratie met een centraal loggingsysteem), zodat ze na een server-herstart behouden blijven.

---

## A.8.24 – Gebruik van cryptografie

**Status:** Afwezig

**Bewijslast:**

```java
// AuthorizationFilter.java, regel 99
String decoded = new String(Base64.decodeBase64(basicAuth), Charset.forName("UTF-8"));
```

**Huidige situatie:**  
De module gebruikt nog Basic Auth, maar vereist standaard secure transport voordat Basic Auth wordt geaccepteerd. Productie zet `webservices.rest.requireSecureTransport=true`; dev/test zetten deze afwijking expliciet op `false` voor lokaal HTTP-gebruik.

**Gewenste situatie (NEN-7510):**  
Gevoelige data (inclusief wachtwoorden en patiëntgegevens) moet tijdens verzending altijd cryptografisch versleuteld zijn.

**Afhandeling:**
1. `AuthorizationFilter.java` weigert Basic Auth zonder secure request met HTTP 426 als secure transport vereist is.
2. `docker-compose.prod.yml` zet secure transport enforcement aan.
3. Reverse proxies moeten TLS tot aan backend borgen of de Tomcat connector correct als secure configureren; proxy headers worden bewust niet vertrouwd.

---

## A.8.28 – Veilig programmeren

**Status:** Opgelost voor standaard foutresponses op 2026-06-19

**Bewijslast:**

```java
// RestUtil.java, regel 855
map.put("code", stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber());
```

**Huidige situatie:**  
Wanneer de API een foutmelding genereert, bevat het `code`-veld niet langer de interne klassenaam en het regelnummer. Stacktrace-details blijven standaard leeg en zijn alleen via de bestaande expliciete debug-property te activeren.

**Gewenste situatie (NEN-7510):**  
Applicaties mogen geen overbodige technische informatie (zoals broncodestructuur, stack traces of versie-informatie) lekken naar eindgebruikers, om te voorkomen dat aanvallers hiermee zwakke plekken kunnen identificeren.

**Afhandeling:**
1. `RestUtil.wrapErrorResponse` vult `code` niet meer met Java class/regelnummer.
2. `RestUtilTest#wrapErrorResponse_shouldNotExposeStackTraceCodeIfAvailable` bewaakt dat class names en regelnummers niet in de standaard response verschijnen.
