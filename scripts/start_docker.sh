#!/bin/bash

scripts/build_docker.sh
cmd="--args=\"$@\""
docker run internhub/scraper "$cmd"
