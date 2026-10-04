/**
 * Regelprüfung und Lösen von Sudokus.
 *
 * <p>{@link de.grumpytoaster.sudoku_solver.solver.BoardValidator} prüft ein
 * Raster auf doppelte Ziffern in Zeilen, Spalten und Blöcken.
 *
 * <p>Alle Solver implementieren {@link de.grumpytoaster.sudoku_solver.solver.SudokuSolver}
 * und liefern ein {@link de.grumpytoaster.sudoku_solver.solver.SolveResult}:
 * <ul>
 *   <li>{@link de.grumpytoaster.sudoku_solver.solver.BacktrackingSolver}: naives
 *       Backtracking, Vergleichsbasis.</li>
 *   <li>{@link de.grumpytoaster.sudoku_solver.solver.PropagationSolver}: leitet mit
 *       Naked und Hidden Singles ab und rät nur, wenn nötig (Bitmasken, MRV).</li>
 * </ul>
 */
package de.grumpytoaster.sudoku_solver.solver;
