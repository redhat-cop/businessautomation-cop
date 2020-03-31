# Feature File Tool Back End

## Summary
Back end, written in nodejs / express, to Feature File Maker tool Front End

* pulls copies of remote repos and stores them locally

* parses repos and pulls out lists of feature files

* parses feature files and extracts lists of scenarios, Given/When/Then statements and Data Tables

* serves to front end:
  * lists of feature files, 
  * lists of scenarios, 
  * lists of Given/When/Then statements with accompanying Data Tables, 
  * specific feature files, 
  * specific scenarios.

* saves new and updated feature files into a new commit and pushes to remote repos

## Remote Repository Setup

Remote repository details must be configured/ hardcoded in `lib/remoteRepoSetup.js`

For more details or alternative authorisation methods see `nodegit` docs at  https://www.nodegit.org/guides/

For each remnote Repo, need to enter:
* id - unique id
* name - unique name that will be presented in the front end ui
* url - repo location
* branch - branch to use for pulls and commits
* credentials: `basic` or `ssh_file`


  * `basic`

    use plaintext username / password

    ```
      credentialsType: 'basic',
      username: process.env.GITHUB_USERNAME,
      password: process.env.GITHUB_PASSWORD
    ```
 
  * `ssh_file`
  
    use pathname to ssh private and public key files, for example: `/Users/rh/.ssh/id_rsa` and `/Users/rh/.ssh/id_rsa.pub`

    ```
      credentialsType: 'ssh_file',
      username: 'git',
      publicKeyContents: fs.readFileSync(process.env.SSH_PUBLICKEY_PATH_FMGITHUB, {
        encoding: "ascii"
      }),
      privateKeyContents: fs.readFileSync(process.env.SSH_PRIVATEKEY_PATH_FMGITHUB, {
        encoding: "ascii"
      })
    ```

`basic` example:
```
remoteRepo = {
  id: '00003',
  name: 'Red Hat Consulting Maven Java Demo (demo-1 branch)',
  url: 'ssh://git@gitlab.consulting.redhat.com:2222/fmahon/feature-file-maven-java-demo.git',
  branch: 'demo-1',
  credentialsType: 'ssh_file',
  username: 'git',
  publicKeyContents: fs.readFileSync(process.env.SSH_PUBLICKEY_PATH_RHGITLAB, {
    encoding: "ascii"
  }),
  privateKeyContents: fs.readFileSync(process.env.SSH_PRIVATEKEY_PATH_RHGITLAB, {
    encoding: "ascii"
  })
}
```

`ssh_file` example:
```
remoteRepo = {
  id: '00002',
  name: 'local-bitbucket-2',
  url: 'http://localhost:7990/scm/feat/bdd-test-app2.git',
  branch: 'master',
  credentialsType: 'basic',
  username: 'fmahon',
  password: 'F33dHenry.'
}
```

## Local copies of remote repos after Git Pull

Remote repos are git pulled and stored locally in directory `LOCAL_REPO_DIR/remoteRepo.name`  where _LOCAL_REPO_DIR_ is either hardcoded in `lib/gitIntegrate.js` or supplied as env variable.

remoteRepo.name is hardcoded in the repo object in `lib/remoteRepoSetup.js`

  `name: 'Red Hat Consulting Maven Java Demo (demo-1 branch)`

## Environment Variables

Env variables can be used to configure local folder for storing git repos and for credentials. For exaample, vscode `launch.json`:

* LOG_LEVEL: silly | debug | verbose | info | warn | error

* LOCAL_REPO_DIR

* APP_ENV: dev | prod


```
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "node",
            "request": "launch",
            "name": "Launch Program",
            "skipFiles": [
                "<node_internals>/**"
            ],
            "program": "${workspaceFolder}/app.js",
            "outputCapture": "std",
            "console":"internalConsole",
            "env":{
                "GITHUB_USERNAME":"username",
                "GITHUB_PASSWORD":"password",
                "SSH_PRIVATEKEY_PATH_RHGITLAB":"/Users/rh/.ssh/id_rsa",
                "SSH_PUBLICKEY_PATH_RHGITLAB":"/Users/rh/.ssh/id_rsa.pub",
                "SSH_PRIVATEKEY_PATH_FMGITHUB":"/Users/rh/.ssh/id2_rsa",
                "SSH_PUBLICKEY_PATH_FMGITHUB":"/Users/rh/.ssh/id2_rsa.pub",
                "LOG_LEVEL":"silly",
                "LOCAL_REPO_DIR":"./remote-repo-copies",
                "APP_ENV":"dev"
            }
        }
    ]
}
```

## Endpoints
Endpoints are coded in `routes/remoteRepo.js`

* **GET remoteRepo/listRepos** 
    * returns list of all remote repos hardcoded in `lib/remoteRepos.js`

* **GET remoteRepo/importRemoteRepo?repoId=00001** 
    * pulls in repo from remote location, parses and serve lists of feature files, scenarios, given/when/then/data tables.  
    * The repoId needs to be passed in as a parameter

* **GET remoteRepogetFeatureFile?fileName=manager-user.feature&pathName=src/test/resources/com/mycompany/app/&repoId=00001**
    * returns a specific feature file
    * filename, pathName and repoId need to be passed in as parameters

* **POST remoteRepo/saveFeatureFile** - saves a specific feature file
        ```
        example body

        {
            "fileName": "Test",
            "pathName": "src/test/resources/com/mycompany/app/",
            "repoId": "00002",
            "contents": "Feature: This is a Test Feature File\n\n  Scenario: This is a scenario\n    Given I am tester\n    When I test\n    Then I get result",
            "isNew": true
        }
        ```

* **POST remoteRepo/forceGitResett?repoId=00001** 
    * deletes local copy of remote repo and then pulls a fresh copy
    * The repoId needs to be passed in as a parameter
   
        ```
        required body: 

        {
            "reset":true
        }
        ```

## Development / Runtime Environment

node version v8.11.3 or higher

run `npm install` to install dependencies.

run `npm test` to run unit tests.

run `npm dev` to start development server

run `npm start` to start server


## Running locally

`npm start` - App starts on `http://localhost:3000`


```
GITHUB_USERNAME=username \
GITHUB_PASSWORD=password \
SSH_PUBLICKEY_PATH_FMGITHUB='/Users/rh/.ssh/id_rsa.pub' \
SSH_PRIVATEKEY_PATH_FMGITHUB='/Users/rh/.ssh/id_rsa' \
SSH_PUBLICKEY_PATH_RHGITLAB='/Users/rh/.ssh/id2_rsa.pub' \
SSH_PRIVATEKEY_PATH_RHGITLAB='/Users/rh/.ssh/id2_rsa' \
npm start
```


## Testing
Unit tests for the feature file parser functions are contained in `test/parserfeatureFile.test.js`

Test files and objects are stored in 'test/testData'


## Plugins

Git Integration carried out using nodegit plugin
* https://www.nodegit.org/
* https://github.com/nodegit/nodegit


## Further Development ?
* add some middleware security for endpoints
* support more auth methods for remote repos
* cache responses in redis ?
* remove old local repos with cron job ?
* how ot handle multiple users /collisions
* how to handle git merge fails
* how to handle git push fails

