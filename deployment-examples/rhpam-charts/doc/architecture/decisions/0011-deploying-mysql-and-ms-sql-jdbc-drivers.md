# 11. Deploying MySQL and MS SQL JDBC drivers

Date: 2022-05-09

## Status

Accepted

## Context

The RHSSO operator does not provide support for other databases apart from PostgreSQL, while
we are requested to manage all of the following:
* MySQL (relatively urgent)
* MS SQL (relatively urgent)
* Oracle (next releases)

It's our internal requirement to manage the RHSSO deployment through the operator, so that it also
falls under the terms of the Enterprise subscription. 

## Decision

Since the operator can't be extended on time to match the partner's milestones, we prepare a custom solution using some
experimental options of the operator to configure the JDBC connectors.

### Design of the MySQL solution
* Create a custom image like `sso75-openshift-rhel8-mysql` where the JDBC jar is pulled from 
the configured repository (`database.driverURL` property) and a custom `sso-extensions.cli` is placed in 
`/opt/eap/extensions` folder
  * The JDBC driver is downloaded from a configurable URL, which might need some reworking
  in case of restricted network environments
  * The `rhsso-operator` Subscription includes an environment variable `RELATED_IMAGE_RHSSO_OPENJDK` with the name of the
  generated image, to extend the default behavior of the operator
* The `sso-extensions.cli` script just installs the JDBC driver and adds the EAP module
* Use the `spec.keycloakDeploymentSpec.experimental.env` section of the Keycloak CRD to inject some DB-specific options and override the
default behavior of the launch scripts
  * `DB_DRIVER`, in particular, triggers the code to handle MySQL DB that already exists in the EAP 7.5 scripts
* Last, the `keycloak-probes` ConfigMap is created before installing the RHSSO operator to override the
`readiness_probe.sh` script that otherwise always look for the default datasource called:
`DATASOURCE_POOL_NAME="keycloak_postgresql-DB"`

### Design of the MS SQL solution
* The design is similar to the MySQL solution, in terms of custom image, extension script,
environment variables and mounting of probe scripts
  * The `DB_URL` environment variable is used to specify the connection URL using DB-specific options like in: 
  `jdbc:sqlserver://HOST:PORT;databaseName=DATABASE`
* The difference is that the `mssql` driver is not managed by the original launch scripts,
so we populate the custom image with reviewed scripts that can handle this case and
replace the defaults. These scripts include:
  * `datasource.sh`
  * `datasource-common.sh`
  * `tx-datasource.sh`

## Consequences

The partner can still leverage the benefits of the RHSSO operator.  

## References
* [RHSSO 7.4 tested integrations](https://access.redhat.com/articles/2342861#Int_7_4)
* [RHSSO 7.5 tested integrations](https://access.redhat.com/articles/2342861#Int_7_5)
* [RHSSO container image - code](https://github.com/jboss-container-images/redhat-sso-7-openshift-image)
* [RHSSO operator - code](https://github.com/keycloak/keycloak-operator)
* [EAP mappings for Datasources](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.4/html-single/getting_started_with_jboss_eap_for_openshift_container_platform/index#reference_datasources)