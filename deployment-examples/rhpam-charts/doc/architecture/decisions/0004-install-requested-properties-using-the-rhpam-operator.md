# 4. Install requested properties using the RHPAM operator

Date: 2022-02-22

## Status

Superceded by [7. Install requested properties as a custom image](0007-install-requested-properties-as-a-custom-image.md)

## Context

The partner requests to install some custom properties in the server runtime, under the `<system-properties>` tag of the
server configuration. Such properties should come from deployed `ConfigMap` or `Secrets` to be easily updated.
The RHPAM operator does not provide this functionality, it requires a manual update of the operator's `ConfigMap` as
described [here](https://github.com/jboss-container-images/rhpam-7-openshift-image/tree/main/quickstarts/post-configure-example)

## Decision

The decision was to document the procedure to create the required mounted volume as described in the procedure linked above.

We will not extend the operator with the tracked enhancement [RHPAM-4164](https://issues.redhat.com/browse/RHPAM-4164)

## Consequences

We can prove the reference architecture to the partner and avoid investing time in developing the operator enhancement.

Once the procedure will be accepted by the partner, we can consider this option.
