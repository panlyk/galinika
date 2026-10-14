$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$version = ([xml](Get-Content 'pom.xml')).project.version
$jar = "galinika-$version.jar"
$inputDirectory = Join-Path $PWD 'target/installer-input'
New-Item -ItemType Directory -Force $inputDirectory | Out-Null
Copy-Item "target/$jar" $inputDirectory

# Extracted from the MSI embedded in the original Galinika-0.1.3.exe release.
# Keep this UUID and the machine-wide scope for every future upgrade.
$upgradeId = '66995049-6C47-42FB-BCB6-FB78E5DBAFBC'
$packageArguments = @('--type', 'exe', '--name', 'Galinika', '--app-version', $version,
    '--input', $inputDirectory, '--main-jar', $jar, '--main-class', 'galinika.Main',
    '--dest', 'release/windows', '--temp', 'target/jpackage', '--vendor', 'Unknown',
    '--install-dir', 'Galinika', '--win-upgrade-uuid', $upgradeId,
    '--win-menu', '--win-menu-group', 'Unknown', '--win-shortcut', '--verbose')
& jpackage @packageArguments
if ($LASTEXITCODE -ne 0) { throw 'jpackage failed' }
$installer = "release/windows/Galinika-$version.exe"
if (!(Test-Path $installer)) { throw 'Installer was not created' }
$hash = (Get-FileHash $installer -Algorithm SHA256).Hash.ToLowerInvariant()
Set-Content "$installer.sha256" "$hash  Galinika-$version.exe" -Encoding ascii
