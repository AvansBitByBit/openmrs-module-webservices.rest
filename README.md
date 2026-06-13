[![Build Status](https://github.com/openmrs/openmrs-module-webservices.rest/actions/workflows/maven.yml/badge.svg)](https://github.com/openmrs/openmrs-module-webservices.rest/actions/workflows/maven.yml) [![Coverage Status](https://coveralls.io/repos/github/openmrs/openmrs-module-webservices.rest/badge.svg?branch=master)](https://coveralls.io/github/openmrs/openmrs-module-webservices.rest?branch=master)

<img src="https://talk.openmrs.org/uploads/default/original/2X/f/f1ec579b0398cb04c80a54c56da219b2440fe249.jpg" alt="OpenMRS"/>

# OpenMRS REST Web Services Module

> REST API for [OpenMRS](http://openmrs.org)

<a href="https://ci.openmrs.org/browse/RESTWS-RESTWS"><img src="https://omrs-shields.psbrandt.io/build/RESTWS/RESTWS" alt="Build"/></a>
<a href="https://modules.openmrs.org/#/show/153/webservices-rest"><img src="https://omrs-shields.psbrandt.io/version/153" alt="Version"/></a>
<a href="https://modules.openmrs.org/#/show/153/webservices-rest"><img src="https://omrs-shields.psbrandt.io/omrsversion/153" alt="OpenMRS Version"/></a>

The module exposes the OpenMRS API as REST web services. If an OpenMRS instance is running the `webservice.rest` module, other applications can retrieve and post certain information to an OpenMRS database.

## Download

If you are not a developer, or just want to install the REST Web Services module into your
system, visit [the module download page](https://modules.openmrs.org/#/show/153/webservices-rest) instead.

> The required OpenMRS version to run the REST Web Services Module is `1.8.4+` or `1.9.0+`

## Build

To build the module from source, clone this repo:

```
git clone https://github.com/openmrs/openmrs-module-webservices.rest
```

Then navigate into the `openmrs-module-webservices.rest` directory and compile the module using Maven:

```
cd openmrs-module-webservices.rest && mvn clean install
```

:pushpin: You will need Maven and Java 8 installed to successfully build and run
the tests.

## Developer Documentation

### OTAP omgevingen en CI/CD

Deze repository gebruikt drie gescheiden deploymentomgevingen:

| Omgeving | GitHub Environment | Compose-bestand | Doel | Branches |
| --- | --- | --- | --- | --- |
| Dev | `Dev` | `docker-compose.dev.yml` | Lokale/dev-validatie met snelle module-build. | `dev` |
| Test | `Test` | `docker-compose.test.yml` | Integratie- en acceptatievalidatie met testconfiguratie. | `acceptance`, `devprodomgeving` |
| Prod | `Prod` | `docker-compose.prod.yml` | Productie-release met verplichte secrets en approval gate. | `master` |

De GitHub Actions workflow staat in `.github/workflows/ci-cd-environments.yml`. Pull requests voeren policy checks en Maven build/tests uit. Deployments lopen via GitHub Environments, zodat configuratie, secrets en approval gates per omgeving gescheiden blijven.

#### Docker Compose

Start een specifieke omgeving met Docker Compose:

```bash
docker compose -f docker-compose.dev.yml up --build
docker compose -f docker-compose.test.yml up --build
docker compose -f docker-compose.prod.yml up --build
```

Dev gebruikt poort `8080`; Test gebruikt standaard poort `8081`; Prod gebruikt standaard poort `8080`. Voor Test en Prod kan de hostpoort worden aangepast met `OPENMRS_HTTP_PORT`.

De compose-bestanden bevatten een `module-builder` service. Die bouwt de `.omod` met Maven en kopieert het resultaat naar `docker/modules/`. De OpenMRS-container mount deze map als `/openmrs/data/modules`, zodat de gebouwde REST-module in de omgeving geladen kan worden. Gegenereerde `.omod` bestanden in `docker/modules/` worden niet gecommit.

Voor Prod moeten secrets expliciet als environment variables worden gezet voordat de omgeving start:

```bash
export OMRS_DB_PASSWORD="change-me"
export OMRS_DB_ROOT_PASSWORD="change-me-root"
export OMRS_ADMIN_USER_PASSWORD="change-me-admin"
docker compose -f docker-compose.prod.yml up --build
```

#### GitHub Environments

Maak in de repository-instellingen onder `Settings > Environments` de volgende environments aan:

```text
Dev
Test
Prod
```

Gebruik per GitHub Environment eigen variables en secrets:

| Naam | Type | Gebruik |
| --- | --- | --- |
| `OPENMRS_BASE_URL` | Environment variable | Basis-URL van de OpenMRS instance voor die omgeving. |
| `DEPLOY_ENABLED` | Environment variable | `true` activeert de echte deploystap nadat het deploycommando is gekoppeld. |
| `DEPLOY_TOKEN` | Environment secret | Token of credential voor deployment naar alleen die omgeving. |
| `OPENMRS_TEST_USERNAME` | Environment secret | Gebruiker voor live API/Postman tests in `Test`. |
| `OPENMRS_TEST_PASSWORD` | Environment secret | Wachtwoord voor live API/Postman tests in `Test`. |

Secrets worden niet als repository-wide secrets gebruikt. Hetzelfde secret-veld mag in `Dev`, `Test` en `Prod` dezelfde naam hebben, maar GitHub bewaart en verstrekt de waarden per environment gescheiden. Lokale `.env`-bestanden en secret-bestanden mogen niet worden gecommit; de workflow blokkeert dit.

#### Protection rules

Configureer de volgende protection rules in GitHub:

| Environment | Protection |
| --- | --- |
| `Dev` | Geen verplichte reviewer nodig; deployment branches beperken tot `dev`. |
| `Test` | Optioneel required reviewers aan; deployment branches beperken tot `acceptance` en `devprodomgeving`. |
| `Prod` | Required reviewers aan met minimaal 1 approver; deployment branches beperken tot `master`; self-review uit waar beschikbaar. |

De productiejob controleert daarnaast zelf dat de deployment vanaf `master` komt. Daardoor kan een handmatige `workflow_dispatch` vanaf een andere branch niet naar Prod deployen.

#### NEN-7510 controls voor CI/CD

De CI/CD-inrichting ondersteunt de volgende NEN-7510-maatregelen:

| Control | Inrichting |
| --- | --- |
| Scheiding van omgevingen | Aparte Docker Compose bestanden en GitHub Environments `Dev`, `Test` en `Prod` met eigen variables, secrets en branch policies. |
| Least privilege | De workflow gebruikt alleen `contents: read` en krijgt secrets pas in de environment-job. |
| Vier-ogenprincipe | Environment approvals zijn verplicht voor `Prod` en optioneel voor `Test`. |
| Traceerbaarheid | GitHub Actions bewaart per run commit, actor, approval en artifact. |
| Wijzigingsbeheer | Productie is beperkt tot `master`; pull requests draaien eerst build en policy checks. |
| Bescherming van vertrouwelijke gegevens | Geen secrets in code, geen repository-wide deployment secrets, en policy check tegen committed `.env`/secret-bestanden. |
| Scheiding testdata/productiedata | Testdata blijft onder `src/test/resources`; de workflow controleert dat deploybare `.omod` artifacts geen testfixtures bevatten. |
| Herleidbare artifacts | De `.omod` wordt als workflow artifact opgeslagen met beperkte retentie. |

#### Voorkomen dat testdata in productie komt

Testdata staat alleen in testresources zoals `omod/src/test/resources`. Maven gebruikt deze resources voor tests, maar ze horen niet in het deploybare `.omod` artifact. De workflow inspecteert elk gebouwd `.omod` artifact en faalt als bekende testfixture-patronen zoals `testDataset`, `_testData`, `create_patient.json` of `update_patient.json` toch in het artifact zitten.

Prod gebruikt daarnaast alleen de `Prod` environment, de `Prod` secrets en de `master` branch. Testdeployments gebruiken de `Test` environment en kunnen niet bij productie-secrets.

#### Nieuwe ontwikkelaar

1. Clone de repository:

```
git clone https://github.com/AvansBitByBit/openmrs-module-webservices.rest
cd openmrs-module-webservices.rest
```

2. Installeer Java 8 en Maven.

3. Build lokaal:

```
mvn clean install
```

4. Run integration tests tegen een lokale OpenMRS server:

```
mvn clean verify -Pintegration-tests -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```

5. Maak voor lokale configuratie alleen lokale, niet-gecommitte bestanden aan. Commit geen `.env`, tokens, wachtwoorden of exportbestanden met zorgdata.

6. Start indien nodig een lokale omgeving:

```
docker compose -f docker-compose.dev.yml up --build
```

7. Werk via pull requests. Een merge naar `dev` kan naar `Dev`; een merge naar `acceptance` of `devprodomgeving` kan de `Test` gate gebruiken; productie loopt alleen via `master` en vereist environment approval in `Prod`.

### Integration Tests

Integration tests can be found in the integration-tests directory. They are written with JUnit and Rest-Assured.
Before you can run integration tests you need to start up a server and install the module.
You can run integration tests with:
```
mvn clean verify -Pintegration-tests -DtestUrl=http://admin:Admin123@localhost:8080/openmrs
```
You can skip the testUrl parameter, if it is the same for your server.

### Wiki Pages

| Page | Description |
| ---- | ----------- |
| [REST Module](https://wiki.openmrs.org/display/docs/REST+Module) | The main module page with a description of the configuration options. |
| [Technical Documentation](https://wiki.openmrs.org/display/docs/REST+Web+Services+Technical+Documentation) | Technical information about the Web Services implementation. |
| [Core Developer Guide](https://wiki.openmrs.org/display/docs/Adding+a+Web+Service+Step+by+Step+Guide+for+Core+Developers) | Description of how to add REST resources to OpenMRS core. |
| [Module Developer Guide](https://wiki.openmrs.org/display/docs/Adding+a+Web+Service+Step+by+Step+Guide+for+Module+Developers) | Description of how to add REST resources to OpenMRS modules. |

### API Documentation

The API documentation is available inside the OpenMRS application and is linked
to the advanced administration screen. The URL should be something like:
> [http://localhost:8080/openmrs/module/webservices/rest/apiDocs.htm](http://localhost:8080/openmrs/module/webservices/rest/apiDocs.htm)

### Example Client code
  * Quick java swing client that displays patients and encounters: http://svn.openmrs.org/openmrs-contrib/examples/webservices/hackyswingexample/
  * You can download a client java application that allows add/edit a person (any resource) by making a query to the webservices.rest module - https://project-development-software-victor-aravena.googlecode.com/svn/trunk/ClientOpenMRSRest/

### Contributing to the API Documentation

The OpenMRS API documentation is built automatically using [Swagger UI](http://swagger.io/swagger-ui/). For details on how to customize the documentation see the [`swagger-ui` branch](https://github.com/psbrandt/openmrs-contrib-apidocs/tree/swagger-ui) in the [`openmrs-contrib-apidocs` repo](https://github.com/psbrandt/openmrs-contrib-apidocs).

## License

[MPL-2.0 w/ HD](http://openmrs.org/license/)
