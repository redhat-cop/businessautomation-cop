#!/bin/bash
EXAMPLE_PATH="$(cd "$(dirname "$0")" && PWD)"
EXAMPLE_ID="$(basename $EXAMPLE_PATH)"

KIE_SERVER_URL="http://localhost:8080/kie-server/services/rest/server"
KIE_SERVER_USER="kieServerUser"
KIE_SERVER_PASSWORD="Pa\$\$w0rd"
EXAMPLE_CONFIGURATION="embedded"
EXAMPLE_RUNTIME="springboot"

declare -a configurations=("remote" "embedded")
declare -a runtimes=("springboot" "quarkus")
declare -a kjars=("com.redhat.cop.pam:dmn-example1-kjar:1.0")

function deploy_kjar(){
    echo "=================== Start deploy of kjar $1 ==================="
    DEPLOY="curl -s -u $KIE_SERVER_USER:$KIE_SERVER_PASSWORD -H 'accept: application/json' -H 'content-type: application/json' -X PUT '$KIE_SERVER_URL/containers/$1' -d@requests/$1.json"
    UN_DEPLOY="curl -s -u $KIE_SERVER_USER:$KIE_SERVER_PASSWORD -H 'accept: application/json' -H 'content-type: application/json' -X DELETE '$KIE_SERVER_URL/containers/$1'"
    CHECK_IS_DEPLOY="curl -s -i -u $KIE_SERVER_USER:$KIE_SERVER_PASSWORD -H 'accept: application/json' -H 'content-type: application/json' -X GET '$KIE_SERVER_URL/containers/$1/release-id'"
    echo "=================== Check if container $1 already deployed ==================="
    IS_DEPLOY_RESULT=$(eval $CHECK_IS_DEPLOY)
    echo $IS_DEPLOY_RESULT
    if [[ $IS_DEPLOY_RESULT == *"ReleaseId for container $1"* ]]; then
        echo "=================== Container $1 already deployed. Uneploying existing version ==================="
        UNDEPLOY_RESULT=$(eval $UN_DEPLOY)
        echo $UNDEPLOY_RESULT
    fi
    echo "=================== Deploying container $1 ==================="
    DEPLOY_RESULT=$(eval $DEPLOY)
    echo $DEPLOY_RESULT
    echo "=================== End deploy of kjar $1 ==================="
}

function deploy_kjars(){
    for i in "${kjars[@]}"
        do
        :
        deploy_kjar $i
    done
}

function usage() {
    echo "
usage: $(basename -- "$0") [-h help]
-c [remote|embedded], default embedded
-r [quarkus|springboot], default springboot
-k url[:kie-server-url] default http://localhost:8080/kie-server/services/rest/server
-u username[:kie-server-username] default kieServerUser
-p password[:kie-server-passwrod] default Pa\$\$w0rd

Options:
    -c  : Specify the configurations to run(default embedded):
            - embedded  : In this configuration the runtime is embedded in the appliction
            - remote    : In this configuration the runtime is remote and is invoked via rest API
    -r  : Specify the runtime to use to run the tests(default springboot):
            - quarkus       : the application is run using quarkus
            - springboot    : the application is run using spring-boot
    -k  : kie server url default value: http://localhost:8080/kie-server/services/rest/server
    -u  : username to use to invoke services on kie-server default value: kieServerUser
    -p  : password to use to invoke service on kie-server default value: Pa\$\$w0rd
    -h  : print this message
    "
}

function start_test(){
    TEST_PROJECT_ABSOLUTE_PATH=$EXAMPLE_PATH/$EXAMPLE_ID-$EXAMPLE_RUNTIME-$EXAMPLE_CONFIGURATION
    MAVEN_PARAMETERS="-Dcom.redhat.cop.pam.kieserver_url=$KIE_SERVER_URL -Dcom.redhat.cop.pam.kieserver_user=$KIE_SERVER_USER -Dcom.redhat.cop.pam.kieserver_password=$KIE_SERVER_PASSWORD"
    POM_ABSOLUTE_PATH="$TEST_PROJECT_ABSOLUTE_PATH/pom.xml"
    echo "=================== Starting execution of test with following configuration ==================="
    echo "kie-server-url: $KIE_SERVER_URL"
    echo "username: $KIE_SERVER_USER"
    echo "password: $KIE_SERVER_PASSWORD"
    echo "configuration: $EXAMPLE_CONFIGURATION"
    echo "runtime: $EXAMPLE_RUNTIME"
    echo "all tests in folder $TEST_PROJECT_ABSOLUTE_PATH will be executed"
    if [ $EXAMPLE_CONFIGURATION == *"remote"* ]
    then
        deploy_kjars
    fi
    echo $POM_ABSOLUTE_PATH
    echo $MAVEN_PARAMETERS
    mvn test -f $POM_ABSOLUTE_PATH $MAVEN_PARAMETERS
    echo "=================== End execution of test with following configuration ==================="
}

while getopts "c:r:k:u:p:h" OPTION
do 
   case $OPTION in
        c)
            echo "configuration: $OPTARG"
            if [[ " ${configurations[*]} " == *"$OPTARG"* ]]; then
                EXAMPLE_CONFIGURATION=$OPTARG
            else
                 usage
                exit 0
            fi
            ;;
        r)
            echo "runtime: $OPTARG"
            if [[ " ${runtimes[*]} " == *"$OPTARG"* ]]; then
                EXAMPLE_RUNTIME=$OPTARG
            else
                 usage
                exit 0
            fi
            ;;
        k)
            echo "kie-server: $OPTARG"
            KIE_SERVER_URL=$OPTARG
            ;;
        u)
            echo "kie username: $OPTARG"
            KIE_SERVER_USER=$OPTARG
            ;;
        p)
            echo "kie password: $OPTARG"
            KIE_SERVER_PASSWORD=$OPTARG
            ;;
        h)
            usage
            exit 0
            ;;
    esac
done

start_test