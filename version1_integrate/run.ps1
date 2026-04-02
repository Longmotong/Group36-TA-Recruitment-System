$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "terminal-build-run.ps1") @args
exit $LASTEXITCODE
