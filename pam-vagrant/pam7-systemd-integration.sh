#!/bin/bash

INSTALL_DIR=/opt

#
# - Install PAM.7 as a systemd service
#
groupadd -r jboss
useradd -r -g jboss -d ${INSTALL_DIR}/jboss -s /sbin/nologin jboss
mv jboss-eap-7.2 ${INSTALL_DIR}
echo 'JAVA_OPTS="$JAVA_OPTS -Xmx2048m "' >> ${INSTALL_DIR}/jboss-eap-7.2/bin/standalone.conf
ln -s ${INSTALL_DIR}/jboss-eap-7.2 ${INSTALL_DIR}/jboss
cp ${INSTALL_DIR}/jboss-eap-7.2/settings.xml /etc/maven/settings.xml
chown -R jboss:jboss ${INSTALL_DIR}/jboss
chown -R jboss:jboss ${INSTALL_DIR}/jboss-eap-7.2
mkdir /etc/jboss

cat << __JBOSS > /etc/jboss/jboss.conf
# The configuration you want to run
JBOSS_CONFIG=standalone.xml

# The mode you want to run
JBOSS_MODE=standalone

# The address to bind to
JBOSS_BIND=0.0.0.0
__JBOSS

cat << __JBOSS_LAUNCH > ${INSTALL_DIR}/jboss/bin/launch.sh
#!/bin/bash

if [ "x\$JBOSS_HOME" = "x" ]; then
    JBOSS_HOME="${INSTALL_DIR}/jboss"
fi

if [[ "\$1" == "domain" ]]; then
    \$JBOSS_HOME/bin/domain.sh -c \$2 -b \$3
else
    \$JBOSS_HOME/bin/standalone.sh -c \$2 -b \$3
fi
__JBOSS_LAUNCH
chown jboss:jboss ${INSTALL_DIR}/jboss/bin/launch.sh
chmod +x ${INSTALL_DIR}/jboss/bin/launch.sh

cat << __JBOSS_SERVICE > /etc/systemd/system/jboss.service
[Unit]
Description=The JBoss EAP Server
After=syslog.target network.target
Before=httpd.service

[Service]
Environment=LAUNCH_JBOSS_IN_BACKGROUND=1
EnvironmentFile=-/etc/jboss/jboss.conf
User=jboss
LimitNOFILE=102642
PIDFile=/var/run/jboss/jboss.pid
ExecStart=${INSTALL_DIR}/jboss/bin/launch.sh \$JBOSS_MODE \$JBOSS_CONFIG \$JBOSS_BIND
StandardOutput=null

[Install]
WantedBy=multi-user.target
__JBOSS_SERVICE

# - setup log folder
mkdir /var/log/jboss && \
chown jboss:jboss /var/log/jboss

# - setup jboss as a service
systemctl daemon-reload
systemctl enable jboss.service
systemctl start jboss.service
