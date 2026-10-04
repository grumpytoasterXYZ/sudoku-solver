# Sudoku Solver

[![CI](https://github.com/grumpytoasterXYZ/sudoku-solver/actions/workflows/ci.yml/badge.svg)](https://github.com/grumpytoasterXYZ/sudoku-solver/actions/workflows/ci.yml)

Ein Sudoku-Solver in Java mit Web-Oberfläche: Ziffern in ein leeres Raster eintragen, bestätigen und das Puzzle wahlweise sofort oder Schritt für Schritt animiert lösen lassen.

## Status

Phase 1 (Kernlogik ohne Web-Oberfläche) ist abgeschlossen. Als Nächstes zeichnen die Solver ihre Lösungsschritte auf, danach folgen REST-API und Oberfläche.

## Solver

| Solver | Vorgehen |
|---|---|
| `BacktrackingSolver` | Probiert in jeder leeren Zelle der Reihe nach 1 bis 9 und geht bei einer Sackgasse zurück. Vergleichsbasis. |
| `PropagationSolver` | Führt Kandidaten als Bitmasken, setzt alle Naked und Hidden Singles und rät erst, wenn nichts mehr ableitbar ist, dann in der Zelle mit den wenigsten Kandidaten (MRV). |

### Messergebnisse

Gezählt werden Rateversuche, also Ziffern, die probeweise gesetzt werden. Ziffern, die logisch feststehen, zählen nicht. Beim naiven Backtracking ist jede gesetzte Ziffer geraten.

| Puzzle | Vorgaben | Backtracking | Propagation |
|---|---:|---:|---:|
| Leicht (Norvig, easy #1) | 32 | 200 | 0 |
| Wikipedia-Beispiel | 30 | 4.208 | 0 |
| Arto Inkala (2010) | 23 | 10.007 | 21 |
| Schwer (Norvig, hard #1) | 17 | 9.727.396 | 25 |

Die Rateversuche sind deterministisch, also bei jedem Lauf gleich. Die Laufzeit ist nur ein Richtwert: Beim schweren Puzzle braucht das Backtracking rund eine Sekunde, der `PropagationSolver` bei allen vier Puzzles weniger als 0,2 ms.

## Voraussetzungen

- JDK 25. Maven verwendet das JDK aus `JAVA_HOME`, prüfen mit `./mvnw -v`.
- Maven muss nicht installiert sein, das Projekt bringt den Maven Wrapper mit.

## Schnellstart

```bash
./mvnw spring-boot:run      # Windows: .\mvnw spring-boot:run
```

Danach http://localhost:8080 öffnen. Die Oberfläche folgt in einer späteren Phase, bis dahin zeigt Spring dort nur eine Fehlerseite.

Tests ausführen:

```bash
./mvnw test                 # Windows: .\mvnw test
```

## Dokumentation

Die Dokumentation steht als Javadoc direkt im Code: ein Überblick pro Package in `package-info.java`, Details an den Klassen und Methoden.

## Lizenz

[MIT](LICENSE)
