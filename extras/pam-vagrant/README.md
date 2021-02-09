![Build for pam-vagrant](https://github.com/redhat-cop/businessautomation-cop/workflows/Build%20for%20pam-vagrant/badge.svg)

# pam-vagrant

Will create a vagrant box named "PAM.7.vm" by installing EAP.7.2, RHPAM and Nexus.
PAM will be configured to use the Nexus repo manager.

PAM will be installed on a single EAP node with the following modules:
 - Business Central
 - One managed KIE Server named "remote-kieserver"

The following users will be created for accessing EAP and PAM modules
 - 'admin' as EAP administrator
 - 'pamAdmin' as Business Central administrator
 - 'kieServerUser' for accessing KIE Server
 - 'controllerUser' for KIE Server to access Business Central

Use 
 - http://localhost:8080/business-central to access PAM
 - http://localhost:8081 to access Nexus

RHPAM will be installed depending on the existence of the following files:

|PAM Version| Files  |
|--|--|
| 7.2 | rhpam-7.2.0-business-central-eap7-deployable.zip, rhpam-7.2.0-kie-server-ee7.zip  |
| 7.3 | rhpam-7.3.0-business-central-eap7-deployable.zip, rhpam-7.3.0-kie-server-ee8.zip  |
| 7.3.1 | rhpam-7.3.1-business-central-eap7-deployable.zip, rhpam-7.3.1-kie-server-ee8.zip  |
| 7.4 | rhpam-7.4.0-business-central-eap7-deployable.zip, rhpam-7.4.0-kie-server-ee8.zip  |
| 7.5 | rhpam-7.5.0-business-central-eap7-deployable.zip, rhpam-7.5.0-kie-server-ee8.zip  |
| 7.5.1 | rhpam-7.5.1-business-central-eap7-deployable.zip, rhpam-7.5.1-kie-server-ee8.zip  |
| 7.6.0 | rhpam-7.6.0-business-central-eap7-deployable.zip, rhpam-7.6.0-kie-server-ee8.zip  |
| 7.7.0 | rhpam-7.7.0-business-central-eap7-deployable.zip, rhpam-7.7.0-kie-server-ee8.zip  |
| 7.7.1 | rhpam-7.7.1-business-central-eap7-deployable.zip, rhpam-7.7.1-kie-server-ee8.zip  |
| 7.8.1 | rhpam-7.8.1-business-central-eap7-deployable.zip, rhpam-7.8.1-kie-server-ee8.zip  |

|DM Version| Files  |
|--|--|
| 7.3.1 | rhdm-7.3.1-decision-central-eap7-deployable.zip, rhdm-7.3.1-kie-server-ee8.zip  |
| 7.4.1 | rhdm-7.4.1-decision-central-eap7-deployable.zip, rhdm-7.4.1-kie-server-ee8.zip  |
| 7.6.0 | rhdm-7.6.0-decision-central-eap7-deployable.zip, rhdm-7.6.0-kie-server-ee8.zip  |
| 7.7.1 | rhdm-7.7.1-decision-central-eap7-deployable.zip, rhdm-7.7.1-kie-server-ee8.zip  |
| 7.8.1 | rhdm-7.8.1-decision-central-eap7-deployable.zip, rhdm-7.8.1-kie-server-ee8.zip  |

For EAP.7.2 the file `jboss-eap-7.2.0.zip` is required as well as the latest patch.

JBoss EAP.7.3, file `jboss-eap-7.3.0.zip`, along with the latest patch, is required for RHPAM.7.8.1 onwards 

At the end of the setup while the vagrant box is being fired up for the first time please wait until PAM is fully loaded to validate the installation. Depending on the local environment it could take up to a minute for PAM to be operational.


## Installation notes

- The Vagrant box launched will be named `PAM7.CentOS8` and relies on VirtualBox to be installed

- 4096MB of memory will be allocated to the VagrantBox. If you wish to increase this please modify accordingly [line 7](https://github.com/erouvas/businessautomation-cop/blob/e3e9e8dab24527df0711d49bd3baa310cdc00896/extras/pam-vagrant/Vagrantfile#L7) of the [Vagrantfile](https://github.com/erouvas/businessautomation-cop/blob/master/extras/pam-vagrant/Vagrantfile)


