#!/bin/sh

docker run --name opendj -it -p 389:389 -p 636:636 -p 4444:4444 opendj
#docker run -it -p 80:80 -p 6666:6666  lb /bin/bash

