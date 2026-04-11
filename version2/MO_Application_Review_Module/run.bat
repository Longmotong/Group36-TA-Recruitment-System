@echo off
setlocal EnableExtensions

cd /d "%~dp0"

echo ========================================
echo   TA Review Standalone (Swing) Runner
echo ========================================
echo.

if not exist "lib" (
  echo [ERROR] Missing lib folder: "%cd%\lib"
  echo Please create lib folder and put Jackson jars inside:
  echo   - jackson-databind-*.jar
  echo   - jackson-core-*.jar
  echo   - jackson-annotations-*.jar
  echo.
  pause
  exit /b 1
)

set "OUT_DIR=out"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

set "DATA_ROOT=..\data2\data"
if not exist "%DATA_ROOT%\applications" (
  echo [WARN] Preferred data folder not found: "%DATA_ROOT%"
  echo [WARN] App will use internal fallback path resolution.
) else (
  echo [INFO] Using data root: "%DATA_ROOT%"
)

echo [1/2] Compiling Java sources...
javac -encoding UTF-8 -d "%OUT_DIR%" -cp "lib/*" src\main\java\edu\ebu6304\standalone\MainApp.java src\main\java\edu\ebu6304\standalone\model\*.java src\main\java\edu\ebu6304\standalone\service\*.java
if errorlevel 1 (
  echo.
  echo [ERROR] Compilation failed.
  pause
  exit /b 1
)

echo [2/2] Launching app...
java -Dapp.data.root="%DATA_ROOT%" -cp "%OUT_DIR%;lib/*" edu.ebu6304.standalone.MainApp

echo.
echo App exited.
pause
endlocal
