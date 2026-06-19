# Samenvatting voor DOCX

Voor LU2 is de onderhoudbaarheid van `openmrs-module-webservices.rest` onderzocht met ISO 25010 Maintainability. De repo heeft een logische Maven-structuur met `omod-common`, `omod` en `integration-tests`, maar centrale frameworkclasses zijn groot en complex. Vooral `RestServiceImpl`, `RestUtil`, `BaseDelegatingResource` en Swagger-generatie zijn hotspots.

De hoofd-PoC is verplaatst van de eerdere kleine `ConversionUtil`-refactor naar een architectuurrefactor van `RestServiceImpl`. Baseline was `RestServiceImpl` 737 LOC, 32 imports en 170 rough decision tokens. De class deed tegelijk resource discovery, search handler selectie en service-coordinatie.

De verbetering splitst dit op in package-private `ResourceRegistry` en `SearchHandlerRegistry`. `RestServiceImpl` blijft de publieke facade en het `RestService` API is niet gewijzigd. Na de PoC is `RestServiceImpl` 197 LOC en heeft nog 19 rough decision tokens. De registries blijven kleiner dan de oude god-class.

Validatie: `RestServiceImplTest` draait groen met 53 tests, `omod-common` draait groen met 121 tests, `git diff --check` is groen en `mvn clean verify` is groen. `mvn clean test` was rood op een bestaande/flaky `ClearDbCacheController2_0Test`, daarom claim ik niet algemeen dat er nul regressierisico in de hele repo is.

Live integration met Docker is geprobeerd, maar Docker Desktop/daemon draaide niet. Daardoor is live OpenMRS integration niet bewezen. Conclusie: de onderhoudbaarheid is aantoonbaar verbeterd voor de gekozen architectuurhotspot, vooral in modularity, analyzability en modifiability, met eerlijke beperkingen rond full reactor stabiliteit en live integration.
