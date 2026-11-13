# End-user test for the Python SDK.
# Builds the wheel, installs it into a throwaway venv (a clean room with no access to src/),
# then consumes the package by name. Mirrors a downstream `pip install`.

$ErrorActionPreference = "Stop"

$RepoRoot     = Resolve-Path "$PSScriptRoot\..\.."
$SdkDir       = Join-Path $RepoRoot "python\src"
$ResourcesDir = Join-Path $RepoRoot "python\tests\resources"
$VenvDir      = Join-Path $env:TEMP "stach_py_enduser"

Write-Host "==> Building wheel..." -ForegroundColor Cyan
Push-Location $SdkDir
try {
    Remove-Item -Recurse -Force build, dist, *.egg-info -ErrorAction SilentlyContinue
    python setup.py sdist bdist_wheel | Out-Null
} finally {
    Pop-Location
}

$Wheel = Get-ChildItem (Join-Path $SdkDir "dist\*.whl") | Select-Object -First 1
if (-not $Wheel) { throw "No wheel produced in $SdkDir\dist" }
Write-Host "    built: $($Wheel.Name)"

Write-Host "==> Creating clean venv at $VenvDir ..." -ForegroundColor Cyan
if (Test-Path $VenvDir) { Remove-Item -Recurse -Force $VenvDir }
python -m venv $VenvDir
$Py = Join-Path $VenvDir "Scripts\python.exe"

Write-Host "==> Installing wheel into venv (resolves all declared deps)..." -ForegroundColor Cyan
& $Py -m pip install --quiet --upgrade pip
& $Py -m pip install --quiet $Wheel.FullName

Write-Host "==> Consuming package as an end user..." -ForegroundColor Cyan
& $Py (Join-Path $PSScriptRoot "consume.py") $ResourcesDir
if ($LASTEXITCODE -ne 0) { throw "Python end-user test FAILED (exit $LASTEXITCODE)" }

Write-Host "`n==> Cleaning up venv..." -ForegroundColor Cyan
Remove-Item -Recurse -Force $VenvDir -ErrorAction SilentlyContinue
Write-Host "Python end-user test completed." -ForegroundColor Green
