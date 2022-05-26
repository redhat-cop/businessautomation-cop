# 2. Install requested properties as a custom image

Date: 2022-04-05

## Status

Accepted

## Context

The request is to install some custom properties in the server runtime, under the <system-properties> tag of the server 
configuration. Such properties should come from deployed ConfigMap or Secrets to be easily updated. 

The RHPAM operator does not provide this functionality, it requires a manual update of the operator's ConfigMap as described 
[here](https://github.com/jboss-container-images/rhpam-7-openshift-image/tree/main/quickstarts/post-configure-example)

## Decision

Instead of relying on the RHPAM operator, which will not support this functionality in time, we use a `BuildConfig` instance
to generate a custom RHPAM server image that includes the files needed to propertly bootstrap the Kie Server and configure
the requested properties.

## Consequences

No changes are requested to the RHPAM operator, the deployment is simplified and we don't need any manual operation.
