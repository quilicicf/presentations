#!/usr/bin/env bash

# Sources asdf.
# This script is supposed to be used in BASH_ENV so that it can be used in non-interactive shells.

set +x

# shellcheck disable=SC1090
if ! source ~/.asdf/asdf.sh; then
  >&2 printf 'Could not load ASDF\n'
  exit 1
fi
