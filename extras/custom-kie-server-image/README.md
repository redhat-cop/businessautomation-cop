
# Build custom KIE Server image with Oracle JDBC Driver

The procedure for building a custom KIE Server image including Oracle JDBC driver is outlined. Using this procedure an extension to the base KIE Server image will be created and it will be integrated into it utilizing the docker layering mechanism through the S2I module.

The procedure has been tested against RHPAM.7.7.1, but should work on earlier version of RHPAM such as 7.7.0 and 7.6.0.


## Repository Structure

The repository has the following structure. 

```
.
├── build.sh                                   => main build script
├── install.sh                                 => will be used during the build process
├── oracle-driver-image
│   ├── Dockerfile                             => Dockerfile for building the KIE Extension image
│   ├── install.properties                     => to be used during the Oracle JDBC driver integration
│   ├── modules
│   │   └── system
│   │       └── layers
│   │           └── openshift
│   │               └── com
│   │                   └── oracle
│   │                       └── main           => Oracle JDBC module directory
│   └── ojdbc8.jar                             => Oracle JDBC driver
└── README.md                                  => this file

14 directories, 27 files
```

## Build Image

Make sure that you have logged in Openshift before executing the following:

```bash
./build.sh --artifact-repo=. \
           --namespace=NAMESACE \
           --registry=REGISTRY \
           --image-tag=VERSION_OF_BASE_KIE_SERVER_IMAGE \
           --env-target=ENV
```

and example of such an invocation would be:

```bash
./build.sh --artifact-repo=. \
           --namespace=propo \
           --registry=default-route-openshift-image-registry.apps.cluster-thurso.example.opentlc.com \
           --image-tag=7.7.1 \
           --env-target=prod
```



