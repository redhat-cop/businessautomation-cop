# 3. Requirements for the immutable server

Date: 2022-02-22

## Status

Accepted

## Context

The request is to deploy an immutable server on the OpenShift cluster using the RHPAM operator, but we're unsure about the expected content in terms
of BPMN processes and custom dependencies

## Decision

According to previous activities for the same partner, we believe we have to deploy at least:
* One BPMN process
* One custom endpoint artifact
* One WorkItemHandler artifact

For this, we'll consider the sample projects developed in the context of [Repeatable process to create immutable image of KIE server](https://github.com/RHEcosystemAppEng/rhpam-deployment/tree/main/openshift/repeatableProcess)

## Consequences

We can have a reasonable reference architecture to show to the partner and start the discussion about the actual requirements