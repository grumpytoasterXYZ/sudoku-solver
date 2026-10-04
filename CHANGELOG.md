# Changelog

Alle nennenswerten Änderungen an diesem Projekt werden hier festgehalten.
Format nach [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), Versionierung nach [Semantic Versioning](https://semver.org/lang/de/).

## [Unreleased]

## [0.1.0] - 2026-10-04

Phase 1: Kernlogik ohne Web-Oberfläche.

### Hinzugefügt

- Projektgerüst aus Spring Initializr (Spring Boot 4.1, Java 25)
- README mit Voraussetzungen und Schnellstart, MIT-Lizenz
- CI mit GitHub Actions: Build und Tests bei jedem Pull Request und jedem Push auf `main`
- `Board`: 9×9-Raster mit Ein- und Ausgabe als 81-Zeichen-String
- `BoardValidator`: findet doppelte Ziffern in Zeilen, Spalten und Blöcken
- `SudokuSolver` als gemeinsamer Vertrag aller Solver, Ergebnis als `SolveResult` mit `SolveStatus`
- `BacktrackingSolver`: naives Backtracking als Vergleichsbasis
- `PropagationSolver`: Kandidaten-Bitmasken, Naked und Hidden Singles, MRV-Heuristik
- Gemeinsame Vertragstests für alle Solver
- Messergebnisse der Solver im README

[Unreleased]: https://github.com/grumpytoasterXYZ/sudoku-solver/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/grumpytoasterXYZ/sudoku-solver/releases/tag/v0.1.0
