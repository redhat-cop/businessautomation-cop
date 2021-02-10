#!/bin/bash

INSTALL_DIR=/opt

TARGET_USER=${1:-pamuser}

#
# - Install PAM.7 as a systemd service
#
groupadd -r $TARGET_USER
useradd -r -g $TARGET_USER -d ${INSTALL_DIR}/$TARGET_USER -s /sbin/nologin $TARGET_USER
src_dir=jboss-eap-7.2 && [[ -d pam ]] && src_dir=pam
mv "$src_dir" "${INSTALL_DIR}"
# echo 'JAVA_OPTS="$JAVA_OPTS -Xmx2048m "' >> ${INSTALL_DIR}/"${src_dir}"/bin/standalone.conf
ln -s ${INSTALL_DIR}/"${src_dir}" ${INSTALL_DIR}/$TARGET_USER
cp ${INSTALL_DIR}/"${src_dir}"/settings.xml /etc/maven/settings.xml
chown -R $TARGET_USER:$TARGET_USER ${INSTALL_DIR}/$TARGET_USER
chown -R $TARGET_USER:$TARGET_USER ${INSTALL_DIR}/"${src_dir}"
mkdir /etc/$TARGET_USER

cat << __JBOSS > /etc/$TARGET_USER/$TARGET_USER.conf
# The configuration you want to run
JBOSS_CONFIG=standalone.xml

# The mode you want to run
JBOSS_MODE=standalone

# The address to bind to
JBOSS_BIND=0.0.0.0
__JBOSS

cat << __JBOSS_LAUNCH > ${INSTALL_DIR}/$TARGET_USER/bin/launch.sh
#!/bin/bash

if [ "x\$JBOSS_HOME" = "x" ]; then
    JBOSS_HOME="${INSTALL_DIR}/$TARGET_USER"
fi

if [[ "\$1" == "domain" ]]; then
    \$JBOSS_HOME/bin/domain.sh -c \$2 -b \$3
else
    \$JBOSS_HOME/bin/standalone.sh -c \$2 -b \$3
fi
__JBOSS_LAUNCH
chown $TARGET_USER:$TARGET_USER ${INSTALL_DIR}/$TARGET_USER/bin/launch.sh
chmod +x ${INSTALL_DIR}/$TARGET_USER/bin/launch.sh

cat << __JBOSS_SERVICE > /etc/systemd/system/$TARGET_USER.service
[Unit]
Description=The JBoss EAP Server
After=syslog.target network.target
Before=httpd.service

[Service]
Environment=LAUNCH_JBOSS_IN_BACKGROUND=1
EnvironmentFile=-/etc/$TARGET_USER/$TARGET_USER.conf
User=$TARGET_USER
LimitNOFILE=102642
PIDFile=/var/run/$TARGET_USER/$TARGET_USER.pid
ExecStart=${INSTALL_DIR}/$TARGET_USER/bin/launch.sh \$JBOSS_MODE \$JBOSS_CONFIG \$JBOSS_BIND
StandardOutput=null

[Install]
WantedBy=multi-user.target
__JBOSS_SERVICE

# - setup log folder
mkdir /var/log/$TARGET_USER && \
chown $TARGET_USER:$TARGET_USER /var/log/$TARGET_USER

# - setup jboss as a service
systemctl daemon-reload
systemctl enable $TARGET_USER.service
systemctl start $TARGET_USER.service

