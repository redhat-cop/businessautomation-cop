

for gar look at master

bacopv1 is in sync with v1

mvn package will place JAR in modules directory

-- with no external service -- 

JASPI_USER_HEADER='userid'
JASPI_TOKEN_HEADER='token'
JASPI_ROLES_HEADER='roleslist'
JASPI_JWT_ALLOWANCE='302'
JASPI_ALLOW_PREFIX='/kie-server/services/rest/server/readycheck,/kie-server/services/rest/server/healthcheck'
JASPI_CERTIFICATE_LOCATION='${jboss.server.config.dir}/certificate_gar.pem'
JASPI_ALLOWED_SYSTEM='kie-server'


projects needed:
- JWT_JASPI implementation
- Drools project
- JWT token and certificates generator
- KIEServer deployment with JWT_JASPI module integration and EAP configuration for JASPI
- sample bash scripts to invoke Drools
- Java client to invoke drools
- OCP image builder

xkcd standards: https://xkcd.com/927/
