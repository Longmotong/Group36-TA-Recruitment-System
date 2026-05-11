@echo off
REM Run from repo root (version1_integrate): shared data is ../data
cd /d "%~dp0"
call "%~dp0run.bat" %*
