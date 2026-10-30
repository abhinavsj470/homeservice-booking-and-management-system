@echo off
title Home Service Booking and Management System
cd /d "%~dp0"
echo ========================================================
echo  Home Service Booking and Management System
echo  Submitted by: Abhinav Sajeev, Alan Jojo, Allan Peter, Sebin Raju
echo ========================================================
echo.
echo [1/2] Compiling source files...
if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. Please inspect the error messages above.
    pause
    exit /b %errorlevel%
)

echo [2/2] Launching GUI Application Window...
java --enable-native-access=ALL-UNNAMED -cp "bin;lib/*" com.homeservice.App
pause
