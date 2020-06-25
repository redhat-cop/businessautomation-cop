#!/bin/bash

set -e

SHOULD_RUN=$1
CMD=$2

if [ -z "${SHOULD_RUN}" ]
then
  echo "SHOULD_RUN is empty. Skipping."
  exit 0
fi

echo "Executing: $CMD"
eval "$CMD"