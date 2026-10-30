@echo off
title Automated Backend Tests - Home Service System
cd /d "%~dp0"
echo ========================================================
echo  Automated Test Suite (Terminal / Console Mode)
echo ========================================================
echo.
echo NOTE: This script runs the automated backend unit & database tests in the terminal.
echo To launch the Graphical User Interface (GUI App), run: run.bat
echo.
echo Compiling tests...
if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt
echo Running test suite...
java --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.homeservice.TestRunner
echo.
echo Test execution complete.
pause
