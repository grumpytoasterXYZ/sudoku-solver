package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;
import de.grumpytoaster.sudoku_solver.solver.BoardValidator;

/**
 * Naiver Solver: probiert in jeder leeren Zelle die Ziffern 1 bis 9 der Reihe nach
 * und geht zurück, sobald eine Zelle keine passende Ziffer mehr hat (Backtracking).
 *
 * <p>Die leeren Zellen werden zeilenweise von oben links abgearbeitet. Der Solver
 * erkennt Sackgassen erst, wenn er in sie hineinläuft, und braucht deshalb bei
 * schweren Puzzles sehr viele Schritte. Er dient als Vergleichsbasis für die
 * schnelleren Solver.
 *
 * <p>Nicht thread-sicher: Eine Instanz darf nicht gleichzeitig mehrere Puzzles lösen.
 */
public final class BacktrackingSolver implements SudokuSolver {

    /** Anzahl der probeweise gesetzten Ziffern im aktuellen Lösungsversuch. */
    private long steps;

    @Override
    public SolveResult solve(Board puzzle) {
        if (BoardValidator.isValid(puzzle) == false) {
           return new SolveResult(SolveStatus.INVALID, null, 0);
        }

        steps = 0;
        Board sudokuCopy = puzzle.copy();

        if (solveFrom(sudokuCopy)) {
            return new SolveResult(SolveStatus.SOLVED, sudokuCopy, steps);
        } else {
            return new SolveResult(SolveStatus.UNSOLVABLE, null, steps);
        }
    }

    /**
     * Füllt ab der ersten leeren Zelle rekursiv alle leeren Zellen.
     *
     * @param board wird während der Suche verändert
     * @return {@code true}, wenn das Board vollständig gelöst wurde
     */
    private boolean solveFrom(Board board) {
        int cell = findFirstEmptyCell(board);
        int row = cell / Board.SIZE;
        int col = cell % Board.SIZE;
        
        if (cell == -1) {
            return true;
        }

        for (int digit = 1; digit <= Board.SIZE; digit++) {
            if (isAllowed(board, row, col, digit)) {
                board.set(row, col, digit);
                steps++;
                if (solveFrom(board)) {
                    return true;
                }
                board.set(row, col, Board.EMPTY);
            }
        }
        return false;
    }

    /**
     * Sucht zeilenweise von oben links die erste leere Zelle.
     *
     * @return Zellindex nach {@link Board#index(int, int)} oder {@code -1},
     *         wenn keine Zelle mehr leer ist
     */
    private static int findFirstEmptyCell(Board board) {
        for (int i = 0; i < Board.CELLS; i++) {
            if (board.get(i / Board.SIZE, i % Board.SIZE) == Board.EMPTY) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Prüft, ob {@code digit} in Zelle ({@code row}, {@code col}) gesetzt werden darf,
     * also weder in der Zeile noch in der Spalte noch im 3×3-Block schon vorkommt.
     */
    private static boolean isAllowed(Board board, int row, int col, int digit) {
        for (int c = 0; c < Board.SIZE; c++) {
            if (board.get(row, c) == digit) {
                return false;
            }
        }

        for (int r = 0; r < Board.SIZE; r++) {
            if (board.get(r, col) == digit) {
                return false;
            }
        }

        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (board.get(r, c) == digit) {
                    return false;
                }
            }
        }
        return true;
    }
}
