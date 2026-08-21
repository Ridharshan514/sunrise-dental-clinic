@echo off
setlocal enabledelayedexpansion
echo ===================================================
echo   Sunrise Dental Clinic - Automated Test Runner
echo ===================================================

if not exist bin mkdir bin

echo Compiling Java source files and test suite...
if exist sources.tmp del sources.tmp
for /r "%~dp0src\main" %%f in (*.java) do (
    set "fpath=%%f"
    set "fpath=!fpath:\=/!"
    echo "!fpath!" >> sources.tmp
)
set "testpath=%~dp0src\test\java\com\sunrisedental\TestRunner.java"
set "testpath=!testpath:\=/!"
echo "!testpath!" >> sources.tmp

javac -encoding UTF-8 -d bin -cp "lib/*" @sources.tmp
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please check your JDK installation.
    if exist sources.tmp del sources.tmp
    pause
    exit /b %errorlevel%
)
if exist sources.tmp del sources.tmp

echo Compilation successful!
echo.
echo ===================================================
echo   Executing 37 Automated Test Cases...
echo ===================================================
java -cp "bin;lib/*" com.sunrisedental.TestRunner
pause
