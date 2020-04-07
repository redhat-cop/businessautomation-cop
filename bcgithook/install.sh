#!/bin/bash

#
# installation script for bcgithook
#
# please refer to : https://gitlab.com/erouvas/bcgithook/-/blob/master/README.md
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
# - declare some useful functions
#

# are we running from a terminal
if test -t 1; then
    # see if it supports colors...
    ncolors=$(tput colors)
    if test -n "$ncolors" && test $ncolors -ge 8; then
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
    local h=`echo $i | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }'`
    local strlenh=${#h}
    [[ $strlenh -gt $maxlen ]] && maxlen=$strlenh
  done
  local spacer=`printf '%*s' $maxlen`
  for i in "${arr[@]}"; do
    local h=`echo $i | grep -v '^-' | awk -F':-' '{ if (NF>0) printf $1; }'`
    local strlen=${#h}
    h=" ${spacer}${h}"
    local r=`echo $i | awk -F':-' '{ if (NF>0) for (i=2; i<=NF; i++) printf $i; }'`
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
    echo ':: '$i
  done
}
error() {
  sout "${bold}${yellow}ERROR${normal} $@"
}

usage() {
echo "
Will install bcgithook post-commit git hook and default 
configuration

usage: `basename $0` [JBOSS_HOME] [-h help]

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

Additional information: https://gitlab.com/erouvas/bcgithook
"
}

#
# - goon with installation
#

# - try to detect CygWin
CYGWIN_ON=no
a=`uname -a` && al=`echo $a | awk '{ print tolower($0); }'` && ac=${al%cygwin} && [[ "$al" != "$ac" ]] && CYGWIN_ON=yes
if [[ "$CYGWIN_ON" == "yes" ]]; then
  sout "CYGWIN DETECTED - WILL TRY TO ADJUST PATHS"
fi


SOURCE="`dirname $0`" && [[ "$CYGWIN_ON" == "yes" ]] && SOURCE=$(cygpath -w ${SOURCE})
DEFAULT_CONF_SOURCE="$SOURCE"/scripts/default.conf.example
POST_COMMIT_SOURCE="$SOURCE"/scripts/post-commit.sh

CONFIG_HOME="$HOME/.bcgithook" && [[ "$CYGWIN_ON" == "yes" ]] && CONFIG_HOME=$(cygpath -w ${CONFIG_HOME})

jbhome="$1" && [[ "$CYGWIN_ON" == "yes" ]] && jbhome=$(cygpath -w ${jbhome})

# - some sanity checks
[[ ! -r "$DEFAULT_CONF_SOURCE" ]] && error "- DEFAULT CONFIGURATION CANNOT BE FOUND - ABORTING" && exit 1
[[ ! -r "$POST_COMMIT_SOURCE" ]]  && error "- POST COMMIT SCRIPT CANNOT BE FOUND - ABORTING" && exit 1
[[ ! -d "$jbhome" ]] && error "- JBOSS_HOME CANNOT BE FOUND - ABORTING" && exit 2

[[ ! -x "$jbhome/bin/jboss-cli.sh" ]] && error "- JBOSS CLI CANNOT BE EXECUTED - ABORTING" && exit 3

mkdir -p "$CONFIG_HOME"
[[ ! -d "$CONFIG_HOME" ]] && error "- CONFIGURATION DIRECTORY CANNOT BE CREATED - ABORTING" && exit 4

cp "$DEFAULT_CONF_SOURCE" "$CONFIG_HOME"/default.conf || ( error "- CONFIGURATION FILE CANNOT BE COPIED INTO PLACE - ABORTING" && exit 5 )

gitHookDir="$jbhome"/git-hooks
mkdir -p "$gitHookDir"
[[ ! -d "$gitHookDir" ]] && error "- GIT HOOKS DIRECTORY CANNOT BE CREATED - ABORTING" && exit 6

cp "$POST_COMMIT_SOURCE" "$gitHookDir/post-commit" || ( error "- POST COMMIT SCRIPT CANNOT BE COPIED INTO PLACE - ABORTING" && exit 7 )
echo '#'                                            >> "$gitHookDir/post-commit"
echo "# INSTALLED AT : `date '+%Y-%m-%d %H:%M:%S'`" >> "$gitHookDir/post-commit" 
echo '#'                                            >> "$gitHookDir/post-commit"

pushd "$jbhome/bin" &> /dev/null

cat << "__CLI" > tmp.cli
embed-server
if (outcome == success) of /system-property=org.uberfire.nio.git.hooks:read-resource
  /system-property=org.uberfire.nio.git.hooks:remove
end-if
/system-property=org.uberfire.nio.git.hooks:add(value=${jboss.home.dir}/git-hooks)
stop-embedded-server
__CLI
./jboss-cli.sh --file=tmp.cli

rm -rf tmp.cli

popd &> /dev/null

sout "${bold}${blue}END RUN${normal} - POST COMMIT GIT HOOKS ARE IN PLACE"


