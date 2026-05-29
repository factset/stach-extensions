# End-user test for the Java SDK.
# Installs the SDK into the local ~/.m2 repo, then a separate Maven project resolves it by GAV
# and consumes it. Mirrors the real `mvn deploy` -> downstream dependency flow, offline.

$ErrorActionPreference = "Stop"

$RepoRoot     = Resolve-Path "$PSScriptRoot\..\.."
$SdkPom       = Join-Path $RepoRoot "java\pom.xml"
$ResourcesDir = Join-Path $RepoRoot "java\src\test\java\Resources"
$ConsumerDir  = Join-Path $PSScriptRoot "consumer"
$ConsumerPom  = Join-Path $ConsumerDir "pom.xml"

# Read the version from the SDK pom so the consumer always matches what we install.
$Version = ([xml](Get-Content $SdkPom)).project.version
Write-Host "==> SDK version: $Version" -ForegroundColor Cyan

Write-Host "==> Installing SDK into local ~/.m2 (skipping tests/signing)..." -ForegroundColor Cyan
# -DskipTests: we are testing consumption, not re-running the SDK's own tests.
# -Dgpg.skip: local install needs no GPG signature (only the real deploy does).
mvn -q -f $SdkPom "-DskipTests" "-Dgpg.skip=true" install
if ($LASTEXITCODE -ne 0) { throw "mvn install of the SDK failed" }

# Apache Arrow (used by the Arrow conversion functions) needs this JVM flag on Java 9+.
# Real consumers on a modern JDK must pass it too; Java 8 does not recognize it.
# Detect the JDK major version from JAVA_HOME's `release` file (avoids stderr from `java -version`).
$JavaMajor = 9   # default: assume a modern JDK that needs the flag
$ReleaseFile = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "release" } else { $null }
if ($ReleaseFile -and (Test-Path $ReleaseFile)) {
    $verItem = Get-Content $ReleaseFile | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
    if ($verItem -match 'JAVA_VERSION="1\.(\d+)') { $JavaMajor = [int]$Matches[1] }   # 1.8 -> 8
    elseif ($verItem -match 'JAVA_VERSION="(\d+)') { $JavaMajor = [int]$Matches[1] }  # 17  -> 17
}
Write-Host "==> Consumer JDK major version: $JavaMajor" -ForegroundColor Cyan
if ($JavaMajor -ge 9) {
    $env:MAVEN_OPTS = ($env:MAVEN_OPTS + " --add-opens=java.base/java.nio=ALL-UNNAMED").Trim()
    Write-Host "    (added --add-opens for Apache Arrow on Java 9+)"
}

Write-Host "==> Resolving + running consumer from local repo (GAV: com.factset.protobuf:stachextensions:$Version)..." -ForegroundColor Cyan
mvn -q -f $ConsumerPom "-Dstachextensions.version=$Version" `
    compile exec:java "-Dexec.args=$ResourcesDir"
if ($LASTEXITCODE -ne 0) { throw "Java end-user test FAILED (exit $LASTEXITCODE)" }

Write-Host "`nJava end-user test completed." -ForegroundColor Green
