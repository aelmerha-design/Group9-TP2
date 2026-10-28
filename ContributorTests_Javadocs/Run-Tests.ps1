param(
    [switch]$Manual,
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$LibraryPath = "$PSScriptRoot/.work/lib"
)

$ErrorActionPreference = 'Stop'
if (-not $JdkHome) { $JdkHome = 'C:/Program Files/Java/jdk-24' }
$java = Join-Path $JdkHome 'bin/java.exe'
$javac = Join-Path $JdkHome 'bin/javac.exe'
$javadoc = Join-Path $JdkHome 'bin/javadoc.exe'
foreach ($tool in @($java, $javac, $javadoc)) {
    if (-not (Test-Path -LiteralPath $tool)) { throw "JDK tool missing: $tool. Supply -JdkHome." }
}
foreach ($library in @('javafx-base.jar', 'javafx-graphics.jar', 'javafx-controls.jar', 'javafx-fxml.jar', 'h2.jar')) {
    if (-not (Test-Path -LiteralPath (Join-Path $LibraryPath $library))) {
        throw "Missing $library in $LibraryPath. See README.md for dependency setup."
    }
}
$repository = Split-Path $PSScriptRoot -Parent
$source = Join-Path $repository 'HW2 2/src'
$classes = Join-Path $PSScriptRoot '.work/classes'
$runId = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$testHome = Join-Path $PSScriptRoot ".work/database-$runId"
$evidence = Join-Path $PSScriptRoot 'screenshots'
$results = Join-Path $PSScriptRoot 'results'
New-Item -ItemType Directory -Force $classes, $testHome, $evidence, $results | Out-Null
$sources = @(Get-ChildItem -LiteralPath $source -Recurse -Filter '*.java' | ForEach-Object FullName)
$ErrorActionPreference = 'Continue'
& $javac -encoding UTF-8 --module-path $LibraryPath -d $classes @sources 2>&1 |
    ForEach-Object { "$_" } |
    Tee-Object -FilePath (Join-Path $results 'compile.txt')
$ErrorActionPreference = 'Stop'
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
if ($Manual) {
    $manualHome = Join-Path $PSScriptRoot '.work/manual-database'
    $ErrorActionPreference = 'Continue'
    & $java --enable-native-access=javafx.graphics --module-path "$LibraryPath;$classes" `
        --add-modules FoundationsF26 -cp (Join-Path $LibraryPath 'h2.jar') `
        "-Dcontributor.test.home=$manualHome" "-Dcontributor.test.evidence=$evidence" `
        -m FoundationsF26/guiLessons.LessonManualTesting
    $ErrorActionPreference = 'Stop'
    if ($LASTEXITCODE -ne 0) { throw 'Manual launcher failed.' }
    return
}
$launch = @('--enable-native-access=javafx.graphics', '--module-path', "$LibraryPath;$classes",
    '--add-modules', 'FoundationsF26', '-cp', (Join-Path $LibraryPath 'h2.jar'),
    "-Dcontributor.test.home=$testHome", "-Dcontributor.test.evidence=$evidence",
    '-m', 'FoundationsF26/guiLessons.LessonTestingAutomation')
foreach ($phase in @('first-run', 'restart-run')) {
    $ErrorActionPreference = 'Continue'
    & $java @launch 2>&1 | ForEach-Object { "$_" } | Tee-Object -FilePath (Join-Path $results "$phase.txt")
    $ErrorActionPreference = 'Stop'
    if ($LASTEXITCODE -ne 0) { throw "$phase failed. See results/$phase.txt." }
}
if (Test-Path -LiteralPath (Join-Path $testHome 'restart-test.txt')) { throw 'Restart marker was not consumed.' }
$documented = @('LessonTestingAutomation.java', 'LessonTestEvidence.java', 'LessonManualTesting.java') | ForEach-Object {
    Join-Path $source "guiLessons/$_"
}
$ErrorActionPreference = 'Continue'
Set-Content -LiteralPath (Join-Path $results 'javadoc.txt') -Value 'Generating contributor test Javadoc.'
& $javadoc -quiet -private -linksource '-Xdoclint:all,-missing' -encoding UTF-8 --module-path $LibraryPath `
    -sourcepath $source -d (Join-Path $PSScriptRoot 'javadoc') @documented 2>&1 |
    ForEach-Object { "$_" } | Tee-Object -Append -FilePath (Join-Path $results 'javadoc.txt')
$ErrorActionPreference = 'Stop'
if ($LASTEXITCODE -ne 0) { throw 'Javadoc generation failed.' }
@("Run: $runId", "Java: $(& $java --version | Select-Object -First 1)",
    'JavaFX: 24.0.2 (Windows)', 'H2: 2.3.232', "Isolated database: $testHome",
    'First JVM: PASS', 'Restart JVM: PASS', 'Javadoc: PASS') |
    Set-Content -Encoding UTF8 (Join-Path $results 'environment.txt')
Write-Host 'Both test processes and Javadoc generation passed.'
