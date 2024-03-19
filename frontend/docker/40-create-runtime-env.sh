#!/usr/bin/env sh
set -e

ENV_FILE=$(ls /usr/share/nginx/html/assets/env-*js)

sed -i \
  -e "s|plausibleDomain:\"\"|plausibleDomain:\"$PLAUSIBLE_DOMAIN\"|" \
  -e "s|plausibleCustomApiHost:\"\"|plausibleCustomApiHost:\"$PLAUSIBLE_API_HOST\"|" \
  $ENV_FILE
