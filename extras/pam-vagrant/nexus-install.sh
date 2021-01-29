#!/bin/bash

#
# - setup latest Nexus repo manager
#

BASE_DIR=/vagrant

command -v java     &> /dev/null || { echo >&2 'ERROR: JAVA not installed. Please install JAVA.8 to continue - Aborting'; exit 1; }
command -v mvn      &> /dev/null || { echo >&2 'ERROR: Maven not installed. Please install Maven.3.3.9 (or later) to continue - Aborting'; exit 1; }
command -v git      &> /dev/null || { echo >&2 'ERROR: GIT not installed. Please install GIT.1.8 (or later) to continue - Aborting'; exit 1; }
command -v unzip    &> /dev/null || { echo >&2 'ERROR: UNZIP not installed. Please install UNZIP to continue - Aborting'; exit 1; }
command -v curl     &> /dev/null || { echo >&2 'ERROR: CURL not installed. Please install CURL to continue - Aborting'; exit 1; }
command -v basename &> /dev/null || { echo >&2 'ERROR: basename not installed. Please install basename to continue - Aborting'; exit 1; }
command -v sed      &> /dev/null || { echo >&2 'ERROR: sed not installed. Please install sed to continue - Aborting'; exit 1; }
command -v tar      &> /dev/null || { echo >&2 'ERROR: tar not installed. Please install tar to continue - Aborting'; exit 1; }

#
# Install Nexus 
#
echo 'Downloading Nexus OSS Repository Manager...'
[[ -r $BASE_DIR/latest-unix.tar.gz ]] && cp $BASE_DIR/latest-unix.tar.gz .
[[ ! -r latest-unix.tar.gz ]] && curl -L -O https://download.sonatype.com/nexus/3/latest-unix.tar.gz
echo 'DONE. Installing Nexus OSS Repository Manager...'
mkdir -p nexus
pushd nexus &> /dev/null
  tar xvf ../latest-unix.tar.gz
  NEXUS_HOME=`ls -d ne* | tail -1`
popd &> /dev/null
cat << __NEXUSBIN > nexus.bin
#!/bin/bash

NEXUS_OP=\${1:-start}

pushd nexus/$NEXUS_HOME/bin &> /dev/null
  ./nexus \$NEXUS_OP
popd &> /dev/null
__NEXUSBIN
chmod u+x nexus.bin

echo 'Starting Nexus for the first time...'
nexusPassFile=nexus/sonatype-work/nexus3/admin.password
counter=0
./nexus.bin start
while [[ ! -r $nexusPassFile ]]; do
    let counter=$((counter+1))
    echo "[ $counter ] Waiting for Nexus to generate admin password... will check again in 5 seconds"
    sleep 5s
done
nexuspasswd=$(cat $nexusPassFile)
echo
echo "Nexus password is [ ${bblue}${nexuspasswd}${normal} ] please keep this password"
echo

echo "NEXUS_ROOT_PASS=${nexuspasswd}" > /root/nexus.pass

echo -n "Stopping Nexus first run..."
./nexus.bin stop

#
# - systemd integration for nexus
#
mv nexus /opt
useradd -d /home/nexus -m -s/bin/bash -U nexus && echo "nexus123#" | passwd nexus --stdin

# changing disk space requirements
sed -i 's/storage.diskCache.diskFreeSpaceLimit=4096/storage.diskCache.diskFreeSpaceLimit=1024/g' "/opt/nexus/$NEXUS_HOME/etc/karaf/system.properties"

# align owenership
chown -R nexus:nexus /opt/nexus

#
# Install Nexus for systemd
#
mkdir -p /etc/systemd/system/
cat << __NEXUS > /etc/systemd/system/nexus.service
[Unit]
Description=nexus service
After=network.target
  
[Service]
Type=forking
LimitNOFILE=65536
ExecStart=/opt/nexus/$NEXUS_HOME/bin/nexus start
ExecStop=/opt/nexus/$NEXUS_HOME/bin/nexus stop
User=nexus
Restart=on-abort
  
[Install]
WantedBy=multi-user.target
__NEXUS

systemctl daemon-reload
systemctl enable nexus.service
systemctl start nexus.service
