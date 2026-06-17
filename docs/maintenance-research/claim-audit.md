# Claim audit

| Claim | Staat in rapport | Bewijsbestand/commando | Klopt? | Actie |
|---|---|---|---|---|
| Baseline commit is `84fdc113631bd193c6cb4502552cd8709e80d463`. | Ja | `evidence/git-evidence.md` | Ja | Behouden. |
| PoC heeft een after commit. | Eerder impliciet onduidelijk. | `git status --short`, `evidence/git-evidence.md` | Nee | Aangepast naar: after-state is working tree, nog niet gecommit. |
| `ConversionUtil.convert(Object, Type)` had baseline 118 LOC. | Ja | `evidence/baseline-metrics.md` | Ja | Behouden. |
| `ConversionUtil.convert(Object, Type)` heeft na PoC 39 LOC. | Ja | `evidence/after-metrics.md` | Ja | Behouden. |
| CC ging van 23 naar 12. | Ja | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | Ja | Behouden. |
| Rough decision tokens gingen van 100 naar 96. | Ja | `evidence/baseline-metrics.md`, `evidence/after-metrics.md` | Ja | Behouden als class-level metric, niet als hoofdclaim. |
| `ConversionUtilTest` ging van 22 naar 26 tests. | Ja | `evidence/baseline-conversionutil-test.txt`, `evidence/after-conversionutil-test-final.txt` | Ja | Behouden. |
| Line coverage ging van 59.27% naar 64.98%. | Ja | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` | Ja | Behouden. |
| Branch coverage ging van 50.58% naar 56.82%. | Ja | `evidence/coverage-baseline.txt`, `evidence/coverage-after.txt` | Ja | Behouden. |
| Full reactor heeft geen regressie. | Te sterk geformuleerd in eerdere versie. | `evidence/baseline-mvn-test.txt`, `evidence/after-mvn-test.txt`, `evidence/after-mvn-verify.txt` | Alleen binnen beperkte scope. | Aangepast naar: geen nieuwe regressie aangetoond binnen `omod-common`/`ConversionUtil`; full reactor blijft rood. |
| `mvn clean test` faalt baseline en after op hetzelfde Maven dependency-plugin punt. | Ja | `evidence/baseline-mvn-test.txt`, `evidence/after-mvn-test.txt` | Ja | Behouden. |
| `mvn clean verify` bewijst full scope. | Niet meer. | `evidence/after-mvn-verify.txt` | Nee | Alleen after-run gedocumenteerd; geen baselinevergelijking. |
| Nieuwe tests bestaan. | Ja | `rg` op `ConversionUtilTest.java`, test logs | Ja | Testnamen expliciet opgenomen. |
| Helper methods bestaan. | Ja | `rg` op `ConversionUtil.java` | Ja | In PoC-verantwoording opgenomen. |
| `NO_CONVERSION` sentinel bestaat. | Ja | `ConversionUtil.java:74` | Ja | In ontwerp en PoC-verantwoording opgenomen. |
| Alleen codefiles `ConversionUtil.java` en `ConversionUtilTest.java` zijn gewijzigd voor PoC-code. | Ja | `evidence/git-evidence.md` | Ja | Behouden; docs/evidence staan apart als onderzoekoutput. |
| Duplicatie is alleen kwalitatief. | Eerder deels. | `evidence/duplication.md` | Verouderd | Vervangen door PMD CPD: 15 duplicate blocks. |
| Package cycles zijn niet bekeken. | Eerder deels. | `evidence/package-cycles.md` | Verouderd | Vervangen door import-edge scan met 11 directe tweerichtingsafhankelijkheden. |
