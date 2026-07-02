FROM openmrs/openmrs-reference-application-3-backend:3.6.0

USER root

RUN rm -f \
    /openmrs/distribution/openmrs_modules/webservices.rest-*.omod \
    && rm -rf \
    /openmrs/distribution/openmrs_config/ocl

RUN sed -i 's/<Connector port="8080"/<Connector port="8080" server="OpenMRS"/' /usr/local/tomcat/conf/server.xml \
    && sed -i '/<\/Host>/i \        <Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />' /usr/local/tomcat/conf/server.xml

COPY docker/modules/webservices.rest-*.omod /openmrs/distribution/openmrs_modules/

USER 1001
