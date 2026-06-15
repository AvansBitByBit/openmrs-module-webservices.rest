# Module gebruiken in OpenMRS

Dit document legt uit hoe de REST Web Services module binnen OpenMRS draait en hoe je dat kunt aantonen tijdens een demo.

## Wat is OpenMRS?

OpenMRS is het elektronisch patientendossier. Het systeem beheert onder andere patienten, bezoeken, observaties, gebruikers en medische gegevens.

De REST Web Services module is een uitbreiding op OpenMRS. Met deze module kunnen andere applicaties via HTTP endpoints met OpenMRS praten.

## Hoe draait de module in onze OTAP setup?

De setup gebruikt een vaste OpenMRS Reference Application versie: `3.6.0`.

| Onderdeel | Rol |
| --- | --- |
| `module-builder` | Bouwt deze repository naar een `.omod` bestand |
| `docker/backend/Dockerfile` | Bouwt een backend image op basis van de officiele OpenMRS demo backend |
| `backend` | OpenMRS met deze REST module geladen |
| `frontend` | OpenMRS gebruikersinterface |
| `gateway` | Routeert browser- en API-verkeer |
| `db` | MariaDB database met OpenMRS data en demo-data |

Er is geen aparte OpenMRS fork nodig. Onze backend overlay gebruikt de officiele OpenMRS backend, schakelt alleen de lokale OCL startup-import uit en vervangt de gebundelde REST module door de lokaal gebouwde module uit deze repository. De normale RefApp backend modules blijven aanwezig voor de O3 frontend. `referencedemodata` blijft actief voor demo-patienten.

## OpenMRS starten

```powershell
docker compose -f docker-compose.dev.yml --profile build-module run --rm module-builder
docker compose -f docker-compose.dev.yml up --build -d
```

Open:

```text
http://localhost:8080/openmrs/spa
```

Login:

```text
admin / Admin123
```

## Aantonen dat de REST module werkt

Controleer de sessie endpoint:

```powershell
curl.exe http://localhost:8080/openmrs/ws/rest/v1/session
```

Verwacht: JSON met `authenticated`.

Controleer in de backend container dat de module aanwezig is:

```powershell
docker compose -f docker-compose.dev.yml exec -T backend sh -c "ls /openmrs/distribution/openmrs_modules/webservices.rest-*.omod"
```

Verwacht: een `webservices.rest-*.omod` bestand. De REST sessie endpoint bewijst daarna dat de module requests afhandelt.

## Aantonen dat demo-data aanwezig is

Controleer het aantal patienten:

```powershell
docker compose -f docker-compose.dev.yml exec -T db mariadb -uopenmrs -popenmrs openmrs_dev -e "SELECT COUNT(*) AS patients FROM patient;"
```

Verwacht: meer dan `0` patienten.

In de UI kun je daarna patient-gerelateerde schermen openen om te laten zien dat OpenMRS met demo-data draait.

## Uitleg voor docenten

Korte uitleg:

> OpenMRS is het zorgsysteem. Onze module voegt REST endpoints toe aan OpenMRS, zodat andere systemen op een gecontroleerde manier gegevens kunnen lezen of schrijven. Voor de demo gebruiken we een vaste OpenMRS versie met demo-data en injecteren we onze lokaal gebouwde module in de backend image.

## OTAP

| Omgeving | Poort | URL |
| --- | --- | --- |
| Dev | `8080` | `http://localhost:8080/openmrs/spa` |
| Test | `8081` | `http://localhost:8081/openmrs/spa` |
| Prod | `8082` | `http://localhost:8082/openmrs/spa` |

Elke omgeving heeft eigen Docker volumes voor database en OpenMRS data.
