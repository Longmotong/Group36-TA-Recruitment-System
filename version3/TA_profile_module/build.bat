@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

echo ========================================
echo   TA Profile Module (standalone build)
echo ========================================
echo.

if exist out\* (
    echo [1/2] Cleaning out...
    rmdir /s /q out
)
mkdir out

set CP=lib\gson-2.11.0.jar;lib\flatlaf-3.4.1.jar

echo [2/2] Compiling profile_module...
echo ----------------------------------------
javac -encoding UTF-8 -d out -cp "%CP%" -sourcepath src src\profile_module\Main.java 2>&1

if errorlevel 1 (
    echo ----------------------------------------
    echo Build FAILED. Ensure lib\gson-2.11.0.jar and lib\flatlaf-3.4.1.jar exist.
    pause
    exit /b 1
)

echo ----------------------------------------
echo Build SUCCESSFUL. Run: run.bat
echo.
pause
