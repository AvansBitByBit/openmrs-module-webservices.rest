FROM openmrs/openmrs-reference-application-3-backend:3.6.0

USER root

RUN rm -f \
    /openmrs/distribution/openmrs_modules/webservices.rest-*.omod \
    && rm -rf \
    /openmrs/distribution/openmrs_config/ocl

COPY docker/modules/webservices.rest-*.omod /openmrs/distribution/openmrs_modules/

USER 1001
