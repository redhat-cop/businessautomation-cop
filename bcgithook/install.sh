#!/usr/bin/env bash

#
# installation script for bcgithook
#
# please refer to : https://github.com/redhat-cop/businessautomation-cop/blob/master/bcgithook/README.md
# for manual installation instructions
#
#
# usage: ./install.sh JBOSS_HOME
#
# - where JBOSS_HOME = the root directory of your JBoss EAP or WildFly installation
#
# this script assumes standard directory layout
# for customized layouts please follow manual installation procedures as outlined
# in the link above
#

#
# sanity environment check
#
CYGWIN_ON=no
MACOS_ON=no
LINUX_ON=no
min_bash_version=4

# - try to detect CYGWIN
# a=`uname -a` && al=${a,,} && ac=${al%cygwin} && [[ "$al" != "$ac" ]] && CYGWIN_ON=yes
# use awk to workaround MacOS bash version
a=$(uname -a) && al=$(echo "$a" | awk '{ print tolower($0); }') && ac=${al%cygwin} && [[ "$al" != "$ac" ]] && CYGWIN_ON=yes
if [[ "$CYGWIN_ON" == "yes" ]]; then
  echo "CYGWIN DETECTED - WILL TRY TO ADJUST PATHS"
  min_bash_version=4
fi

# - try to detect MacOS
a=$(uname) && al=$(echo "$a" | awk '{ print tolower($0); }') && ac=${al%darwin} && [[ "$al" != "$ac" ]] && MACOS_ON=yes
[[ "$MACOS_ON" == "yes" ]] && min_bash_version=5 && echo "macOS DETECTED"

# - try to detect Linux
a=$(uname) && al=$(echo "$a" | awk '{ print tolower($0); }') && ac=${al%linux} && [[ "$al" != "$ac" ]] && LINUX_ON=yes
[[ "$LINUX_ON" == "yes" ]] && min_bash_version=4

bash_ok=no && [ "${BASH_VERSINFO:-0}" -ge $min_bash_version ] && bash_ok=yes
[[ "$bash_ok" != "yes" ]] && echo "ERROR: BASH VERSION NOT SUPPORTED - PLEASE UPGRADE YOUR BASH INSTALLATION - ABORTING" && exit 1 

#
# end of checks
#

#
# - declare some useful functions
#
# are we running from a terminal
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

declare -a summaryAr
summary() {
  declare -a arr=("$@")
  summaryAr=("${summaryAr[@]}" "${arr[@]}")
}

prettyPrinter() {
  declare -a arr=("$@")
  local maxlen=0
  for i in "${arr[@]}"; do
    local h=$(echo "$i" | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }')
    local strlenh=${#h}
    [[ $strlenh -gt $maxlen ]] && maxlen=$strlenh
  done
  local spacer=$(printf '%*s' "$maxlen")
  for i in "${arr[@]}"; do
    local h=$(echo "$i" | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }')
    local strlen=${#h}
    h=" ${spacer}${h}"
    local r=$(echo "$i" | awk -F':-' '{ if (NF>0) for (i=2; i<=NF; i++) printf $i; }')
    local rprefix=""
    local rsuffix=""
    [[ "$r" == "" ]] && r=$i && rprefix="${bold}${white}"
    [[ "$r" != "$i" ]] && echo -n " ${h: -$maxlen}:" && rprefix=" [${bold}${blue}" && rsuffix=" ]"
    echo "${rprefix}${r}${normal}${rsuffix}"
  done
}

sout() {
  declare -a arr=("$@")
  for i in "${arr[@]}"; do
    echo ':: '"$i"
  done
}
error() {
  # NOTE: Investigate if should be fixed, disabled for time being
  # shellcheck disable=SC2145
  sout "${bold}${yellow}ERROR${normal} $@"
}

usage() {
echo "
Will install bcgithook post-commit git hook and default 
configuration

usage: $(basename "$0") [JBOSS_HOME] [-h help]

JBOSS_HOME should point to a JBoss EAP or WildFly installation.

Standard directory structure is assumed as the one 
produced by unzipping the JBoss EAP or WildFly binary.

This script will attempt to:
  - create the necessary directories 
    - $HOME/.bcgithook
    - JBOSS_HOME/git-hooks
  - create the $HOME/.bcgithook/default.conf 
    configuration file using scripts/default.conf.example 
    as a template
  - modify standalone.xml and standalone-full.xml adding 
    required system properties
  - create the post-commit git hook script based on 
    the scripts/post-commit.sh

Please make sure JBoss EAP or WildFly is NOT RUNNING 
before executing this script

Additional information: https://github.com/redhat-cop/businessautomation-cop/blob/master/bcgithook
"
}

#
# - goon with installation
#

SOURCE="$(dirname "$0")" && [[ "$CYGWIN_ON" == "yes" ]] && SOURCE="$(cygpath -w "${SOURCE}")"
DEFAULT_CONF_SOURCE="$SOURCE"/scripts/default.conf.example
POST_COMMIT_SOURCE="$SOURCE"/scripts/post-commit.sh

CONFIG_HOME="$HOME/.bcgithook" && [[ "$CYGWIN_ON" == "yes" ]] && CONFIG_HOME=$(cygpath -w "${CONFIG_HOME}")

jbhome="$1" && [[ "$CYGWIN_ON" == "yes" ]] && jbhome="$(cygpath -w "${jbhome}")"

# - some sanity checks
[[ ! -r "$DEFAULT_CONF_SOURCE" ]] && error "- DEFAULT CONFIGURATION CANNOT BE FOUND - ABORTING" && exit 1
[[ ! -r "$POST_COMMIT_SOURCE" ]]  && error "- POST COMMIT SCRIPT CANNOT BE FOUND - ABORTING" && exit 1
[[ ! -d "$jbhome" ]] && error "- JBOSS_HOME CANNOT BE FOUND - ABORTING" && exit 2

[[ ! -x "$jbhome/bin/jboss-cli.sh" ]] && error "- JBOSS CLI CANNOT BE EXECUTED - ABORTING" && exit 3

mkdir -p "$CONFIG_HOME"
[[ ! -d "$CONFIG_HOME" ]] && error "- CONFIGURATION DIRECTORY CANNOT BE CREATED - ABORTING" && exit 4

if [[ ! -r "$CONFIG_HOME"/default.conf ]]; then
  cp "$DEFAULT_CONF_SOURCE" "$CONFIG_HOME"/default.conf || ( error "- CONFIGURATION FILE CANNOT BE COPIED INTO PLACE - ABORTING" && exit 5 )
else
  cp "$DEFAULT_CONF_SOURCE" "$CONFIG_HOME"/default.conf.new || ( error "- CONFIGURATION FILE CANNOT BE COPIED INTO PLACE - ABORTING" && exit 5 )
  sout "${bold}${white}CONFIGURATION FILE NOT INSTALLED${normal} - EXISTING CONFIGURATION FILE NOT OVERRIDEN"
  sout "${bold}${white}NEW CONFIGURATION FILE COPIED AS .new${normal}"
fi

gitHookDir="$jbhome"/git-hooks
mkdir -p "$gitHookDir"
[[ ! -d "$gitHookDir" ]] && error "- GIT HOOKS DIRECTORY CANNOT BE CREATED - ABORTING" && exit 6

cp "$POST_COMMIT_SOURCE" "$gitHookDir/post-commit" || ( error "- POST COMMIT SCRIPT CANNOT BE COPIED INTO PLACE - ABORTING" && exit 7 )
echo "
#                                             
# INSTALLED AT : $(date '+%Y-%m-%d %H:%M:%S')
#
">> "$gitHookDir/post-commit"

pushd "$jbhome/bin" &> /dev/null

cat << "__CLI" > tmp.cli
embed-server
if (outcome == success) of /system-property=org.uberfire.nio.git.hooks:read-resource
  /system-property=org.uberfire.nio.git.hooks:remove > tmp.out
end-if
/system-property=org.uberfire.nio.git.hooks:add(value=${jboss.home.dir}/git-hooks) > tmp.out
stop-embedded-server
__CLI
./jboss-cli.sh --file=tmp.cli

rm -rf tmp.cli tmp.out

popd &> /dev/null

sout "${bold}${blue}END RUN${normal} - POST COMMIT GIT HOOKS ARE IN PLACE"


