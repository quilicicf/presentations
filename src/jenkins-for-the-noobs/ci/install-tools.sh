#!/usr/bin/env bash

set -xe

# Prepares the asdf-installed tools so they're usable afterwards.
#
# If a .tool-versions file is in the current working directory, asdf install is run.
# This is to install the repository-specific tools.
#
# One can also install job-specific tools (that should NOT leak in the client repositories)
# by passing the list of tools to install as parameters.
#
# $@: Each parameter must represent a pair of tool name and version to install
#     separated by a space. Ex: 'jq 1.6'
main() (
  while [[ "$#" -gt 0 ]]; do
    toolName="$(awk '{print $1}' <<< "$1")"
    toolVersion="$(awk '{print $2}' <<< "$1")"
    if [[ -z "${toolName}" || -z "${toolVersion}" ]]; then
      printf "Each parameter must match: \`\${TOOL_NAME} \${TOOL_VERSION}\`\n"
      printf 'Got: %s\n' "$1"
      return 1
    fi
    shift
    installTool "${toolName}" "${toolVersion}"
  done

  if [[ -f ./.tool-versions ]]; then
    asdf install # Installs all the missing tools in the current folder's .tool-versions file
  fi

  asdf reshim # Re-links all the shims (some commands fail when this is not done)
  asdf current # Outputs the currently used tools to the console for debugging purposes
)

installTool() (
  toolName="${1:?Missing tool name}"
  toolVersion="${2:?Missing tool version}"

  asdf install "${toolName}" "${toolVersion}" # Installs the tool
  asdf global  "${toolName}" "${toolVersion}" # Uses it everywhere
)

main "$@"
