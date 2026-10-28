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
$manifest.Add('These browser screenshots display actual Java sources, including internal comments, with original line numbers. Pages cover all four test classes. The HTML is an escaped copy of the source, not an alternative implementation. Regenerate after changing source or Javadoc.')
$manifest.Add('')
$manifest.Add('| Source file / SHA-256 | Lines | Source HTML | Screenshot |')
$manifest.Add('| --- | --- | --- | --- |')
$caseManifest = [System.Collections.Generic.List[string]]::new()
$caseManifest.Add('| Case | Commented implementation begins | Screenshot |')
$caseManifest.Add('| --- | --- | --- |')
$captureJobs = [System.Collections.Generic.List[object]]::new()
$methodScreenshots = @{}
foreach ($className in @('LessonTestingAutomation', 'LessonManualTesting', 'LessonTestEvidence', 'ContributorStoryTests')) {
    $sourcePath = Join-Path $repository "HW2 2/src/guiLessons/$className.java"
    $sourceLines = [System.IO.File]::ReadAllLines($sourcePath)
    $sourceHash = (Get-FileHash -LiteralPath $sourcePath -Algorithm SHA256).Hash
    $linesPerPage = 45
    if ($className -eq 'ContributorStoryTests') {
        for ($methodIndex = 0; $methodIndex -lt $sourceLines.Length; $methodIndex++) {
            if ($sourceLines[$methodIndex] -match 'public static void (test\w+)\(') {
                $methodName = $Matches[1]
                $methodEnd = $sourceLines.Length - 1
                for ($following = $methodIndex + 1; $following -lt $sourceLines.Length; $following++) {
                    if ($sourceLines[$following] -match '^\s*(public|private) static ') { $methodEnd = $following - 1; break }
                }
                $pageLinks = @()
                for ($pageOffset = [int]([Math]::Floor($methodIndex / $linesPerPage) * $linesPerPage);
                    $pageOffset -le $methodEnd; $pageOffset += $linesPerPage) {
                    $pageLast = [Math]::Min($pageOffset + $linesPerPage, $sourceLines.Length)
                    $pageName = "source-$className-$($pageOffset + 1)-$pageLast"
                    $pageLinks += "[$pageName.png](screenshots/$pageName.png)"
                }
                $methodScreenshots[$methodName] = $pageLinks -join ', '
            }
        }
    }
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
            if ($sourceLines[$lineIndex] -match 'public static void (test\w+)\(') {
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
$manifest.Add('Manual procedures MT-01 through MT-13 are documented in ManualTests.md and in the LessonManualTesting Javadoc. Its source screenshots show the fixture, real MVC launch, screenshot key, and orderly shutdown used by those procedures; the human performs the actions in the checklist.')
foreach ($documentation in @(
    @{ Class = 'LessonTestingAutomation'; Image = '06-test-javadoc.png' },
    @{ Class = 'LessonTestEvidence'; Image = '07-helper-javadoc.png' },
    @{ Class = 'LessonManualTesting'; Image = '08-manual-javadoc.png' },
    @{ Class = 'ContributorStoryTests'; Image = '11-story-tests-javadoc.png' }
)) {
    $pagePath = Join-Path $PSScriptRoot "javadoc/FoundationsF26/guiLessons/$($documentation.Class).html"
    if (-not (Test-Path -LiteralPath $pagePath)) { throw "Generate Javadoc first: $pagePath" }
    $captureJobs.Add(@{ Page = $pagePath; Image = (Join-Path $screenshots $documentation.Image) })
}
$storyRows = [System.Collections.Generic.List[string]]::new()
$storyRows.Add('# Requirement / named test / exact HTML and screenshot filenames')
$storyRows.Add('')
$storyRows.Add('| Requirement | Named test | Documenting Javadoc HTML | Commented implementation PNGs |')
$storyRows.Add('| --- | --- | --- | --- |')
$requirements = @(
    @('C01', 'Add valid core information under the contributor''s ownership', 'testCreateValid', 'testCreateInvalid'),
    @('C02', 'List and examine only the contributor''s lessons; deny foreign IDs', 'testOwnListAndRead', 'testForeignAccessDenied'),
    @('C03', 'Reflect permitted other-role edits and deletions of contributed content', 'testOtherRoleEditVisible', 'testOtherRoleDeletionVisible'),
    @('C04', 'Show field/whole locks and prohibit locked-field edits or contributor lock changes', 'testUnlockedEdit', 'testLockedEditsAndIndicators'),
    @('C05', 'Permit unlocked deletion; prohibit deletion with any active lock', 'testUnlockedDelete', 'testLockedDeletionDenied'),
    @('C06', 'Hide and preserve other-role additions while allowing core edits', 'testMetadataHidden', 'testMetadataPreserved'),
    @('C07', 'Append what/how/time/team-effort experiences after repeated attempts; preserve history', 'testExperienceAppend', 'testExperienceRejected'),
    @('C08', 'Persist core content, locks, privacy, and experience history across executions', 'testRestartRestoresState', 'testRestartKeepsRestrictions'),
    @('C09', 'Support lesson collection operations and non-destructive keyword subsets', 'testListOperations', 'testListEdges'),
    @('C10', 'Privileged bulk lock changes preserve individual locks and roll back failures', 'testBatchLocks', 'testBatchRollback'),
    @('C11', 'Validate required core fields and optional experience at defined boundaries', 'testBoundaryAccepted', 'testBoundaryRejected'),
    @('C12', 'Accommodate Phase Three search/time/effort/visibility attributes without exposing restricted additions', 'testRestartRestoresState', 'testRestartKeepsRestrictions')
)
foreach ($requirement in $requirements) {
    foreach ($methodName in @($requirement[2], $requirement[3])) {
        if (-not $methodScreenshots.ContainsKey($methodName)) { throw "Missing named test $methodName" }
        $html = "javadoc/FoundationsF26/guiLessons/ContributorStoryTests.html#$methodName()"
        $storyRows.Add("| $($requirement[0]): $($requirement[1]) | $methodName | [ContributorStoryTests.html / $methodName]($html) | $($methodScreenshots[$methodName]) |")
    }
}
# Remove only obsolete generated source-page files in these two dedicated directories.
$expectedPages = @($captureJobs | ForEach-Object { $_.Page })
$expectedImages = @($captureJobs | ForEach-Object { $_.Image })
foreach ($stale in Get-ChildItem -LiteralPath $sourceDirectory -File -Filter 'source-*.html') {
    if ($stale.FullName -notin $expectedPages) { Remove-Item -LiteralPath $stale.FullName }
}
foreach ($stale in Get-ChildItem -LiteralPath $screenshots -File -Filter 'source-*.png') {
    if ($stale.FullName -notin $expectedImages) { Remove-Item -LiteralPath $stale.FullName }
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
[System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'StoryEvidence.md'), $storyRows)
Write-Host "Saved $($captureJobs.Count) screenshots and SourceScreenshots.md."
