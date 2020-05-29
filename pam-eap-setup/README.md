
# pam-setup

Makes installation of multi-node PAM on non-OCP EAP nodes trivial.
Configurable through command line parameters. Tested on Linux and Cygwin.
The installation is geared towards creating an environment suitable for developing PAM-based applications.
EAP will be installed in standalone mode.

[CHANGELOG](CHANGELOG)

Supported configurations:

- Business Central and multiple or single KIE Sever nodes on localhost
- Business Central and multiple KIE Server nodes on different hosts

Supported (i.e. tested) versions:

- EAP 7.2
  - patch level 7.2.1 supported for PAM.7.3.1
  - patch level 7.2.2 supported for PAM.7.4
  - patch level 7.2.x onwards supported for 7.5 and later
- PAM versions 7.2, 7.3, 7.3.1, 7.4, 7.5, 7.5.1, 7.6.0, 7.7.0
- DM version 7.3.1, 7.4.1, 7.6.0

For details about node configuration check out [Nodes Configuration](#nodes-configuration) section at the end of this document. Also:

- [Install location](#install-location)
- [Enabling DEBUG logging](#enabling-debug-logging)
- [Configuring an Oracle datasource](#configuring-an-oracle-datasource)

## Usage Scenarios

### Single node installation with Business Central and KIE Server

    ./pam-setup.sh -n localhost:8080 -b both
    +--------------------------------+
    |   EAP                          |
    |                                |
    | +-----------+ +------------+   |
    | |           | |            |   |
    | | Business  | | KIE Server |   |
    | | Central   | |            |   |
    | |           | |            |   |
    | +-----------+ +------------+   |
    |                                |
    +--------------------------------+

### Multi node PAM installation on same host

    ./pam-setup.sh -n localhost:8080 -b multi=3
    +--------------------------------------------------------------------+
    | EAP                                                                |
    |                                                                    |
    | +--- standalone -------------+  +- node 2 -----+  +- node 3 -----+ |
    | |+-----------+ +------------+|  |+------------+|  |+------------+| |
    | ||           | |            ||  ||            ||  ||            || |
    | || Business  | | KIE Server ||  || KIE Server ||  || KIE Server || |
    | || Central   | |            ||  ||            ||  ||            || |
    | ||           | |            ||  ||            ||  ||            || |
    | |+-----------+ +------------+|  |+------------+|  |+------------+| |
    | +- :8080 --------------------+  +- :8180 ------+  +- :8280 ------+ |
    |                                                                    |
    +--------------------------------------------------------------------+
Three nodes will be created out of a shared EAP installation.

- "standalone" node is equivalent to "both" mode of installation hosting a Business Central and a KIE Server
- Nodes 2 and 3 are equivalent to "kie" mode of installation each hosting a single KIE Server

Offset on nodes will be auto-assigned.
All KIE Servers share the same KIE Server ID and are thus managed as a group from Business Central

### Multi node PAM installation on different hosts

       ./pam-setup.sh -n 192.168.1.20:8080 -b controller
       ./pam-setup.sh -n 192.168.1.21:8080 -b kie -c 192.168.1.20:8080
       ./pam-setup.sh -n 192.168.1.22:8080 -b kie -c 192.168.1.20:8080
       ./pam-setup.sh -n 192.168.1.23:8080 -b kie -c 192.168.1.20:8080
                             +---------------+
                             |  EAP          |
                             |  xx.xx.1.20   |
                             |               |
                             | +-----------+ |
                             | |           | |
                             | | Business  | |
                             | | Central   | |
                             | |           | |
                             | +-----------+ |
                             +---------------+
                                     |
                                     |
                +-----------------------------------------+
                |                    |                    |
                |                    |                    |
        +-------+--------+   +-------+--------+   +-------+--------+
        | EAP node 1     |   | EAP node 2     |   | EAP node 3     |
        | xx.xx.1.21     |   | xx.xx.1.22     |   | xx.xx.1.23     |
        |                |   |                |   |                |
        | +------------+ |   | +------------+ |   | +------------+ |
        | |            | |   | |            | |   | |            | |
        | | KIE Server | |   | | KIE Server | |   | | KIE Server | |
        | |            | |   | |            | |   | |            | |
        | +------------+ |   | +------------+ |   | +------------+ |
        +----------------+   +----------------+   +----------------+
All KIE Servers share the same KIE Server ID and are thus managed as a group from Business Central

## Prerequisites

Download binaries and place them in the same directory as this script. Most recent version will be used depending on the files present.

For EAP.7.2 the file "*jboss-eap-7.2.0.zip*" is required.

- EAP.7.2 patching is supported. To enable patching place either the latest patch file, for example "`jboss-eap-7.2.8-patch.zip`", in the same directory as the script. The script will use the latest patch version available.
- More information about supported configurations can be found at <https://access.redhat.com/articles/3405381>

Depending on PAM version the following files are required:

        |PAM Version| Files                                                                             |
        |-----------|-----------------------------------------------------------------------------------|
        | 7.2       | rhpam-7.2.0-business-central-eap7-deployable.zip, rhpam-7.2.0-kie-server-ee7.zip  |
        | 7.3       | rhpam-7.3.0-business-central-eap7-deployable.zip, rhpam-7.3.0-kie-server-ee8.zip  |
        | 7.3.1     | rhpam-7.3.1-business-central-eap7-deployable.zip, rhpam-7.3.1-kie-server-ee8.zip  |
        | 7.4       | rhpam-7.4.0-business-central-eap7-deployable.zip, rhpam-7.4.0-kie-server-ee8.zip  |
        | 7.5       | rhpam-7.5.0-business-central-eap7-deployable.zip, rhpam-7.5.0-kie-server-ee8.zip  |
        | 7.5.1     | rhpam-7.5.1-business-central-eap7-deployable.zip, rhpam-7.5.1-kie-server-ee8.zip  |
        | 7.6.0     | rhpam-7.6.0-business-central-eap7-deployable.zip, rhpam-7.6.0-kie-server-ee8.zip  |
        | 7.7.0     | rhpam-7.7.0-business-central-eap7-deployable.zip, rhpam-7.7.0-kie-server-ee8.zip  |

        |DM Version| Files                                                                           |
        |----------|---------------------------------------------------------------------------------|
        | 7.3.1    | rhdm-7.3.1-decision-central-eap7-deployable.zip, rhdm-7.3.1-kie-server-ee8.zip  |
        | 7.4.1    | rhdm-7.4.1-decision-central-eap7-deployable.zip, rhdm-7.4.1-kie-server-ee8.zip  |
        | 7.6.0    | rhdm-7.6.0-decision-central-eap7-deployable.zip, rhdm-7.6.0-kie-server-ee8.zip  |
        | 7.7.1    | rhdm-7.7.1-decision-central-eap7-deployable.zip, rhdm-7.7.1-kie-server-ee8.zip  |

## Usage

Invoke with no arguments for usage info:

        Will install PAM on a standalone EAP node or an EAP cluster. Execute this script on each
        node that will be part of the cluster.

        usage: pam-setup.sh [-h help]
                             -n ip[:node]
                             -b [kie|controller|both|multi=2...], defaults to 'both'
                             [-c ip1:port1,ip2:port2,...]
                             [-s smart_router_ip:port]
                             [-o option1=value1[:option2=value2...]]

        example: pam-setup.sh -n localhost

        Options:
            -n : the IP[:port] of the node it will operate on, default is localhost:8080

            -b : Configure PAM installation
                     kie : only the KIE ES component
              controller : only the Business Central component without the KIE ES
                    both : (default) full suite of PAM along with a separate KIE ES having
                           same node as controller, suitable for a development environment

                   multi : a multi-node managed KIE Server installation, with the following
                           configuration:
                           node 1 : 'both' mode installation, i.e. Business Central and KIE ES
                           node 2 : 'kie' mode installation, i.e. only the KIE ES
                           node 3 onwards: 'kie' mode installation

                           Startup scripts will be generated according to the number of nodes specified
                           Node 1 will use the IP:PORT specified with '-n' with each subsequent node
                           using a port offset of 100

            -c :  Manadatory for 'kie' mode of PAM installation, ignored in other modes
                  Specify list of controllers that this KIE ES should connect to.
                  List of controllers in the form of comma-sperated list of 'IP:PORT' pairs
                  e.g. 10.10.1.20:8080,192.168.1.34:8350

            -s : Only for KIE ES, optional. Specify Smart Router location, eg 10.10.1.23:9000

            -o : Specify additional options. To specify multiple options use ':' as separator. 
                 Example: '-o option1=value1[:option2=value2:...]'

                 Supported options are:

                 - nodeX_config=file : declare file with additional commands to be applied by
                                       EAPs jboss-cli tool for each node installed
                                       X stands for the number of each node, e.g. node1_config, node2_config, etc

                 - debug_logging     : if present will set logging level to DEBUG

                 - dump_requests     : if present will enable request dumping to log file

                                       WARNING: enabling debug_logging and dump_requests
                                       can generate copious amount of output and can have
                                       significant impact on perforance

                 - install_dir       : Installation directory. Defaults to 'pam7'.
                                       If specified, installation will first happen to default location
                                       and then moved to this one.

                 - jvm_memory        : Configures the '-Xmx' parameter of JVM. Number is assumed to imply MB.
                                       Example 'jvm_memory=4096' will be '-Xmx4096m'

                Configuring an Oracle datasource

                 - ojdbc_location    : location of the Oracle JDBC driver
                                       Example to "$PWD/oracle_jdbc_driver/ojdbc8.jar"

                 - oracle_host,      : These variables are used for bulding the Oracle JDBC connection URL
                   oracle_port,        which is of the form
                   oracle_sid             jdbc:oracle:thin:@//ORACLE_HOST:ORACLE_PORT/ORACLE_SID

                 - oracle_user       : The user name to be used for connecting to Oracle DB

                 - oracle_pass       : The password for the Oracle user

                                       WARNING
                                       To properly configure an Oracle datasource all Oracle related
                                       parameters need to be specified

            -h : print this message

        Notes:
          - If a file named settings.xml is found in the current directory during
            installation it will be set as custom maven settings through the use of
            kie.maven.settings.custom system property.
            The file will be copied to EAP_HOME and the system property will point to
            that.

          - The following users will be added as part of the installation:

              User            Role
              ----            ----
              admin           EAP admin
              pamAdmin        kie-server,rest-all,admin,analyst,kiemgmt,manager,user,developer,process-admin
              pamAnalyst      analyst
              pamDeveloper    developer
              pamUser         user
              kieServerUser   kie-server,rest-all
              controllerUser  kie-server,rest-all

            Passwords for these users will be printed at the end of the installation in stdout

A summary is provided at installation end with the settings used. For example, a multi-node PAM installation on same host will result in the following summary:

    :: PROCEEDING WITH PAM731
    :: PAM Installation mode : multi with 3 nodes
    :: Installing EAP at jboss-eap-7.2 using jboss-eap-7.2.0.zip
    :: Installing node1
    :: Installing node2
    :: Installing node3
    --- Installation Summary ---

           PAM Installation mode : [ multi with 3 nodes ]
           Using Controller List : [ http://localhost:8080 ]
     Using Smart Router location : [ NOT INSTALLED ]
             Installed EAP using : [ jboss-eap-7.2.0.zip ]
            EAP install location : [ jboss-eap-7.2 ]
    --- Node instalation node1
             Installed PAM using : [ rhpam-7.3.1-business-central-eap7-deployable.zip ]
            Installed KIE SERVER : [ rhpam-7.3.1-kie-server-ee8.zip ]
            Added EAP admin user : [ admin / ***** ]
            Added PAM admin user : [ pamAdmin / ***** ]
           Added KIE Server user : [ kieServerUser / ***** ]
          Added Conntroller user : [ controllerUser / ***** ]
                  Startup script : [ gopam.sh ]
    --- Node instalation node2
            Installed KIE SERVER : [ rhpam-7.3.1-kie-server-ee8.zip ]
            Added EAP admin user : [ admin / ***** ]
            Added PAM admin user : [ pamAdmin / ***** ]
           Added KIE Server user : [ kieServerUser / ***** ]
          Added Conntroller user : [ controllerUser / ***** ]
                  Startup script : [ gonode2.sh ]
    --- Node instalation node3
            Installed KIE SERVER : [ rhpam-7.3.1-kie-server-ee8.zip ]
            Added EAP admin user : [ admin / ***** ]
            Added PAM admin user : [ pamAdmin / ***** ]
           Added KIE Server user : [ kieServerUser / ***** ]
          Added Conntroller user : [ controllerUser / ***** ]
                  Startup script : [ gonode3.sh ]

## Nodes configuration

The script will configure the installation with the following settings:

- Will use `standalone-full-ha.xml` instead of `standalone.xml` by renaming files. Original files are also kept.
- Sampe startup scripts are generated for each node installed
- KIE Servers installed will all have the ID `remote-kieserver` and managed as a group from Business Central
- EAP logging will be modified for CONSOLE output along with the regular FILE output
- GIT, Maven and Indexer directories will be relative to the base dir of the node, i.e. `$JBOSS_HOME/standalone` or`$JBOSS_HOME/node2` depending on node created.
BC or KIE nodes will be created using the same EAP base installation, as per <https://access.redhat.com/solutions/350683>
- Will use a custom `settings.xml` file based on <https://access.redhat.com/maven-repository>
- Passwords are secured with a keystore. More information at : <https://access.redhat.com/solutions/3669631>
- Identify workbench as DecisionCentral instead of BusinessCentral for RHDM installations, <https://access.redhat.com/solutions/4840041>
- node-identifier property in EAP configured as per <https://access.redhat.com/solutions/721613>
- ActiveMQ Artemis disk threshold increased as per <https://access.redhat.com/solutions/4390511> to better handle near full disks
- Enable CORS for first four nodes of KIE Server as per <https://access.redhat.com/solutions/4036301> and <https://access.redhat.com/solutions/3713131>

### Install location

By default PAM will be installed in a directory named `pam` in the current directory. `pam` will be created if it does not exist.  Installation location can be overriden by the `install_dir` option. The startup script will be named after the installation location with a `go_` prefix.

Example:
  - `./pam-setup.sh` will install PAM in a directory named `pam` and the startup script will be named `go_pam.sh`
  - `./pam-setup.sh -o install_dir=wick` will install PAM in a directory named `wick` and the startup script will be named `go_wick.sh`

### Enabling DEBUG logging

Enabling `debug_logging` will have the effect of placing the following defintions in your `standalone.xml` file.

```
<logger category="org.jboss.as.domain">
    <level name="TRACE"/>
</logger>
<logger category="org.wildfly.security">
    <level name="TRACE"/>
</logger>
<root-logger>
    <level name="DEBUG"/>
    <handlers>
        <handler name="FILE"/>
        <handler name="CONSOLE"/>
    </handlers>
</root-logger>
```

This will also log login related events. The combined effect will be quite extensive amount of logging that could have a detrimental effect to performance. It is recommended that for normal operations the logging level is switched to informational level by replacing the `DEBUG` directive above with `INFO`.


### Configuring an Oracle datasource

By specifying the Oracle related parameters an Oracle datasource will be configured. Example invocation:

```

OJDBC_LOCATION=$PWD/oracle_jdbc_driver/ojdbc8.jar
ORACLE_HOST=hostname_or_ip
ORACLE_PORT=port_to_connect_to
ORACLE_SID=oracle_sid
ORACLE_USER=oracle_user
ORACLE_PASS=oracle_password

./pam-setup.sh -b both -o ojdbc_location=$OJDBC_LOCATION:\
                          oracle_host=$ORACLE_HOST:oracle_port=$ORACLE_PORT:\
                          oracle_sid=$ORACLE_SID:\
                          oracle_user=$ORACLE_USER:oracle_pass=$ORACLE_PASS
```

All parameters are required for the configuration to be succesful. The Oracle JDBC URL that will be configured is of the following format:

```
jdbc:oracle:thin:@//ORACLE_HOST:ORACLE_PORT/ORACLE_SID
```

The OJDBC driver must be provided and placed in a directory readble by the installation script. Please make sure that the version of OJDBC driver you are providing matches the version of the Oracle database you are using.

The datasource will be named `OracleDS` and will be configured to use the `oracle.jdbc.xa.client.OracleXADataSource` class for connections. The [When RHPAM is configured to use Oracle as a database, the first start-up will work fine but every subsequent restart will show ORA-00955 and ORA-02275 in the server.log file](https://access.redhat.com/solutions/3494221), KB3494221, article is also taken into account.

System properties set also include

- `org.kie.server.persistence.ds` set to `java:jboss/OracleDS`
- `org.kie.server.persistence.dialect` set to `org.hibernate.dialect.Oracle10gDialect`

---
> Written with [StackEdit](https://stackedit.io/).
> ASCII charts with the help of [ASCIIFlow](http://asciiflow.com/)
