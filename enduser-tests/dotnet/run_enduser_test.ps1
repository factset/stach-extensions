# End-user test for the .NET SDK.
# Packs the SDK into a LOCAL NuGet feed folder, then a separate console app consumes it by
# package id+version. Mirrors the real `nuget push` -> `dotnet add package` flow, offline.

$ErrorActionPreference = "Stop"

$RepoRoot     = Resolve-Path "$PSScriptRoot\..\.."
$Csproj       = Join-Path $RepoRoot "dotnet\StachExtensions\FactSet.Protobuf.Stach.Extensions\FactSet.Protobuf.Stach.Extensions.csproj"
$ResourcesDir = Join-Path $RepoRoot "dotnet\StachExtensions\FactSet.Protobuf.Stach.Extensions.Tests\Resources"
$ConsumerDir  = Join-Path $PSScriptRoot "Consumer"
$LocalFeed    = Join-Path $env:TEMP "stach_local_nuget"
$PackageId    = "FactSet.Protobuf.Stach.Extensions"

# Read the version straight from the csproj so the consumer always matches the packed artifact.
$Version = ([xml](Get-Content $Csproj)).Project.PropertyGroup.Version | Where-Object { $_ } | Select-Object -First 1
Write-Host "==> SDK version: $Version" -ForegroundColor Cyan

Write-Host "==> Building SDK (Release)..." -ForegroundColor Cyan
dotnet build $Csproj --configuration Release
if ($LASTEXITCODE -ne 0) { throw "dotnet build failed" }

Write-Host "==> Packing NuGet package into local feed: $LocalFeed" -ForegroundColor Cyan
if (Test-Path $LocalFeed) { Remove-Item -Recurse -Force $LocalFeed }
New-Item -ItemType Directory -Force $LocalFeed | Out-Null
dotnet pack $Csproj --configuration Release --no-build --output $LocalFeed
if ($LASTEXITCODE -ne 0) { throw "dotnet pack failed" }

# NuGet caches restored packages by version. Evict any stale copy so we always get the fresh pack.
$CacheDir = Join-Path $env:USERPROFILE ".nuget\packages\$($PackageId.ToLower())\$Version"
if (Test-Path $CacheDir) {
    Write-Host "==> Evicting cached $PackageId $Version from global packages folder" -ForegroundColor Cyan
    Remove-Item -Recurse -Force $CacheDir
}

Write-Host "==> Writing scoped NuGet.config (local feed + nuget.org only)..." -ForegroundColor Cyan
# <clear/> drops any inherited corporate feeds so we genuinely exercise the local pack;
# nuget.org is kept for transitive public dependencies (Apache.Arrow, Google.Protobuf, ...).
$NugetConfig = Join-Path $ConsumerDir "NuGet.config"
@"
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <clear />
    <add key="local-stach" value="$LocalFeed" />
    <add key="nuget.org" value="https://api.nuget.org/v3/index.json" />
  </packageSources>
</configuration>
"@ | Set-Content -Encoding utf8 $NugetConfig

Write-Host "==> Restoring + running consumer against the local feed..." -ForegroundColor Cyan
Push-Location $ConsumerDir
try {
    # Clean any prior restore so the local feed is genuinely exercised.
    Remove-Item -Recurse -Force obj, bin -ErrorAction SilentlyContinue

    # -p sets the version the PackageReference resolves to; sources come from NuGet.config above.
    dotnet run --configuration Release -p:StachExtensionsVersion=$Version -- $ResourcesDir
    if ($LASTEXITCODE -ne 0) { throw ".NET end-user test FAILED (exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

Write-Host "`n.NET end-user test completed." -ForegroundColor Green
