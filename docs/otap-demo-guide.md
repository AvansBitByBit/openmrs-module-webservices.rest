# OTAP demo guide

This guide explains how to demonstrate the REST Web Services module running inside a fixed OpenMRS Reference Application `3.6.0` Docker stack.

For the step-by-step local setup, see [`setup.md`](setup.md).

## Architecture

| Part | Role |
| --- | --- |
| `openmrs-module-webservices.rest` | Builds the REST module `.omod` |
| Backend overlay | Starts from `openmrs/openmrs-reference-application-3-backend:3.6.0`, disables the OCL startup import config, and injects the local `.omod` |
| Frontend | `openmrs/openmrs-reference-application-3-frontend:3.6.0` |
| Gateway | `openmrs/openmrs-reference-application-3-gateway:3.6.0` |
| MariaDB | Stores OpenMRS data and demo data |

No separate OpenMRS fork is required. The OCL backend module remains installed because the official frontend depends on it, but the local OCL startup import config is removed because that import makes first boot unreliable. The other normal RefApp backend modules remain so the O3 frontend dependencies resolve; `referencedemodata` remains enabled for demo patients.

## Start Dev

```bash
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

## Readiness Checks

REST endpoint:

```bash
curl http://localhost:8080/openmrs/ws/rest/v1/session
```

Expected result: JSON with an `authenticated` field. HTML means OpenMRS is not ready yet.

Demo patients:

```bash
docker compose -f docker-compose.dev.yml exec -T db mariadb -uopenmrs -popenmrs openmrs_dev -e "SELECT COUNT(*) AS patients FROM patient;"
```

Expected result: patient count greater than `0`.

Loaded REST module:

```bash
docker compose -f docker-compose.dev.yml exec -T backend sh -c "ls /openmrs/distribution/openmrs_modules/webservices.rest-*.omod"
```

Expected result: a `webservices.rest-*.omod` file. The REST session endpoint returning JSON proves the module is serving requests.

## OTAP Commands

| Environment | Start | URL |
| --- | --- | --- |
| Dev | `docker compose -f docker-compose.dev.yml up --build -d` | `http://localhost:8080/openmrs/spa` |
| Test | `docker compose -f docker-compose.test.yml up --build -d` | `http://localhost:8081/openmrs/spa` |
| Prod | `docker compose -f docker-compose.prod.yml up --build -d` | `http://localhost:8082/openmrs/spa` |

Build the module before starting any environment:

```bash
docker compose -f docker-compose.dev.yml --profile build-module run --rm module-builder
```

For Prod, set secrets first:

```bash
export OMRS_DB_PASSWORD="replace-me"
export MYSQL_ROOT_PASSWORD="replace-me-root"
```

## Demo Script

1. Show this repository and explain that it contains an OpenMRS module.
2. Show `docker-compose.dev.yml`: backend, frontend, gateway and database are started together.
3. Show `docker/backend/Dockerfile`: the official OpenMRS backend is reused and the local module is injected.
4. Open `http://localhost:8080/openmrs/spa` and log in.
5. Show that demo patients exist.
6. Call `/openmrs/ws/rest/v1/session` to prove the REST API is responding.

## Reset Rules

Stop without deleting data:

```bash
docker compose -f docker-compose.dev.yml down
```

Reset database and demo data:

```bash
docker compose -f docker-compose.dev.yml down -v --remove-orphans
```

Use `down -v` only when you want a clean database. The next startup must initialize OpenMRS and demo data again.

## Security Note

This is a controlled education/demo setup. Real production needs TLS, real secret management, backups, monitoring, hardening and operational procedures.
