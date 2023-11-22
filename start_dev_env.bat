@echo off
setlocal enabledelayedexpansion

:: Check if podman-compose is available
where podman-compose > nul 2>&1
if %errorlevel% equ 0 (
    set "exec=podman-compose"
) else (
    :: Check if docker-compose is available
    where docker-compose > nul 2>&1
    if %errorlevel% equ 0 (
        set "exec=docker-compose"
    ) else (
        :: If neither is available, assume docker compose
        set "exec=docker compose"
    )
)

:: Set hostname
set "HOSTNAME=%COMPUTERNAME%.local"

echo Container startup
:: Execute the selected command
call %exec% up %*

endlocal
