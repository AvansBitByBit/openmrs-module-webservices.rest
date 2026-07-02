param(
	[string]$Revision = "WORKTREE",
	[string[]]$Paths = @(
		"omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/RestServiceImpl.java",
		"omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/ResourceRegistry.java",
		"omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/SearchHandlerRegistry.java",
		"omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/DefaultResourceRegistry.java",
		"omod-common/src/main/java/org/openmrs/module/webservices/rest/web/api/impl/DefaultSearchHandlerRegistry.java"
	)
)

$ErrorActionPreference = "Stop"
# Deliberately rough, source-based complexity indicator. It counts control-flow tokens but is not
# a replacement for AST-based cyclomatic complexity.
$decisionPattern = '\b(?:else\s+if|if|for|while|case|catch)\b|&&|\|\|'
$publicMethodPattern = '^\s*public\s+(?:<[^>]+>\s+)?[\w<?>\[\],.]+\s+\w+\s*\('
$interfaceMethodPattern = '^\s*(?!default\b|static\b)[\w<?>\[\],.]+\s+\w+\s*\([^;]*\)\s*(?:throws\s+[\w., ]+)?;\s*$'

function Get-SourceLines([string]$Path) {
	if ($Revision -eq "WORKTREE") {
		return @(Get-Content -LiteralPath $Path)
	}

	$source = @(git show "${Revision}:$Path")
	if ($LASTEXITCODE -ne 0) {
		throw "Could not read $Path at revision $Revision"
	}
	return $source
}

function Count-Matches([string[]]$Lines, [string]$Pattern) {
	$count = 0
	foreach ($line in $Lines) {
		$count += ([regex]::Matches($line, $Pattern)).Count
	}
	return $count
}

$metrics = foreach ($path in $Paths) {
	$lines = Get-SourceLines $path
	$imports = @($lines | Where-Object { $_ -match '^\s*import\s+' } | ForEach-Object { $_.Trim() } | Sort-Object -Unique)
	$isInterface = ($lines -match '^public interface ').Count -gt 0

	[pscustomobject]@{
		File = $path
		Lines = $lines.Count
		Nonblank = @($lines | Where-Object { $_.Trim().Length -gt 0 }).Count
		ImportFanOut = $imports.Count
		DecisionTokens = Count-Matches $lines $decisionPattern
		PublicMethods = Count-Matches $lines $publicMethodPattern
		InterfaceMethods = if ($isInterface) { Count-Matches $lines $interfaceMethodPattern } else { 0 }
	}
}

Write-Output "# Registry architecture metrics"
Write-Output ""
Write-Output "Revision: $Revision"
Write-Output "Generated: $([DateTimeOffset]::Now.ToString('o'))"
Write-Output ""
Write-Output "| File | Lines | Nonblank | Import fan-out | Decision tokens | Public methods | Interface methods |"
Write-Output "|---|---:|---:|---:|---:|---:|---:|"
foreach ($metric in $metrics) {
	Write-Output ("| {0} | {1} | {2} | {3} | {4} | {5} | {6} |" -f $metric.File, $metric.Lines,
		$metric.Nonblank, $metric.ImportFanOut, $metric.DecisionTokens, $metric.PublicMethods,
		$metric.InterfaceMethods)
}

$allImports = foreach ($path in $Paths) {
	Get-SourceLines $path | Where-Object { $_ -match '^\s*import\s+' } | ForEach-Object { $_.Trim() }
}

Write-Output ""
Write-Output "Unique imports across measured subsystem: $(@($allImports | Sort-Object -Unique).Count)"
Write-Output "Total physical LOC: $(($metrics | Measure-Object -Property Lines -Sum).Sum)"
Write-Output "Total nonblank LOC: $(($metrics | Measure-Object -Property Nonblank -Sum).Sum)"
Write-Output "Total decision tokens: $(($metrics | Measure-Object -Property DecisionTokens -Sum).Sum)"
