package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;

/**
 * Ergebnis eines Lösungsversuchs.
 *
 * @param status   wie der Versuch ausgegangen ist
 * @param solution das gelöste Board bei {@link SolveStatus#SOLVED}, sonst {@code null}
 * @param steps    Anzahl der Rateversuche, also der probeweise gesetzten Ziffern.
 *                 Ziffern, die ein Solver logisch ableitet, zählen nicht. Beim naiven
 *                 Backtracking ist jede gesetzte Ziffer geraten. Messgröße für den
 *                 Vergleich der Solver
 */
public record SolveResult(SolveStatus status, Board solution, long steps) {
}
