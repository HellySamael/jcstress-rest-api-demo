param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("10", "20", "30", "40", "50", "60")]
    [string]$Step,
    [switch]$Force
)

$branches = @{
    "10" = "demo/10-hashmap-racy"
    "20" = "demo/20-sync"
    "30" = "demo/30-concurrent-hashmap"
    "40" = "demo/40-jmm-threadsafe"
    "50" = "demo/50-db-racy"
    "60" = "demo/60-db-safe"
}

if (-not $Force) {
    $dirty = git status --porcelain
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Unable to read git status."
        exit 1
    }
    if ($dirty) {
        Write-Error "Working tree is not clean. Commit or stash first, or rerun with -Force."
        exit 1
    }
}

$target = $branches[$Step]
git checkout $target
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Switched to $target"
