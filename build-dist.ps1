# ============================================================
#  build-dist.ps1  —  Eon Weather Generator distribution builder
#  Run this from the project root after: mvn clean package
# ============================================================

$ErrorActionPreference = "Stop"

$JarName  = "eon-weather-generator-1.0-SNAPSHOT.jar"
$DistDir  = "dist"
$JavaHome = $env:JAVA_HOME

if (-not $JavaHome) {
    $JavaHome = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory |
                Where-Object { $_.Name -like "jdk-21*" } |
                Select-Object -First 1 -ExpandProperty FullName
}
if (-not $JavaHome) {
    Write-Error "Could not find Java 21. Set JAVA_HOME or adjust the path in this script."
    exit 1
}
Write-Host "Using Java: $JavaHome"

# ── Clean and create dist/ ────────────────────────────────────
if (Test-Path $DistDir) { Remove-Item $DistDir -Recurse -Force }
New-Item -ItemType Directory -Path "$DistDir\javafx" | Out-Null

# ── Copy JAR ──────────────────────────────────────────────────
$JarPath = "target\$JarName"
if (-not (Test-Path $JarPath)) {
    Write-Error "JAR not found at $JarPath. Run 'mvn clean package' first."
    exit 1
}
Copy-Item $JarPath "$DistDir\eon-weather.jar"
Write-Host "Copied JAR."

# ── Copy all src/ data files (everything except .java source) ─
New-Item -ItemType Directory -Path "$DistDir\src" | Out-Null
Get-ChildItem -Path "src" -Recurse | Where-Object {
    -not $_.PSIsContainer -and $_.Extension -ne ".java" -and $_.Extension -ne ".class"
} | ForEach-Object {
    $dest = $_.FullName.Replace((Resolve-Path "src").Path, (Resolve-Path "$DistDir\src").Path)
    $destDir = Split-Path $dest -Parent
    if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
    Copy-Item $_.FullName $dest
}
# Ensure comments dir exists for runtime use
New-Item -ItemType Directory -Path "$DistDir\src\comments" -Force | Out-Null
Write-Host "Copied src data files."

# ── Extract JavaFX JARs from the fat JAR into javafx/ ─────────
Write-Host "Extracting JavaFX JARs from fat JAR..."
$TempDir = "$DistDir\_extract"
New-Item -ItemType Directory -Path $TempDir | Out-Null
Push-Location $TempDir
& "$JavaHome\bin\jar.exe" xf "..\..\$JarPath"
Pop-Location

# Copy the local Maven cache JavaFX jars instead - more reliable
$MavenFX = "$env:USERPROFILE\.m2\repository\org\openjfx"
$FXModules = @("javafx-base", "javafx-graphics", "javafx-controls")
foreach ($mod in $FXModules) {
    $jar = Get-ChildItem "$MavenFX\$mod\21.0.1\*win*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jar) {
        Copy-Item $jar.FullName "$DistDir\javafx\$($jar.Name)"
        Write-Host "  Copied $($jar.Name)"
    } else {
        Write-Warning "  Could not find $mod in Maven cache"
    }
}
Remove-Item $TempDir -Recurse -Force

# ── Build trimmed JRE ─────────────────────────────────────────
Write-Host "Building trimmed runtime with jlink..."
& "$JavaHome\bin\jlink.exe" `
    --module-path "$JavaHome\jmods" `
    --add-modules java.base,java.desktop,java.logging,java.xml,java.naming,java.scripting,jdk.unsupported,java.datatransfer,java.prefs `
    --output "$DistDir\runtime" `
    --strip-debug `
    --compress 2 `
    --no-header-files `
    --no-man-pages

if ($LASTEXITCODE -ne 0) {
    Write-Warning "jlink failed - copying full JRE instead."
    Copy-Item $JavaHome "$DistDir\runtime" -Recurse
}
Write-Host "Runtime ready."

# ── Write launchers ───────────────────────────────────────────
@'
@echo off
SET DIR=%~dp0
"%DIR%runtime\bin\java.exe" --module-path "%DIR%javafx" --add-modules javafx.controls,javafx.graphics,javafx.base -jar "%DIR%eon-weather.jar"
'@ | Set-Content "$DistDir\run.bat" -Encoding ASCII

@'
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "$dir\runtime\bin\java.exe" --module-path "$dir\javafx" --add-modules javafx.controls,javafx.graphics,javafx.base -jar "$dir\eon-weather.jar"
'@ | Set-Content "$DistDir\run.ps1" -Encoding UTF8

Write-Host ""
Write-Host "============================================"
Write-Host " dist/ is ready!"
Write-Host " Run with: dist\run.bat  or  dist\run.ps1"
Write-Host "============================================"
