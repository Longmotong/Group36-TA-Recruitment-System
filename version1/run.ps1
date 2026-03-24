$ErrorActionPreference = "Stop"

Write-Host "== TA App: Build & Run ==" -ForegroundColor Cyan

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Host "Error: javac not found. Please install JDK and add it to PATH." -ForegroundColor Red
    exit 1
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Error: java not found. Please install JDK and add it to PATH." -ForegroundColor Red
    exit 1
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$outDir = Join-Path $projectRoot "out"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$javaFiles = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if (-not $javaFiles -or $javaFiles.Count -eq 0) {
    Write-Host "Error: no .java files found under src." -ForegroundColor Red
    exit 1
}

Write-Host "Compiling sources..." -ForegroundColor Yellow
javac -encoding UTF-8 -d $outDir $javaFiles
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Starting application..." -ForegroundColor Green
java -cp $outDir com.taapp.Main
exit $LASTEXITCODE

