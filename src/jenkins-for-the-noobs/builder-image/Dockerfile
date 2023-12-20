FROM rockylinux:9.3

# Install OS packages
RUN set -eux \
  && dnf -y update-minimal --security --sec-severity=Important --sec-severity=Critical \
  && dnf install --assumeyes git unzip \
  && dnf clean all

# Create non-root user $JENKINS_USER that matches the user in Jenkins instances.
# By default, Jenkins user has id 1000.
ENV JENKINS_UID='1000'
ENV JENKINS_USER='jenkins'
ENV JENKINS_USERGROUP="${JENKINS_USER}"
ENV HOME="/home/${JENKINS_USER}"
RUN set -eux \
  && groupadd --system "${JENKINS_USERGROUP}" --gid "${JENKINS_UID}" \
  && useradd "${JENKINS_USER}" \
    --system \
    --create-home \
    --no-log-init \
    --uid "${JENKINS_UID}" \
    --gid "${JENKINS_USERGROUP}" \
    --home-dir "${HOME}" \
    --shell /sbin/nologin \
  && chmod 755 "${HOME}"

USER "${JENKINS_UID}"

# Install asdf and plugins
ENV ASDF_DIR="${HOME}/.asdf"
RUN set -eux \
  && git clone 'https://github.com/asdf-vm/asdf.git' "${ASDF_DIR}" \
  && source "${ASDF_DIR}/asdf.sh" \
  \
  && asdf plugin add 'deno' \
  && asdf plugin add 'java' \
  && asdf plugin add 'gradle' \
  && asdf plugin add 'groovy' \
  \
  && printf 'Load asdf and its completions when running interactive scripts\n' \
  && printf '\n# Link ASDF\n. "%s"\n' "${ASDF_DIR}/asdf.sh" >> "${HOME}/.bashrc" \
  && printf '\n# Add ASDF auto-completion\n. "%s"\n' "${ASDF_DIR}/completions/asdf.bash" >> "${HOME}/.bashrc"

# Load asdf when running non-interactive scripts
COPY --chown=${JENKINS_UID}:${JENKINS_USERGROUP} 'bashenv.sh' "${HOME}/bashenv.sh"
ENV BASH_ENV="${HOME}/bashenv.sh"

WORKDIR "${HOME}"
