#!/usr/bin/env bash

# Sources asdf.
# This script is supposed to be used in BASH_ENV so that it can be used in non-interactive shells.
# It tries not to mess with any option set in the calling script but also not to log too much.
# This means updating some options and resetting them when it's done.

set +x

# shellcheck disable=SC1090
if source ~/.asdf/asdf.sh; then
  >&2 printf 'ASDF loaded\n'
else
  >&2 printf 'Could not load ASDF\n'
  exit 1
fi
