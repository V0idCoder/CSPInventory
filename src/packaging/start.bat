@echo off
setlocal enabledelayedexpansion

set "BASE_DIR=%~dp0"
set "APP_DIR=%BASE_DIR%app"
set "JAR_FILE="

for %%f in ("%APP_DIR%\csp-inventory-*.jar") do (
    set "JAR_FILE=%%f"
    goto :jar_found
)

echo Jar file not found in %APP_DIR%
exit /b 1

:jar_found
if "%CSPINVENTORY_HOME%"=="" set "CSPINVENTORY_HOME=%BASE_DIR%CSPInventory"

if not exist "%CSPINVENTORY_HOME%\models" mkdir "%CSPINVENTORY_HOME%\models"
if not exist "%CSPINVENTORY_HOME%\backups" mkdir "%CSPINVENTORY_HOME%\backups"
if not exist "%CSPINVENTORY_HOME%\data" mkdir "%CSPINVENTORY_HOME%\data"

java -Dcspinventory.home="%CSPINVENTORY_HOME%" -jar "%JAR_FILE%"
