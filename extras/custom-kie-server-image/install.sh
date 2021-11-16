#!/bin/bash

set -e

# import the common functions for installing modules and configuring drivers
source /usr/local/s2i/install-common.sh

# should be the directory where this script is located
injected_dir="$1"

# install configuraiton
cp "${injected_dir}"/configuration/standalone-openshift.xml "${JBOSS_HOME}"/standalone/configuration/standalone-openshift.xml

# install the JDBC client module
chmod -R ugo+rX "${injected_dir}"/modules
install_modules "${injected_dir}"/modules

chmod -R ugo+rX "${injected_dir}"/deployments
install_deployments "${injected_dir}"/deployments

# configure the JDBC driver in standalone-openshift.xml.  Driver is named "derby"
configure_drivers "${injected_dir}"/install.properties
