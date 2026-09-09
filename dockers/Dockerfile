ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-17
ARG RUBY_IMAGE=ruby:3.3.4-bookworm
ARG BASE_IMAGE=registry.example.org/wildfly/wildfly:34.0.0.Final-jdk17

FROM ${RUBY_IMAGE} AS documentation

WORKDIR /documentation
COPY docs/Gemfile docs/Gemfile.lock ./
RUN gem install bundler -v 4.0.9 --no-document \
    && bundle config set deployment true \
    && bundle install --jobs 4 --retry 3
COPY docs/ ./
COPY LICENSE /project-license/LICENSE
RUN JEKYLL_ENV=production bundle exec jekyll build --config _config.yml,_config.docker.yml --destination /documentation-site \
    && cp /project-license/LICENSE /documentation-site/LICENSE.txt

FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace
COPY . .
COPY --from=documentation /documentation-site/ elections-admin-web/src/main/webapp/docs/
RUN mvn clean package -DskipTests

FROM ${BASE_IMAGE}

# Copiar artefactos, configuración, version y módulos
#version
COPY --chown=jboss:jboss dockers/deployed-version.txt /opt/jboss/wildfly/standalone/configuration/deployed-version.txt


#artefactos deploy
COPY --chown=jboss:jboss --from=build /workspace/elections-ejb/target/elections-ejb-1.0.jar /opt/jboss/wildfly/standalone/deployments/
COPY --chown=jboss:jboss --from=build /workspace/elections-admin-web/target/elections.war /opt/jboss/wildfly/standalone/deployments/
COPY --chown=jboss:jboss --from=build /workspace/elections-services/target/elections-ws.war /opt/jboss/wildfly/standalone/deployments/
COPY --chown=jboss:jboss wildfly/deployments/pai-auth-ws-client-1.5.1.jar /opt/jboss/wildfly/standalone/deployments/
COPY --chown=jboss:jboss wildfly/configuration/pai.properties /opt/jboss/wildfly/standalone/configuration/pai.properties

#modulos
COPY --chown=jboss:jboss wildfly/modules/ /opt/jboss/wildfly/modules/

#configuracion
COPY --chown=jboss:jboss wildfly/configuration/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml


# Exponer el puerto necesario
EXPOSE 8080

CMD sh -c 'echo "WS_AUTH_METHOD=${WS_AUTH_METHOD}" > /opt/jboss/wildfly/standalone/configuration/elections.properties && \
           echo "WS_AUTH_TOKEN=${WS_AUTH_TOKEN}" >> /opt/jboss/wildfly/standalone/configuration/elections.properties && \
           echo "WS_AUTHORIZED_IPS=${WS_AUTHORIZED_IPS}" >> /opt/jboss/wildfly/standalone/configuration/elections.properties && \
           echo "WS_LACNIC_AUTH_URL=${WS_LACNIC_AUTH_URL}" >> /opt/jboss/wildfly/standalone/configuration/elections.properties && \
           echo "URL_PORTAL_WS=${URL_PORTAL_WS}" > /opt/jboss/wildfly/standalone/configuration/pai.properties && \
           echo "PORTAL_APIKEY=${PORTAL_APIKEY}" >> /opt/jboss/wildfly/standalone/configuration/pai.properties && \
/opt/jboss/wildfly/bin/standalone.sh -b=0.0.0.0'
