FROM azul/zulu-openjdk-alpine:8u222-jre

LABEL authors="Joost van der Griendt <joostvdg@gmail.com>"
LABEL version="0.2.0"
LABEL description="OpenDJ container"

WORKDIR /opt
EXPOSE 389 636 4444

ENV CHANGE_DATE='20190916-2100'
ENV JAVA_HOME /usr/lib/jvm/zulu-8
ENV OPENDJ_JAVA_HOME /usr/lib/jvm/zulu-8
ENV VERSION=4.4.3
ENV ROOT_USER_DN='cn=admin'
ENV ROOT_PASSWORD='password'
RUN apk add --no-cache tini

ENTRYPOINT ["/sbin/tini", "-vv","-g","-s", "--"]
CMD ["/opt/opendj/bin/start-ds", "--nodetach"]

RUN wget --quiet \
    https://github.com/OpenIdentityPlatform/OpenDJ/releases/download/$VERSION/opendj-$VERSION.zip && \
    unzip opendj-$VERSION.zip && \
    rm -r opendj-$VERSION.zip

RUN /opt/opendj/setup --cli \
    -p 389 \
    --ldapsPort 636 \
    --enableStartTLS \
    --generateSelfSignedCertificate \
    --baseDN dc=jboss,dc=org \
    -h localhost \
    --rootUserDN "$ROOT_USER_DN" \
    --rootUserPassword "$ROOT_PASSWORD" \
    --acceptLicense \
    --no-prompt \
    --doNotStart

# ADD example.ldif /var/tmp/example.ldiff
ADD users.ldif /var/tmp/example.ldiff
# RUN /opt/opendj/bin/import-ldif --help
RUN /opt/opendj/bin/import-ldif --includeBranch dc=jboss,dc=org --backendID userRoot --offline --ldifFile /var/tmp/example.ldiff
