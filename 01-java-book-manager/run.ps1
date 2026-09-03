$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$out = Join-Path $root "out"
if (Test-Path $out) { Remove-Item -LiteralPath $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

$sourceFiles = (Get-ChildItem -LiteralPath (Join-Path $root "src") -Recurse -Filter *.java).FullName
$connector = Read-Host "请输入 mysql 驱动 jar 文件名（放在项目根目录，如 mysql-connector-j-8.0.33.jar）"
$connectorPath = Join-Path $root $connector
if (-not (Test-Path -LiteralPath $connectorPath)) {
    Write-Error "找不到 $connector，请先下载 MySQL Connector/J 并放到项目根目录。"
}

javac -encoding UTF-8 -d $out @($sourceFiles)
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Set-Location $root
java -cp "$out;$connectorPath" com.demo.library.Main
