# 1. Authoring environment from RHPAM operator

Date: 2022-02-22

## Status

Accepted

## Context

Partner shared an example of RHPAM configuration (from `standalone-full.xml`) showing the instructions to configure an
RHPAM authoring environment. We've been asked to implement the same using the RHPAM operator.

The whole system includes Keycloak but we are unsure about the actual DB type.

## Decision

Since this is the initial POC to use the RHPAM operator to perform the equivalent deployment, we'll start with a simple
`rhpam-authoring` environment that includes:
* Keycloak authentication
* PostgreSQL database (as the driver is already in the RHPAM image)
* An external Maven repo on [repsy.io](repsy.io)

## Consequences

Once the external systems have been installed (Maven, Keycloak, PostgreSQL), the setup can be easily deployed with a 
single `oc apply` command.

Furthermore, the delivered configuration of the `KieApp` instance can define a starting point to extend the deployment
using Helm charts.
