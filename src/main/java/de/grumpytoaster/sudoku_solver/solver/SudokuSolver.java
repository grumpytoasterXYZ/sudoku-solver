package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;

/**
 * Gemeinsamer Vertrag aller Sudoku-Solver.
 *
 * <p>Jede Implementierung löst auf ihre eigene Art, hält sich aber an diese Regeln:
 * <ul>
 *   <li>Das übergebene {@code puzzle} wird nicht verändert.</li>
 *   <li>Enthält das Puzzle doppelte Ziffern, ist das Ergebnis
 *       {@link SolveStatus#INVALID}, ohne dass gesucht wird.</li>
 *   <li>Bei {@link SolveStatus#SOLVED} ist die Lösung vollständig, regelkonform
 *       und enthält alle Vorgaben des Puzzles.</li>
 * </ul>
 */
public interface SudokuSolver {

    /**
     * Löst ein Sudoku.
     *
     * @param puzzle das zu lösende Puzzle; leere Zellen sind {@link Board#EMPTY}
     * @return Status, gegebenenfalls Lösung und Anzahl der Schritte
     * @throws NullPointerException wenn {@code puzzle} {@code null} ist
     */
    SolveResult solve(Board puzzle);
}
