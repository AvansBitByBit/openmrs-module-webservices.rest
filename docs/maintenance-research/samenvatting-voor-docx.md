# Samenvatting voor DOCX

Voor LU2 is de onderhoudbaarheid van `openmrs-module-webservices.rest` onderzocht met ISO 25010 Maintainability. De repo heeft een logische Maven-indeling met `omod-common`, `omod` en `integration-tests`, maar centrale frameworkclasses zijn groot en complex.

De analyse gebruikt meerdere metrieken: volume/LOC, rough complexity, component balance, import coupling, unit interfacing, package coupling, PMD CPD duplicatie en coverage. Grote hotspots zijn `SwaggerSpecificationCreator.java` (1241 LOC), `RestUtil.java` (951 LOC), `BaseDelegatingResource.java` (902 LOC), `RestServiceImpl.java` (738 LOC) en `ConversionUtil.java`.

De PoC richt zich op `ConversionUtil.convert(Object, Type)`. Deze methode had in de baseline 118 LOC en cyclomatic complexity 23. De verbetering is bewust klein gehouden: de public API bleef gelijk, maar de interne logica is opgesplitst in private helper methods voor collections/arrays, strings, dates/classes, `valueOf` en numbers.

Er zijn vier regressietests toegevoegd. Na de PoC draait `ConversionUtilTest` groen met 26 tests en draait `omod-common` groen met 121 tests. De methode daalde van 118 naar 39 LOC en van CC 23 naar CC 12. De JaCoCo coverage voor `ConversionUtil` steeg van 59.27% naar 64.98% line coverage en van 50.58% naar 56.82% branch coverage.

De regressieconclusie is bewust afgebakend: er is geen nieuwe regressie aangetoond binnen de uitgevoerde `omod-common`/`ConversionUtil` scope. De full Maven reactor is niet groen; `mvn clean test` faalt baseline en after op dezelfde bestaande dependency-plugin failure, en `mvn clean verify` heeft alleen after-evidence met 1 testfailure in `omod`.

Conclusie: de PoC verbetert onderhoudbaarheid lokaal en aantoonbaar, vooral analyzability en testability van `ConversionUtil.convert`. De repo als geheel heeft nog vervolgwerk nodig in grotere centrale classes en in de full reactor test/buildstatus.
