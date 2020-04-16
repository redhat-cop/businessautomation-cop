#!/bin/bash

#
# THE FOLLOWING ENCLOSED IN DASHES SHOULD BE PLACED IN FILE
#
# $HOME/.bcgithook/default.conf
#
# UNCOMMENT THE VARIABLES AND REPLACE WITH APPROPRIATE VALUES
#
# ------------------------------------------------------------------------------
#
# GIT_TYPE      = "azure" for Azure DevOps, blank otherwise
# GIT_USER_NAME = username for remote git repository
# GIT_PASSWD    = password to connect to remote git repository
# GIT_URL       = URL that points to the remote git repository
#                 examples:
#                   Git repositories   example URL
#                   ----------------   --------------------------------
#                   gitlab             https://gitlab.com/GIT_USER_NAME
#                   github             https://github.com/GIT_USER_NAME
#                   gitea (localhost)  http://localhost:3000/GIT_USER_NAME
#
# LOG_LOCATION  = were bcgithook log files should be stored, defaults to home directory of user
#                 executing bcgithook post-commit
#
# LOG_SYSTEM_REPOS = [yes|no], set to "yes" to log access to PAM system
#                              repositories, increases verbosity
#
#
#GIT_USER_NAME='git_user_name'
#GIT_PASSWD='git_password'
#GIT_URL='https://gitlab.com/git_user_name'
#LOG_LOCATION=$HOME
#
# ------------------------------------------------------------------------------
#
# Gitea cheat sheet: https://docs.gitea.io/en-us/config-cheat-sheet/
#
# ------------------------------------------------------------------------------

CONFIG_HOME="$HOME/.bcgithook"
[[ ! -d "$CONFIG_HOME" ]] && mkdir -p "$CONFIG_HOME" 
CONFIG_FILE="$CONFIG_HOME/default.conf"

LOG_LOCATION="$HOME"
LOG_FILE="bcgithook_operations.log"
ERR_FILE="bcgithook_error.log"

PRJ_NAME=null && hwd="`pwd`" && PRJ_NAME="`basename ${hwd%%\/hooks} .git`"

PRJ_CONFIG_FILE="$CONFIG_HOME/$PRJ_NAME.conf" && [[ -r "$PRJ_CONFIG_FILE" ]] && CONFIG_FILE="$PRJ_CONFIG_FILE"

debug() {
  timestamp=`date '+%Y-%m-%d %H:%M:%S'`
  out "[$timestamp] [$CONFIG_FILE] [$PRJ_NAME] $1"
}
out() {
  if [ -n "$LOG_FILE" ] ; then
    printf "%s\n" "$@" >> "$LOG_LOCATION/$LOG_FILE"
  fi
}
urlencode() {
  old_lc_collate=$LC_COLLATE
  LC_COLLATE=C
  
  local length="${#1}"
  for (( i = 0; i < length; i++ )); do
      local c="${1:i:1}"
      case $c in
          [a-zA-Z0-9.~_-]) printf "$c" ;;
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
source "$CONFIG_FILE"

[[ ! -d "$LOG_LOCATION" ]] && LOG_LOCATION="$CONFIG_HOME" \
                           && debug "LOG_LOCATION $LOG_LOCATION NOT FOUND - USING $HOME"

( [[ -z "$GIT_USER_NAME" ]] || [[ -z "$GIT_PASSWD" ]] || [[ -z "$GIT_URL" ]] ) && LOG_FILE="$ERR_FILE" \
                          && debug "USER NAME, PASSWORD OR GIT URL IS MISSING - ABORTING" \
                          && exit 2

gitUserName="$GIT_USER_NAME"
gitPasswd="$GIT_PASSWD"

remoteGitUrl="$GIT_URL"

BBID=bbgit

workusername=`urlencode "$gitUserName"`
workpasswd=`urlencode "$gitPasswd"`

# - ignore system repos
hwd=`pwd` && hwd=${hwd#*\.niogit\/}
frag=${hwd#system}       && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#dashbuilder}  && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#\.archetypes} && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0
frag=${hwd#*\.config}    && [[ "$frag" != "$hwd" ]] && ( [[ "$LOG_SYSTEM_REPOS" != "yes" ]] || debug "SKIPPING BUILTIN PROJECT" ) && exit 0

addBBID=yes
while read gitName gitUrl gitOps; do
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
if [[ `git remote -v | wc -l` -eq 0 ]]; then
  hwd=`pwd`
  debug "NEW PROJECT ADDING $BBID TO $bburl/$REPO_NAME"
  git remote add $BBID $bburl/$REPO_NAME
fi

if [[ "$addBBID" == "yes" ]]; then
  while read gitName gitUrl gitOps; do
    if [[ "$gitName" != "$BBID" ]]; then
      debug "ADDING MISSING $BBID TO $bburl"
      git remote add $BBID $bburl
      break
    fi
  done < <( git remote -v )
fi

debug "PUSHING TO $remoteGitUrl"
git push -u $BBID --all 

