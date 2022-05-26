# 10. Custom JDBC driver for RHSSO

Date: 2022-04-27

## Status

Accepted

## Context

In a restricted network cluster, the workloads cannot access the Red Hat image registry, so they can't pull the runtime images.

## Decision

We initially decided to use the internal registry of the cluster and share instructions on how to populate it first, then 
use this local registry instead of the remote one, but this could not cover other special requirements like:
* Pulling extension images
* Images pulled from the operators under the scenes (e.g., PostgreSQL image of `RHSSO Operator`)
* JDBC drivers downloaded from custom `BuildConfig`'s are not passing through the registry, in any case 
Because of the unamanaged limitations, we preferred to avoid any partial implementation until we have a precise requirement
to cover.

## Consequences

Deployments on restricted network migh t work with a mirrored image registry, but only with the default databases. Ad-hoc
changes are required to support this case.
