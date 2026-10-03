package de.grumpytoaster.sudoku_solver.solver;

/**
 * Ausgang eines Lösungsversuchs.
 */
public enum SolveStatus {

    /** Eine Lösung wurde gefunden. */
    SOLVED,

    /** Die Eingabe ist regelkonform, hat aber keine Lösung. */
    UNSOLVABLE,

    /** Die Eingabe enthält schon doppelte Ziffern; es wurde gar nicht erst gesucht. */
    INVALID
}
