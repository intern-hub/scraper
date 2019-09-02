#!/bin/bash

scripts/build_docker.sh
docker run internhub/scraper "--args=\"$1\""
