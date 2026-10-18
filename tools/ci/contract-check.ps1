<#
.SYNOPSIS
    Local CI: contract check + all module unit tests + :app debug build; exits nonzero on any failure.

.DESCRIPTION
    Implements task package 02 section 3 (tools\ci\contract-check.ps1) and section 4.2 manual checkpoint #4.
    Usage:
        .\tools\ci\contract-check.ps1                 # normal
        .\tools\ci\contract-check.ps1 -NoBuildCache   # force real execution (disable build cache and
                                                      # incremental tasks, to avoid false passes)
    Prerequisite: JAVA_HOME points to JDK 17+; the Android SDK path is in local.properties (sdk.dir).
#>
[CmdletBinding()]
param(
    [switch]$NoBuildCache
)

$ErrorActionPreference = 'Stop'

# Repository root = two levels up from this script's directory (tools\ci)
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location -LiteralPath $repoRoot

if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME is not set: this project requires JDK 17+ (Android modules compile with Java 17).'
}

$gradlew = Join-Path $repoRoot 'gradlew.bat'
if (-not (Test-Path -LiteralPath $gradlew)) {
    $gradlew = Join-Path $repoRoot 'gradlew'
}

$commonArgs = @('--no-daemon', '--console=plain')
if ($NoBuildCache) {
    $commonArgs += @('--no-build-cache', '--rerun-tasks')
}

$steps = @('contractCheck', 'test', 'assembleDebug')
$failed = @()

foreach ($step in $steps) {
    Write-Host ''
    Write-Host "==================== CI STEP: $step ====================" -ForegroundColor Cyan
    & $gradlew $step @commonArgs
    $code = $LASTEXITCODE
    Write-Host "---- $step exit=$code ----"
    if ($code -ne 0) { $failed += $step }
}

Write-Host ''
if ($failed.Count -gt 0) {
    Write-Host "CI FAILED: $($failed -join ', ')" -ForegroundColor Red
    exit 1
}
Write-Host 'CI OK: contractCheck + test + assembleDebug all green' -ForegroundColor Green
exit 0
