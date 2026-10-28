# PowerShell script to compile and launch the Smart Learning Companion Java Backend
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " Compiling Smart Learning Companion Java Backend..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = Get-ChildItem -Path "backend\src" -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp "backend\lib\*" -d bin $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Compilation successful! Starting Java server on http://localhost:8080..." -ForegroundColor Green
java -cp "bin;backend\lib\*" com.smartlearning.Main 8080
