echo "******  RUNNING ADDITIONAL CONFIGURATIONS WITH JBOSS-CLI - ADDING CUSTOM PROPERTIES **********"
echo "trying to execute /opt/eap/bin/jboss-cli.sh --file=/opt/eap/extensions/custom-properties.cli"
ls -l /opt/eap/bin/jboss-cli.xml
grep resolve-parameter-values /opt/eap/bin/jboss-cli.xml

sed -i "s/<resolve-parameter-values>false<\/resolve-parameter-values>/ \
<resolve-parameter-values>true<\/resolve-parameter-values>/" \
/opt/eap/bin/jboss-cli.xml

printenv | grep CUSTOM_ > /opt/eap/bin/custom.props

/opt/eap/bin/jboss-cli.sh --properties=/opt/eap/bin/custom.props --file=/opt/eap/extensions/custom-properties.cli
echo "END - custom properties added"
