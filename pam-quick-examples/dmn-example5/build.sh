#!/bin/bash
EXAMPLE_PATH="$(cd "$(dirname "$0")" && PWD)"
EXAMPLE_ID="$(basename $EXAMPLE_PATH)"

declare -a example_projects=("parent" "kjar" "springboot-embedded" "springboot-remote")

function maven_clean_install_skip_tests(){
    POM_ABSOLUTE_PATH="$EXAMPLE_PATH/$EXAMPLE_ID-$1/pom.xml"
    echo $POM_ABSOLUTE_PATH
    mvn clean install -f $POM_ABSOLUTE_PATH -DskipTests
}

function build_projects(){
    for i in "${example_projects[@]}"
        do
        :
        maven_clean_install_skip_tests $i
    done
}

build_projects

