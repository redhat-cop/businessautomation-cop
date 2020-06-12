

# Contribution Guide

In the context of this repo, a _Quickstart_ is a collection of knowledge assets with clear instruction on how to build and deploy to RHPAM  instances either in Openshift or in standalone EAP instances. Knowledge assets can BPMN, DMN, case management or Optaplanner models, Drools rules, Decision Tables, Business Objects, Kogito projects and every other model that is supported by the RHPAM execution engine. Knowledge assets could also be embedded in Spring Boot applications. Each quickstart should be independently consumable and user friendly. Each quickstart can either stand on its own or many could be part of a larger entity constituting a learning trail.

Knowledge assets is not the only contribution that can be made in this repo. Other forms include tooling around RHPAM such as installation helpers, git hooks, automation using the REST APIs exposed by Business Central, KIE Server or Kogito applications, Smart Router and Complex Event Processing as well as architectural recommendations. 

| NOTE: If you have some interesting material you would like to contribute as examples or references and they do not fit the above description, please get in touch with the repo maintainer  |
| --- |

## Guiding Principles for Writing a Quickstart

In general, a good quickstart should:

- Serve as a standalone example of some aspect of RHPAM. Quickstarts can be grouped together to form a learning trail providing that each one van stand on its own. 
- Have comprehensive, clear documentation
- Follow the standard structure documented below

## Structure of a QuickStart

This documents the expected directory structure of a Quickstart.

```
/<name of project/
  README.md - Please include a comprehensive README documenting the use case for this quickstart (see below of readme guidance)
  /name-of-project-<tech_suffix> - where <tech_suffix> relates to the deploy target, for example KJAR, kogito, spring-boot, quarkus, etc
```

## Guidance for Quickstart README

Good documentation is key to a good quickstart. Helping a consumer quickly understand the purpose of your quickstart and how to deploy it will make it more widely consumable to the community. We are constantly trying to achieve more consistency across quickstart documentation to make for a better user experience. With that in mind, we've put together the following starter skeleton for a quickstart README.

```
# Showcase of X feature of RHPAM

Provide a brief description of the feature being showcased, and a link to more info about it.

## Prerequisites & Assumptions

Describe the tools, environments, and assumed skills needed to understand the quickstart

## Deploy Quickstart

As briefly as possible, walk through the steps to deploy.

## Architecture and Details

Please include a deeper explanation of the showcase that get deployed and the recommended usage pattern to best showcase the feature
```

## Opening a Pull Request

We follow a standard open source forking workflow in this repo. Please see the [Red Hat Communities of Practice Contribution Guide](https://redhat-cop.github.io/contrib/) for a walkthrough of this workflow.

> Written with [StackEdit](https://stackedit.io/).
