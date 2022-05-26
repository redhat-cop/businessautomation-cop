# 5. An architecture for the immutable build

Date: 2022-02-22

## Status

Accepted

## Context

The request includes the requirement to deploy an RHPAM immutable server with (guessed) BPMN processes and dependencies.

## Decision

The decision is to use Helm charts to model the server deployment together with the generation of the immutable image.

The immutable image is built in the following steps, to validate multiple options at the same time:
* The custom endpoints project is built using the OpenShift s2i build and stored in the internal registry
* The BPMN and the WIH artifacts are instead built from the command line and stored in the shared Maven repo
* The custom endpoints artifact is then placed into a custom server image, at the expected path (`.../WEB-INF/lib`) 
* The immutable server starts from the custom image and deploys the BPMN process using the `KIE_SERVER_CONTAINER_DEPLOYMENT`
environment variable

## Consequences

We can demonstrate a single setup that integrates multiple build options at the same time, so the partner can identify the 
one that best suits their use case
