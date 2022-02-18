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

verbose=false;
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


# - delete container before deploying
eout("Deleting container ${containerName}");
response = httpDeleteWithHeaders("${baseURL}/containers/${containerName}",propConfig);
pout('ResponseCode: ['+response.statusCode+']');

# - deploy container
var putData = [
                '{',
                ' "container-id" : "'+containerName+'",',
                ' "container-name" : "'+containerName+'",',
                '	  "release-id" : {',
                '        "group-id" : "'+GAV_Group+'",',
                '        "artifact-id" : "'+GAV_Artifact+'",',
                '        "version" : "'+GAV_Version+'"',
                '    },',
                '	"config-items": [',
                '    {',
                '      "itemName": "RuntimeStrategy",',
                '      "itemValue": "SINGLETON"',
                '    },',
                '    {',
                '      "itemName": "MergeMode",',
                '      "itemValue": "MERGE_COLLECTIONS"',
                '    }',
                '  ],',
                '	"scanner": {',
                '    "poll-interval": "5000",',
                '    "status": "STOPPED"',
                '  },',
                '	"status" : "STARTED"',
                '}'
              ].join('');
response = httpPutWithHeaders("${baseURL}/containers/${containerName}",propConfig,putData);
pout('ResponseCode: ['+response.statusCode+']');
if (response.statusCode==201) {
  eout("${containerName} has been deployed with ${GAV_Group}:${GAV_Artifact}:${GAV_Version}");  
} else {
  eout("ERROR - NOT DEPLOYED ${containerName} with ${GAV_Group}:${GAV_Artifact}:${GAV_Version} HAT NOT BEEN DEPLOYED");
}

END_RUN()
