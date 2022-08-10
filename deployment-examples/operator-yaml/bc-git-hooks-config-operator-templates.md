### Get the git-hook post-commit script & its config file from [the RHBA CoP repo](https://github.com/redhat-cop/businessautomation-cop/tree/master/extras/bcgithook/scripts)
```
wget https://raw.githubusercontent.com/redhat-cop/businessautomation-cop/master/extras/bcgithook/scripts/default.conf.example
wget https://raw.githubusercontent.com/redhat-cop/businessautomation-cop/master/extras/bcgithook/scripts/post-commit.sh
```

### Change the file name extensions
```
mv default.conf.example default.conf
mv post-commit.sh post-commit
vi default.conf # Provide the relevant values for the variables GIT_URL, GIT_USER_NAME, GIT_PASSWD
                # If the remote repo is on Github, set the "personal access token" to the variable GIT_PASSWD instead of password
```

### Create a config map that would hold the files "post-commit" and "default.conf" in it. 
```
oc project <<project-name>>
oc create configmap git-hooks --from-file=post-commit=post-commit --from-file=default.conf=default.conf
```

### If using BA operator 
#### In the CR, provide the mountPath (/opt/kie/data/git/hooks) where this configmap has to be mounted (console --> gitHooks)
```
apiVersion: app.kiegroup.org/v2
kind: KieApp
metadata:
  name: authoring-with-bc-hook
spec:
  commonConfig:
    adminPassword: P@ssw0rd
    adminUser: rhpamAdmin
  environment: rhpam-authoring
  useImageTags: true
  objects:
    console:
      gitHooks:
        mountPath: /opt/kie/data/git/hooks
        from:
          kind: ConfigMap
          name: git-hooks
      replicas: 1
    servers:
      - database:
          type: h2
        replicas: 1
        resources:
          limits:
            memory: 1Gi
          requests:
            memory: 1Gi
```


### If using templates
#### 1. provide the mount point path (/opt/kie/data/git/hooks) in the template env variable GIT_HOOKS_DIR. 
#### 2. mount the git-hooks directory to the DeploymentConfig
```
oc set volume dc/<app>-rhpamcentr --add --type configmap --configmap-name git-hooks  --mount-path=/opt/kie/data/git/hooks --name=git-hooks
```
