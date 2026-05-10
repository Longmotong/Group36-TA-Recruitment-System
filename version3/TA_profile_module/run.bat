@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set CP=out;lib\gson-2.11.0.jar;lib\flatlaf-3.4.1.jar

java -cp "%CP%" profile_module.Main
