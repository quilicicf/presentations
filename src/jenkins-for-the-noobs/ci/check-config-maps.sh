#!/usr/bin/env bash

set -xe

# Makes sure that the config maps for a Jenkins instance contain the source code
# from src/main. This validates that the pre-commit hook has run and that the
# changes will be picked up in production.
main() (
  repositoryPath="$(git rev-parse --show-toplevel)"
  deno run \
    --allow-read="${repositoryPath}" \
    --allow-write="${repositoryPath}" \
    --allow-run='git' \
    "${repositoryPath}/src/jenkins-for-the-noobs/ci/write-config-maps.ts" \
    '--verbose'

  if [[ -n "$(git diff --name-only)" ]]; then
    printf 'The K8s files have not been updated! Install the pre-commit git hook!\n'
    printf 'The diff:\n'
    git diff
    exit 1
  fi
)

main "$@"
