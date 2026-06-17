# Lokale OTAP setup

Deze handleiding start de REST Web Services module in een vaste OpenMRS Reference Application runtime. Iedereen gebruikt dezelfde OpenMRS versie: `3.6.0`.

De setup gebruikt geen aparte OpenMRS fork. Deze repository bouwt zelf de `.omod` en plaatst die in een kleine backend image bovenop de officiele OpenMRS backend.

De backend overlay laat de normale RefApp modules staan, inclusief OCL, maar verwijdert de lokale OCL startup-importconfiguratie. Die import veroorzaakt lokaal de lange/failed import-runs. De normale RefApp backend modules blijven aanwezig, zodat de O3 frontend geen ontbrekende backend dependencies meldt. `referencedemodata` blijft aanwezig, zodat demo-patienten worden aangemaakt.

## Benodigdheden

- Docker Desktop
- Docker Compose v2
- Internetverbinding om Docker images en Maven dependencies te downloaden
- Deze repository: `openmrs-module-webservices.rest`

Controleer Docker:

```powershell
docker --version
docker compose version
```

## Dev starten

Ga naar de repository:

```powershell
cd "C:\Users\yazan\OneDrive\Bureaublad\2.4\2.4lu2\openmrs-module-webservices.rest"
```

Bouw eerst de lokale REST module:

```powershell
docker compose -f docker-compose.dev.yml --profile build-module run --rm module-builder
```

Start daarna Dev:

```powershell
docker compose -f docker-compose.dev.yml up --build -d
```

Open OpenMRS:

```text
http://localhost:8080/openmrs/spa
```

Login voor lokale demo:

```text
Username: admin
Password: Admin123
```

## Controleren of OpenMRS klaar is

Controleer de REST API:

```powershell
curl.exe http://localhost:8080/openmrs/ws/rest/v1/session
```

Verwacht: JSON met onder andere `authenticated`. Als je HTML of een installatiepagina ziet, is OpenMRS nog niet klaar of is de database opnieuw aan het initialiseren.

Controleer of demo-patienten aanwezig zijn:

```powershell
docker compose -f docker-compose.dev.yml exec -T db mariadb -uopenmrs -popenmrs openmrs_dev -e "SELECT COUNT(*) AS patients FROM patient;"
```

Verwacht: `patients` is groter dan `0`.

Controleer of deze module in de backend image zit:

```powershell
docker compose -f docker-compose.dev.yml exec -T backend sh -c "ls /openmrs/distribution/openmrs_modules/webservices.rest-*.omod"
```

Verwacht: een `webservices.rest-*.omod` bestand. De REST sessie endpoint hierboven bewijst daarna dat de module ook requests afhandelt.

## Stoppen en resetten

Stoppen zonder demo-data te verwijderen:

```powershell
docker compose -f docker-compose.dev.yml down
```

Volledig resetten, inclusief database en demo-data:

```powershell
docker compose -f docker-compose.dev.yml down -v --remove-orphans
```

Gebruik `down -v` alleen als je bewust opnieuw wilt initialiseren. Daarna moet OpenMRS de demo database opnieuw opbouwen.

## Test en Prod

Test gebruikt poort `8081`:

```powershell
docker compose -f docker-compose.test.yml --profile build-module run --rm module-builder
docker compose -f docker-compose.test.yml up --build -d
```

Open:

```text
http://localhost:8081/openmrs/spa
```

Prod gebruikt poort `8082` en vereist database secrets:

```powershell
$env:OMRS_DB_PASSWORD = "replace-me"
$env:MYSQL_ROOT_PASSWORD = "replace-me-root"
docker compose -f docker-compose.prod.yml --profile build-module run --rm module-builder
docker compose -f docker-compose.prod.yml up --build -d
```

Open:

```text
http://localhost:8082/openmrs/spa
```

## Wat draait er?

| Service | Betekenis |
| --- | --- |
| `db` | MariaDB database voor OpenMRS |
| `backend` | OpenMRS backend `3.6.0` met deze REST module |
| `frontend` | OpenMRS frontend `3.6.0` |
| `gateway` | Router/proxy naar frontend en backend |
| `module-builder` | Bouwt deze repository naar `docker/modules/webservices.rest-*.omod` |

De backend image wordt lokaal gebouwd vanuit `docker/backend/Dockerfile`. Die image start vanaf `openmrs/openmrs-reference-application-3-backend:3.6.0`, verwijdert de OCL startup-importconfiguratie, verwijdert de gebundelde REST module en kopieert de lokaal gebouwde REST module naar OpenMRS.
