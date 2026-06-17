# RestServiceImpl baseline metrics

HEAD: 6a36c8d532e6f7ab0d2ce7c80e50651acd8340e3
Generated: 2026-06-18T01:30:43.7127826+02:00

| File | Lines | Nonblank | Import fan-out | Public/protected methods | Rough decision tokens |
|---|---:|---:|---:|---:|---:|
| omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java | 737 | 631 | 32 | 21 | 170 |

## Imported packages from RestServiceImpl
- java.io.IOException
- java.util.ArrayList
- java.util.concurrent.ExecutorService
- java.util.HashMap
- java.util.HashSet
- java.util.Iterator
- java.util.List
- java.util.Map
- java.util.Map.Entry
- java.util.Set
- org.apache.commons.lang.StringUtils
- org.hibernate.proxy.HibernateProxy
- org.openmrs.api.APIException
- org.openmrs.module.ModuleUtil
- org.openmrs.module.webservices.rest.web.annotation.SubResource
- org.openmrs.module.webservices.rest.web.api.RestHelperService
- org.openmrs.module.webservices.rest.web.api.RestService
- org.openmrs.module.webservices.rest.web.OpenmrsClassScanner
- org.openmrs.module.webservices.rest.web.representation.CustomRepresentation
- org.openmrs.module.webservices.rest.web.representation.NamedRepresentation
- org.openmrs.module.webservices.rest.web.representation.Representation
- org.openmrs.module.webservices.rest.web.resource.api.Resource
- org.openmrs.module.webservices.rest.web.resource.api.SearchConfig
- org.openmrs.module.webservices.rest.web.resource.api.SearchHandler
- org.openmrs.module.webservices.rest.web.resource.api.SearchParameter
- org.openmrs.module.webservices.rest.web.resource.api.SearchQuery
- org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler
- org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler
- org.openmrs.module.webservices.rest.web.response.InvalidSearchException
- org.openmrs.module.webservices.rest.web.response.UnknownResourceException
- org.openmrs.module.webservices.rest.web.RestConstants
- org.openmrs.util.OpenmrsConstants

## Package cycle scan
Rough scan: source package import graph over omod-common/src/main/java and omod/src/main/java.
- org.openmrs.module.webservices.docs.swagger <-> org.openmrs.module.webservices.rest
- org.openmrs.module.webservices.rest <-> org.openmrs.module.webservices.rest.util
- org.openmrs.module.webservices.rest <-> org.openmrs.module.webservices.rest.web
- org.openmrs.module.webservices.rest.util <-> org.openmrs.module.webservices.rest.web.resource.impl
- org.openmrs.module.webservices.rest.web <-> org.openmrs.module.webservices.rest.web.representation
- org.openmrs.module.webservices.rest.web <-> org.openmrs.module.webservices.rest.web.resource.api
- org.openmrs.module.webservices.rest.web <-> org.openmrs.module.webservices.rest.web.resource.impl
- org.openmrs.module.webservices.rest.web.annotation <-> org.openmrs.module.webservices.rest.web.representation
- org.openmrs.module.webservices.rest.web.annotation <-> org.openmrs.module.webservices.rest.web.resource.impl
- org.openmrs.module.webservices.rest.web.api <-> org.openmrs.module.webservices.rest.web.resource.impl
- org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8 <-> org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8
