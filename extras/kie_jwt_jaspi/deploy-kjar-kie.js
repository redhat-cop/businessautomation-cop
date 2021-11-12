#!/usr/bin/env jjs
#
# - enable scripting mode
#


#
# - arguments should be: IP:PORT:CONTAINER_ID:GROUP:ARTEFACT:VERSION
#
gavar=arguments[0].split(/:/);

kieIP=gavar[0];
kiePort=gavar[1];
containerName=gavar[2];
GAV_Group=gavar[3];
GAV_Artifact=gavar[4];
GAV_Version=gavar[5];

#
# - configure credentials
#
kieAdminName='pamAdmin';
kieAdminPasswd='S3cr3tK3y#';


baseURL='http://'+kieIP+':'+kiePort+'/kie-server/services/rest/server';


#
# - do not modify below this line
#

function read(inputStream){
    var inReader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
    var inputLine;
    var response = new java.lang.StringBuffer();

    while ((inputLine = inReader.readLine()) != null) {
           response.append(inputLine);
    }
    inReader.close();
    return response.toString();
}

function write(outputStream, data){
    var wr = new java.io.DataOutputStream(outputStream);
    wr.writeBytes(data);
    wr.flush();
    wr.close();
}

function asResponse(con){
    var d = '';
    try {
      d = read(con.inputStream);
    } catch (e) {
      // NOP
    }

    return {data : d, statusCode : con.responseCode};
}

function httpPutWithHeaders(theUrl,requestProperties,putData) {
    var con = new java.net.URL(theUrl).openConnection();
    con.requestMethod = "PUT";
    for (prop in requestProperties) {
      con.setRequestProperty(prop, requestProperties[prop]);
    }
    con.doOutput=true;
    write(con.outputStream, putData);

    return asResponse(con);
}
function httpGetWithHeaders(theUrl,requestProperties) {
    var con = new java.net.URL(theUrl).openConnection();
    con.requestMethod = "GET";
    for (prop in requestProperties) {
      con.setRequestProperty(prop, requestProperties[prop]);
    }

    return asResponse(con);
}
function httpDeleteWithHeaders(theUrl,requestProperties) {
    var con = new java.net.URL(theUrl).openConnection();
    con.requestMethod = "DELETE";
    for (prop in requestProperties) {
      con.setRequestProperty(prop, requestProperties[prop]);
    }

    return asResponse(con);
}
function httpGet(theUrl){
    var con = new java.net.URL(theUrl).openConnection();
    con.requestMethod = "GET";

    return asResponse(con);
}

function httpPost(theUrl, data, requestProperties){
    var con = new java.net.URL(theUrl).openConnection();

    con.requestMethod = "POST";
    for (prop in requestProperties) {
      con.setRequestProperty(prop, requestProperties[prop]);
    }

    // Send post request
    con.doOutput=true;
    write(con.outputStream, data);

    return asResponse(con);
}

verbose=true;
function logit(s,err) { if (verbose||err) print(s); }
function pout(s) { logit(s,false); }
function eout(s) { logit(s,true); }
function sout(s) { pout(''); pout(s); }
function PASS(s) { pout("Test PASSED"+(s?" : "+s:"")); }
function FAIL(s) { eout("Test FAILED"+(s?" : "+s:"")); END_RUN(2); }
function END_RUN(exitCode) { logit('--- END-RUN',exitCode); exit((exitCode?exitCode:0)); }

pout("");
pout("--- BEGIN");

invokedOK=true
invokedOK=(invokedOK && (baseURL.length()>0))
invokedOK=(invokedOK && (kieAdminName.length()>0))
invokedOK=(invokedOK && (kieAdminPasswd.length()>0))
invokedOK=(invokedOK && (containerName.length()>0))
invokedOK=(invokedOK && (GAV_Group.length()>0))
invokedOK=(invokedOK && (GAV_Artifact.length()>0))
invokedOK=(invokedOK && (GAV_Version.length()>0))
if (!invokedOK) {
  eout('ERROR: Mising configuration, please provide values for the following:');
  eout('               baseURL: the URL where the KIE Server is reachable,');
  eout('                        eg. http://localhost:8080/kie-server/services/rest/server');
  eout('         kieAdminName: a user able to perform administrative tasks on KIE Server, e.g. pamAdmin');
  eout('       kieAdminPasswd: the password of said user, e.g. s3cr3tp4ss');
  eout('             GAV_Group: the GROUP part of the Maven GAV vector, e.g. com.example.rules');
  eout('          GAV_Artifact: the ARTIFACT part of the Maven GAV vector, e.g. validation');
  eout('           GAV_Version: the VERSION part of the Maven GAV vector, e.g. 1.0-SNAPSHOT or 2.5.1');
  END_RUN(1);
}

kieAuth='Basic '+java.util.Base64.getEncoder().encodeToString((kieAdminName+':'+kieAdminPasswd).getBytes('utf-8'));

kieOK=false;

propConfig = { };
propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
propConfig['Authorization'] = kieAuth;
response = httpGetWithHeaders("${baseURL}",propConfig);
scCode = response.statusCode;
if (scCode==200) { PASS("Verified that KIE Server is reachable at ${baseURL}"); bpmsOK=true; } else FAIL("KIE Server is unreachable at ${baseURL}");

END_RUN()

if (bpmsOK) {
  sout('Testing available KIE Execution Servers for this controller...');
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
  propConfig['Authorization'] = bpmsAuth;
  response = httpGetWithHeaders("${baseURL}/${controllerPrefix}/rest/controller/management/servers",propConfig);
  if (response.statusCode==200) {
    PASS();
    jon = JSON.parse(response.data);
    // print(JSON.stringify(jon,null,'\t'));
    var srvId = jon['server-template'][0]['server-id'];
    var srvName = jon['server-template'][0]['server-name'];
    var srvTemplate =jon['server-template'][0]['server-instances'][0]['server-template-id']; 
    var srvUrl =jon['server-template'][0]['server-instances'][0]['server-url']; 
    pout('Server Details:');
    pout('\t         ID:'+srvId);
    pout('\t       Name:'+srvName);
    pout('\t TemplateID:'+srvTemplate);
    //pout('\t        URL:'+srvUrl);
  } else {
    FAIL('No KIE Execution Server found on this controller');
  }
  srvId=serverId

  sout("Deleting container ${containerName} on the Controller");
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
  propConfig['Authorization'] = bpmsAuth; 
  response = httpDeleteWithHeaders("${baseURL}/${controllerPrefix}/rest/controller/management/servers/${srvId}/containers/${containerName}",propConfig);
  pout('ResponseCode: ['+response.statusCode+']');

  sout("Trying to creating container ${containerName} at Business Central ...");
  var putData = [
                  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
                  '<container-spec-details>',
                  '	<container-id>'+"${containerName}"+'</container-id>',
                  '	<container-name>'+"${containerName}"+'</container-name>',
                  '	<server-template-key>',
                  '		<server-id>'+"${srvId}"+'</server-id>',
                  '	</server-template-key>',
                  '	<release-id>',
                  "		<group-id>${GAV_Group}</group-id>",
                  "		<artifact-id>${GAV_Artifact}</artifact-id>",
                  "		<version>${GAV_Version}</version>",
                  '	</release-id>',
                  '	<configs>',
                  '		<entry>',
                  '			<key>PROCESS</key>',
                  '			<value xsi:type="processConfig"',
                  '				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">',
                  '             <runtimeStrategy>SINGLETON</runtimeStrategy>',
                  '             <kbase></kbase>',
                  '             <ksession></ksession>',
                  '             <mergeMode>MERGE_COLLECTIONS</mergeMode>',
                  '			</value>',
                  '		</entry>',
                  '		<entry>',
                  '			<key>RULE</key>',
                  '			<value xsi:type="ruleConfig"',
                  '				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">',
                  '				<scannerStatus>STOPPED</scannerStatus>',
                  '			</value>',
                  '		</entry>',
                  '	</configs>',
                  '	<status>STARTED</status>',
                  '</container-spec-details>'].join('');
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/xml' };
  propConfig['Authorization'] = bpmsAuth;
  response = httpPutWithHeaders("${baseURL}/${controllerPrefix}/rest/controller/management/servers/${srvId}/containers/${containerName}",propConfig,putData);
  if (response.statusCode==201) {
    PASS();
    sout("Associating with remote server ... ");
    var putData = [
                    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
                    '<kie-container container-id="'+"${containerName}"+'">',
                    '  <release-id>',
                    "    <group-id>${GAV_Group}</group-id>",
                    "    <artifact-id>${GAV_Artifact}</artifact-id>",
                    "    <version>${GAV_Version}</version>",
                    '  </release-id>',
                    '</kie-container>'].join('');
    propConfig = { };
    propConfig = { 'Accept':'application/json', 'Content-Type':'application/xml' };
    propConfig['Authorization'] = bpmsAuth;
  } else {
    pout('ResponseCode: ['+response.statusCode+']');
    FAIL("ERR11 Could NOT create ${containerName} updated with ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");
  }


 }

eout("${containerName} updated with ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");
END_RUN();

