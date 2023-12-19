#!/usr/bin/env sh

# NOTE: Authentication is done via environment (check docker-compose.yaml)

# Wait for Vault to start
printf 'Waiting for Vault\n'
until vault status > /dev/null 2>&1; do
  printf '.'
  sleep 2
done
printf '\n'
printf 'Vault is ready\n'

# Create policy for Jenkins users
# See: https://developer.hashicorp.com/vault/tutorials/policies/policies
{
  printf 'path "secret/data/myteam/*" {\n'
  printf '  capabilities = ["read"]\n'
  printf '}\n'
} | tee jenkins-policy.hcl
vault policy write 'jenkins' 'jenkins-policy.hcl'

# Create approle
# See: https://developer.hashicorp.com/vault/tutorials/auth-methods/approle
vault auth enable 'approle'
vault write 'auth/approle/role/jenkins' \
  token_policies='jenkins' \
  token_ttl='1h' \
  token_max_ttl='4h' \
  role_id='ca71432d-a9b7-48f3-99be-6001305ec376'
vault write 'auth/approle/role/jenkins/custom-secret-id' \
  secret_id='eb11a825-27ec-4b9e-b5e2-aa8f01ac0577'

# Create secrets
# See: https://developer.hashicorp.com/vault/tutorials/getting-started/getting-started-first-secret
printf 'Token: %s\n' "${GITHUB_PASSWORD:-ghp_dummy}"
vault kv put \
  -mount=secret \
  myteam/jenkins \
  github-ci-user="${GITHUB_PASSWORD:-ghp_dummy}" \
  github-oauth-clientid-jenkins-myteam="${GITHUB_APP_CLIENT_ID:-goc_dummy}" \
  github-oauth-secret-jenkins-myteam="${GITHUB_APP_SECRET_ID:-gos_dummy}"
