#!/bin/bash

REPOSITORY="semcon"
CONTAINER="srv-semantics"

# read commandline options
BUILD_CLEAN=false
DOCKER_UPDATE=false

while [ $# -gt 0 ]; do
    case "$1" in
        --clean*)
            BUILD_CLEAN=true
            ;;
        --dockerhub*)
            DOCKER_UPDATE=true
            ;;
        *)
            printf "unknown option(s)\n"
            if [ "${BASH_SOURCE[0]}" != "${0}" ]; then
                return 1
            else
                exit 1
            fi
    esac
    shift
done

if $BUILD_CLEAN; then
    mvn clean install
    cp target/validation-service-1.1.1-SNAPSHOT-jar-with-dependencies.jar docker
    docker build --no-cache -f ./docker/Dockerfile -t $REPOSITORY/$CONTAINER .
else
    docker build -f ./docker/Dockerfile -t $REPOSITORY/$CONTAINER .
fi

if $DOCKER_UPDATE; then
    docker push $REPOSITORY/$CONTAINER
fi
