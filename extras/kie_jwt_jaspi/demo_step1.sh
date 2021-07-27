#!/usr/bin/env bash

echo "
STEP 1 - INSTALL LOCAL KIE SERVER

Before continuing JBoss EAP 7.2 (and optionally patch) as well as
RHPAM.7.7.1 should be downloaded with the following files being present
in the current directory

- jboss-eap-7.2.0.zip
- jboss-eap-7.2.9-patch.zip (or any other EAP.7.2 patch, optional)
- rhpam-7.7.0-kie-server-ee8.zip
- rhpam-7.7.0-business-central-eap7-deployable.zip

"

read -p "Press ENTER to start the installation ..."
echo; echo

../../deployment-examples/pam-eap-setup/pam-setup.sh -b multi=2
