# 9. Building the MS SQL JDBC extension

Date: 2022-04-08

## Status

Accepted

## Context

The MS SQL connector in `RHPAM Kie Server` requires a JDBC extension image with the required driver and JBoss module, but there 
is no such pre-built image for the MS SQL driver, as it happens for the MySQL database, in the [kiegroup
organization](https://quay.io/organization/kiegroup) in `Quay.io`. 

Probably this was done for licensing issues.

## Decision

Starting from the reference implementation of the [rhpam-7-openshift-image](https://github.com/jboss-container-images/rhpam-7-openshift-image/tree/main/templates/contrib/jdbc/cekit)
we generate an `BuildConfig` `mssql-extension-build` that generates the required extension image in the deployment namespace.

The `mssql-extension` `ConfigMap` stores the configuration for the generated image (those files were taken from the above mentioned
`rhpam-7-openshift-image` repository).

## Consequences

The solution is independent from any external extension image.
