# RHPAM packaged with Helm chart 

This repository holds a configurable Helm chart of [RHPAM](https://developers.redhat.com/products/rhpam/overview), to simplify
the deployment in the OpenShift environment, exposing the following features:
* Managed by Red Hat operators
* Authoring and production environments
* External data storage
* SSO authentication with `RHSSO`
* External Maven repository
* Git hooks integration (with SSH authentication)

## TL;DR

```shell
git clone https://github.com/redhat-cop/businessautomation-cop.git
cd businessautomation-cop/deployment-examples/rhpam-charts
helm install -n my-namespace -f my-rhpam.yaml my-rhpam .
```

## Introduction
This chart provides an Helm-based deployment of RHPAM according to the following requirements:
* The RH Operators are installed as part of the deployment
* The authentication is managed by an [RHSSO](https://access.redhat.com/products/red-hat-single-sign-on) instance
  * The authentication server is created and configured as part of the deployment
* Data storage is provided by an already configured DB (`PostgreSQL`, `MySQL` and `MS SQL` are supported for both RHPAM and RHSSO)
* The artifact repository is configured as an external `Maven repo` to publish and pull artifacts
* Both `authoring` and `production` environments are supported:
  * `authoring` is made by one `Business Central` and one or more `Kie Server` instances
    * No containers are pre-loaded in the servers
  * `production` is made by one or more immutable `Kie Server` instances
    * Configurable deployments can be pre-loaded in the server
  * In both cases, external dependencies (e.g., custom endpoints) can be pre-loaded in a custom image, pulled from the 
    configured `Maven repo`
* Custom properties can be defined that will be added to the runtime properties of the Kie Server
  * Properties are added under the `<system-properties>` tag of the `standalone-openshift.xml` configuration

The chart is designed with two different subcharts, namely [rhpam](./charts/rhpam) and [rhsso](./charts/rhsso): you can 
find more design details in the related document [DESIGN.md](./DESIGN.md).

Architecture Decision Records have been created using the [adr-tools](https://github.com/npryce/adr-tools) and are available
[here](./doc/architecture/decisions/README.md).

## Tested configurations
References:
* [Red Hat Single Sign-On Supported Configurations](https://access.redhat.com/articles/2342861)
* [Red Hat Process Automation Manager 7 Supported Configurations](https://access.redhat.com/articles/3405381)

| Configuration                | Tested version(s)                 | 
| -----------------------------| ----------------------------------| 
| RHSSO Operator               | 7.4+, 7.5+                        | 
| RHSSO Image                  | sso74-openshift-rhel8:7.4<br/>sso75-openshift-rhel8:7.5 |
| RHSSO PostgreSQL             | 13.2                              |
| RHSSO MySQL                  | 8.0+  (only sso75-openshift-rhel8:7.5) |
| RHSSO MS SQL                 | 2019 (only sso75-openshift-rhel8:7.5, no XA transaction) |
| RHPAM Operator               | 7.12+                             |
| RHPAM Image                  | rhpam-kieserver-rhel8:7.9.x<br/>rhpam-businesscentral-rhel8:7.9.x |
| RHPAM PostgreSQL             | 13.2                              |
| RHPAM MySQL                  | 8.0+                              |
| RHPAM MS SQL                 | 2019                              |

## Prerequisites
* OpenShift 4.8+
* Helm 3.7.0+
* PV provisioner support in the underlying infrastructure

## Installing the chart
To install the chart in the `my-namespace` namespace with the release name `my-rhpam` and using custom configuration in `my-rhpam.yaml`:
**TODO chart repo**
```shell
git clone https://github.com/redhat-cop/businessautomation-cop.git
cd deployment-examples/rhpam-charts
helm install -n my-namespace -f my-rhpam.yaml my-rhpam .
```

The command deploys RHPAM on the OpenShift cluster in the given configuration. 
The [Parameters](#parameters) section lists the parameters that can be configured during installation.

> **Tip**: List all releases using `helm list -A` or `helm list -n my-namespace`

## Upgrading the chart
To upgrade the above deployment with updated values from the custom configuration in `my-rhpam.yaml`:
```shell
helm upgrade -n my-namespace -f my-rhpam.yaml my-rhpam redhat-cop/rhpam
```

## Uninstalling the chart
To uninstall the my-rhpam deployment:
```shell
helm uninstall -n my-namespace my-rhpam
```
The command removes all the OpenShift resources and deletes the release. Some resources might remain as they are
installed as Helm hooks.

To delete all the remaining resources associated with my-rhpam:
```sh
oc delete namespaces -l helm-app=RHPAM
```
> **Note**: Deleting the namespaces will delete all the contained resources data as well. Please be cautious before doing it.

## Parameters
### Global parameters
Global parameters are defined in [global values.yaml](./values.yaml).

| Name                         | Description                                   | Default Value         |
| -----------------------------| ----------------------------------------------| --------------------- |
| `global.imageRegistry`       | Image registry                                | `registry.redhat.io`  |
| `global.internalImageRegistry` | Internal image registry, can be retrieved as:</br>`oc get image.config.openshift.io cluster -ojsonpath='{.status.internalRegistryHostname}'`| `image-registry.openshift-image-registry.svc:5000`  |
| `global.rhpam.namespace`     | Target RHPAM namespace (created if not exists)| `rhpam`               |
| `global.rhpam.environment`   | Environment type, one of authoring/production | `authoring`           |
| `global.rhpam.installPostconfigure` | Whether to install the postconfigure.sh script | `true`        |
| `global.rhsso.namespace`     | Target RHSSO namespace (created if not exists)                    | `rhsso`           |
| `global.rhsso.realm`                           | Name of realm to be created                     | `rhpam`               |
| `global.rhsso.clients.business-central.secret` | Configured secret for `business-central` client |  `""`                |
| `global.rhsso.clients.kie-server.secret`       | Configured secret for `kie-server` client       | `""`                 |
| `global.labels`              | Labels to be propagated to the deployed resources | helm-app: RHPAM<br/>author: Red Hat |

### RHSSO sub-chart
RHSSO parameters are defined in [rhsso values.yml](./charts/rhsso/values.yaml).

This chart builds an RHSSO instance with:
  * One realm with configurable name and the required roles
  * Two clients with configurable secrets
  * Two users:
    * `rhpamadmin` to manage the RHPAM servers, with configurable name and password
    * `admin` to login to the `Business Central` (same password) 

| Name                           | Description                                   | Value                       |
| -------------------------------| ----------------------------------------------| --------------------------- |
| `image.context`                | Context part of RHSSO image                   | `rh-sso-7`                  |
| `image.name`                   | Name of RHSSO image                           | `sso74-openshift-rhel8`     |
| `image.initContainerName`      | Name of RHSSO init-container image            | `sso7-rhel8-init-container` |
| `image.tag`                    | Image tag (both container and init-container) | `7.4`                       |
| `database.driver`              | One of: postgresql, mysql, mssql              | `postgresql`                |
| `database.host`                | Database host                                 | `""`                        |
| `database.port`                | Database port                                 | `5432`                      |
| `database.name`                | Database name                                 | `""`                        |
| `database.password`            | Database user password                        | `""`                        |
| `database.username`            | Database user name                            | `""`                        |
| `database.xa`                  | Whether to use an XA transaction              | `true`                      |
| `operator`                     | Specification of the `rhsso-operator` Subscription | channel: alpha</br>installPlanApproval: Automatic</br>name: rhsso-operator</br>source: redhat-operators</br>sourceNamespace: openshift-marketplace|
| `rhpam.admin.username`         | Name of user created to admin RHPAM           | `rhpamadmin`                |
| `rhpam.admin.password`         | Password of user created to admin RHPAM       | `redhat123#`                |

* Sample DB configurations for `PostgreSQL`:
```yaml
    driver: postgresql
    host: HOST
    name: DATABASE
    password: PASSWORD
    port: PORT
    username: USERNAME
```
* Sample DB configurations for `My SQL`:
```yaml
    driver: mysql
    driverURL: 'https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-8.0.22.zip'
    host: HOST
    name: DATABASE
    password: PASSWORD
    port: PORT
    username: USERNAME
```
* Sample DB configurations for `MS SQL`:
```yaml
    driver: mssql
    driverURL: 'https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/7.2.2.jre11/mssql-jdbc-7.2.2.jre11.jar'
    host: HOST
    name: DATABASE
    password: PASSWORD
    port: PORT
    username: USERNAME
    xa: false
```
#### RHPAM sub-chart
RHPAM parameters are defined in [rhpam values.yaml](./charts/rhpam/values.yaml).

This chart builds a custom Kie server image using a Dockerfile strategy, downloading all the required dependencies defined in
[values.yml](./charts/rhpam/values.yaml) as:
```yaml
artifacts:
  - gav: groupId1:artifactId1:version1
  - gav: groupId2:artifactId2:version2
```
> **Note**: these artifacts must be already available in the configured Maven repo.

In case of `production` environment, pre-built artifacts are loaded from the external Maven repo, as specified in the
[values.yaml](./charts/rhpam/values.yaml) as:
```yaml
containers:
  - 'container1=groupId1:artifactId1:version1'
  - 'container2=groupId2:artifactId2:version2'
```
> **Note**: these artifacts must be already available in the configured Maven repo.

Finally, this configuration allows specifying custom properties that will be injected into the `<system-properties>` of the
RHPAM Kie Server, using the following template:
```yaml
  customProps:
    prop1: value1
    prop2: value2
  customSecrets:
    prop3: value3
    prop4: value4
```

Those under the `customProps` key will be added to the `rhpam-custom-configmap` ConfigMap, while those under the
`customSecrets` key will be defined in the `rhpam-custom-secret` Secret

| Name                       | Description                                   | Default Value           |
| -------------------------- | ----------------------------------------------| ------------------------|
| `common.imageContext`      | The context part of the RHPAM image paths     | `7.9.1`                 |
| `common.version`           | RHPAM version (validated for 7.9.0-7.9.1)     | `rhpam-7`               |
| `common.server.image`      | Kie Server image name                         | `rhpam-kieserver-rhel8` |
| `common.server.replicas`   | Number of Kie Server replicas                 | 1                       |
| `common.console.image`     | Business Central image name                   | `rhpam-businesscentral-rhel8` |
| `common.isRelease`         | For production environment: true when the deployed containers are of RELEASE version | true                  |
| `operator`                 | Specification of the `businessautomation-operator` Subscription | channel: stable</br>installPlanApproval: Automatic</br>name: businessautomation-operator</br>source: redhat-operators</br>sourceNamespace: openshift-marketplace|
| `artifacts`                | List of pre-deployed containers, following the format:</br>`<containerId>(<aliasId>)=<groupId>:<artifactId>:<version>` | `""`       |
| `containers`               | List of pre-deployed containers, following the format:</br>`<containerId>(<aliasId>)=<groupId>:<artifactId>:<version>` | `""`       |
| `maven.repo.url`           | URL of Maven repo                             | `""`                  |
| `maven.repo.id`            | ID of Maven repo                              | `rhpam`               |
| `maven.repo.username`      | Name of Maven repo user                       | `""`                  |
| `maven.repo.password`      | Password of Maven repo                        | `""`                  |
| `database.dialect`         | Hibernate dialect for connected DB            | `org.hibernate.dialect.PostgreSQLDialect` |
| `database.driver`          | JDBC driver name (`postgres`, `mysql` or `mssql`) | `postgresql`                          |
| `database.host`            | Database host                                 | `""`                                     |
| `database.name`            | Database name                                 | `""`                                     |
| `database.username`        | Database user name                            | `""`                                     |
| `database.password`        | Database user password                        | `""`                                     |
| `database.port`            | Database port                                 | `5432`                                    |
| `database.nonXA`           | Datasource type                               | `false`                                   |
| `database.extensionImageStreamTag`| ImageStreamTag containing the drivers  | Commented for PostgreSQL and MS SQL<br/> For MySQL use `jboss-kie-mysql-extension-openshift-image:8.0.12` |
| `database.version`         | JDBC connector version (MS SQL only)          | Commented for PostgreSQL and MySQL        |
| `database.jdbcURL`         | JDBC connection URL                           | Commented                                 |
| `customProps`              | Custom properties to be configured in a ConfigMap | `""`                                 |
| `customSecrets`            | Custom properties to be configured in a Secret    |  `""`                                |

> **Note**: the `containers` are deployed only when `global.rhpam.environment` is set to `production`
> **Note**: for the `database` configuration, one of the following set of options is required:
> - `name` and `host`
> - `jdbcURL`
>  The latter option allows to integrate vendor-specific configurations, like `useSSL=false`

* Sample DB configurations for `PostgreSQL`:
```yaml
    dialect: org.hibernate.dialect.PostgreSQLDialect
    driver: postgresql
    host: HOST
    name: DATABASE
    password: B7TqbS4mHa
    port: 5432
    username: postgres
```
* Sample DB configurations for `MS SQL`:
  * There is no need to define `extensionImageStreamTag` because the extension image is generated by the
  `mssql-extension-build` BuildConfig
  * The version field is required and must point to an existing version of the MS SQL JDBC connector
```yaml
    dialect: org.hibernate.dialect.SQLServer2012Dialect
    driver: mssql
    version: 7.2.2.jre11
    host: mssql-service
    name: DATABASE
    password: PASSWORD
    port: 31433
    username: USER
```
* Sample DB configurations for `MySQL`:
```yaml
    extensionImageStreamTag: 'jboss-kie-mysql-extension-openshift-image:8.0.12'
    dialect: org.hibernate.dialect.MySQL5InnoDBDialect
    driver: mysql
    jdbcURL: jdbc:mysql://HOST:3306/DATABASE?useSSL=false
    password: PASSWORD
    port: 3306
    username: USER
```
## Configuration and installation details
### Configuring the git repository (`authoring` environment only)
Assuming that an external Git server is available (e.g., Bitbucket or GitHub), the following steps are required 
prior the chart installation, to configure it:
* Create an SSH key and store it under the [git/](./charts/rhpam/resources/git) folder as `id_rsa`:
```shell
ssh-keygen -t rsa -b 4096 -C "bci@redhat" -m PEM
```
* From the settings page of the Git console, add a new entry with the content of the public key (`id_rsa.pub`)
* Create the `known_hosts` file for the Git repository host. E.g., for Bitbucket you can use:
```shell
ssh-keyscan -t rsa bitbucket.org >  ./charts/rhpam/resources/git/known_hosts
```

These 2 files will be added to the Secret called `git-hooks-secret`.

The `rhpam-git-hooks` ConfigMap, instead, contains the [post-commit](./charts/rhpam/resources/git/post-commit) hook that
is automatically installed to guarantee that each change in the local repository will also be committed on the remote one
(if it was defined).

Given the above settings, the `KieApp` instance defined in [kieapp-yml](./charts/rhpam/templates/kieapp.yml) takes care of:
* Configuring `Business Central` to automatically install the given Git hook in each new project
* Install the SSH keys in the `Business Central` to push to the changes in the remote Git repository using the SSH protocol 
(no authentication required)

**Note**: the `Business Central` deployment is configured with a persistent storage for the user data, so projects are
not lost in case an instance is restarted (see [Is git repository in RHPAM on OpenShift persisted?](https://access.redhat.com/solutions/3720291))

### Pushing artifacts to the Maven repository
In order to publich artifacts on the configured Maven repository, every new project must have a `<distributionManagement>` 
tag in the `pom.xml` file like the following:
```xml
<distributionManagement>
  <snapshotRepository>
    <id>rhpam</id>
    <url>...</url>
  </snapshotRepository>
  <repository>
    <id>rhpam</id>
    <url>...</url>
  </repository>
</distributionManagement>
```
> **Note**: the `<id>` section must match the value of `maven.repo.id` in the [values.yaml](./charts/rhpam/values.yaml) 
> of the RHPAM sub-chart 

## Troubleshooting
### Missing RHSSO admin password
When we install the RHSSO instance several times using the same DB, a new password is generated for the `admin` user,
available from the `credential-rhsso` Secret, but this would not match the value already stored in the DB, causing the 
login to fail because of the wrong password.

Follow these instructions to create a new admin user that allows you to login and reset the password of the `admin`
user in the `master` Realm: 
* Open the terminal console for the `keycloak-0` Pod
* Run these commands:
```shell
/opt/eap/bin/add-user-keycloak.sh -u test -p test123
/opt/eap/bin/jboss-cli.sh --connect --command=reload
```

After resetting the `admin` password, you can delete the `test` user from the RHSSO console.

## License
See the associated [LICENSE](./LICENSE) file

## Tips

### Importing from a remote Git repository
In order to create a new project that is automatically kept in sync with the local repository, the suggested steps are the following:
* Create an empty repository on the external Git (*Note**: no README, .gitignore or any other file must be there) with an 
initial `master` branch
* Import the empty project in `Business Central` using the SSH URL

### Working with retricted network cluster
TBD
