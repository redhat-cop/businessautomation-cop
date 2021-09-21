#!/usr/bin/env bash

#
# - STEP 2
#

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

echo "STEP 2 - BUILD DM_PROJECT"

# - check that BusinessCentral is up and running
result=$(curl -s http://localhost:8080/business-central/rest/ready | jq -r '.success')
[[ "x$result" != "xtrue" ]] && echo "ERROR - Please start BusinessCentral with './go_pam.sh' before running this script" && exit 1

goon=nay
pushd dm_project/parent &> /dev/null
  mvn clean deploy -PLOCAL_BC -s../../settings.xml
  result="$?"
  pushd ../rules &> /dev/null
    PRJ_GAV=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.groupId}:${project.artifactId}:${project.version}' --non-recursive exec:exec 2>/dev/null)
  popd &> /dev/null
  goon=nay && [[ "x$result" == "x0" ]] && goon=aye
popd &> /dev/null

if [[ "$goon" == "aye" ]]; then
  echo
  echo "Sample Drools project built and artefacts deployed to Business Central"
  echo
  echo "Artefact version deployed $PRJ_GAV"
  echo
  echo "Deploying artefact to KIE Server with KIE Container ID 'geo_location'"
  echo ./utils/deploy-kjar-bc.js "remote-kieserver:geo_location:$PRJ_GAV"
  ./utils/deploy-kjar-bc.js "remote-kieserver:geo_location:$PRJ_GAV"
fi



