#!/usr/bin/env sh
set -e

if [ -x "$(command -v podman-compose)" ]; then
  exec='podman-compose'
elif [ -x "$(command -v docker-compose)" ]; then
  exec='docker-compose'
else
  exec='docker compose'
fi

export HOSTNAME="$(hostname).local"

echo 'Container startup'
eval "$exec up $@"
