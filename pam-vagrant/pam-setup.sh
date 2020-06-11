#!/bin/bash

#
# - setup a local environment with BusinessCentral and one KIE Server
#   installed in a single EAP node
#

#
# === ENVIRONMENT DIVISION - CONFIGURATION SECTION ===
#
# Please refer to documentation for configuration variables
#

#
eapAdminName='admin'
eapAdminPasswd='S3cr3tK3y#'

kieServerUserName='kieServerUser'
#kieServerUserPasswd='kieServerUser1234;'

kieControllerUserName='controllerUser'
#kieControllerUserPasswd='controllerUser1234;'

# configure PAM users
declare -a uList
declare -A uPass
declare -A uRole
u="pamAdmin"               && uList+=( "$u" ) && uPass["$u"]='S3cr3tK3y#' && uRole["$u"]='kie-server,rest-all,admin,analyst,kiemgmt,manager,user,developer,process-admin'
u="pamAnalyst"             && uList+=( "$u" ) && uPass["$u"]='r3dh4t456^' && uRole["$u"]='rest-all,analyst'
u="pamDeveloper"           && uList+=( "$u" ) && uPass["$u"]='r3dh4t456^' && uRole["$u"]='rest-all,developer'
u="pamUser"                && uList+=( "$u" ) && uPass["$u"]='r3dh4t456^' && uRole["$u"]='rest-all,user'
u="$kieServerUserName"     && uList+=( "$u" ) && uPass["$u"]='kieServerUser1234;'  && uRole["$u"]='kie-server,rest-all'
u="$kieControllerUserName" && uList+=( "$u" ) && uPass["$u"]='controllerUser1234;' && uRole["$u"]='kie-server,rest-all'

# configure KIE Server id
serverId='remote-kieserver'

#
# === END OF CONFIGURATION SECTION
#
# -- No need to configure anything beyond this point
#

#
# sanity checks on environment environment
#
command -v java &> /dev/null || { echo >&2 'ERROR: JAVA not installed. Please install JAVA.8 to continue - Aborting'; exit 1; }
command -v unzip &> /dev/null || { echo >&2 'ERROR: UNZIP not installed. Please install UNZIP to continue - Aborting'; exit 1; }
command -v curl &> /dev/null || { echo >&2 'ERROR: CURL not installed. Please install CURL to continue - Aborting'; exit 1; }
command -v sqlite3 &> /dev/null || { echo >&2 'ERROR: SQLite not installed. Please install SQLite to continue - Aborting'; exit 1; }
# required to checkout and built dependencies
command -v mvn &> /dev/null || { echo >&2 'ERROR: Maven not installed. Please install Maven.3.3.9 (or later) to continue - Aborting'; exit 1; }
command -v git &> /dev/null || { echo >&2 'ERROR: GIT not installed. Please install GIT.1.8 (or later) to continue - Aborting'; exit 1; }

# check if stdout is a terminal...
if test -t 1; then

    # see if it supports colors...
    ncolors=$(tput colors)

    if test -n "$ncolors" && test "$ncolors" -ge 8; then
        bold="$(tput bold)"
        underline="$(tput smul)"
        standout="$(tput smso)"
        normal="$(tput sgr0)"
        black="$(tput setaf 0)"
        red="$(tput setaf 1)"
        green="$(tput setaf 2)"
        yellow="$(tput setaf 3)"
        blue="$(tput setaf 4)"
        magenta="$(tput setaf 5)"
        cyan="$(tput setaf 6)"
        white="$(tput setaf 7)"
    fi
fi

MASTER_CONFIG=master.conf

#
# versions supported
#
cat << "__CONFIG" > "$MASTER_CONFIG"
PAM771 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.7.1-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.7.1-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
DM771  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhdm-7.7.1-business-central-eap7-deployable.zip  | KIE_ZIP=rhdm-7.7.1-kie-server-ee8.zip  | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=DM
PAM770 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.7.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.7.0-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
DM760  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhdm-7.6.0-decision-central-eap7-deployable.zip  | KIE_ZIP=rhdm-7.6.0-kie-server-ee8.zip  | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=DM
PAM760 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.6.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.6.0-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
PAM751 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.5.1-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.5.1-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
PAM75  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.5.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.5.0-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
DM741  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhdm-7.4.1-decision-central-eap7-deployable.zip  | KIE_ZIP=rhdm-7.4.1-kie-server-ee8.zip  | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=DM
PAM741 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.4.1-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.4.1-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
PAM74  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.4.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.4.0-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
PAM731 | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=jboss-eap-7.2.*-patch.zip | PAM_ZIP=rhpam-7.3.1-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.3.1-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
DM731  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=                          | PAM_ZIP=rhdm-7.3.1-decision-central-eap7-deployable.zip  | KIE_ZIP=rhdm-7.3.1-kie-server-ee8.zip  | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=DM
PAM73  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=                          | PAM_ZIP=rhpam-7.3.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.3.0-kie-server-ee8.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
PAM72  | EAP7_ZIP=jboss-eap-7.2.0.zip | EAP_PATCH_ZIP=                          | PAM_ZIP=rhpam-7.2.0-business-central-eap7-deployable.zip | KIE_ZIP=rhpam-7.2.0-kie-server-ee7.zip | PAM_PATCH_ZIP= | INSTALL_DIR=jboss-eap-7.2 | TARGET_TYPE=PAM
__CONFIG

#
# define helper functions
#
function extractHeaders() {
  grep -v '^#' "$MASTER_CONFIG" | awk -F'|' '{ if (NF>0) printf "\t"$1"\n"; }'
}

function extractConfiguration() {
  grep -v '^#' "$MASTER_CONFIG" | grep "^$2 " | awk -F'|' '{ if (NF>0) for (i=2; i<=NF; i++) printf $i"\n"; }' > $1
  cp "$1" "/tmp/$2.conf"
}

function randomid() {
  # longer version, seems overkill
  #echo `od -x /dev/urandom | head -1 | awk '{OFS="-"; print $2$3,$4,$5,$6,$7$8$9}'`
  echo $(od -x /dev/urandom | head -1 | awk '{OFS="-"; print $2$3$4}')
}

function bigString() {
  local in=$1
  local le=${#in}
  local o=$in;
  if [ $le -gt 60 ]; then
    o=${in:0:15}'...'${in: -25}
  fi
  echo $o
}

function sout() {
  declare -a arr=("$@")
  for i in "${arr[@]}"; do
    echo ':: '$i
  done
}

function waitForKey() {
  echo '... :::         ::: ...'
  echo '... ::: WAITING ::: ...'
  echo '... :::         ::: ...'
  echo
  echo "$@"
  echo
  read -p 'Press ENTER key to continue...'
  echo
}

declare -a summaryAr
function summary() {
  declare -a arr=("$@")
  summaryAr=("${summaryAr[@]}" "${arr[@]}")
}

function prettyPrinter() {
  declare -a arr=("$@")
  local maxlen=0
  for i in "${arr[@]}"; do
    local h=$(echo $i | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }')
    local strlenh=${#h}
    [[ $strlenh -gt $maxlen ]] && maxlen=$strlenh
  done
  local spacer=$(printf '%*s' $maxlen)
  for i in "${arr[@]}"; do
    local h=$(echo $i | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }')
    local strlen=${#h}
    h=" ${spacer}${h}"
    local r=$(echo $i | awk -F':-' '{ if (NF>0) for (i=2; i<=NF; i++) printf $i; }')
    local rprefix=""
    local rsuffix=""
    [[ "$r" == "" ]] && r=$i && rprefix="${bold}${white}"
    [[ "$r" != "$i" ]] && echo -n " ${h: -$maxlen}:" && rprefix=" [${bold}${blue}" && rsuffix=" ]"
    echo "${rprefix}${r}${normal}${rsuffix}"
  done
}


#
# echo the value for the config key escaping slashes
# value specified in pam.config file for key overrides supplied value
#
# key is either "key" or "COMMENT:key"
#
function prepareConfigLine() {
  local key="$1"
  local val="$2"
  local result="$val"
  local args=() && while read -rd:; do args+=("$REPLY"); done <<<"$key:" && local keyPrefix="${args[0]}" && key="${args[1]}"
  [[ -z $key ]] && key="$keyPrefix"
  if [[ "x$key" != "xCOMMENT" ]]; then
    [[ ! -z "$key" ]] && [ ${pamConfigAr[$key]+xxx} ] && result="${pamConfigAr[$key]}"
    # result=$(echo ${result} | sed -e "s#/#\\\/#g")
  fi
  
  if [[ "x$keyPrefix" != "xCOMMENT" ]] && [[ ! -z "$result" ]]; then
    pamConfigList+=( "if (outcome == success) of /system-property=$key:read-resource" )
    pamConfigList+=( "  /system-property=$key:remove" )
    pamConfigList+=( "end-if" )
    pamConfigList+=( "/system-property=$key:add(value=$result)" )
  fi
}

function usage() {
echo "
Will install PAM on a standalone EAP node or an EAP cluster. Execute this script on each
node that will be part of the cluster.

usage: `basename $0` [-h help]
                     -n ip[:node]
                     -b [kie|controller|both|multi=2...], defaults to 'both'
                     [-c ip1:port1,ip2:port2,...]
                     [-s smart_router_ip:port]
                     [-o additional_options ], specify additional options

example: `basename $0` -n localhost

Options:
    -n : the IP[:port] of the node it will operate on, default is localhost:8080

    -b : Configure PAM installation
             kie : only the KIE ES component
      controller : only the Business Central component without the KIE ES
            both : (default) full suite of PAM along with a separate KIE ES having
                   same node as controller, suitable for a development environment

           multi : a multi-node managed KIE Server installation, with the following
                   configuration:
                   node 1 : 'both' mode installation, i.e. Business Central and KIE ES
                   node 2 : 'kie' mode installation, i.e. only the KIE ES
                   node 3 onwards: 'kie' mode installation

                   Startup scripts will be generated according to the number of nodes specified
                   Node 1 will use the IP:PORT specified with '-n' with each subsequent node
                   using a port offset of 100

    -c :  Manadatory for 'kie' mode of PAM installation, ignored in other modes
          Specify list of controllers that this KIE ES should connect to.
          List of controllers in the form of comma-sperated list of 'IP:PORT' pairs
          e.g. 10.10.1.20:8080,192.168.1.34:8350

     -s : Only for KIE ES, optional. Specify Smart Router location, eg 10.10.1.23:9000

     -o : Specify additional options. Supported options are:

          - nodeX_config=file : declare file with additional commands to be applied by
                                EAPs jboss-cli tool for each node installed
                                X stands for the number of each node, e.g. node1_config, node2_config, etc
                                Files to enable CORS for KIE Server in the first four nodes
                                are provided.
                                For more nodes copy the relevant files from the addons directory

          - debug_logging     : if present will set logging level to DEBUG

          - dump_requests     : if present will enable request dumping to log file

                                WARNING: enabling debug_logging and dump_requests
                                can generate copious amount of output and can have
                                significant impact on perforance

     -h : print this message

Notes:
  - If a file named settings.xml is found in the current directory during
    installation it will be set as custom maven settings through the use of
    kie.maven.settings.custom system property.
    The file will be copied to EAP_HOME and the system property will point to
    that.

  - The following users will be added as part of the installation:

      User            Role
      ----            ----
      admin           EAP admin
      pamAdmin        rest-all,kie-server,admin,analyst,kiemgmt
      pamAnalyst      analyst
      pamDeveloper    developer
      pamUser         user
      kieServerUser   kie-server,rest-all
      controllerUser  kie-server,rest-all

    Passwords for these users will be printed at the end of the installation in stdout
"
}

#
# - install Users - same users for all nodes
#
function installUsers() {
  local nodedir=${1:-standalone}
  # have to add EAP admin user as PAM overrides config
  local scPath="$EAP_HOME/${nodedir}/configuration"
  [[ "$CYGWIN_ON" == "yes" ]] && scPath=$(cygpath -w ${scPath})
  pushd $EAP_HOME/bin &> /dev/null
    ./add-user.sh -sc $scPath -s --user "$eapAdminName" --password "$eapAdminPasswd"
     summary "Added EAP admin user :- $eapAdminName / $eapAdminPasswd"
    #
    # look for : org.jbpm.ht.admin.user, org.jbpm.ht.admin.group, 16.3.1. Tasks visible to the current user
    #
    for u in "${uList[@]}"; do
      ./add-user.sh -sc $scPath -s -a --user "$u" --password "${uPass[$u]}" --role "${uRole[$u]}"
      summary "Added PAM user :- $u / ${uPass[$u]} / ${uRole[$u]}"
    done
  popd &> /dev/null
}

#
# - install BC
#
function installBC() {
  cd $WORKDIR
  unzip -qq -o $PAM_ZIP
  summary "Installed PAM using :- `bigString $PAM_ZIP`"
}

#
# - install KIE Server
#
function installKIE() {
  local nodedir=${1:-standalone}
  cd $WORKDIR
  rm -rf tmp/kie_zip
  mkdir -p tmp/kie_zip
  cp $KIE_ZIP tmp/kie_zip
  pushd tmp/kie_zip &> /dev/null
    unzip -qq -o $KIE_ZIP
  popd &> /dev/null
  cp -r tmp/kie_zip/kie-server.war $EAP_HOME/${nodedir}/deployments
  : > $EAP_HOME/${nodedir}/deployments/kie-server.war.dodeploy
  cp tmp/kie_zip/SecurityPolicy/* $EAP_HOME/bin
  rm -rf tmp
  summary "Installed KIE SERVER :- `bigString $KIE_ZIP`"
}


#
# - check configuration - PAM needs full, clustering best with HA
#
function checkConfiguration() {
  local nodedir=${1:-standalone}
  cd $WORKDIR
  xmlConfig=$EAP_HOME/${nodedir}/configuration/standalone.xml
  xmlConfigHA=$EAP_HOME/${nodedir}/configuration/standalone-full-ha.xml
  if [ ! -r $xmlConfig ]; then
    sout "ERROR: Cannot read configuration $xmlConfig -- exiting"
    exit 1;
  fi
  if [ ! -r $xmlConfigHA ]; then
    sout "ERROR: Cannot read configuration $xmlConfigHA -- exiting"
    exit 1;
  fi
  cp $xmlConfig $xmlConfig-orig
  cp $xmlConfigHA $xmlConfig
  [[ "$CYGWIN_ON" == "yes" ]] && xmlConfig=$(cygpath -w ${xmlConfig})
  #
  # custom directories to accommodate multinode installation
  #
  mkdir -p $EAP_HOME/${nodedir}/{kie,git,metaindex}
}

#
# -- modify $xmlConfig with defaults for PAM setup and EAP cluster setup
#
#    more on system properties at:
#    https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.3/html/installing_and_configuring_red_hat_process_automation_manager_on_red_hat_jboss_eap_7.2/run-dc-standalone-proc#run-standalone-properties-con
#
# sample:
#   enter the password to be stored:
#   echo "foufoutos" | keytool -importpassword -keystore skye.jceks -keypass passwdForKey -alias keyOne -storepass keystorePass -storetype JCEKS
#
function modifyConfiguration() {
  local nodedir=${1:-standalone}
  #local nodeCounter=${2:-0}
  #nodeCounter=$((nodeCounter-1)) && [[ "$nodedir" == "standalone" ]] && nodeCounter=0
  #local nodeOffset=${nodeConfig['nodeOffset']}
  #nodePort=$((basePort+nodeOffset))
  checkConfiguration $nodedir
  BASE_URL='http://'${nodeIP}:${nodeConfig['nodePort']}

  #
  # - if pam.config found, take it into account
  #
  #   comments support : https://stackoverflow.com/a/48155918
  #
  declare -a pamConfigList
  declare -A pamConfigAr
  if [[ -r pam.config ]]; then
    while read PARAM VALUE; do
     [[ "$PARAM" =~ ^[[:space:]]*# ]] && continue
     if [[ ! -z $PARAM ]] && [[ ! -z $VALUE ]]; then
       pamConfigAr[$PARAM]="$VALUE"
     fi
    done < pam.config
  fi

  #
  # - create keystore with the KIE server and Controller user
  #
  local ksPath="$EAP_HOME/${nodedir}/configuration"
  [[ "$CYGWIN_ON" == "yes" ]] && ksPath=$(cygpath -w ${ksPath})
  echo "${uPass[$kieServerUserName]}"     | keytool -noprompt -importpassword -keystore "$ksPath/eigg.jceks" -keypass kieServerUserPasswd     -alias kieServerUser     -storepass eiggPass -storetype JCEKS 2> /dev/null
  echo "${uPass[$kieControllerUserName]}" | keytool -noprompt -importpassword -keystore "$ksPath/eigg.jceks" -keypass kieControllerUserPasswd -alias kieControllerUser -storepass eiggPass -storetype JCEKS 2> /dev/null

  # - generate random serverId if none specified
  if [ "$serverId" == "" ]; then
    serverId='r-'`randomid`
  fi
  tmpf=tmp.`randomid`
  # following property not required for PAM, useful for clustered deployemnts, harmless otherwise
  prepareConfigLine "jboss.node.name"                       "${nodeConfig[nodeName]}"  
  prepareConfigLine "jboss.tx.node.id"                      "${nodeConfig[nodeName]}"  
  prepareConfigLine "org.kie.server.repo"                   '${jboss.server.data.dir}'  
  prepareConfigLine "org.uberfire.nio.git.dir"              '${jboss.server.base.dir}/git'  
  prepareConfigLine "org.uberfire.metadata.index.dir"       '${jboss.server.base.dir}/metaindex'  
  prepareConfigLine "org.guvnor.m2repo.dir"                 '${jboss.server.base.dir}/kie'  
  prepareConfigLine "org.guvnor.project.gav.check.disabled" 'true'  
  # <property name="org.kie.server.domain" value="user_authntication_JAAS_LoginContext_domain_when_using_JMS"/>
  # check for settings.xml and (un)comment accordingly while copying settings.xml in place
  local keyPrefix="COMMENT" && [[ -r settings.xml ]] && cp settings.xml $EAP_HOME && keyPrefix=""
  # cPrefix='<!-- ' && cSuffix=' -->' && [[ -r settings.xml ]] && cPrefix="" && cSuffix=""
  # set kie.maven.settings.custom to your custom settings.xml
  prepareConfigLine "${keyPrefix}:kie.maven.settings.custom"     '${jboss.home.dir}/settings.xml'  
  prepareConfigLine "COMMENT:org.kie.server.controller.connect"  '10000_milliseconds_delay_for_controller_connect'  
  prepareConfigLine "appformer.ssh.keys.storage.folder"          '${jboss.server.base.dir}/ssh_keys'  
  # uncomment following line to enable git hooks
  # prepareConfigLine "org.uberfire.nio.git.hooks"                 '${jboss.home.dir}/git-hooks'  
  # uncomment the following to set the property and modify the sample file to customize messages from git operations
  # prepareConfigLine "appformer.git.hooks.bundle"         '${jboss.home.dir}/git-hooks/Messages.properties'  
  # properties for Unified KIE Server setup
  # the following values must be the same on all of the KIE Execution Servers
  prepareConfigLine "org.kie.server.persistence.ds"              'java:jboss/datasources/ExampleDS'  
  prepareConfigLine "org.kie.server.persistence.dialect"         'org.hibernate.dialect.H2Dialect'  
  prepareConfigLine "org.kie.executor.jms.queue"                 'queue/KIE.SERVER.EXECUTOR'  
  prepareConfigLine "kie.keystore.keyStoreURL"                   'file:///${jboss.server.config.dir}/eigg.jceks'  
  prepareConfigLine "kie.keystore.keyStorePwd"                   'eiggPass'  

  if [ "$pamInstall" != "kie" ]; then
    # properties for controller/business-central in a managed KIE Server setup
    prepareConfigLine "org.kie.server.user"                        "$kieServerUserName"  
    #
    # following is deprecated as of RHPAM.7.4
    # echo "$(prepareConfigLine "org.kie.server.pwd"             "$kieServerUserPasswd"  
    #
    prepareConfigLine "kie.keystore.key.server.alias"              'kieServerUser'  
    prepareConfigLine "kie.keystore.key.server.pwd"                'kieServerUserPasswd'  
  fi

  if [ "$pamInstall" != "controller" ]; then
    # - build clv based on controllerListAr
    clv=''
    for i in ${controllerListAr[@]}; do
      local baseController=business-central
      [[ "$TARGET_TYPE" == "DM" ]] && baseController=decision-central
      clv="$clv,${i}/$baseController/rest/controller"
    done
    clv=${clv#,}
    nodeConfig['controllerUrl']="${clv}"
    # KIE server capabilities
    prepareConfigLine "org.drools.server.ext.disabled"             'false'  
    if [[ "$TARGET_TYPE" != "DM" ]]; then
      prepareConfigLine "org.jbpm.server.ext.disabled"             'false'  
      prepareConfigLine "org.jbpm.ui.server.ext.disabled"          'false'  
    fi
    prepareConfigLine "org.optaplanner.server.ext.disabled"        'false'  
    if [ "$pamInstall" == "kie" ] || [ "$pamInstall" == "both" ]; then
      #local sedclv=$(echo ${clv} | sed -e "s#/#\\\/#g")
      # properties for managed KIE Server
      prepareConfigLine "org.kie.server.id"                        "$serverId"  
      prepareConfigLine "org.kie.server.location"                  "$BASE_URL/kie-server/services/rest/server"  
      prepareConfigLine "org.kie.server.controller"                "${clv}"  
      prepareConfigLine "org.kie.server.controller.user"           "$kieControllerUserName"  
      # overcome 256 character limitation in process variable values
      # if you uncomment the following parameter, remember to alter your database column accordingly
      # see also: https://issues.redhat.com/browse/JBPM-4221
      # prepareConfigLine "COMMENT:org.jbpm.var.log.length"          "1000"                    
      #
      # deprecated after RHPAM.7.4
      # echo '<!-- UNIQ_MARK_1 --><property name="org.kie.server.controller.pwd"  value="REPLACE_ME"/>' | sed s/REPLACE_ME/"$kieControllerUserPasswd"/g >> 
      #
      prepareConfigLine "kie.keystore.key.ctrl.alias"              "kieControllerUser"  
      prepareConfigLine "kie.keystore.key.ctrl.pwd"                "kieControllerUserPasswd"  
      if [[ ! -z $smartRouter ]]; then
        prepareConfigLine "org.kie.server.router"                  "http://${smartRouter}"  
      fi
      # set following to false to enable Prometheus end points
      prepareConfigLine "org.kie.prometheus.server.ext.disabled"   "true"  
      # provide custom prediction service
      # prepareConfigLine "org.jbpm.task.prediction.service" 'SMILERandomForest'  
    fi
  fi

  # properties for controller/Business-Central
  # default is full profile
  if [ "$pamInstall" == "controller" ]; then
    # prepareConfigLine "org.kie.active.profile"                     'full'  
    if [[ "$TARGET_TYPE" == "DM " ]]; then
      # identify itself as DecisionCentral instead of BusinessCentral
      prepareConfigLine "org.kie.workbench.profile"          "FORCE_PLANNER_AND_RULES"    
      prepareConfigLine "org.jbpm.designer.perspective"      "ruleflow"                   
    fi
  else
    prepareConfigLine "COMMENT:org.kie.active.profile"                     'one_of_full|exec-server|ui-server'  
  fi

  #
  # - if pam.config found, include all variables in the output
  #
  if [[ -r pam.config ]]; then
    # configuration properties found at pam.config
    for key in "${!pamConfigAr[@]}"; do
       prepareConfigLine "${key}"         "${pamConfigAr[$key]}"  
     done
  fi
  # since jboss-cli.sh in embedded mode cannot handle multiple base dirs we need to play tricksies...
  if [[ "$nodedir" != "standalone" ]]; then
    cp $EAP_HOME/standalone/configuration/standalone.xml $EAP_HOME/standalone/configuration/standalone.xml.backup
    cp $xmlConfig $EAP_HOME/standalone/configuration/standalone.xml
  fi
  pushd $EAP_HOME/bin &> /dev/null
    local pamConfigFile=/tmp/pamConfigFile.$INSTALL_ID && [[ "$CYGWIN_ON" == "yes" ]] && pamConfigFile=$(cygpath -w ${pamConfigFile})
    echo 'embed-server' > $pamConfigFile
    printf '%s\n' "${pamConfigList[@]}"  >> $pamConfigFile
    cat $ADDITIONAL_NODE_CONFIG >> $pamConfigFile
    echo 'stop-embedded-server' >> $pamConfigFile
    ./jboss-cli.sh --file=$pamConfigFile
  popd &> /dev/null
  if [[ "$nodedir" != "standalone" ]]; then
    cp $EAP_HOME/standalone/configuration/standalone.xml $xmlConfig 
    cp $EAP_HOME/standalone/configuration/standalone.xml.backup $EAP_HOME/standalone/configuration/standalone.xml
  fi
  rm -f $tmpf* $ADDITIONAL_NODE_CONFIG $pamConfigFile
  # try to safeguard exposed interfaces
  local dc=2  && [[ "$CYGWIN_ON" == "yes" ]] && dc=3
  l=`grep -H -n '<interface name="management">' $xmlConfig | head -1 | cut -d':' -f$dc`;
  #  modify management interface, should already be 127.0.0.1 but making sure
  let lno=$((l+1))
  sed -i "${lno}s/:.*}/:127.0.0.1}/" $xmlConfig
  sync
  # - modify public interface
  let lno=$((l+4))
  sed -i "${lno}s/:.*}/:$nodeIP}/" $xmlConfig
  sync
  # - modify private interface
  let lno=$((l+7))
  sed -i "${lno}s/:.*}/:$nodeIP}/" $xmlConfig
  sync
  rm -f $tmpf*

  #
  # modify logging to output to console as well as file
  #
  tmpf=tmp.`randomid`
  : > $tmpf
  l=`grep -H -n 'periodic-rotating-file-handler' $xmlConfig | head -1 | cut -d':' -f$dc`;
  echo '<!-- NEW_LOG_HANDLER --> <console-handler name="CONSOLE">' >> $tmpf
  echo '<!-- NEW_LOG_HANDLER -->     <level name="INFO"/>' >> $tmpf
  echo '<!-- NEW_LOG_HANDLER -->     <formatter>' >> $tmpf
  echo '<!-- NEW_LOG_HANDLER -->         <named-formatter name="COLOR-PATTERN"/>' >> $tmpf
  echo '<!-- NEW_LOG_HANDLER -->     </formatter>' >> $tmpf
  echo '<!-- NEW_LOG_HANDLER --> </console-handler>' >> $tmpf
  let lno=$((l-1))
  sed -i -e "${lno}r $tmpf" $xmlConfig
  : > $tmpf
  l1=`grep -H -n '<root-logger>' $xmlConfig | head -1 | cut -d':' -f$dc`;
  let l2=$((l1+2))
  echo '<!-- NEW_LOG_HANDLER --> <handler name="CONSOLE"/>' > $tmpf
  sed -i -e "${l2}r $tmpf" $xmlConfig
  rm -f $tmpf*
}

#
# - create a sample start up script
#
function startUp() {
  local nodedir=${1:-standalone}
  local nodeCounter=${2:-0}
  # nodeCounter=$((nodeCounter-1)) && [[ "$nodedir" == "standalone" ]] && nodeCounter=0
  # local nodeOffset=$((nodeCounter*100))
  # local nodePort=$((basePort+nodeOffset))
  # nodeOffset=$((nodePort-8080))
  local nodeOffset=${nodeConfig['nodeOffset']}
  local startScript=gopam.sh
  [[ "$nodeCounter" -gt 0 ]] && startScript=go${nodedir}.sh
  nodeConfig['startScript']="$startScript"
  cat << __GOPAM > $startScript
  #!/bin/bash

  #
  # usage: ./gopam.sh configuration-xml IP-to-bind-to port-offset
  #
  # example: ./${startScript}
  #          by default will start on localhost with standalone.xml and default ports (8080)
  #
  #          ./${startScript} standalone.xml 0.0.0.0 100
  #          will start with standalone.xml binding on all IPs and on port 8180 (port offset 100)

  JBOSS_CONFIG=\${1:-standalone.xml}
  JBOSS_BIND=\${2:-localhost}
  JBOSS_PORT_OFFSET=\${3:-$nodeOffset}
  [[ "\$JBOSS_PORT_OFFSET" != "0" ]] && JBOSS_PORT_OFFSET="-Djboss.socket.binding.port-offset=\$JBOSS_PORT_OFFSET"
  [[ "\$JBOSS_PORT_OFFSET" == "0" ]] && JBOSS_PORT_OFFSET=" "
  [[ -z "\$JBOSS_HOME" ]] && JBOSS_HOME="$EAP_HOME"
  CLI_OPTIONS=" -Djava.security.egd=file:/dev/./urandom "
  if [[ \$(uname | grep -i CYGWIN) ]]; then
    JBOSS_HOME=\$(cygpath -w \${JBOSS_HOME})
    CLI_OPTIONS=" "
  fi

  pushd \${JBOSS_HOME}/bin/ &> /dev/null
    ./standalone.sh -b \$JBOSS_BIND -c \$JBOSS_CONFIG \$JBOSS_PORT_OFFSET -Djboss.server.base.dir=\$JBOSS_HOME/$nodedir \$CLI_OPTIONS
  popd &> /dev/null
__GOPAM
  chmod u+x $startScript
  summary "Startup script :- $startScript"
}

#
# - enable HTTP request dumping and access log
#
function applyAdditionalNodeConfig() {
  local nodedir=${1:-standalone}
  local pamAdmPort=$((9990+${nodeConfig[nodeOffset]}))
  local startScript=${nodeConfig[startScript]}
  local k="${nodeConfig[nodeCounter]}_config"
  local nodeOffset=${nodeConfig['nodeOffset']}
  local default_config="$WORKDIR/addons/$k"
  # apply node offset
  if [[ "$nodeOffset" != 0 ]]; then
    echo "/socket-binding-group=standard-sockets/:write-attribute(name=port-offset,value=\${jboss.socket.binding.port-offset:$nodeOffset})" >> $ADDITIONAL_NODE_CONFIG
  fi
  #
  # apply any node specific configuration
  #
  [[ ! -r "$default_config" ]] && [[ ! -r "${configOptions[$k]}" ]] && [[ -z "${configOptions[debug_logging]}" ]] && [[ -z "${configOptions[dump_requests]}" ]] && return
  [[ -r "$default_config" ]] && cat "$default_config" >> $ADDITIONAL_NODE_CONFIG
  #
  if [[ -r "${configOptions[$k]}" ]]; then
    sout "Applying additional configuration for ${nodeConfig[nodeCounter]} based on ${configOptions[$k]}"
     cat "${configOptions[$k]}" >> $ADDITIONAL_NODE_CONFIG
  fi
  if [[ ! -z "${configOptions[dump_requests]}" ]]; then
     cat '/subsystem=undertow/configuration=filter/expression-filter=requestDumperExpression:add(expression="dump-request")' >> $ADDITIONAL_NODE_CONFIG
     cat "/subsystem=undertow/server=default-server/host=default-host/filter-ref=requestDumperExpression:add" >> $ADDITIONAL_NODE_CONFIG
  fi
}


function prepareConfigDB() {
  sqlite3 $CONFIG_DB "drop table if exists pamrc"
  local sql=""
  sql="$sql create table pamrc ("
  sql="$sql     pkey integer primary key, "
  sql="$sql     installId varchar, "
  sql="$sql     pamInstall varchar, "
  sql="$sql     nodeName varchar, "
  sql="$sql     nodeBase varchar, "
  sql="$sql     nodeOffset varchar, "
  sql="$sql     nodePort varchar, "
  sql="$sql     controllerUrl varchar, "
  sql="$sql     installDir varchar, "
  sql="$sql     pamTarget varchar, "
  sql="$sql     startScript varchar, "
  sql="$sql     nodeCounter varchar "
  sql="$sql     )"
  sqlite3 $CONFIG_DB "$sql"
}

function nodeConfigSave() {
  local sql=""
  local pi=""
  for pi in ${nodeConfig['pamInstall']}; do
    sql="insert or ignore into pamrc (installId,pamInstall,nodeName,nodeBase,nodeOffset,nodePort,controllerUrl,installDir,pamTarget,startScript,nodeCounter) values ("
    sql="${sql}'$INSTALL_ID','$pi','${nodeConfig[nodeName]}','${nodeConfig[nodeBase]}','${nodeConfig[nodeOffset]}','${nodeConfig[nodePort]}','${nodeConfig[controllerUrl]}','$INSTALL_DIR','$target','${nodeConfig[startScript]}','${nodeConfig[nodeCounter]}')"
    sqlite3 $CONFIG_DB "${sql};"
  done
}

#
# - end of function definitions
#

INSTALL_ID=`randomid`

#
# - try to detect CYGWIN
#
CYGWIN_ON=no
# a=`uname -a` && al=${a,,} && ac=${al%cygwin} && [[ "$al" != "$ac" ]] && CYGWIN_ON=yes
# use awk to workaround MacOS bash version
a=`uname -a` && al=`echo $a | awk '{ print tolower($0); }'` && ac=${al%cygwin} && [[ "$al" != "$ac" ]] && CYGWIN_ON=yes
if [[ "$CYGWIN_ON" == "yes" ]]; then
  sout "CYGWIN DETECTED - WILL TRY TO ADJUST PATHS"
fi

WORKDIR=$PWD
[[ "$CYGWIN_ON" == "yes" ]] && WORKDIR=$(cygpath -w ${WORKDIR})

CONFIG_DB="$WORKDIR"/pam-config.db
ADDITIONAL_NODE_CONFIG=/tmp/additionalNodeConfig.$INSTALL_ID  && [[ "$CYGWIN_ON" == "yes" ]] && ADDITIONAL_NODE_CONFIG=$(cygpath -w ${ADDITIONAL_NODE_CONFIG})

pamTargets=`extractHeaders`
TARGET_CONFIG=target.conf
goon=no
for target in $pamTargets; do
  #
  # initialize config vars
  #
  EAP7_ZIP=
  EAP_PATCH_ZIP=
  PAM_ZIP=
  KIE_ZIP=
  PAM_PATCH_ZIP=
  INSTALL_DIR=
  extractConfiguration $TARGET_CONFIG $target
  . $TARGET_CONFIG
  [[ -r "$EAP7_ZIP" ]] && [[ -r "$PAM_ZIP" ]] && [[ -r "$KIE_ZIP" ]] && [[ -r "$KIE_ZIP" ]] && goon=yes && break;
done

if [[ "$goon" == "yes" ]]; then
  sout "PROCEEDING WITH ${bold}${yellow}$target${normal} "
else
  echo "NO BINARIES FOR PAM INSTALLATION FOUND - ABORTING"
  exit 1
fi

rm -f $MASTER_CONFIG $TARGET_CONFIG

nodeIP=''
optB=''
optC=''
optS=''
optO=''
multiNode=1
while getopts ":n:b:c:s:o:h" option; do
  case $option in
  	n ) nodeIP=$OPTARG;;
    b ) optB=$OPTARG;;
    c ) optC=$OPTARG;;
    s ) optS=$OPTARG;;
    o ) optO=$OPTARG;;
  	h ) usage; exit 1;;
  	: ) echo "No argument given"; opt=1; exit 1;;
  	* ) echo "Unknown option"; usage; opt=1; exit 1;;
  esac
done

prepareConfigDB

echo "PAM_VERSION=$target" > /root/pam.version
summary "--- $target Installation Summary ---" " "

# - check nodeIP values - default to localhost:8080
nodeIP=${nodeIP:-localhost:8080}
if [ -z $nodeIP ]; then
  echo "${bold}${red}*** ERROR ***${normal}: Option ${bold}${cyan}-n${normal} is mandatory"
  usage
  exit 1
fi
tmpfs=$IFS; IFS=':'; declare -a ar=($nodeIP); IFS=$tmpfs; nodeIP=${ar[0]}; nodePort=${ar[1]}; nodePort=':'${nodePort:-8080}; unset ar
BASE_URL='http://'${nodeIP}${nodePort}
basePort=${nodePort/:/} # this port will be used to autocalculate offset from 8080
# - check PAM installation value
pamValid=no
pamInstall=${optB:-both}
if [[ "$optB" =~ ^multi.* ]]; then
  pamInstall=multi
  declare -a multiArgs
  multiArgs=()
  while read -rd=; do multiArgs+=("$REPLY"); done <<<"${optB}="
  multiOption=${multiArgs[0]}
  multiNode=${multiArgs[1]}
  unset multiArgs multiOption
  [[ "$multiNode" -lt 2 ]] && echo "${bold}${red}*** ERROR ***${normal} : multi node cannot be less than 2 - ABORTING " && exit 1
fi
declare -a validPAMInstall=(kie controller both multi)
for i in "${validPAMInstall[@]}"; do
  if [ "$pamInstall" == "$i" ]; then
    pamValid=yes
  fi
done
if [ "$pamValid" != "yes" ]; then
  echo "${bold}${red}*** ERROR ***${normal} : Invalid mode for PAM installation [$pamInstall]"
  usage
  exit 1
else
  if [[ "$pamInstall" != "multi" ]]; then
    sout "PAM Installation mode : ${bold}${yellow}$pamInstall${normal}"
    summary "PAM Installation mode :- $pamInstall"
  else
    sout "PAM Installation mode : ${bold}${yellow}$pamInstall${normal} with ${bold}${yellow}$multiNode${normal} nodes"
    summary "PAM Installation mode :- $pamInstall with $multiNode nodes"
  fi
fi
# - check controller values for 'kie' PAM install mode
if [ "$pamInstall" != "kie" ]; then
  dv=${nodeIP}${nodePort}
fi
controllerList=${optC:-${dv}}
tmpfs=$IFS; IFS=','; declare -a ar=($controllerList); IFS=$tmpfs;
declare -a controllerListAr
for i in "${ar[@]}"; do
  controllerListAr=("${controllerListAr[@]}" http://${i})
done
unset ar
summary "Using Controller List :- ${controllerListAr[@]}"
if [ "$pamInstall" == "kie" ] && [ ${#controllerListAr[@]} -lt 1 ]; then
  sout "ERROR: Controllers are madnatory for kie mode installation, none specified"
  usage
  exit 1
fi
smartRouter="$optS"
summary "Using Smart Router location :- ${smartRouter:-NOT INSTALLED}"
#
# - get extra options
#   option1=value1:option2=value2:
#
declare -A configOptions
if [[ ! -z "$optO" ]]; then
  declare -a multiOptions
  while read -rd:; do multiOptions+=("$REPLY"); done <<<"${optO}:"
  for ondx in ${!multiOptions[@]}; do
    while read -rd=; do tmpar+=("$REPLY"); done <<<"${multiOptions[$ondx]}="
    k="${tmpar[0]}"
    v="${tmpar[1]}" && v="${v:-1}"
    configOptions["$k"]="${v}"
    unset tmpar
  done
  unset tmpar multiOptions
fi
#
[[ ! -r $EAP7_ZIP ]]      && sout "ERROR: Cannot read EAP.7 ZIP file $EAP7_ZIP -- exiting"          && exit 1;
patchEAP=yes
[[ -z $EAP_PATCH_ZIP ]] && patchEAP=no
eap_patch_file_found=""
if [[ "$patchEAP" == "yes" ]]; then
  for eap_patch_file in `ls $EAP_PATCH_ZIP 2> /dev/null`; do
    [[ -r "$eap_patch_file" ]] && eap_patch_file_found="$eap_patch_file"
  done
fi
EAP_PATCH_ZIP="$eap_patch_file_found"
[[ -z $EAP_PATCH_ZIP ]] && patchEAP=no
[[ ! -z $EAP_PATCH_ZIP ]] && [[ ! -r $EAP_PATCH_ZIP ]] && sout "WARNING: Cannot read EAP PATCH ZIP file $EAP_PATCH_ZIP -- will proceed without it" && patchEAP=no

installPAM=yes
[[ ! -r $PAM_ZIP ]] && sout "WARNING: Cannot read PAM ZIP file $PAM_ZIP -- will proceed without it" && installPAM=no && patchPAM=no

skip_install=no && [[ -d $INSTALL_DIR ]] && sout "INFO: Installation detected at $INSTALL_DIR -- skipping installation" && skip_install=yes

EAP_HOME=$WORKDIR/$INSTALL_DIR

if [ "$skip_install" != "yes" ]; then
  #
  sout "Installing EAP at $EAP_HOME using $EAP7_ZIP"
  unzip -qq $EAP7_ZIP
  summary "Installed EAP using :- $EAP7_ZIP"
  summary "EAP install location :- $INSTALL_DIR"
  #
  cd $EAP_HOME
  if [[ "$patchEAP" == "yes" ]]; then
    sout "Patching EAP with $EAP_PATCH_ZIP"
    bin/jboss-cli.sh --output-json "patch apply $WORKDIR/$EAP_PATCH_ZIP"
    summary "Patched EAP with :- `bigString $EAP_PATCH_ZIP`"
    pushd $EAP_HOME/bin &> /dev/null
      ./add-user.sh -s --user "$eapAdminName" --password "$eapAdminPasswd"
    popd &> /dev/null
  fi
  cd $WORKDIR
  #
  # - deploy sample application to test session replication
  #   use
  #     http://load-balancer-ip/SessionCounter/Counter
  #   to get a session-based counter
  #   experiment by shutting down and restarting nodes while requesting
  #   for counter
  #
  war=SessionCounter.war && [[ -r $war ]] && cp $war $EAP_HOME/standalone/deployments
  #
  # - if multiNode make suitable number of copies
  #
  if [[ "$multiNode" -gt 1 ]]; then
    pushd $EAP_HOME &> /dev/null
      for node in `seq 2 $multiNode`; do
          cp -a standalone node${node}
      done
    popd &> /dev/null
  fi
  #
  cd $WORKDIR
  if [[ "$installPAM" == "yes" ]]; then
    save_pamInstall=''
    for node in `seq 1 $multiNode`; do
      declare -A nodeConfig
      save_pamInstall=$pamInstall
      [[ "$node" -eq 1 ]] && [[ "$pamInstall" == "multi" ]] && pamInstall=both
      [[ "$node" -gt 1 ]] && [[ "$pamInstall" == "multi" ]] && pamInstall=kie && nodeParam=node${node}
      nodeConfig['pamInstall']=" "
      nodeParam=${nodeParam:-standalone}
      nodeConfig['nodeName']=${nodeParam}_`randomid`
      nodeConfig['nodeBase']=$nodeParam
      nodeConfig['nodeCounter']=node$node
      nodeCounter=$((node-1)) && [[ "$nodeParam" == "standalone" ]] && nodeCounter=0
      nodePort=$((basePort+nodeCounter*100))
      nodeOffset=$((nodePort-8080))
      nodeConfig['nodeOffset']=$nodeOffset
      nodeConfig['nodePort']=$nodePort
      sout "Installing ${bold}${blue}node${node}${normal} as ${bold}${blue}${nodeConfig[nodeName]}${normal}"
      summary "--- Node instalation node${node} as $nodeParam : ${nodeConfig[nodeName]}"
      ( [[ "$pamInstall" == "controller" ]] || [[ "$pamInstall" == "both" ]] ) && installBC && nodeConfig['pamInstall']="${nodeConfig['pamInstall']} controller"
      ( [[ "$pamInstall" == "kie" ]]        || [[ "$pamInstall" == "both" ]] ) && [[ -r $KIE_ZIP ]] && installKIE $nodeParam  && nodeConfig['pamInstall']="${nodeConfig['pamInstall']} kie"
      installUsers $nodeParam
      applyAdditionalNodeConfig $nodeParam
      modifyConfiguration $nodeParam
      startUp $nodeParam $nodeCounter
      # declare -p nodeConfig
      nodeConfigSave
      pamInstall=$save_pamInstall
      unset nodeConfig nodeCounter nodeOffset nodePort nodeParam node
    done
    unset save_pamInstall nodeParam component
  fi
  #
  # Configuring for post-commit hooks - relevant only for BC node
  #
  pushd $EAP_HOME &> /dev/null
    mkdir -p git-hooks
    cd git-hooks
    : > Messages.properties
    for f in `seq 0 128`; do
      [[ $f -eq 0 ]] && echo "$f=Successfully commited to remote repository" >> Messages.properties
      [[ $f -ne 0 ]] && echo "$f=Error Code $f" >> Messages.properties
    done
    cp Messages.properties Messages_en.properties
    if [[ ! -r ../../post-commit ]]; then
      : > post-commit
      echo "#!/bin/sh" >> post-commit
      echo "git push origin +master" >> post-commit
      chmod 744 post-commit
    else
      cp ../../post-commit .
      chmod 744 post-commit
    fi
  popd &> /dev/null
  # print summary of installation
  summary " "
  prettyPrinter "${summaryAr[@]}"
fi  # - end of skip_install check


# - for debugging, enable command output, stop on first error
# set -x
# set -e

#
# - end of script
#
