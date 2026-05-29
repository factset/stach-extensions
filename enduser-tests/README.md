# End-User Tests

These scripts verify each SDK **the way a real consumer would use it** — by building the
release artifact, installing it into a *local* package repository (no public registry / no
`release-sdks.yml` needed), and consuming it **by package name from outside the source tree**.

This catches packaging bugs that unit tests run from `src/` cannot — e.g. a dependency that is
imported but not declared (your dev machine has it; a fresh install does not).

Each script exercises all three V3 entry points: `ConvertJsonToTable`, `ConvertArrowFileToTable`,
`ConvertArrowStreamToTable`.

## Run

From the repo root, in PowerShell:

```powershell
# Python  -> builds wheel, installs into a throwaway venv, runs consume.py
./enduser-tests/python/run_enduser_test.ps1

# .NET    -> dotnet pack to a local NuGet feed folder, consumes from a separate console app
./enduser-tests/dotnet/run_enduser_test.ps1

# Java    -> mvn install into ~/.m2, consumes from a separate Maven project
./enduser-tests/java/run_enduser_test.ps1
```

Each script prints `END-USER TEST PASSED` on success and exits non-zero on failure.

## Prerequisites

| Script | Needs on PATH |
|--------|---------------|
| python | `python` (3.9+) with `pip`, `venv` |
| dotnet | `dotnet` SDK |
| java   | `mvn` + a JDK (JAVA_HOME set) |

## How "local repository" maps to the real release

| Lang   | Release (`release-sdks.yml`)        | Local equivalent used here              |
|--------|-------------------------------------|-----------------------------------------|
| Python | `twine upload` to PyPI              | `pip install <wheel>` into a clean venv |
| .NET   | `nuget push` to nuget.org           | `dotnet nuget push` to a local folder feed |
| Java   | `mvn deploy` to Maven Central       | `mvn install` to local `~/.m2` repo     |
