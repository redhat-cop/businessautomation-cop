![Build for bcgithook](https://github.com/redhat-cop/businessautomation-cop/workflows/Build%20for%20bcgithook/badge.svg)

# bcgithook: Business Central git hooks in bash
Business Central is able to push changes into remote git repositories utilizing post-commit git hooks.
This project offers a bash-based implementation for such git hooks.

## Features
* Lightweight, relies only on standard git client and bash
* Works with any git provider, e.g. [GitLab](https://gitlab.com/), [GitHub](https://github.com/), [Bitbucket](https://bitbucket.org/), [Azure DevOps Repos](https://azure.microsoft.com/en-gb/services/devops/repos/), [Gitea](https://gitea.io/en-us/), [Gogs](https://gogs.io/), etc
* Can push to different git repository per project
* Supports run-of-the-mill git operations such as create a new project, create branch, commit/push changes to branches
* Works on Linux, Windows (on a Cygwin environment), probably on Mac (not tested)
* Scripted or manual installation mode
* Configurable logging of operations
## Configuration
**bcgithook** will look for its configuration in file `default.conf` placed in `$HOME/.bcgithook` directory. This file must be present event if per-project configuration files are used. The following variables need to be configured:

|Variable|Type|Content|
|--|--|--|
|`GIT_USER_NAME` | **required** | The ID for the git repo you are using. Surround the value in single quotation marks. |
|`GIT_PASSWD` | **required** | The password for the git repo. Surround the value in single quotation marks. |
|`GIT_URL` | **required** | The URL to connect to the git repo. See below for examples for various Git repos. Surround the value in single quotation marks.|
|`GIT_TYPE` | optional | Leave blank or undefined for all Git repos. Use **"azure"** (in quotation marks) for Azure DevOps |
|`LOG_LOCATION` | optional | The directory where logs should be written. Defaults to `$HOME` |
|`LOG_SYSTEM_REPOS` | optional | If set to "yes" will log access to system repositories, can result in some verbosity |
|`BRANCH_ALLOW`| optional | A comma-separated list, or a regular expession, of branches to allow commits to be pushed to.  When using a comma-separated list of branches, do not leave space between the comma and the branch name or enclose in quotes. |
|`BRANCH_ALLOW_REGEX`| optional | A regular expression specifying branches to allow commits for. If defined overrides `BRANCH_ALLOW`. Use quotes to specify a regular expression, for example `"^(feature/)\|^(hotfix)"` |
|`BRANCH_DENY`| optional | a comma-separated list of branches to deny commits to be pushed to. Do not leave space between the comma and the branch name or enclose in quotes. |

See below for example configurations for various Git repos.

### Commits per branch
Post-commit git hooks by default will push all commits in a branch to the configured remote git repository. This behaviour can be modified by declaring ALLOW and DENY lists.

ALLOW and DENY lists refer to branches that the post-commit git hooks will selectively push commits to according to the following rules:

* Both lists are optional. You can define either ALLOW or DENY, both or none.
* If present, ALLOW_REGEX will override ALLOW.
* If no list is defined post-commit git hooks will by default allow all commits to be pushed to the remote git repo.
* If only the ALLOW (or ALLOW_REGEX) list is defined, commits will be pushed to the remote git repo only for branches that can be found in this list.
* If only the DENY list is defined, commits to branches that can be found in this list will NOT be pushed to the remote git repo.
* If both ALLOW (or ALLOW_REGEX) and DENY lists are defined, then the DENY list takes precedence. If a branch can be found at both the ALLOW (or ALLOW_REGEX) and DENY lists, then commits to that branch will not be pushed to the remote git repo.

**Example 1: Separate branches in ALLOW and DENY lists**

| Definition | Expected Action
|-|-|
|`BRANCH_ALLOW=branch2,feature/fa`|Commits in branches "branch2" and "feature/fa" will be pushed to the remote git repo.|
|`BRANCH_DENY=master,release`|Commits in branches "master" and "release" will not be pushed to the remote git repo.|

**Example 2: Some branches in both ALLOW and DENY lists**

| Definition | Expected Action
|-|-|
|`BRANCH_ALLOW=branch2,feature/fa`|Commits in branch "feature/fa" will be pushed to the remote git repo. Branch "branch2" is also declared at `BRANCH_DENY` which takes precedence.|
|`BRANCH_DENY=master,release,branch2`|Commits in branches "master", "release" and "branch2" will not be pushed to the remote git repo.|

**Example 3: Separate branches in ALLOW and DENY lists, with regular expressions**

| Definition | Expected Action
|-|-|
|`BRANCH_ALLOW_REGEX="^(hotfix/)\|^(feature/)"`| Specifies branches to allow commits for in the form of regular expressions. In this example commits in branches starting with `hotfix/` or `feature/` will be allowed to be pushed to the remote git repo |
|`BRANCH_ALLOW="branch2,feature/fa"`| This variable is overriden by `BRANCH_ALLOW_REGEX` and will be ignored |
|`BRANCH_DENY=master,release`| Commits in branches "master" and "release" will not be pushed to the remote git repo.|

### per-project configuration
**bcgithook** allows for different configuration per-project. For this to happen a file with the same name as the project having the `.conf` suffix should be placed in `$HOME/.bcgithook` directory. `default.conf` can be used as a template however only values that are different from `default.conf` need to be defined. For example, a project named "FormApplicationProcess" would use the `FormApplicationProcess.conf` configuration file if that file is found.

> Please follow case sensitivity rules for your operating system when naming configuration files.

For new projects you can create the configuration beforehand so when BusinessCentral creates them the project specific configuration will be used automatically. That way different projects created in BusinessCentral can be associated to different repositories.

Please note that projects imported in Business Central will always be associated with the git repository they were imported from.

## Installation
Please execute the `install.sh` script providing the directory of your [JBoss EAP](https://developers.redhat.com/products/eap/overview) or [WildFly](https://wildfly.org/) installation (a.k.a `JBOSS_HOME`). The script assumes standard directory layout and will perform the following steps:

> **IMPORTANT** : Please make sure that JBoss EAP or WildFly is not running before you execute following steps of run the `install.sh` script.

> If your installation does not follow standard directory layout, i.e. the result of extracting the EAP or WildFly ZIP file, please execute manually the steps outlined below

* Create default configuration in file `$HOME/.bcgithook/default.conf`. Missing directories will be created.
* Modify [JBoss EAP](https://developers.redhat.com/products/eap/overview) or [WildFly](https://wildfly.org/) configuration in `JBOSS_HOME/standalone/configuration/standalone.xml` by adding the following system property
```xml
<property name="org.uberfire.nio.git.hooks" value="${jboss.home.dir}/git-hooks"/>
```
* Create the `JBOSS_HOME/git-hooks` directory
* Copy the `scripts/post-commit.sh` script into the `JBOSS_HOME/git-hooks/post-commit` directory
* Modify the contents of **bcgithook** configuration in `$HOME/.bcgithook/default.conf` to match your needs before starting JBoss EAP or WildFly.

> **bcgithook** can be installed at anytime after Business Central is used, but post-commit git hooks will only be applied to projects created (or imported) after *bcgithook* installation

## Notes on Git Repos
### GitLab
Pushing to GitLab works without any additional configuration.
When a project is created in Business Central it will be pushed to a same-named repository to Gitlab.

Example configuration:
```bash
GIT_TYPE=""
GIT_USER_NAME=gitlab_id
GIT_PASSWD=passwd
GIT_URL='https://gitlab.com/<gitlab_id>'
```
replace `gitlab_id` with your GitLab Id. Do not put a trailing `/` in the `GIT_URL`. By not specifying a specific project in `GIT_URL` you can reuse the configuration for multiple projects.

### GitHub
Create the repository to GitHub before trying to push to it. The repository should be created empty without README, license or `.gitginore` file. Once the repository is created at GitHub a project with the same name can be created at Business Central and it will be pushed to GitHub

Example configuration:
```bash
GIT_TYPE=""
GIT_USER_NAME=github_id
GIT_PASSWD=passwd
GIT_URL='https://github.com/<github_id>'
```
replace `github_id` with your GitHub Id. Do not put a trailing `/` in the `GIT_URL`. By not specifying a specific project in `GIT_URL` you can reuse the configuration for multiple projects.

No operation should be performed in the repository after it has been created. For example, do not create a file and delete it afterwards. Even if the repository looks empty the git version would have changed preventing Business Central from synchronizing with it.

### Gitea
For a localhost installation of Gitea `ENABLE_PUSH_CREATE_USER` must be set to `true` to allow the corresponding repository to be created at the time the project is created in Business Central.
Unless otherwise configured the file to be modified is `custom/conf/app.ini`
The [Gitea Cheat Sheet](https://docs.gitea.io/en-us/config-cheat-sheet/) provides additional guidance.

Example configuration:
```bash
GIT_TYPE=""
GIT_USER_NAME=gitea_id
GIT_PASSWD=passwd
GIT_URL='https://localhost:3000.com/<gitea_id>'
```
replace `gitea_id` with your Gitea Id. Do not put a trailing `/` in the `GIT_URL`. By not specifying a specific project in `GIT_URL` you can reuse the configuration for multiple projects.

### Azure DevOps
Create the repository in Azure DevOps before trying to push to it. The repository should be created empty without README, license or `.gitginore` file. Once the repository is created at Azure DevOps a project with the same name can be created at Business Central and it will be pushed to Azure DevOps

Example configuration:
```bash
GIT_TYPE="azure"
GIT_USER_NAME=azure_id
GIT_PASSWD=passwd
GIT_URL='https://<azure_id>@dev.azure.com/<azure_id>/<organisation>/_git'
```
`GIT_TYPE` should be set to **azure** for Azure DevOps. Leave blank or empty for all other types of Git repos.
Replace `azure_id` , `organisation` with the appropriate values. Do not put a trailing `/` in the `GIT_URL`. By not specifying a specific project in `GIT_URL` you can reuse the configuration for multiple projects.

No operation should be performed in the repository after it has been created. For example, do not create a file and delete it afterwards. Even if the repository looks empty the git version would have changed preventing Business Central from synchronizing with it.

### Bitbucket
Create the repository to Bitbucket before trying to push to it. The repository should be created empty without README, license or `.gitginore` file. Once the repository is created at Bitbucket a project with the same name can be created at Business Central and it will be pushed to Bitbucket

Example configuration:
```bash
GIT_TYPE=""
GIT_USER_NAME=bitbucket_id
GIT_PASSWD=passwd
GIT_URL='https://<bitbucket_id>@bitbucket.org/<bitbucket_id>'
```
replace `bitbucket_id` with your Bitbucket Id. Do not put a trailing `/` in the `GIT_URL`. By not specifying a specific project in `GIT_URL` you can reuse the configuration for multiple projects.

No operation should be performed in the repository after it has been created. For example, do not create a file and delete it afterwards. Even if the repository looks empty the git version would have changed preventing Business Central from synchronizing with it.

## Compatibility
**bcgithook** should be compatible with all versions of [RHPAM](https://developers.redhat.com/products/rhpam/overview), [jBPM](https://www.jbpm.org/) and [Drools](https://www.drools.org/) but it had only been tested with the following:
* RHPAM, versions 7.4, 7.4.1, 7.5, 7.6 and 7.7

## Other Implementations

Other implementations providing git hook support for Business Central are:
* [bc-git-integration-push](https://github.com/porcelli/bc-git-integration-push) Java based

## Links

* BA CoP Trello board: https://trello.com/c/3rhn7EyB

> Written with [StackEdit](https://stackedit.io/).
