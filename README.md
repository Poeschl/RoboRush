# RoboRush

A game about multiple robots trying to get from A to B on a map with quadratic tiles.
The first robot which reaches the target tile wins!

<!-- CodeQL security scanning has been added to ensure code quality and security -->

Get in the game by controlling your robot via REST calls and try to find the fastest way.
Or block the others.

## Features

* Robots
  * A robot can travel in a 2D space (no diagonals)
* Several maps with start and target zones
  * Maps my contain gas stations
  * Every step costs some fuel
* Round-based gameplay
  * Every robot schedules its action, then all actions are executed

And more technical:

* Watch all robots go via a web-interface.
* Included "How to play on" for players
* Full API docs for learning to control the robots and get map information
* Additional web-sections for registering, first steps, initial debug of your robot actions
* Included root user settings to change maps, import own ones and change game settings
* Self-hosting setup with docker

## Setup

For an easy setup, a docker-compose file is provided in the `deploy` folder.
It is just a basic setup with traefik as reverse proxy on `http`.
Depending on the environment a certificate for TLS is recommended.

### Environment variables frontend

* `PLAUSIBLE_DOMAIN`: The tracked domain for Plausible.
* `PLAUSIBLE_API_HOST` (optional): An alternative Plausible api host. If not set https://plausible.io is used.

### Environment variables backend

* `SPRING_DATASOURCE_*`: Those environment variables are used to connect to an external database.
* `SPRING_PROFILES_ACTIVE`: Set this environment variable to `prod` to disable some dev features.
  It will also hide the OpenApi Docs for all internal interfaces.
* `INITIAL_ROOT_PASSWORD` (optional): The initial root user password.
  If not set a random one is generated at first start and output in the backend log.
* `AUTH_ISSUER` (optional): Set an explicit issuer string for the auth tokens.
  This can be useful for parallel instances and should be set in production-like envs.
* `AUTH_KEY`(optional): Set the input for the JWT signing key.
  This should be a random string with the length of 64. If changed every user needs to re-login to make auth work correctly again.

### Admin authentication

At the first start the user `root` is created with a random password which gets displayed **one-time at the first backend start** in the start logs.
The password can also be specified via an environment variable, but keep it mind it will only be used one-time at the first start.

The admin user can never participate in a game!

### Plausible tracking

If configured the application can be monitored with [Plausible](https://plausible.io/). So far only page calls are tracked.

To enable this set the environment variable `PLAUSIBLE_DOMAIN`
and if you are running on a self-hosted instance `PLAUSIBLE_API_HOST`.
An example is shown in the deployment docker-compose.

Please create the following goals to also get the events:

* `User logged in`
* `User registered`

## Note

This software will get no versioning and lives on the bloody main branch.

## Development

### Documentation

The basics will be here in this README, but there are more diagrams and documents in the `docs` and `maps` folder.

### Requirements

Have a [Java 21 LTS](https://adoptium.net/de/temurin/releases/?package=jdk&version=21), [node 20](https://nodejs.org/en/download/) and
[python 3.10](https://www.python.org/downloads/) installation is required to make it all run.
Make sure you have [podman](https://podman.io/docs/installation) and [podman-compose](https://github.com/containers/podman-compose)
(or docker and docker-compose) installed on your system, since the dev environment runs on a container-based reverse proxy.

> [!INFO]
> Make sure to have also git lfs installed on your system! Some files are stored in the large file system.

For the database test testcontainers is used. So make sure you make the podman socket available.
([See here for more](https://podman-desktop.io/docs/migrating-from-docker/using-the-docker_host-environment-variable))
As a second step the used builder `ryuk` needs to be disabled, since it doesn't play well with podman right now.
For this set the environment variable `TESTCONTAINERS_RYUK_DISABLED` to `true`.

### Set up

For development there is a little script in the project root named `start_dev_env.(sh|bat)` (according to your OS).
Execute this first, before you do anything else.

#### Coding Styles

To comply to the coding style, there are some [pre-commit](https://pre-commit.com/) rules.
Those should be automatically executed before every git commit.
To make this happen automatically, make sure to have `pre-commit` installed on your machine (`pip install pre-commit`)
and set up your git hooks (`pre-commit install` | from project root).

#### Run via Intellij

Navigate to the `Application.kt` file and click on the green play button beside the main method. The backend is now running.

For the frontend open up the `package.json` file and click the green play button to the left of the `dev` script.
Make sure you have all dependencies installed via the Intellij UI or install them with `npm install --dev` from terminal.

Now the application can be reached by http://localhost:8888 and http://localhost:8888/api/swagger-ui.

#### Run via terminal

Then run `./gradlew backend:bootRun` (from project root) (or execute it through your IDE) to start your backend.

For the frontend install all packages with `npm install --dev` and run the VUE app with `npm run dev` (from the `frontend` folder).

Now the application can be reached by http://localhost:8888 and http://localhost:8888/api/swagger-ui.
Also, a "secure" version is available via port `8889` as https://localhost:8888.
