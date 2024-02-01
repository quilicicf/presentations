#!/usr/bin/env bash

set -euo pipefail

export CYAN='\e[0;36m'
export DEFAULT='\e[0m'

export DIR
DIR="$(cd "$( dirname "${BASH_SOURCE[0]}")" && pwd)"

trap cleanup EXIT

main() (
  buildBuilderImage
  startVaultInstance
  startTestInstance
)

buildBuilderImage () (
  printf '%bBuilding builder image%b\n' "${CYAN}" "${DEFAULT}"
  cd "${DIR}/builder-image"
  docker build . \
     --build-arg "USER=$(whoami)" \
     --tag 'my-builder:latest'
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
