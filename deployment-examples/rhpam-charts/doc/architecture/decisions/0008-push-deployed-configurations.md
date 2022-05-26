# 8. Push deployed configurations

Date: 2022-04-07

## Status

Accepted

## Context

We have the need to reliably save and share the deployment configurations.

## Decision

Use `gpg` and `sops` to encrypt and decrypt the files and store them in git.
The SSH key will be shared among the team members together with instructions at:
[https://github.com/zvigrinberg/securing-secrets-on-git](https://github.com/zvigrinberg/securing-secrets-on-git)

## Consequences

Team members can easily share deployment configurations and partner's secrets.