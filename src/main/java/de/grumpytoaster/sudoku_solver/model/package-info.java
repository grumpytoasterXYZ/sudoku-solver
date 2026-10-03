/**
 * Datenmodell des Sudoku-Solvers.
 *
 * <p>{@link de.grumpytoaster.sudoku_solver.model.Board} hält das 9×9-Raster als
 * Array mit 81 Zellen und bietet Zugriff über Zeile und Spalte (jeweils 0–8).
 * Leere Zellen haben den Wert {@link de.grumpytoaster.sudoku_solver.model.Board#EMPTY}.
 * Ein Raster lässt sich aus einem 81-Zeichen-String lesen und wieder als
 * solcher ausgeben.
 *
 * <p>Die Klassen in diesem Package prüfen nur Wertebereiche. Die Sudoku-Regeln
 * und das Lösen liegen in anderen Packages.
 */
package de.grumpytoaster.sudoku_solver.model;
