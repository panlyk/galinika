$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$version = ([xml](Get-Content 'pom.xml')).project.version
$oldProduct = '{2742FFED-B5D1-3928-AC03-8B73281DF99F}'
$upgradeId = '{66995049-6C47-42FB-BCB6-FB78E5DBAFBC}'
$uninstall = 'HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall'
New-Item -ItemType Directory -Force 'target/upgrade-test' | Out-Null

# Test on an isolated Windows runner, never on a developer's installed application.
Invoke-WebRequest 'https://github.com/panlyk/galinika/releases/download/v0.1.3/Galinika-0.1.3.exe' -OutFile 'target/upgrade-test/old.exe'
if ((Get-FileHash 'target/upgrade-test/old.exe' -Algorithm SHA256).Hash -ne 'B7506640FB9C0BF06CFE14FF0BDB7E79AB9B345C4E124B6BE8F35529017765B2') {
    throw 'Original installer checksum mismatch'
}
# jpackage embeds an OLE/MSI resource in its EXE wrapper. Extract it for silent setup.
$bytes = [IO.File]::ReadAllBytes((Join-Path $PWD 'target/upgrade-test/old.exe'))
$magic = [byte[]](0xD0,0xCF,0x11,0xE0,0xA1,0xB1,0x1A,0xE1)
$offset = -1
for ($i = 0; $i -lt [Math]::Min($bytes.Length - 8, 8MB); $i++) {
    if ($bytes[$i] -ne $magic[0]) { continue }
    $match = $true
    for ($j = 1; $j -lt 8; $j++) { if ($bytes[$i+$j] -ne $magic[$j]) { $match = $false; break } }
    if ($match) { $offset = $i; break }
}
if ($offset -lt 0) { throw 'Embedded MSI not found' }
$oldMsi = Join-Path $PWD 'target/upgrade-test/old.msi'
$stream = [IO.File]::Create($oldMsi)
try { $stream.Write($bytes, $offset, $bytes.Length - $offset) } finally { $stream.Dispose() }

function Read-MsiProperty($msi, $property) {
    $installer = New-Object -ComObject WindowsInstaller.Installer
    $database = $installer.OpenDatabase((Resolve-Path $msi).Path, 0)
    $view = $database.OpenView("SELECT ``Value`` FROM ``Property`` WHERE ``Property``='$property'")
    [void]$view.Execute(); $record = $view.Fetch()
    if ($null -eq $record) { throw "Missing MSI property $property" }
    $value = $record.StringData(1)
    [void]$view.Close()
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($record)
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($view)
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($database)
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($installer)
    return $value
}
$newMsi = @(Get-ChildItem 'target/jpackage' -Filter '*.msi' -Recurse)[0].FullName
$oldUpgrade = Read-MsiProperty $oldMsi 'UpgradeCode'
$newUpgrade = Read-MsiProperty $newMsi 'UpgradeCode'
Write-Host "::notice title=Installer identity::Original=$oldUpgrade New=$newUpgrade Expected=$upgradeId"
if ([guid]$oldUpgrade -ne [guid]$upgradeId -or [guid]$newUpgrade -ne [guid]$upgradeId) {
    throw "Upgrade identity changed: original=$oldUpgrade new=$newUpgrade expected=$upgradeId"
}
if ((Read-MsiProperty $newMsi 'ProductVersion') -ne $version) { throw 'Incorrect version' }
if ((Read-MsiProperty $newMsi 'ALLUSERS') -ne '1') { throw 'Installer must retain machine-wide scope' }

$log = Join-Path $PWD 'target/upgrade-test/old-install.log'
$p = Start-Process msiexec.exe -ArgumentList "/i `"$oldMsi`" /qn /norestart /L*v `"$log`"" -Wait -PassThru
if ($p.ExitCode -notin @(0,3010)) { throw "Old installation failed: $($p.ExitCode)" }
if (!(Test-Path "$uninstall\$oldProduct")) { throw 'Old product registration missing' }

# Keep valid user libraries outside Program Files, as the original app does.
$data = Join-Path $env:APPDATA 'Galinika'
New-Item -ItemType Directory -Force $data | Out-Null
[IO.File]::WriteAllText((Join-Path $data 'substances.csv'), "CUSTOM INGREDIENT,1.23`n", [Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText((Join-Path $data 'types.csv'), "CUSTOM TYPE,50,10,2.26,0.21`n", [Text.UTF8Encoding]::new($false))
$before = @{}
foreach ($file in Get-ChildItem $data -Filter '*.csv') { $before[$file.Name] = (Get-FileHash $file.FullName).Hash }

$exe = (Resolve-Path "release/windows/Galinika-$version.exe").Path
$log = Join-Path $PWD 'target/upgrade-test/new-install.log'
$p = Start-Process $exe -ArgumentList "/quiet /norestart /L*v `"$log`"" -Wait -PassThru
if ($p.ExitCode -notin @(0,3010)) { throw "Upgrade failed: $($p.ExitCode)" }
if (Test-Path "$uninstall\$oldProduct") { throw 'Old product was not removed' }
$installed = @(Get-ChildItem $uninstall | Get-ItemProperty | Where-Object { $_.PSObject.Properties['DisplayName'] -and $_.DisplayName -eq 'Galinika' })
if ($installed.Count -ne 1 -or $installed[0].DisplayVersion -ne $version) { throw 'Expected one upgraded Galinika installation' }
foreach ($name in $before.Keys) {
    if ((Get-FileHash (Join-Path $data $name)).Hash -ne $before[$name]) { throw "User data changed: $name" }
}
$app = Join-Path $env:ProgramFiles 'Galinika'
$installedJar = Join-Path $app "app/galinika-$version.jar"
if (!(Test-Path $installedJar)) { throw 'Upgraded application JAR missing' }
# Exercise the installed JAR using the installed Java runtime, not the runner JDK.
& "$app/runtime/bin/java.exe" -cp "$installedJar;$PWD/target/test-classes" galinika.UiSmokeTest
if ($LASTEXITCODE -ne 0) { throw 'Installed application smoke test failed' }
Write-Host "PASS: 0.1.3 -> $version upgrade, one product, saved libraries preserved, installed UI works"
