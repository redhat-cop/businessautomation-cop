const Git = require("nodegit");
const FA = require('fasy');  // used for concurrent async mapping
const _ = require('lodash')
const path = require("path");
const fse = require("fs-extra");

const log = require('../lib/logger').logger;
const parser = require('./parseFeatureFile')
const remoteRepos = require('../lib/remoteRepoSetup');

const LOCAL_REPO_DIR = process.env.LOCAL_REPO_DIR || './remote-repo-copies';

function getRepoDetails(repoId) {
  const repoDetails = remoteRepos.getRepo(repoId)
  if (!repoDetails) {
    throw new Error('Error: Unable to find remote repository details for id: ' + repoId)
  } else {
    return repoDetails;
  }
}

// comparator for returning unique given / when/ then steps
// 1. compare statement text
// 2. when datatables present on both comapre header/title row of data table 
function areSame(a, b) {
  return a.text === b.text &&
    (!a.dataTable || !b.dataTable) ||
    (
      a.dataTable && b.dataTable &&
      _.isEqual(a.dataTable[0], b.dataTable[0])
    )
}

async function checkoutBranch(repo, branch) {
  try {
    //try to checkout branch, it will fail if branch not found
    log.debug('checkout branch ')
    await repo.checkoutBranch(branch)
  }
  catch (err) {
    // branch doesn't exist, create iy
    log.debug('checkout branch message', err)
    log.debug('creating new branch... ')

    // get reference to last commit on the remote branch
    const reference = await repo.getBranch('refs/remotes/origin/' + branch)
    await repo.checkoutRef(reference);
    const targetCommit = await repo.getHeadCommit()
    await repo.createBranch(branch, targetCommit, true)
    await repo.checkoutBranch(branch)
  }
}


// call with the repodetails object and this returns a credentials callback function 
// configured with the with the current user credential;s
function returnCredsCallback(repoDetails) {

  return {
    credentials: (url, userName) => {

      if (repoDetails.credentialsType === 'ssh_file') {
        // git authenticate using ssh and reading local ssh private/public key files 
        return Git.Cred.sshKeyMemoryNew(
          repoDetails.username,
          repoDetails.publicKeyContents,
          repoDetails.privateKeyContents,
          "").then(function (cred) {
            log.info('returning ssh creds')
            return cred
          });
      }
      // default to git username / password
      log.info('returning plaintext creds')
      return Git.Cred.userpassPlaintextNew(repoDetails.username, repoDetails.password)
    }
  }
}


// clone external repo and save copy locally  in LOCAL_REPO_DIR
// or get reference to lcoal repo if has already been cloned
async function getRepo(repoId) {
  const repoDetails = getRepoDetails(repoId)

  const cloneOptions = {};
  cloneOptions.fetchOpts = {
    callbacks: {
      certificateCheck: () => 0,
      credentials: returnCredsCallback(repoDetails).credentials
    }
  }

  try {
    // clone external repo.  If it's already there it throws an error and goes to catch block 
    const repo = await Git.Clone(repoDetails.url, path.join(LOCAL_REPO_DIR, repoDetails.name), cloneOptions)
    if (repo) {
      const commit = await repo.getBranchCommit(repoDetails.branch);
      log.info(new Date() + `: Clone successful - Latest Commit after Clone:  ${commit.sha()} - ${commit.message()}`);
    }
    return repo;
  }
  catch (err) {
    // external repo has already been cloned, get a reference to the local clone
    const repo = await Git.Repository.open(path.join(LOCAL_REPO_DIR, repoDetails.name));
    // fetch and merge from remote to make sure we are up to date
    await repo.fetch('origin', { callbacks: returnCredsCallback(repoDetails) })

    // switch branch if necessary
    await checkoutBranch(repo, repoDetails.branch)
    // merge remote
    log.debug('Merging to branch ')
    await repo.mergeBranches(repoDetails.branch, "origin/" + repoDetails.branch)
    const commit = await repo.getBranchCommit(repoDetails.branch)
    log.info(`Latest commit after merge:  ${commit.sha()} - ${commit.message()}`);

    return repo
  }
}


// return list of feature file objects containing name, path and file contents for each .feature file
async function importRemoteRepo(repo, repoId) {
  const repoDetails = getRepoDetails(repoId)
  const HEAD = await repo.getBranchCommit(repoDetails.branch);
  const tree = await HEAD.getTree();
  const treeEntries = tree.entries();

  let featureFiles = [];
  const stepsObj = {};
  stepsObj.givenSteps = [];
  stepsObj.whenSteps = [];
  stepsObj.thenSteps = [];
  let scenarios = [];

  // reference https://gist.github.com/getify/f5b111381413f9d9f4b2571c7d5822ce
  // iterate through file system and extract .feature file details
  // return:
  //  featurfiles - array of filenames / filepaths of all .feature files
  //  scenarios - array of scenarioNames / filenames / filepaths for all scenarios
  //  steps - step name and datatable for all Given / When / Thens
  await FA.concurrent.flatMap(async function getContents(entry) {
    if (entry.isFile() && entry.name().indexOf('.feature') > -1) {
      const fileText = (await entry.getBlob()).toString();
      const featureFileRef = {
        fileName: entry.name(),
        pathName: entry.path().replace(entry.name(), '')
      };
      featureFiles.push(featureFileRef);
      // call to extract Steps details
      const stepsIn = parser.extractStepsAndScenarios(fileText);
      stepsObj.givenSteps = [...stepsObj.givenSteps, ...stepsIn.given];
      stepsObj.whenSteps = [...stepsObj.whenSteps, ...stepsIn.when];
      stepsObj.thenSteps = [...stepsObj.thenSteps, ...stepsIn.then];
      scenarios.push(
        ...stepsIn.scenarios.map((s) => {
          return {
            scenarioName: s,
            fileName: entry.name(),
            pathName: entry.path().replace(entry.name(), '')
          }
        })
      );
    }
    else if (entry.isDirectory()) {
      let dirEntries = (await entry.getTree()).entries();
      return FA.concurrent.flatMap(getContents, dirEntries);
    }
  }, treeEntries);

  // remove duplicate steps and sort
  const stepsArr = ['givenSteps', 'whenSteps', 'thenSteps']
  for (let index = 0; index < stepsArr.length; index++) {
    stepsObj[stepsArr[index]] = _.chain(stepsObj[stepsArr[index]])
      .uniqWith(areSame)
      .sortBy(['text'])
      .value()
  }

  return {
    featureFiles,
    givenSteps: stepsObj.givenSteps,
    whenSteps: stepsObj.whenSteps,
    thenSteps: stepsObj.thenSteps,
    scenarios
  };
}

// get specific .feature file from local copy of repo LOCAL_REPO_DIR
async function getFile(repo, repoId, fileName, pathName) {
  const repoDetails = getRepoDetails(repoId)
  const commit = await repo.getBranchCommit(repoDetails.branch);
  const file = await commit.getEntry(path.join(pathName, fileName))
  const blob = (await file.getBlob()).toString()
  return blob;
}


// save file to local repo in LOCAL_REPO_DIR
// create a commit and push to external repo
async function saveFile(repo, repoId, fileName, filePath, fileContent, isNew) {
  //save file
  const repoDetails = getRepoDetails(repoId)

  await fse.ensureDir(path.join(repo.workdir(), filePath));
  const writeFile = await fse.writeFile(
    path.join(repo.workdir(), filePath, fileName),
    fileContent
  );

  // add to index
  const index = await repo.refreshIndex();
  await index.addByPath(path.posix.join(filePath, fileName));
  await index.write();
  // stage
  const oid = await index.writeTree();
  const head = await Git.Reference.nameToId(repo, "HEAD");
  const commitParent = await repo.getCommit(head);
  var author = Git.Signature.now("Feature File Tool", "email@test.com");
  await repo.createCommit("HEAD", author, author, `${isNew ? 'Adding ' : 'Updating '} file ${fileName} + ${new Date()}`, oid, [commitParent]);
  const commit = await repo.getBranchCommit(repoDetails.branch);
  const remoteRepo = await repo.getRemote("origin");
  log.info(new Date() + `: Pushing commit  ${commit.sha()} - ${commit.message()}`)
  const push = await remoteRepo.push(
    ["refs/heads/" + repoDetails.branch + ":refs/heads/" + repoDetails.branch],
    {
      callbacks: returnCredsCallback(repoDetails)
    }
  );
  return push
}

function validateSave(repoId, fileName, pathName, contents) {
  if (!repoId || !remoteRepos.getRepo(repoId)) {
    throw new Error('Unable to find remote repo details for id: ' + repoId)
  }
  if (!fileName) {
    throw new Error('No file name in save POST request')
  }
  if (!pathName) {
    throw new Error('No path name in save POST request')
  }
  if (!contents) {
    throw new Error('No file contents in save POST request')
  }
}

// clone repo or get reference if has already been cloned
// TODO for existing reple update to check commit to mke sure it is most recent
async function forceReset(repoId) {
  const repoDetails = getRepoDetails(repoId)
  //save file
  await fse.remove(path.join(LOCAL_REPO_DIR, repoDetails.name))
  return { result: "local git repo removed successfully" }

}

module.exports = {
  getRepo,
  importRemoteRepo,
  saveFile,
  getFile,
  validateSave,
  forceReset
};
