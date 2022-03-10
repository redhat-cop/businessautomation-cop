#!/usr/bin/env bash

#
# - clean-up previous installations
#
rm -rf go_pam* pam gopam.sh jboss-eap-7.2 rh-sso-7.3 gonode*.sh gosso.sh pam-config.db gostandalone.sh "$INSTALL_DIR"
rm -rf jboss-eap-7.3


cat << "__END_OF_DATA"
            _____           _____
88      ,ad8PPPP88b,     ,d88PPPP8ba,     88888888ba      db         88b           d88
88     d8P"      "Y8b, ,d8P"      "Y8b    88      "8b    d88b        888b         d888
88    dP'           "8a8"           `Yd   88      ,8P   d8'`8b       88`8b       d8'88
88    8(              "              )8   88aaaaaa8P'  d8'  `8b      88 `8b     d8' 88
88    I8                             8I   88""""""'   d8YaaaaY8b     88  `8b   d8'  88
88     Yb,                         ,dP    88         d8""""""""8b    88   `8b d8'   88
88      "8a,                     ,a8"     88        d8'        `8b   88    `888'    88
88        "8a,                 ,a8"       88       d8'          `8b  88     `8'     88 
            "Yba             adP"
              `Y8a         a8P'
                `88,     ,88'
                  "8b   d8"  
                   "8b d8"   
                    `888'
                      "
__END_OF_DATA

echo "

STEP 1 - INSTALL LOCAL KIE SERVER

Before continuing JBoss EAP 7.2 (and optionally patch) as well as
RHPAM.7.7.1 should be downloaded with the following files being present
in the current directory

- jboss-eap-7.2.0.zip
- jboss-eap-7.2.8-patch.zip (optional, though not 7.2.9)
- rhpam-7.7.1-kie-server-ee8.zip
- rhpam-7.7.1-business-central-eap7-deployable.zip

"

# read -p "Press ENTER to start the installation ..."
echo; echo

ln -sf ../../deployment-examples/pam-eap-setup/settings.xml
../../deployment-examples/pam-eap-setup/pam-setup.sh -b custom=controller,kie,ukie

