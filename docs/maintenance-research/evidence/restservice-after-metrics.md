# RestService architecture after metrics

HEAD: 0c796f5e3bc8b901670179d8923a2dc277d808d6
Generated: 2026-06-18T01:50:11.9680147+02:00

| File | Lines | Nonblank | Import fan-out | Public/protected methods | Rough decision tokens |
|---|---:|---:|---:|---:|---:|
| omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java | 197 | 167 | 16 | 16 | 19 |
| omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/ResourceRegistry.java | 244 | 196 | 17 | 0 | 45 |
| omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/SearchHandlerRegistry.java | 300 | 255 | 18 | 2 | 43 |

Package cycle scan: same direct two-package cycles as baseline; no new cycle introduced by the two registries because they remain in the existing impl package.
