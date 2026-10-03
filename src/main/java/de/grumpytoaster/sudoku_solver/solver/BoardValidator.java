package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Prüft ein {@link Board} auf Verstöße gegen die Sudoku-Regeln.
 *
 * <p>Ein Sudoku hat 27 Einheiten: 9 Zeilen, 9 Spalten und 9 Blöcke zu je 3×3 Zellen.
 * In keiner Einheit darf eine Ziffer mehrfach vorkommen.
 * Leere Zellen zählen nicht mit, ein unvollständiges Board kann also gültig sein.
 *
 * <p>Der Validator liefert alle Zellen, die an einem Konflikt beteiligt sind, und nicht nur den ersten Fund.
 * So kann das Frontend jede betroffene Zelle markieren.
 */
public final class BoardValidator {

    /** Kantenlänge eines Blocks (3×3). */
    private static final int BOX_SIZE = 3;

    /** Eine Zelle, angegeben über Zeile und Spalte. */
    private record Cell(int row, int col) { }

    // Utility-Klasse
    private BoardValidator() {
    }

    /**
     * Findet alle Zellen, deren Ziffer in ihrer Zeile, ihrer Spalte oder ihrem Block mehrfach vorkommt.
     *
     * @param board das zu prüfende Board
     * @return Zellindizes nach {@link Board#index(int, int)}, aufsteigend
     *         sortiert und ohne Duplikate; 
     *         leer, wenn es keinen Konflikt gibt.
     *         Die Liste ist unveränderlich.
     * @throws NullPointerException wenn {@code board} {@code null} ist
     */
    public static List<Integer> findConflicts(Board board) {
        Objects.requireNonNull(board, "board darf nicht null sein");

        // TreeSet: keine Duplikate (sowohl Zeile als auch Spalte können in Konflikt mit der Zelle stehen, es reicht aber nur ein Konflikt)
        // und automatisch aufsteigend sortiert
        Set<Integer> conflicts = new TreeSet<>();
        for (int i = 0; i < Board.SIZE; i++) {
            collectConflicts(board, rowCells(i), conflicts);
            collectConflicts(board, columnCells(i), conflicts);
            collectConflicts(board, boxCells(i), conflicts);
        }
        return List.copyOf(conflicts);
    }

    /**
     * Prüft eine Einheit (Zeile, Spalte oder Block) und fügt alle Zellen mit
     * mehrfach vorkommender Ziffer zu {@code conflicts} hinzu.
     */
    private static void collectConflicts(Board board, List<Cell> unit, Set<Integer> conflicts) {
        // 1. Durchlauf: zählen, wie oft jede Ziffer vorkommt.
        //    Index = Ziffer; count[0] zählt die leeren Zellen und wird ignoriert.
        int[] count = new int[Board.SIZE + 1];
        for (Cell cell : unit) {
            count[board.get(cell.row(), cell.col())]++;
        }

        // 2. Durchlauf: jede Zelle merken, deren Ziffer mehr als einmal vorkommt
        for (Cell cell : unit) {
            int value = board.get(cell.row(), cell.col());
            if (value != Board.EMPTY && count[value] > 1) {
                conflicts.add(Board.index(cell.row(), cell.col()));
            }
        }
    }

    /**
     * Prüft, ob das Board keine doppelten Ziffern enthält.
     *
     * @param board das zu prüfende Board
     * @return {@code true}, wenn {@link #findConflicts(Board)} nichts findet
     * @throws NullPointerException wenn {@code board} {@code null} ist
     */
    public static boolean isValid(Board board) {
        return findConflicts(board).isEmpty();
    }

    /** Die 9 Zellen einer Zeile. */
    private static List<Cell> rowCells(int row) {
        List<Cell> cells = new ArrayList<>(Board.SIZE);
        for (int col = 0; col < Board.SIZE; col++) {
            cells.add(new Cell(row, col));
        }
        return cells;
    }

    /** Die 9 Zellen einer Spalte. */
    private static List<Cell> columnCells(int col) {
        List<Cell> cells = new ArrayList<>(Board.SIZE);
        for (int row = 0; row < Board.SIZE; row++) {
            cells.add(new Cell(row, col));
        }
        return cells;
    }

    /**
     * Die 9 Zellen eines Blocks. Blöcke sind zeilenweise von 0 (oben links)
     * bis 8 (unten rechts) nummeriert.
     */
    private static List<Cell> boxCells(int box) {
        int startRow = (box / BOX_SIZE) * BOX_SIZE;
        int startCol = (box % BOX_SIZE) * BOX_SIZE;
        List<Cell> cells = new ArrayList<>(Board.SIZE);
        for (int k = 0; k < Board.SIZE; k++) {
            cells.add(new Cell(startRow + k / BOX_SIZE, startCol + k % BOX_SIZE));
        }
        return cells;
    }
}
