#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

profile=default
skip=
args=()
for var in "$@"; do
    # Ignore known bad arguments
    case "$var" in
        -P)
            skip=yes
            ;;
        *)
          if [ -z "$skip" ]; then
              args+=("$var")
          else
              profile=$var
          fi;
          skip=
    esac;
done


curlcookies=cc.txt
#"$(mktemp /tmp/curlcookies.XXXXXXXXXX)"
./cookiefire > "$curlcookies"
curl -b "$curlcookies" "${args[@]}" ;
