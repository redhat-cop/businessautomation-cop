#!/usr/bin/env jjs
#
# - enable scripting mode
#


#
# - configuration variables
#

#
# arguments should be: KIE_SERVER_ID:CONTAINER_ID:GROUP:ARTEFACT:VERSION
#
gavar=arguments[0].split(/:/);

#
# - configure application details
#
serverId=gavar[0];
containerName=gavar[1];
GAV_Group=gavar[2];
GAV_Artifact=gavar[3];
GAV_Version=gavar[4];

#
# - configure required capabilities with true or false
#
require_rules=true
require_bpmn=false
require_planner=false

#
# - configure BPMS installation details
#
baseURL='http://localhost:8080';
bpmsAdminName='pamAdmin';
bpmsAdminPasswd='S3cr3tK3y#';

controllerPrefix='business-central';

#
# - configure verbosity
#     true : will print all messages
#    false : will print only errors, defaults to false
#
verbose=true

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

function getKieServerContainerRelease(kieUrl,bpmsAuth,containerId) {
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
  propConfig['Authorization'] = bpmsAuth;
  response = httpGetWithHeaders("${kieUrl}/containers/${containerId}/release-id",propConfig);
  jon = JSON.parse(response.data);
  return jon;
}

if (verbose!=true) verbose=false;
function logit(s,err) { if (verbose||err) print(s); }
function pout(s) { logit(s,false); }
function eout(s) { logit(s,true); }
function sout(s) { pout(''); pout(s); }
function PASS(s) { pout("Test PASSED"+(s?" : "+s:"")); }
function FAIL(s) { eout("Test FAILED"+(s?" : "+s:"")); END_RUN(2); }
function END_RUN(exitCode) { logit('--- END-RUN',exitCode); exit((exitCode?exitCode:0)); }

pout("");
pout("--- BEGIN");
pout("");
pout("Attempting deployment of ");
pout("     Deployment Unit (KJAR): ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");
pout("           to KIE Container: ${containerName}");
pout("            for KIE Servers: ${serverId}");
pout("");

invokedOK=true
invokedOK=(invokedOK && (baseURL.length()>0))
invokedOK=(invokedOK && (bpmsAdminName.length()>0))
invokedOK=(invokedOK && (bpmsAdminPasswd.length()>0))
invokedOK=(invokedOK && (containerName.length()>0))
invokedOK=(invokedOK && (GAV_Group.length()>0))
invokedOK=(invokedOK && (GAV_Artifact.length()>0))
invokedOK=(invokedOK && (GAV_Version.length()>0))
if (!invokedOK) {
  eout('ERROR: Mising configuration, please provide values for the following:');
  eout('               baseURL: the URL where the Business Central is reachable,');
  eout('                        eg. http://localhost:8080/business-central');
  eout('         bpmsAdminName: the name of the BPMS administrator, e.g. bpmsAmdin');
  eout('       bpmsAdminPasswd: the password of the BPMS administrator, e.g. s3cr3tp4ss');
  eout('             GAV_Group: the GROUP part of the Maven GAV vector, e.g. com.example.rules');
  eout('          GAV_Artifact: the ARTIFACT part of the Maven GAV vector, e.g. validation');
  eout('           GAV_Version: the VERSION part of the Maven GAV vector, e.g. 1.0-SNAPSHOT or 2.5.1');
  END_RUN(1);
}

bpmsAuth='Basic '+java.util.Base64.getEncoder().encodeToString((bpmsAdminName+':'+bpmsAdminPasswd).getBytes('utf-8'));

eapOK=false;
bpmsOK=false;

scCode = httpGet(baseURL).statusCode;
if (scCode==200) { PASS("EAP reachable at "+baseURL); eapOK=true; } else FAIL('EAP is unreachable at '+baseURL);

if (eapOK) {
  bcurl="${baseURL}/${controllerPrefix}"
  scCode = httpGet("${bcurl}").statusCode;
  if (scCode==200) { PASS("Business Central is reachable at ${bcurl}"); bpmsOK=true; } else FAIL("Business Central is unreachable at ${bcurl}");
}

if (bpmsOK) {
  sout('Looking for KIE Servers managed by this controller...');
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
  propConfig['Authorization'] = bpmsAuth;
  response = httpGetWithHeaders("${baseURL}/${controllerPrefix}/rest/controller/management/servers",propConfig);
  jon = [];
  kieServerNo=0;
  if (response.statusCode=200) {
    jon = JSON.parse(response.data);
    kieServerNo=jon['server-template'].length;
  }
  if (kieServerNo>0) {
    // print(JSON.stringify(jon,null,'\t'));
    pout("KIE Servers found : ${kieServerNo}");
    var kiefound = false;
    var jonsrv = jon['server-template'];
    pout('Server Details:');
    for (var i=0;i<jonsrv.length;i++) {
      var kies = jonsrv[i];
      var srvId = kies['server-id'];
      var srvName = kies['server-name'];
      pout('\t ID:'+srvId+'\t Name:'+srvName);
      if (srvId==serverId) {
        kiefound = true;
      }
    }
    if (!kiefound) {
      FAIL("KIE Server ${serverId} could not be found");
    }
    // pout('\t TemplateID:'+srvTemplate);
    //pout('\t        URL:'+srvUrl);
  } else {
    FAIL('No KIE Servers found on this controller');
  }
  srvId=serverId

  sout("Deleting container ${containerName} from the controller");
  propConfig = { };
  propConfig = { 'Accept':'application/json', 'Content-Type':'application/json' };
  propConfig['Authorization'] = bpmsAuth; 
  response = httpDeleteWithHeaders("${baseURL}/${controllerPrefix}/rest/controller/management/servers/${srvId}/containers/${containerName}",propConfig);
  pout('HTTP ResponseCode: ['+response.statusCode+']');

  sout("Creating container ${containerName} at the controller");
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
  pout('HTTP ResponseCode: ['+response.statusCode+']');
  pout("");
  if (response.statusCode==201) {
    PASS("KIE Container ${containerName} has been created for deployment unit (KJAR) ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");
    var jonsrv=jon['server-template'];
    for (var i=0;i<jonsrv.length;i++) {
      pout('');
      var kies = jonsrv[i];
      var srvId = kies['server-id'];
      var srvName = kies['server-name'];
      if (srvId==serverId) {
        pout('KIE Server ID:'+srvId+'\t Name:'+srvName);
        var kieremotes = kies['server-instances'];
        for (var j=0; j<kieremotes.length; j++) {
          var remoteId = kieremotes[j]['server-instance-id'];
          var remoteUrl = kieremotes[j]['server-url'];
          var deployed = false;
          var reljon = getKieServerContainerRelease(remoteUrl,bpmsAuth,containerName);
          if (reljon.type=="FAILURE") {
            deployed = false;
          } else {
            var realrel = reljon['result']['release-id']['group-id']+':'+reljon['result']['release-id']['artifact-id']+':'+reljon['result']['release-id']['version'];
            var wishrel = GAV_Group+':'+GAV_Artifact+':'+GAV_Version;
            if (realrel==wishrel) {
              deployed = true;
            } else {
              deployed = false;
            }
          }
          var ds = (deployed?" DEPLOYED ":"DEPLOYMENT FAILURE");
          pout("    ${remoteId} at ${remoteUrl}  ${ds}");
        }
      }
    }
  } else {
    pout('ResponseCode: ['+response.statusCode+']');
    FAIL("ERR11 Could NOT create ${containerName} updated with ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");
  }
}

pout("");
END_RUN();

