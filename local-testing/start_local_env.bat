@echo off
setlocal enabledelayedexpansion

:: Check if podman machine is available and not already running
where podman > nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('podman machine inspect') do (
        echo %%i | findstr /i /c:"\"State\": \"running\"" > nul
        if !errorlevel! neq 0 (
            echo Podman machine detected but not running, starting machine...
            podman machine start
            goto :continue
        ) else (
            echo Podman machine is already running.
            goto :continue
        )
    )
)

:continue
:: Check if docker machine is available and not already running
where docker-machine > nul 2>&1
if %errorlevel% equ 0 (
    docker-machine ls --filter "STATE=Running" | findstr /i "Running" > nul
    if %errorlevel% neq 0 (
        echo Docker machine detected but not running, starting the default machine...
        docker-machine start default
        :: Set Docker environment variables
        for /f "tokens=*" %%i in ('docker-machine env --shell cmd default') do %%i
    ) else (
        echo Docker machine is already running.
    )
)

:: Determine the appropriate compose command
where podman-compose > nul 2>&1
if %errorlevel% equ 0 (
    set "exec=podman-compose"
) else (
    where docker-compose > nul 2>&1
    if %errorlevel% equ 0 (
        set "exec=docker-compose"
    ) else (
        set "exec=docker compose"
    )
)

echo Container startup
call %exec% up %*

endlocal
