#!/bin/bash

set -ueo pipefail

if [ -n "${DEBUG:-}" ] ; then
    set -x
fi

function print_help() {
    echo "----------------------------------------------------------"
    echo "Builds and pushes JDBC Driver images to a docker registry. This has to be executed from a folder containing a Dockerfile"
    echo ""
    echo "Usage: "
    echo "   ../build.sh [--registry=myregistry.example.com:5000] [--artifact-repo=https://myrepo.example.com/maven/public]"
    echo "Options:"
    echo "   Available driver images to build: db2,derby,mssql,oracle,mariadb,sybase"
    echo "   --registry         Specifies the docker registry to use for tagging and pushing. Defaults to docker-registry.default.svc:5000"
    echo "   --artifact-repo    Specifies the Maven repository where the jdbc drivers are available. Oracle does not have a default value"
    echo "   --image-tag        Specifies the tag to use when building the image. Defaults to 7.6.0"
    echo "   --namespace        Specifies the namespace where the build will take place (default openshift)"
    echo "   --env-target       Specifies configuration file to be used. Different per environment, defaults to dev"
}

while (($#))
do
    case $1 in
        --registry=*)
            registry="${1#*=}"
        ;;
        --artifact-repo=*)
            artifact_repo="${1#*=}"
        ;;
        --image-tag=*)
            image_tag="${1#*=}"
        ;;
        --namespace=*)
            namespace="${1#*=}"
        ;;	
        --env-target=*)
            env_target="${1#*=}"
        ;;	
        -h)
            print_help
            exit 0
        ;;
        --help)
            print_help
            exit 0
        ;;
    esac
shift
done

if [[ ! -r oracle-driver-image/Dockerfile ]]
then
    echo "Error: No Dockerfile found"
    print_help
    exit 1
fi

current_dir="${PWD##*/}"
current_dir=oracle-driver-image
driver=$(echo $current_dir | cut -d '-' -f 1)
image_tag="${image_tag:-1.1}"
namespace="${namespace:-openshift}"
env_target="${env_target:-dev}"

registry=${registry:-image-registry.openshift-image-registry.svc:5000}

function docker_login() {
    if [[ $(oc whoami -t > /dev/null; echo $?) == 1 ]]; then
        echo "You must be logged in"
        exit 1
    fi
     docker login -u `oc whoami` -p `oc whoami -t` $registry
}

function build() {
    local driver=$1
    local tag=$2
    local artifact_repo=${3:-}
    echo Building $driver
    if [[ -n $artifact_repo ]]
    then
        echo "docker build -f $current_dir/Dockerfile . -t $tag --build-arg ARTIFACT_MVN_REPO=$artifact_repo"
        docker build -f $current_dir/Dockerfile . -t $tag --build-arg ARTIFACT_MVN_REPO=$artifact_repo
    else
        echo "docker build -f $current_dir/Dockerfile . -t $tag"
        docker build -f $current_dir/Dockerfile . -t $tag
    fi
    echo Finished bulding $tag
}

function push() {
    local tag=$1
    echo Pushing $tag
    docker push $tag
    echo Pushed $tag
}

function create_build() {
    local driver=$1
    local version=$2
    local namespace=$3

    # Delete previous BuildConfig in case we are running an update of the base image
    echo "oc delete bc rhpam-kieserver-rhel8-$image_name"
    oc delete bc rhpam-kieserver-rhel8-$image_name || echo "BuildConfig rhpam-kieserver-rhel8-$image_name not found for deletion"
    
    echo "oc new-build -n $namespace \
        --name rhpam-kieserver-rhel8-$image_name \
        --image-stream=$namespace/rhpam-kieserver-rhel8:$image_tag \
        --source-image=$namespace/$image_name:$version \
        --source-image-path=/extensions:$driver-driver/ \
        --to=rhpam-kieserver-rhel8-$image_name:$image_tag \
        -e CUSTOM_INSTALL_DIRECTORIES=$driver-driver/extensions"

    oc new-build -n $namespace \
        --name rhpam-kieserver-rhel8-$image_name \
        --image-stream=$namespace/rhpam-kieserver-rhel8:$image_tag \
        --source-image=$namespace/$image_name:$version \
        --source-image-path=/extensions:$driver-driver/ \
        --to=rhpam-kieserver-rhel8-$image_name:$image_tag \
        -e CUSTOM_INSTALL_DIRECTORIES=$driver-driver/extensions
}

docker_login



# pushd .. &> /dev/null


image_name=$driver-image
image_name="${driver}-${env_target}"
version=$(grep version $current_dir/Dockerfile | awk -F"=" '{print $2}' | sed 's/"//g')
echo ":: IMAGE_NAME: $image_name"
echo ":: DRIVER: $driver"
echo ":: VERSION: $version"
tag=$registry/$namespace/$image_name:$version
build $image_name $tag ${artifact_repo:-}
push $tag
create_build $driver $version $namespace


# popd &> /dev/null
