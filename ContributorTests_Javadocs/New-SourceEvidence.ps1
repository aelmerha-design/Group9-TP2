param(
    [string]$BrowserPath = 'C:/Program Files/Google/Chrome/Application/chrome.exe'
)

$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $BrowserPath)) { throw 'Supply -BrowserPath pointing to Chrome or Edge.' }
$repository = Split-Path $PSScriptRoot -Parent
$sourceDirectory = Join-Path $PSScriptRoot 'source'
$screenshots = Join-Path $PSScriptRoot 'screenshots'
$browserProfile = Join-Path $PSScriptRoot '.work/evidence-browser'
New-Item -ItemType Directory -Force $sourceDirectory, $screenshots, $browserProfile | Out-Null
$manifest = [System.Collections.Generic.List[string]]::new()
$manifest.Add('# Test source screenshots')
$manifest.Add('')
$manifest.Add('These browser screenshots display the actual Java sources, including internal comments, with original line numbers. Pages cover every line of all three test classes. The HTML is an escaped copy of the source, not an alternative test implementation. Regenerate after changing source or Javadoc.')
$manifest.Add('')
$manifest.Add('| Source file / SHA-256 | Lines | Source HTML | Screenshot |')
$manifest.Add('| --- | --- | --- | --- |')
$caseManifest = [System.Collections.Generic.List[string]]::new()
$caseManifest.Add('| Case | Commented implementation begins | Screenshot |')
$caseManifest.Add('| --- | --- | --- |')
$captureJobs = [System.Collections.Generic.List[object]]::new()
foreach ($className in @('LessonTestingAutomation', 'LessonManualTesting', 'LessonTestEvidence')) {
    $sourcePath = Join-Path $repository "HW2 2/src/guiLessons/$className.java"
    $sourceLines = [System.IO.File]::ReadAllLines($sourcePath)
    $sourceHash = (Get-FileHash -LiteralPath $sourcePath -Algorithm SHA256).Hash
    $linesPerPage = 45
    for ($offset = 0; $offset -lt $sourceLines.Length; $offset += $linesPerPage) {
        $lastIndex = [Math]::Min($offset + $linesPerPage, $sourceLines.Length) - 1
        $firstLine = $offset + 1
        $lastLine = $lastIndex + 1
        $baseName = "source-$className-$firstLine-$lastLine"
        $htmlPath = Join-Path $sourceDirectory "$baseName.html"
        $imagePath = Join-Path $screenshots "$baseName.png"
        $rows = for ($lineIndex = $offset; $lineIndex -le $lastIndex; $lineIndex++) {
            $escaped = [System.Net.WebUtility]::HtmlEncode($sourceLines[$lineIndex].Replace("`t", '    '))
            '<div class="line"><span class="number">' + ($lineIndex + 1) + '</span><code>' + $escaped + '</code></div>'
            if ($sourceLines[$lineIndex] -match '// CASE (\d+):') {
                $caseManifest.Add("| $($Matches[1]) | $className.java line $($lineIndex + 1) | [Source screenshot](screenshots/$baseName.png) |")
            }
        }
        $page = @"
<!doctype html><html lang="en"><meta charset="utf-8"><title>$className - lines $firstLine-$lastLine</title>
<style>body{margin:24px;background:white;color:#17212b;font-family:Arial,sans-serif}h1{font-size:22px;margin:0 0 8px}p{font-size:14px;margin:6px 0}.source{margin-top:16px;border:1px solid #ccd4dc;padding:10px;background:#f8fafc}.line{display:flex;white-space:pre;font:14px/21px Consolas,monospace;min-height:21px}.number{color:#526270;display:inline-block;flex:0 0 48px;text-align:right;margin-right:18px;border-right:1px solid #d6dce1;padding-right:12px}code{font:inherit}.hash{font:12px Consolas,monospace}</style>
<h1>$className.java - source lines $firstLine-$lastLine</h1><p>Contributor test implementation / original Java source and internal documentation</p>
<p class="hash">SHA-256: $sourceHash</p><div class="source">$($rows -join "`n")</div></html>
"@
        [System.IO.File]::WriteAllText($htmlPath, $page)
        $manifest.Add("| $className.java / $sourceHash | $firstLine-$lastLine | [HTML](source/$baseName.html) | [PNG](screenshots/$baseName.png) |")
        $captureJobs.Add(@{ Page = $htmlPath; Image = $imagePath })
    }
}
$manifest.Add('')
$manifest.Add('## Case locations')
$manifest.Add('')
$manifest.Add('Each link locates the case comment. Continue into subsequent numbered source pages when the implementation crosses a page boundary. Shared validation, comparison, dialog, and reporting helpers are included in the complete source pages above.')
$manifest.Add('')
$manifest.AddRange($caseManifest)
$manifest.Add('')
$manifest.Add('Manual procedures MT-01 through MT-08 are documented in ManualTests.md and in the LessonManualTesting Javadoc. Its source screenshots show the fixture, real MVC launch, screenshot key, and orderly shutdown used by those procedures; the human performs the actions in the checklist.')
foreach ($documentation in @(
    @{ Class = 'LessonTestingAutomation'; Image = '06-test-javadoc.png' },
    @{ Class = 'LessonTestEvidence'; Image = '07-helper-javadoc.png' },
    @{ Class = 'LessonManualTesting'; Image = '08-manual-javadoc.png' }
)) {
    $pagePath = Join-Path $PSScriptRoot "javadoc/FoundationsF26/guiLessons/$($documentation.Class).html"
    if (-not (Test-Path -LiteralPath $pagePath)) { throw "Generate Javadoc first: $pagePath" }
    $captureJobs.Add(@{ Page = $pagePath; Image = (Join-Path $screenshots $documentation.Image) })
}
foreach ($capture in $captureJobs) {
    $pageUri = [System.Uri]::new($capture.Page).AbsoluteUri
    $arguments = @('--headless', '--disable-gpu', '--no-first-run', '--no-default-browser-check',
        '--hide-scrollbars', '--window-size=2000,1200', '--virtual-time-budget=1000',
        "--user-data-dir=`"$browserProfile`"", "--screenshot=`"$($capture.Image)`"", "`"$pageUri`"")
    $browser = Start-Process -FilePath $BrowserPath -ArgumentList $arguments -WindowStyle Hidden -PassThru
    if (-not $browser.WaitForExit(30000)) {
        $browser.Kill()
        throw "Browser timed out capturing $pageUri"
    }
    if (-not (Test-Path -LiteralPath $capture.Image)) { throw "Screenshot missing: $($capture.Image)" }
    Write-Host "Captured $([System.IO.Path]::GetFileName($capture.Image))"
}
[System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'SourceScreenshots.md'), $manifest)
Write-Host "Saved $($captureJobs.Count) screenshots and SourceScreenshots.md."
