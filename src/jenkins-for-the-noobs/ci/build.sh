#!/usr/bin/env bash

set -euxo pipefail

main() (
  cd ./src/jenkins-for-the-noobs/gitops-repo
  gradle test
)

main "$@"
