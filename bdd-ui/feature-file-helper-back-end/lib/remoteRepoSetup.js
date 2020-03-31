
const _ = require('lodash')
const fs = require("fs");

//create list of hardcoded repos
const remoteReposList = []
let remoteRepo

// Github - plaintext username / password 
/*
remoteRepo = {
    id: '00001',
    name: 'private repo github - username/pw - demo-1 branch ',
    url: 'https://github.com/<repo path>',
    branch: 'demo-1',
    credentialsType: 'basic',
    username: process.env.GITHUB_USERNAME,
    password: process.env.GITHUB_PASSWORD
}
remoteReposList.push(remoteRepo)
*/

// Github - SSH 
/*
remoteRepo = {
    id: '00002',
    name: 'private github repo - ssh - demo-1 branch',
    url: 'git@github.com:finmahon/<repo path>',
    branch: 'demo-1',
    credentialsType: 'ssh_file',
    username: 'git',
    publicKeyContents: fs.readFileSync(process.env.SSH_PUBLICKEY_PATH_FMGITHUB, {
      encoding: "ascii"
    }),
    privateKeyContents: fs.readFileSync(process.env.SSH_PRIVATEKEY_PATH_FMGITHUB, {
      encoding: "ascii"
    })
}
remoteReposList.push(remoteRepo)
*/

// GITLAB - SSH 
/*
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
remoteReposList.push(remoteRepo)
*/

function listRepos() {
  return remoteReposList
}

function getRepo(id) {
  return _.find(remoteReposList, { id: id }) || null;
}


module.exports = {
  listRepos,
  getRepo
};
