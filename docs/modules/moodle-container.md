# Moodle Container (moodle)

## Purpose
Provides a Moodle LMS instance for integration testing.

## Build Details
- Dockerfile: [moodle/Dockerfile](moodle/Dockerfile)
- Entry point: [moodle/entrypoint.sh](moodle/entrypoint.sh)

Key behaviors:
- Builds from `php:8.4-apache` and clones Moodle `MOODLE_500_STABLE`.
- Installs PHP extensions required by Moodle.
- Creates `moodledata` and configures OPcache.
- Starts `cron` and Apache in the entrypoint.

## Runtime Settings
The service is started via [docker-compose.yml](docker-compose.yml):
- Exposed port: 80
- Data volume: `moodledata` for persistent Moodle files

## Integration Notes
The orchestrator uses the Moodle REST endpoint configured in [demo/src/main/resources/application.properties](demo/src/main/resources/application.properties).
