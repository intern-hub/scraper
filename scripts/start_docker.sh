#!/bin/bash

scripts/build_docker.sh
cmd="--args=\"$@\""
logs="$(pwd)/logs"
docker run -v "$logs":/app/logs internhub/scraper "$cmd"
