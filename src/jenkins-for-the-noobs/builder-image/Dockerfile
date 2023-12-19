FROM bash:5.2.21

# Install git
RUN set -euxo pipefail && apk add --no-cache git

# Create non-root user $JENKINS_USER that matches the user in Jenkins instances.
# By default, Jenkins user has id 1000.
ENV JENKINS_UID='1000'
ENV JENKINS_USER='jenkins'
ENV JENKINS_USERGROUP="${JENKINS_USER}"
ENV HOME="/home/${JENKINS_USER}"
RUN set -x \
  && addgroup --system "${JENKINS_USERGROUP}" --gid "${JENKINS_UID}" \
  && adduser "${JENKINS_USER}" \
    --system \
    --gecos 'Jenkins user' \
    --uid "${JENKINS_UID}" \
    --ingroup "${JENKINS_USERGROUP}" \
    --home "${HOME}" \
    --shell /bin/bash \
  && chmod 755 "${HOME}"

USER "${JENKINS_UID}"

# Install asdf and plugins
RUN set -euxo pipefail \
  && git clone 'https://github.com/asdf-vm/asdf.git' "${HOME}/.asdf" \
  && export ASDF_DIR="${HOME}/.asdf" \
  && source "${HOME}/.asdf/asdf.sh" \
  \
  && asdf plugin add 'deno' \
  && asdf plugin add 'java' \
  && asdf plugin add 'gradle' \
  \
  && printf 'Load asdf and its completions when running interactive scripts\n' \
  && printf '\n# Link ASDF\n. "%s"\n' "${HOME}/.asdf/asdf.sh" >> "${HOME}/.bashrc" \
  && printf '\n# Add ASDF auto-completion\n. "%s"\n' "${HOME}/.asdf/completions/asdf.bash" >> "${HOME}/.bashrc"

# Load asdf when running non-interactive scripts
COPY --chown=${JENKINS_UID}:${JENKINS_USERGROUP} 'bashenv.sh' "${HOME}/bashenv.sh"
ENV BASH_ENV="${HOME}/bashenv.sh"

WORKDIR "${HOME}"
