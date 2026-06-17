# 02 Teststrategie

## Bestaande tests

Voor de hoofd-PoC zijn deze bestaande tests gebruikt:

- `omod/src/test/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImplTest.java`
- `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/api/RestServiceTest.java`

`RestServiceImplTest` bestaat dus echt en is niet aangenomen. De class bevat 53 tests.

## Characterization gedrag

De bestaande tests dekken de belangrijkste risico's:

| Gedrag | Status |
|---|---|
| Duplicate resource order -> `IllegalStateException` | aanwezig |
| Superclass/direct-superclass lookup | aanwezig |
| Duplicate search handler id -> `IllegalStateException` | aanwezig |
| Ambiguous handlers met `default` -> default gekozen | aanwezig |
| Ambiguous handlers zonder `default` -> `InvalidSearchException` | aanwezig |

Omdat dit al bestond, zijn geen dubbele tests toegevoegd.

## Commands

Belangrijkste testcommands:

```bash
mvn --batch-mode --no-transfer-progress -pl omod-common,omod "-Dtest=RestServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
mvn --batch-mode --no-transfer-progress -pl omod-common test
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
git diff --check
```

## Resultaat

Focused en module-tests zijn groen. `clean verify` is groen. `clean test` was rood op `ClearDbCacheController2_0Test`, een failure buiten de gekozen PoC en ook eerder/flaky waargenomen. Daarom is de regressieclaim beperkt en niet absoluut.
