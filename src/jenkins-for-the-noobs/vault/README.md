# Integration with Vault

> Instructions to start a dummy Vault instance to try the configuration of the test Jenkins instance. 

## Run a local Vault instance

```shell
docker run \
  --publish '8200:8200' \
  --env 'VAULT_DEV_ROOT_TOKEN_ID=dev-only-token' \
  vault:1.13.3
```

## Enable approle authentication

Follow the steps in [the tutorial](https://developer.hashicorp.com/vault/tutorials/auth-methods/approle).

In place of the tutorial's policy, use:

```groovy
path "secret/data/myteam/*" {
  capabilities = [ "read" ]
}
```

> Note: the path in the policy has a segment `data` that's not in the secret's path.
> This is intended and reflects [the API](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-version)

You'll get a `roleId` and `secretId`, keep them close at hand, you'll need them next.

## Create secrets

Open the Vault instance you just started, and create the secrets:

* In path `secret/myteam/approle`:
  * `approle-id`: the `roleId` generated earlier
  * `approle-token`: the `secretId` generated earlier
* In path `secret/myteam/jenkins`: the credentials defined in [the CasC file](../gitops-repo/src/main/resources/casc/credentials.yaml)

## Map the secrets

### For the test instance

Open [jenkins.yaml](../gitops-repo/jenkins.yaml) and put the `roleId` resp. `secretId` in `CASC_VAULT_APPROLE` resp. `CASC_VAULT_APPROLE_SECRET`.

### For production

Create a file `secret.yaml` containing:

```yaml
apiVersion: v1
data:
  approle: ${ENCODED_SECRETS} # From previous step
kind: Secret
metadata:
  name: jenkins-vaas-vault
  namespace: ci
type: Opaque
```

Seal it with:

```shell
# Get the certificate to use to seal the secret
kubeseal \
  --fetch-cert \
  --namespace 'admin' \
  --controller-namespace 'admin' \
  --controller-name 'sealed-secrets' \
  > pub-cert.pem

# Use the certificate to seal the secret
kubeseal \
  --format=yaml \
  --cert pub-cert.pem \
  < "${unsealed_secret}" \
  > "sealed-${unsealed_secret}"
```

The generated file (`sealed-secret.yaml`) needs to be in the instance folder to be picked up by Flux.

## Resources

* [Approle configuration](https://developer.hashicorp.com/vault/tutorials/auth-methods/approle)
* [Jenkins Vault plugin](https://plugins.jenkins.io/hashicorp-vault-plugin)
