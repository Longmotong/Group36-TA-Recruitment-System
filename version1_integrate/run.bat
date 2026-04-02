@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0terminal-build-run.ps1" %*
exit /b %errorlevel%
