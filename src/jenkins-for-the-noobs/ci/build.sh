#!/usr/bin/env bash

set -euxo pipefail

main() (
  cd ./src/jenkins-for-the-noobs/gitops-repo

  asdf current
  which java
  gradle test
)

main "$@"
