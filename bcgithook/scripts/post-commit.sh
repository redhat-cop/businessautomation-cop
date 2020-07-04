#!/usr/bin/env bash

#
# BY DEFAULT CONFIGURATION FILE IS LOCATED AT
#
# $HOME/.bcgithook/default.conf
#
# CHECK DOCUMENTATION FOR CONFIGURATION DETAILS
#
# ------------------------------------------------------------------------------

CONFIG_HOME="$HOME/.bcgithook"
[[ ! -d "$CONFIG_HOME" ]] && mkdir -p "$CONFIG_HOME"
CONFIG_FILE="$CONFIG_HOME/default.conf"

LOG_LOCATION="$HOME"
LOG_FILE="bcgithook_operations.log"
ERR_FILE="bcgithook_error.log"

PRJ_NAME=null && hwd="$(pwd)" && PRJ_NAME="$(basename ${hwd%%\/hooks} .git)"

PRJ_CONFIG_FILE="$CONFIG_HOME/$PRJ_NAME.conf" && [[ -r "$PRJ_CONFIG_FILE" ]] && CONFIG_FILE="$PRJ_CONFIG_FILE"

# fast and reasonably random
randomid="$(date +%s)_$RANDOM"

debug() {
  timestamp=$(date '+%Y-%m-%d %H:%M:%S')
  out "[$timestamp] [$randomid] [$CONFIG_FILE] [$PRJ_NAME] $1"
}
out() {
  if [ -n "$LOG_FILE" ] ; then
    printf "%s\n" "$@" >> "$LOG_LOCATION/$LOG_FILE"
  fi
}

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

urlencode() {
  old_lc_collate=$LC_COLLATE
  LC_COLLATE=C

  local length="${#1}"
  for (( i = 0; i < length; i++ )); do
      local c="${1:i:1}"
      case $c in
          [a-zA-Z0-9.~_-]) printf "%s" "$c" ;;
          *) printf '%%%02X' "'$c" ;;
      esac
  done

  LC_COLLATE=$old_lc_collate
}
urldecode() {
  local url_encoded="${1//+/ }"
  printf '%b' "${url_encoded//%/\\x}"
}

[[ ! -r "$CONFIG_FILE" ]] && LOG_FILE="$ERR_FILE" \
                          && debug "CONFIG FILE $CONFIG_FILE NOT FOUND - ABORTING" \
                          && exit 1

# shellcheck source=/dev/null
source "$CONFIG_FILE"

debug "START"

[[ ! -d "$LOG_LOCATION" ]] && LOG_LOCATION="$CONFIG_HOME" \
                           && debug "LOG_LOCATION $LOG_LOCATION NOT FOUND - USING $HOME"

( [[ -z "$GIT_USER_NAME" ]] || [[ -z "$GIT_PASSWD" ]] || [[ -z "$GIT_URL" ]] ) && LOG_FILE="$ERR_FILE" \
                          && debug "USER NAME, PASSWORD OR GIT URL IS MISSING - ABORTING" \
                          && exit 2

gitUserName="$GIT_USER_NAME"
gitPasswd="$GIT_PASSWD"

remoteGitUrl="$GIT_URL"

BBID=bbgit

workusername=$(urlencode "$gitUserName")
workpasswd=$(urlencode "$gitPasswd")

# - ignore system repos
hwd=$(pwd) && hwd=${hwd#*\.niogit\/}
frag=${hwd#system}       && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#dashbuilder}  && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#\.archetypes} && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#*\.config}    && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0

addBBID=yes
while read -r gitName gitUrl gitOps; do
  debug "CHECKING $gitName $gitUrl $gitOps"
  [[ "$gitName" == "$BBID" ]] && addBBID=no
  [[ "$gitName" == "origin" ]] && remoteGitUrl="$gitUrl"
done < <( git remote -v )

useme=""
bburl=""
gitUrl="$remoteGitUrl"
isit="https://" && checkurl=${gitUrl#$isit} && [[ "$checkurl" != "$gitUrl" ]] && useme="$isit"
isit="http://"  && checkurl=${gitUrl#$isit} && [[ "$checkurl" != "$gitUrl" ]] && useme="$isit"
[[ "$isit" != "" ]] && bburl=${gitUrl#$useme} && bburl=${bburl/*@/} && bburl="${useme}$workusername:$workpasswd@$bburl"

[[ "$bburl" == "" ]] && debug "REMOTE URL CANNOT BE DETERMINED - ABORTING" && exit 3

REPO_NAME="$PRJ_NAME.git"
[[ "$GIT_TYPE" == "azure" ]] && REPO_NAME="$PRJ_NAME"

# - newly created projects
if [[ $(git remote -v | wc -l) -eq 0 ]]; then
  hwd=$(pwd)
  debug "NEW PROJECT ADDING $BBID TO $bburl/$REPO_NAME"
  git remote add "$BBID" "$bburl"/"$REPO_NAME"
fi

if [[ "$addBBID" == "yes" ]]; then
  while read -r gitName gitUrl gitOps; do
    if [[ "$gitName" != "$BBID" ]]; then
      debug "ADDING MISSING $BBID TO $bburl"
      git remote add "$BBID" "$bburl"
      break
    fi
  done < <( git remote -v )
fi

bruli="$(git branch | colrm 1 2 | awk '{print $1}')"

debug "PUSHING TO $remoteGitUrl"
# git push -u $BBID --all
for bru in $bruli; do
  deny=no
  if [[ -n "$BRANCH_ALLOW_REGEX" ]]; then
    deny=yes && [[ "$bru" =~ $BRANCH_ALLOW_REGEX ]] && deny=no
  else
    [[ -n "$BRANCH_ALLOW" ]] && deny=yes && for de in ${BRANCH_ALLOW//,/ }; do [[ "$de" == "$bru" ]] && deny=no; done
  fi
  for de in ${BRANCH_DENY//,/ }; do [[ "$de" == "$bru" ]] && deny=yes; done
  if [[ "$deny" == "yes" ]]; then
    debug "COMMITS TO BRANCH [$bru] ARE NOT ALLOWED - COMMIT NOT PUSHED TO $remoteGitUrl"
  else
    debug "PUSHING BRANCH [$bru] TO $remoteGitUrl"
    git push -u "$BBID" "$bru":"$bru"
  fi
done

debug "END"

#
# INSTALLED AT : 2020-07-01 19:04:08
#

