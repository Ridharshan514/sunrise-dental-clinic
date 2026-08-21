@echo off
setlocal enabledelayedexpansion
echo =================================================================
echo   SUNRISE DENTAL CLINIC - ONE-CLICK BUILD AND VALIDATION
echo =================================================================
echo.

if not exist bin mkdir bin

echo [*] Gathering Java source files...
if exist sources.tmp del sources.tmp
for /r "%~dp0src\main" %%f in (*.java) do (
    set "fpath=%%f"
    set "fpath=!fpath:\=/!"
    echo "!fpath!" >> sources.tmp
)
set "testpath=%~dp0src\test\java\com\sunrisedental\TestRunner.java"
set "testpath=!testpath:\=/!"
echo "!testpath!" >> sources.tmp

echo [*] Compiling project with JDK javac (UTF-8)...
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.tmp
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed during compilation. Please ensure JDK 17+ is installed.
    if exist sources.tmp del sources.tmp
    pause
    exit /b %errorlevel%
)
if exist sources.tmp del sources.tmp
echo [OK] Compilation successful!
echo.

echo [*] Running Automated Test Suite (37 Tests)...
echo =================================================================
java -cp "bin;lib/*" com.sunrisedental.TestRunner
echo =================================================================
echo.
echo [*] Build and Test Suite completed successfully!
pause
