#!/bin/bash
set -e
curl -s --max-time 10 --fail http://$(hostname -i):8080/auth > /dev/null
