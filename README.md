# PathSeeker

A game about multiple robots trying to get from A to B on an map with quadratic tiles.

## Features

//TBD

## Setup

For an easy setup, a docker-compose file is provided in the `deploy` folder.
It is just a basic setup with traefik as reverse proxy on `http`.
Depending on the environment a certificate for TLS is recommended.

### Plausible tracking

If configured the application can be monitored with [Plausible](https://plausible.io/).

To enable this set the environment variable `PLAUSIBLE_DOMAIN`
and if you are running on a self-hosted instance `PLAUSIBLE_API_HOST`.
An example is shown in the deployment docker-compose.

## Note

This software will get no versioning and lives on the bloody main branch.

## Development

### Requirements

Have a [Java 17 LTS](https://adoptium.net/de/temurin/releases/?package=jdk&version=17), [node](https://nodejs.org/en/download/) and
[python 3.10](https://www.python.org/downloads/) installation is required to make it all run.
Make sure you have podman (or docker) installed on your system, since the dev environment runs on a container-based reverse proxy.

### Set up

For development there is a little script in the project root named `start_dev_env.(sh|bat)` (according to your OS).
Execute this first, before you do anything else.
Then run `./gradlew backend:bootRun` (from project root) (or execute it through your IDE) to start your backend.

For the frontend install all packages with `npm install --dev` and run the VUE app with `npm run dev` (from the `frontend` folder).

Now the application can be reached by http://localhost:8888.

To comply to the coding style, there are some [pre-commit](https://pre-commit.com/) rules.
Those should be automatically executed before every git commit.
To make this happen automatically, make sure to have `pre-commit` installed on your machine (`pip install pre-commit`)
and set up your git hooks (`pre-commit install` | from project root).
