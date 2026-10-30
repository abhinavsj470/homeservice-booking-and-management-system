@echo off
title Build Standalone Windows EXE - Home Service System
cd /d "%~dp0"

echo ========================================================
echo  Home Service System - Standalone EXE Generator
echo ========================================================
echo.

:: 1. Locate JDK Tools (jar.exe and jpackage.exe)
set "JAR_CMD=jar"
set "JPACKAGE_CMD=jpackage"

where jar >nul 2>&1
if errorlevel 1 (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
    ) else if exist "C:\Program Files\Java\jdk-27\bin\jar.exe" (
        set "JAR_CMD=C:\Program Files\Java\jdk-27\bin\jar.exe"
    ) else if exist "C:\Program Files\Java\latest\bin\jar.exe" (
        set "JAR_CMD=C:\Program Files\Java\latest\bin\jar.exe"
    )
)

where jpackage >nul 2>&1
if errorlevel 1 (
    if exist "%JAVA_HOME%\bin\jpackage.exe" (
        set "JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe"
    ) else if exist "C:\Program Files\Java\jdk-27\bin\jpackage.exe" (
        set "JPACKAGE_CMD=C:\Program Files\Java\jdk-27\bin\jpackage.exe"
    ) else if exist "C:\Program Files\Java\latest\bin\jpackage.exe" (
        set "JPACKAGE_CMD=C:\Program Files\Java\latest\bin\jpackage.exe"
    )
)

taskkill /f /im HomeService.exe >nul 2>&1

echo [1/5] Compiling Java source files...
if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
if errorlevel 1 (
    del sources.txt >nul 2>&1
    echo [ERROR] Compilation failed. Aborting.
    pause
    exit /b %errorlevel%
)
del sources.txt >nul 2>&1
echo   - Sources compiled successfully.

echo.
echo [2/5] Packaging application JAR and dependencies...
if not exist "dist" mkdir dist
if exist "dist\staging" rmdir /s /q "dist\staging"
mkdir "dist\staging"

echo Main-Class: com.homeservice.App> dist\staging\manifest.txt
echo Class-Path: flatlaf-3.4.1.jar postgresql-42.7.3.jar mysql-connector-j-8.3.0.jar>> dist\staging\manifest.txt

"%JAR_CMD%" cfm "dist\staging\HomeService.jar" "dist\staging\manifest.txt" -C bin .
copy /y "lib\*" "dist\staging\" >nul
del "dist\staging\manifest.txt" >nul 2>&1
echo   - HomeService.jar created.

echo.
echo [3/5] Bundling standalone Windows EXE with embedded runtime via jpackage...
echo   (This bundles a lightweight private JRE so other devices don't need Java installed)
if exist "dist\HomeService" rmdir /s /q "dist\HomeService"

"%JPACKAGE_CMD%" --input "dist\staging" --name "HomeService" --main-jar "HomeService.jar" --main-class "com.homeservice.App" --type app-image --dest "dist"
if errorlevel 1 (
    echo [ERROR] jpackage failed to generate standalone application.
    pause
    exit /b %errorlevel%
)
rmdir /s /q "dist\staging" >nul 2>&1
echo   - HomeService.exe created successfully.

echo.
echo [4/5] Copying database configuration and UI assets into release package...
copy /y "db.properties" "dist\HomeService\" >nul
if exist "icons" (
    xcopy /e /i /y "icons" "dist\HomeService\icons" >nul
)
if not exist "dist\HomeService\avatars" mkdir "dist\HomeService\avatars"
echo   - db.properties, icons, and avatars folder integrated.

echo.
echo [5/6] Creating shareable ZIP archive for other devices...
if exist "dist\HomeService-Windows.zip" del /f /q "dist\HomeService-Windows.zip"
powershell -NoProfile -Command "Compress-Archive -Path 'dist\HomeService' -DestinationPath 'dist\HomeService-Windows.zip' -Force"
echo   - Created: dist\HomeService-Windows.zip

echo.
echo [6/6] Generating single-file 1-Click Installer (HomeServiceSetup.exe)...
set "CSC_CMD=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if exist "%CSC_CMD%" (
    if exist "dist\HomeServiceSetup.exe" del /f /q "dist\HomeServiceSetup.exe"
    "%CSC_CMD%" /nologo /target:winexe /out:dist\HomeServiceSetup.exe /r:System.Windows.Forms.dll,System.Drawing.dll,Microsoft.CSharp.dll,System.Core.dll,C:\Windows\Microsoft.NET\Framework64\v4.0.30319\System.IO.Compression.FileSystem.dll /resource:dist\HomeService-Windows.zip,payload.zip installer\SetupForm.cs >nul
    echo   - Created: dist\HomeServiceSetup.exe (Single executable - no ZIP extraction needed!)
)

echo.
echo ========================================================
echo  BUILD COMPLETE!
echo ========================================================
echo.
echo  BEST WAY TO SHARE WITH OTHER DEVICES:
echo  --------------------------------------
echo  Send: dist\HomeServiceSetup.exe
echo  (The other laptops just double-click HomeServiceSetup.exe to install and run!)
echo.
echo  Or Portable ZIP: dist\HomeService-Windows.zip
echo ========================================================
pause
