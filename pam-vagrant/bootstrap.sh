#!/bin/bash

BASE_DIR=/vagrant

#
# - config vars
#
WGET_CMD='wget --no-check-certificate -q '

#
# disable SELinux
#
setenforce 0
cp /etc/sysconfig/selinux /etc/sysconfig/selinux.orig
cat << __SELNX_OFF > /etc/sysconfig/selinux
SELINUX=disabled
SELINUXTYPE=targeted
__SELNX_OFF

# dnf install -y wget
# dnf update -y
dnf install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm
#
# additional packages 
#
dnf -y install gcc java-1.8.0-openjdk java-1.8.0-openjdk-devel 
dnf -y install libssh2-devel openssl-devel openssl mod_ssl net-tools
dnf -y install maven.noarch git-all.noarch unzip zip jq

#
# configure SSHD to accept login by s/key
#
sed -i s/^ChallengeResponseAuthentication\ no/ChallengeResponseAuthentication\ yes/ /etc/ssh/sshd_config
systemctl restart sshd.service

#
# enable Apache HTTPD
#
systemctl enable httpd.service
systemctl start httpd.service

#
# Install Nexus
#
$BASE_DIR/nexus-install.sh
source /root/nexus.pass

#
# install EAP and PAM
#
for f in $BASE_DIR/*; do
  ln -s $f
done
$BASE_DIR/pam-setup.sh -b both -n localhost:8080
source /root/pam.version

#
# modifying settings.xml with the nexus password obtained
#
pushd jboss-eap-7.2 &> /dev/null
  sed -i "s/@@NEXUS_PASSWORD@@/${NEXUS_ROOT_PASS}/g" settings.xml
  sed -i "s/@@NEXUS_IP@@/127.0.0.1/g" settings.xml
  cp settings.xml $BASE_DIR/settings.xml.nexus
popd &> /dev/null

#
# systemd integration
#
$BASE_DIR/pam7-systemd-integration.sh

#
# Continue with RHPAM and BC setup
#
counter=0
goon=no
while [[ "$goon" == "no" ]]; do
  let counter=$((counter+1))
  if [[ ! -r /opt/jboss/standalone/deployments/business-central.war.deployed ]]; then
    echo "[ $counter ] Waiting for PAM to be fully deployed... will check again in 5 seconds..."
    sleep 5s
  else
    goon=yes
  fi
done

echo "PAM ${PAM_VERSION} along with Nexus Repository Manager have been installed"
echo
echo "Use http://localhost:8080/business-central   for PAM"
echo "    http://localhost:8081                    for Nexus"
echo
echo "Nexus admin password : [ ${NEXUS_ROOT_PASS} ] - do not change it"
echo
