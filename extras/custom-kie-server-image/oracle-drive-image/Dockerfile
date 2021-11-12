FROM scratch

LABEL   maintainer="erouvas@redhat.com" \
        name="Oracle 12 JDBC Driver" \
        version="12.1-1.1"

# Provide the right value during build
ARG ARTIFACT_MVN_REPO

COPY install.sh oracle-driver-image/install.properties /extensions/
COPY oracle-driver-image/modules /extensions/modules/


# Download the driver into the module folder
#ADD ${ARTIFACT_MVN_REPO}/com/oracle/ojdbc7/12.1.0.1/ojdbc7-12.1.0.1.jar \
#    /extensions/modules/system/layers/openshift/com/oracle/main/ojdbc7.jar

# COPY Driver from local location into the image location (if driver locally available rather than in remote repository)
COPY oracle-driver-image/ojdbc8.jar /extensions/modules/system/layers/openshift/com/oracle/main/ojdbc8.jar

# Oracle datasource
COPY oracle-driver-image/standalone-openshift.xml /extensions/configuration/standalone-openshift.xml


