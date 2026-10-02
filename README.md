# Sudoku Solver

<!-- CI-Badge folgt in Schritt A7, sobald der Workflow auf GitHub läuft -->
[![CI](https://github.com/grumpytoasterXYZ/sudoku-solver/actions/workflows/ci.yml/badge.svg)](https://github.com/grumpytoasterXYZ/sudoku-solver/actions/workflows/ci.yml)

Ein Sudoku-Solver in Java mit Web-Oberfläche: Ziffern in ein leeres Raster eintragen, bestätigen und das Puzzle wahlweise sofort oder Schritt für Schritt animiert lösen lassen.

## Status

In Arbeit, aktuell: Projekt-Setup.

## Voraussetzungen

- JDK 25. Maven verwendet das JDK aus `JAVA_HOME`, prüfen mit `./mvnw -v`.
- Maven muss nicht installiert sein, das Projekt bringt den Maven Wrapper mit.

## Schnellstart

```bash
./mvnw spring-boot:run      # Windows: .\mvnw spring-boot:run
```

Danach http://localhost:8080 öffnen.

Tests ausführen:

```bash
./mvnw test                 # Windows: .\mvnw test
```

## Dokumentation

Die Dokumentation steht als Javadoc direkt im Code: ein Überblick pro Package in `package-info.java`, Details an den Klassen und Methoden.

## Lizenz

[MIT](LICENSE)
