# 7. Install requested properties as a custom image

Date: 2022-04-05

## Status

Accepted

Supercedes [4. Install requested properties using the RHPAM operator](0004-install-requested-properties-using-the-rhpam-operator.md)

## Context

Goal is to address the request described in [4. Install requested properties using the RHPAM operator](0004-install-requested-properties-using-the-rhpam-operator.md)

## Decision

Instead of relying on the RHPAM operator, which will not support this functionality in time, we use a `BuildConfig` instance
to generate a custom RHPAM server image that includes the files needed to propertly bootstrap the Kie Server and configure
the requested properties.

## Consequences

No changes are requested to the RHPAM operator, the deployment is simplified and we don't need any manual operation.
