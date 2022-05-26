# 2. Helm charts to install subsystems

Date: 2022-02-22

## Status

Accepted

## Context

The deployed environment includes external subsystems that are not part of the requested deployment, but need to be 
configured offline as prelimirary requiments

## Decision

We will use Helm charts to deploy simple instances (no cluster) of the needed subsystems, and configure them manually
whenever it is requested. This applies to:
* Keycloak
* MySQL and PostgreSQL DB

## Consequences

We'll get familiar with deploying pre-built Helm charts and simplify the automated deployment of the environment