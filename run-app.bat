@echo off
setlocal enabledelayedexpansion
echo ===================================================
echo   Sunrise Dental Clinic Management System
echo   Starting Standalone REST API & Web Dashboard...
echo ===================================================

if not exist bin mkdir bin

if not exist bin\com\sunrisedental\server\DentalAppServer.class (
    echo Compiling application classes...
    if exist sources.tmp del sources.tmp
    for /r "%~dp0src\main" %%f in (*.java) do (
        set "fpath=%%f"
        set "fpath=!fpath:\=/!"
        echo "!fpath!" >> sources.tmp
    )
    javac -encoding UTF-8 -d bin -cp "lib/*" @sources.tmp
    if %errorlevel% neq 0 (
        echo [ERROR] Compilation failed. Please check your JDK installation.
        if exist sources.tmp del sources.tmp
        pause
        exit /b %errorlevel%
    )
    if exist sources.tmp del sources.tmp
    echo Compilation completed successfully!
    echo.
)

java -cp "bin;lib/*" com.sunrisedental.server.DentalAppServer
pause
