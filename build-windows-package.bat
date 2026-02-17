@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

set "MVN_CMD="
if exist "%~dp0mvnw.cmd" (
    set "MVN_CMD=%~dp0mvnw.cmd"
) else (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo Maven introuvable et Maven Wrapper absent.
        echo Ajoute le fichier mvnw.cmd au projet ou installe Maven.
        exit /b 1
    )
    set "MVN_CMD=mvn"
)

where jpackage >nul 2>nul
if errorlevel 1 (
    echo jpackage introuvable. Installe un JDK 21 et verifie PATH/JAVA_HOME.
    exit /b 1
)

if "%MAVEN_OPTS%"=="" (
    set "MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT"
) else (
    set "MAVEN_OPTS=%MAVEN_OPTS% -Djavax.net.ssl.trustStoreType=Windows-ROOT"
)

echo [1/4] Build Maven ^(wrapper en priorite^)...
call "%MVN_CMD%" -DskipTests clean package
if errorlevel 1 exit /b 1

set "INPUT_DIR=target\jpackage-input"
set "OUTPUT_DIR=target\installer"

if exist "%INPUT_DIR%" rmdir /s /q "%INPUT_DIR%"
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%INPUT_DIR%"
mkdir "%INPUT_DIR%\lib"
mkdir "%OUTPUT_DIR%"

set "APP_JAR="
for %%f in (target\csp-inventory-*.jar) do (
    set "APP_JAR=%%~nxf"
)

if "%APP_JAR%"=="" (
    echo Jar principal introuvable dans target\.
    exit /b 1
)

copy /y "target\%APP_JAR%" "%INPUT_DIR%\%APP_JAR%" >nul
for %%f in (target\*.jar) do (
    if /I not "%%~nxf"=="%APP_JAR%" (
        copy /y "%%f" "%INPUT_DIR%\lib\%%~nxf" >nul
    )
)

echo [2/4] Creation app-image Windows...
jpackage ^
  --type app-image ^
  --name CSPInventory ^
  --dest "%OUTPUT_DIR%" ^
  --input "%INPUT_DIR%" ^
  --main-jar "%APP_JAR%" ^
  --main-class com.cspinventory.app.MainApp ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-shortcut

if errorlevel 1 exit /b 1

if /I "%1"=="msi" (
    echo [3/4] Creation installateur MSI...
    jpackage ^
      --type msi ^
      --name CSPInventory ^
      --dest "%OUTPUT_DIR%" ^
      --input "%INPUT_DIR%" ^
      --main-jar "%APP_JAR%" ^
      --main-class com.cspinventory.app.MainApp ^
      --java-options "-Dfile.encoding=UTF-8" ^
      --win-dir-chooser ^
      --win-menu ^
      --win-shortcut
    if errorlevel 1 exit /b 1
)

echo [4/4] Termine.
echo Resultat: %OUTPUT_DIR%\CSPInventory\
if /I "%1"=="msi" echo Resultat MSI: %OUTPUT_DIR%\CSPInventory-*.msi
exit /b 0
