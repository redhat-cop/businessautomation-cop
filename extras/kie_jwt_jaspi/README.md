

for gar look at master

bacopv1 is in sync with v1

mvn package will place JAR in modules directory

-- with no external service -- 

```
JASPI_USER_HEADER='userid'
JASPI_TOKEN_HEADER='token'
JASPI_ROLES_HEADER='roleslist'
JASPI_JWT_ALLOWANCE='302'
JASPI_ALLOW_PREFIX='/kie-server/services/rest/server/readycheck,/kie-server/services/rest/server/healthcheck'
JASPI_CERTIFICATE_LOCATION='${jboss.server.config.dir}/certificate_gar.pem'
JASPI_ALLOWED_SYSTEM='kie-server'
```

projects needed:
- JWT_JASPI implementation
- Drools project
- JWT token and certificates generator
- KIEServer deployment with JWT_JASPI module integration and EAP configuration for JASPI
- sample bash scripts to invoke Drools
- Java client to invoke drools
- OCP image builder

xkcd standards: https://xkcd.com/927/

Sequence:

- Install PAM.7.7.0/KIE-Server only using [pam-eap-setup](https://github.com/redhat-cop/businessautomation-cop/tree/master/deployment-examples/pam-eap-setup)


## Overview of demo

A locally installed KIE Server will be used to demonstrate JWT processing as a JASPI module.
The installation will be performed using [pam-eap-setup](https://github.com/redhat-cop/businessautomation-cop/tree/master/deployment-examples/pam-eap-setup). Two KIE Servers will be installed, one will be left unmodified and on the other the JWT processing module will be installed.

A sample Drools-based project will be deployed on both KIE Servers and will be invoked both with and without a JWT token.
The Drools project will be invoked with and without the JWT token to demonstrate the effect the JWT processing module has on invoking KIE Server endpoints.

An OCP image will be built incorporating the custom JWT processign module. Handling of liveness and health probes will need to be handled.


## Step 1 - Local install of KIE Servers

Installation will be based on [pam-eap-setup](https://github.com/redhat-cop/businessautomation-cop/tree/master/deployment-examples/pam-eap-setup) using the following :

```
../../deployment-examples/pam-eap-setup/pam-setup.sh -b multi=2
```


