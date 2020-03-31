const express = require('express');
const router = express.Router();

const log = require('../lib/logger').logger;
const git = require('../lib/gitIntegrate');
const remoteRepos = require('../lib/remoteRepoSetup');


// get feature files and Given/When/Then Statments
router.get('/listRepos', function (req, res, next) {
  const repos = remoteRepos.listRepos().map((r) => {
    return { id: r.id, name: r.name }
  })
  res.send(repos)
});

// get feature files and Given/When/Then Statments
router.get('/importRemoteRepo', function (req, res, next) {
  git.getRepo(req.query.repoId)
    .then((repo) => {
      log.info('cloned repo successfully ');
      return git.importRemoteRepo(repo, req.query.repoId)
    })
    .then(featureFiles => {
      log.info('found and parsed featureFiles ');
      log.silly('feature file obj', featureFiles)
      return res.send(featureFiles)
    })
    .catch(err => {
      log.error('Error in importRemoteRepo', err);
      next(err)
    });
});

// get feature files and Given/When/Then Statments
router.get('/getFeatureFile', function (req, res, next) {
  git.getRepo(req.query.repoId)
    .then((repo) => git.getFile(repo, req.query.repoId, req.query.fileName, req.query.pathName))
    .then(file => res.send(file))
    .catch(err => {
      log.error('Error in getFeatureFile ', err);
      next(err)
    });
});

// save feature file and commit back to external repo
router.post('/saveFeatureFile', function (req, res, next) {
  const repoId = req.body.repoId || null
  const fileName = req.body.fileName || null;
  let pathName = req.body.pathName || null;
  const contents = req.body.contents || null;
  const isNew = req.body.isNew || false;
  git.validateSave(repoId, fileName, pathName, contents)
  //TODO this won't work on windows
  pathName = pathName[pathName.length - 1] === '/' ? pathName.slice(0, pathName.length - 1) : pathName
  //TODO check if this works on Windows
  git.getRepo(repoId) // includes a git pull to get latest commits
    .then(repo => git.saveFile(repo, repoId, fileName, pathName, contents, isNew))
    .then(result => {
      log.info(`${new Date()} -  ${pathName}${fileName} saved ok`)
      res.send(
        {
          success: true,
          message: `${fileName} ${isNew ? 'Added' : 'Updated'}`,
          result
        })
    })
    .catch(err => {
      log.error('Error in saveFeatureFile', err);
      next(err)
    });
});


router.post('/forceGitReset', function (req, res, next) {
  const reset = req.body.reset || null;
  if (reset) {
    git.forceReset(req.query.repoId)
      .then(result => {
        log.info(`forceReset: local repo deleted ok`)
        res.send(result)
      })
      .catch(err => {
        log.error('Error in forceReset', err);
        next(err)
      });

  } else {
    throw new Error('Error forceReset - invalid body submitted')
  }
});


module.exports = router;
