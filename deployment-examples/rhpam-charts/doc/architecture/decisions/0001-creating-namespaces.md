# 1. Creating namespaces

Date: 2022-04-05

## Status

Accepted

## Context

Deployed Helm charts include multiple services with possibly different namespaces, which might be either
already available or absent at deployment time.

## Decision

The selected solution is to create configurable namespaces in the `templates` folder, using variable values to 
configure the given name, adding an existence check to skip the creation if the same namespace already exists.

## Consequences

The `rhpam` and `rhsso` subcharts will include a template to create the target namespace.

## Alternatives

Adding the namespace definition under the `crds` option is another option but would not allow us to include template
variables in the YAML.
