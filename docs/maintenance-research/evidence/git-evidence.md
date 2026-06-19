# Git evidence

Datum: 2026-06-18
Repo: openmrs-module-webservices.rest

## Interpretatie

- Baseline branch: `Onderhoudbaarheidsonderzoek`.
- Baseline commit / huidige HEAD: `84fdc113631bd193c6cb4502552cd8709e80d463`.
- PoC branch: `Onderhoudbaarheidsonderzoek`.
- PoC/after commit: nog niet gecommit; after-state is de huidige working tree diff bovenop baseline commit.
- Compare/PR: geen remote compare of PR vastgelegd in deze lokale evidence.

## git rev-parse HEAD
```text
84fdc113631bd193c6cb4502552cd8709e80d463
```

## git branch --show-current
```text
Onderhoudbaarheidsonderzoek
```

## git status --short
```text
 M omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java
 M omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java
?? docs/diagrams/
?? docs/maintenance-research/
```

## git log --oneline -n 10
```text
84fdc11 Merge pull request #6 from AvansBitByBit/module_integreren_in_openmrs
47fe39b Merge branch 'dev' into module_integreren_in_openmrs
79d0748 Add OTAP Docker Compose, CI workflow, and docs
15b6d56 Merge pull request #4 from AvansBitByBit/devprodomgeving
41682b9 Add Docker Compose envs and CI updates
2cacb77 Documentation: Gap-analyse
2631cb9 Add tests and CI live API/Postman job
e1cd197 Add CI/CD environments workflow and docs
8eeeddd Merge pull request #3 from AvansBitByBit/dependabot/maven/com.fasterxml.jackson.core-jackson-core-2.21.1
ee5edf9 Merge pull request #2 from AvansBitByBit/dependabot/maven/org.openmrs.web-openmrs-web-2.8.6
```

## git diff --stat 84fdc113631bd193c6cb4502552cd8709e80d463 --
```text
 .../webservices/rest/web/ConversionUtil.java       | 335 ++++++++++++---------
 .../webservices/rest/web/ConversionUtilTest.java   |  31 ++
 2 files changed, 216 insertions(+), 150 deletions(-)
```

## git diff --name-only 84fdc113631bd193c6cb4502552cd8709e80d463 --
```text
omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java
omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java
```
