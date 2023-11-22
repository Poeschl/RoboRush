#!/usr/bin/env sh
set -e

jq -n \
  --arg plausibleDomain "$PLAUSIBLE_DOMAIN" \
  --arg plausibleCustomApiHost "$PLAUSIBLE_API_HOST" \
   '$ARGS.named' > /usr/share/nginx/html/config.json
