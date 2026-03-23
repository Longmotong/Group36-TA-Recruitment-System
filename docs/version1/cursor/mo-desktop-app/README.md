# MO Desktop (Swing) - Stand-alone Java App

## What’s included

- `MO Dashboard`
- `Application Review Module`
- No `Job Management Module`

All MO/TA/course/application/status data are loaded from `version1/data（1）/data` JSON files (no fabricated data).

## Prerequisites

- JDK 17+ (tested with Java 21)

## Build

Open PowerShell in this folder (`.cursor/mo-desktop-app`) and run:

```powershell
$files = Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -source 17 -target 17 -d target/classes $files
```

## Run

```powershell
java -cp target/classes com.ebuko.moapp.Main
```

## Behavior notes

- Default “logged-in” MO is `u_mo_001` (from your data).
- The top-right MO dropdown refreshes the dashboard and review list.
- Clicking an application loads its details; you can set `accepted/rejected` and click `Save Review` to persist changes back to `data/.../applications/app_*.json`.

