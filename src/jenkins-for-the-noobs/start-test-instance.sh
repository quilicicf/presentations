#!/usr/bin/env bash

set -euo pipefail

export CYAN='\e[0;36m'
export DEFAULT='\e[0m'

export DIR
DIR="$(cd "$( dirname "${BASH_SOURCE[0]}")" && pwd)"

trap cleanup EXIT

main() (
  prepareCaches
  buildBuilderImage
  startVaultInstance
  startTestInstance
)

createFolderIfNotExists() (
  folder="$1"
  if ! test -d "${folder}"; then
    mkdir "${folder}"
    sudo chown --recursive "$(whoami):$(whoami)" "${folder}"
  fi
)

prepareCaches() (
  baseCacheFolder="${HOME}/.jenkins_cache"
  createFolderIfNotExists "${baseCacheFolder}"
  createFolderIfNotExists "${baseCacheFolder}/asdf"
  createFolderIfNotExists "${baseCacheFolder}/gradle"
)

buildBuilderImage () (
  printf '%bBuilding builder image%b\n' "${CYAN}" "${DEFAULT}"
  cd "${DIR}/builder-image"
  docker build --tag 'my-builder:latest' .
)

startVaultInstance() (
  printf '%bStarting Vault instance%b\n' "${CYAN}" "${DEFAULT}"
  cd "${DIR}/vault"
  docker compose up --detach
)

startTestInstance() (
  printf '%bStarting Jenkins instance%b\n' "${CYAN}" "${DEFAULT}"
  cd "${DIR}/gitops-repo"
  export CASC_VAULT_APPROLE='ca71432d-a9b7-48f3-99be-6001305ec376'
  export CASC_VAULT_ENGINE_VERSION=2
  export CASC_VAULT_APPROLE_SECRET='eb11a825-27ec-4b9e-b5e2-aa8f01ac0577'
  export CASC_VAULT_PATHS='secret/myteam/jenkins,secret/myteam/approle'
  export CASC_VAULT_URL='http://localhost:8200'
  gradle :test --tests "TestInstance"
)

cleanup() (
  cd "${DIR}/vault"
  docker compose down
)

main "$@"
