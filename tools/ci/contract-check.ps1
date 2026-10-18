<#
.SYNOPSIS
    本地 CI：契约校验 + 全部模块单元测试 + :app debug 构建，任一失败即非零退出。

.DESCRIPTION
    对应任务包 02 §3（tools\ci\contract-check.ps1）与 §4.2 人工检查点 #4。
    用法：
        .\tools\ci\contract-check.ps1                # 常规
        .\tools\ci\contract-check.ps1 -NoBuildCache  # 强制真实执行（禁用构建缓存与增量，防假通过）
    前置：JAVA_HOME 指向 JDK 17+；Android SDK 路径见 local.properties 的 sdk.dir。
#>
[CmdletBinding()]
param(
    [switch]$NoBuildCache
)

$ErrorActionPreference = 'Stop'

# 仓库根 = 本脚本所在目录（tools\ci）的上级的上级
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location -LiteralPath $repoRoot

if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME 未设置：本工程需要 JDK 17+（Android 模块按 Java 17 编译）。'
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
    Write-Host "CI FAILED：$($failed -join ', ')" -ForegroundColor Red
    exit 1
}
Write-Host 'CI OK：contractCheck + test + assembleDebug 全绿' -ForegroundColor Green
exit 0
