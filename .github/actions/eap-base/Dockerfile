FROM registry.access.redhat.com/ubi8/ubi-minimal:8.2

LABEL version="1.0.0"
LABEL repository="https://github.com/redhat-cop/businessautomation-cop"
LABEL homepage="https://github.com/redhat-cop/businessautomation-cop"
LABEL maintainer="Red Hat CoP"
LABEL "com.github.actions.name"="eap-base"
LABEL "com.github.actions.description"="Action to run Red Hat EAP"
LABEL "com.github.actions.branding.icon"="monitor"
LABEL "com.github.actions.branding.color"="purple"

RUN microdnf install --assumeyes --nodocs tar wget git zip java-1.8.0-openjdk sqlite findutils curl maven && \
    microdnf clean all

ADD entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
