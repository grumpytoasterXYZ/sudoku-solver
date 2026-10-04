package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;

/**
 * Solver mit Kandidaten-Bitmasken, Ableitungsregeln und MRV-Heuristik.
 *
 * <p><b>Bitmasken.</b> Für jede Zeile, Spalte und jeden Block merkt sich der Solver
 * in einem {@code int}, welche Ziffern dort schon stehen:
 * Bit 0 steht für die 1,
 * Bit 1 für die 2, und so weiter bis
 * Bit 8 für die 9.
 * Die Kandidaten einer Zelle sind dann alle Ziffern, die in keiner ihrer drei Einheiten vorkommen:
 * <pre>{@code
 * candidates = ~(rowUsed[row] | colUsed[col] | boxUsed[box]) & ALL_DIGITS
 * }</pre>
 * Das ersetzt das Durchsuchen von 27 Zellen durch eine einzige Rechnung.
 *
 * <p><b>Ableiten.</b> Bevor geraten wird, setzt der Solver alle Ziffern, die logisch
 * feststehen, und wiederholt das, bis sich nichts mehr ändert:
 * <ul>
 *   <li><i>Naked Single:</i> Eine leere Zelle hat nur noch einen Kandidaten.</li>
 *   <li><i>Hidden Single:</i> Eine Ziffer passt in einer Zeile, Spalte oder einem Block
 *       nur noch in eine einzige Zelle, auch wenn diese Zelle selbst mehrere Kandidaten hat.</li>
 * </ul>
 * Dabei fallen auch Widersprüche sofort auf: eine leere Zelle ohne Kandidaten oder eine
 * Ziffer, die in einer Einheit keinen Platz mehr hat.
 *
 * <p><b>MRV</b> (<i>Minimum Remaining Values</i>).
 * Geraten wird nur, wenn nichts mehr ableitbar ist, und dann in der leeren Zelle
 * mit den wenigsten Kandidaten. So ist die Trefferwahrscheinlichkeit am höchsten.
 *
 * <p><b>Zurücknehmen.</b> Ein Rateversuch zieht meist viele abgeleitete Ziffern nach sich.
 * Statt sie einzeln zurückzunehmen, sichert der Solver vor dem Raten Board und Masken
 * und stellt beides nach einem Fehlversuch wieder her.
 *
 * <p>{@link SolveResult#steps()} zählt nur die Rateversuche. Abgeleitete Ziffern zählen
 * nicht, weil sie nicht probeweise gesetzt werden. Ein Puzzle, das sich rein durch
 * Ableiten lösen lässt, braucht also 0 Schritte.
 *
 * <p>Nicht thread-sicher: Eine Instanz darf nicht gleichzeitig mehrere Puzzles lösen.
 *
 * @see <a href="https://www.norvig.com/sudoku.html">Peter Norvig – Solving Every Sudoku Puzzle</a>
 */
public final class PropagationSolver implements SudokuSolver {

    /** Bits 0–8 gesetzt: alle Ziffern 1–9. */
    private static final int ALL_DIGITS = 0x1FF; // == 0b111111111

    /** Kantenlänge eines Blocks. */
    private static final int BOX_SIZE = 3;

    /**
     * Die 27 Einheiten als Listen von Zellindizes ({@code row * 9 + col}):
     * Index 0–8 sind die Zeilen, 9–17 die Spalten, 18–26 die Blöcke.
     */
    private static final int[][] UNITS = buildUnits();

    /** Anzahl der Rateversuche im aktuellen Lösungsversuch. */
    private long steps;

    /** Arbeitskopie des Puzzles, wird während der Suche verändert. */
    private Board board;

    /** Pro Zeile: welche Ziffern dort schon stehen, als Bitmaske. */
    private final int[] rowUsed = new int[Board.SIZE];

    /** Pro Spalte: welche Ziffern dort schon stehen, als Bitmaske. */
    private final int[] colUsed = new int[Board.SIZE];

    /** Pro Block: welche Ziffern dort schon stehen, als Bitmaske. */
    private final int[] boxUsed = new int[Board.SIZE];

    @Override
    public SolveResult solve(Board puzzle) {
        if (!BoardValidator.isValid(puzzle)) {
            return new SolveResult(SolveStatus.INVALID, null, 0);
        }

        steps = 0;
        board = puzzle.copy();
        initMasks();

        if (search()) {
            return new SolveResult(SolveStatus.SOLVED, board, steps);
        }
        return new SolveResult(SolveStatus.UNSOLVABLE, null, steps);
    }

    /**
     * Setzt alle Masken zurück und trägt die Vorgaben des Boards ein.
     * Nötig, weil eine Solver-Instanz mehrere Puzzles nacheinander lösen kann.
     */
    private void initMasks() {
        for (int i = 0; i < Board.SIZE; i++) {
            rowUsed[i] = 0;
            colUsed[i] = 0;
            boxUsed[i] = 0;
        }
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                int digit = board.get(row, col);
                if (digit != Board.EMPTY) {
                    markUsed(row, col, digit);
                }
            }
        }
    }

    /**
     * Sucht rekursiv eine Lösung: erst alles Ableitbare setzen, dann in der leeren Zelle
     * mit den wenigsten Kandidaten raten und für jeden Kandidaten weitersuchen.
     *
     * @return {@code true}, wenn das Board vollständig gelöst wurde
     */
    private boolean search() {
        if (!propagate()) {
            return false;                   // Widerspruch beim Ableiten: Sackgasse
        }

        // MRV (Minimum Remaining Values): leere Zelle mit den wenigsten Kandidaten finden.
        // Nach propagate() hat jede leere Zelle mindestens zwei Kandidaten.
        int bestCell = -1;
        int bestCount = Board.SIZE + 1;
        for (int cell = 0; cell < Board.CELLS; cell++) {
            if (!isEmpty(cell)) {
                continue;
            }
            int count = Integer.bitCount(candidates(cell));
            if (count < bestCount) {
                bestCount = count;
                bestCell = cell;
            }
        }

        if (bestCell == -1) {
            return true;                    // keine leere Zelle mehr: gelöst
        }

        // Zustand sichern: ein Fehlversuch nimmt alles zurück, was seitdem gesetzt wurde
        Board savedBoard = board.copy();
        int[] savedMasks = saveMasks();

        // Nur die Kandidaten der gewählten Zelle durchgehen, kleinste Ziffer zuerst
        int remaining = candidates(bestCell);
        while (remaining != 0) {
            int digit = Integer.numberOfTrailingZeros(remaining) + 1;   // niedrigstes gesetztes Bit
            remaining &= remaining - 1;                                 // dieses Bit löschen

            place(bestCell, digit);
            steps++;
            if (search()) {
                return true;
            }
            restore(savedBoard, savedMasks);                            // Backtracking
        }
        return false;
    }

    /**
     * Wendet Naked und Hidden Singles an, bis sich nichts mehr ändert.
     *
     * @return {@code false}, wenn dabei ein Widerspruch auftaucht: eine leere Zelle ohne
     *         Kandidaten oder eine Ziffer, die in einer Einheit keinen Platz mehr hat
     */
    private boolean propagate() {
        boolean changed = true;
        while (changed) {
            changed = false;

            // Naked Singles: Zelle mit genau einem Kandidaten
            for (int cell = 0; cell < Board.CELLS; cell++) {
                if (!isEmpty(cell)) {
                    continue;
                }
                int candidates = candidates(cell);
                if (candidates == 0) {
                    return false;
                }
                if (Integer.bitCount(candidates) == 1) {
                    place(cell, Integer.numberOfTrailingZeros(candidates) + 1);
                    changed = true;
                }
            }

            // Hidden Singles: Ziffer mit genau einem möglichen Platz in einer Einheit
            for (int unit = 0; unit < UNITS.length; unit++) {
                int once = 0;               // Ziffern, die in mindestens einer Zelle möglich sind
                int twice = 0;              // Ziffern, die in mindestens zwei Zellen möglich sind
                for (int cell : UNITS[unit]) {
                    if (isEmpty(cell)) {
                        int candidates = candidates(cell);
                        twice |= once & candidates;
                        once |= candidates;
                    }
                }

                int missing = ~usedIn(unit) & ALL_DIGITS;
                if ((missing & ~once) != 0) {
                    return false;           // eine fehlende Ziffer passt nirgends mehr hin
                }

                int singles = once & ~twice;
                while (singles != 0) {
                    int digit = Integer.numberOfTrailingZeros(singles) + 1;
                    singles &= singles - 1;

                    int cell = onlyCellFor(unit, digit);
                    if (cell == -1) {
                        // Die einzige mögliche Zelle hat eben eine andere Ziffer bekommen
                        return false;
                    }
                    place(cell, digit);
                    changed = true;
                }
            }
        }
        return true;
    }

    /**
     * Die leere Zelle der Einheit, in die {@code digit} noch passt.
     *
     * @return Zellindex oder {@code -1}, wenn es keine solche Zelle gibt
     */
    private int onlyCellFor(int unit, int digit) {
        for (int cell : UNITS[unit]) {
            if (isEmpty(cell) && (candidates(cell) & bit(digit)) != 0) {
                return cell;
            }
        }
        return -1;
    }

    /** Ziffern, die in Zelle ({@code row}, {@code col}) noch erlaubt sind, als Bitmaske. */
    private int candidates(int row, int col) {
        int used = rowUsed[row] | colUsed[col] | boxUsed[box(row, col)];
        return ~used & ALL_DIGITS;
    }

    /** Wie {@link #candidates(int, int)}, mit Zellindex. */
    private int candidates(int cell) {
        return candidates(row(cell), col(cell));
    }

    /** Ziffern, die in einer Einheit (Index in {@link #UNITS}) schon stehen. */
    private int usedIn(int unit) {
        if (unit < Board.SIZE) {
            return rowUsed[unit];
        }
        if (unit < 2 * Board.SIZE) {
            return colUsed[unit - Board.SIZE];
        }
        return boxUsed[unit - 2 * Board.SIZE];
    }

    private boolean isEmpty(int cell) {
        return board.isEmpty(row(cell), col(cell));
    }

    /** Setzt eine Ziffer ins Board und trägt sie in alle drei Masken ein. */
    private void place(int cell, int digit) {
        int row = row(cell);
        int col = col(cell);
        board.set(row, col, digit);
        markUsed(row, col, digit);
    }

    /** Trägt eine Ziffer in die Masken von Zeile, Spalte und Block ein. */
    private void markUsed(int row, int col, int digit) {
        int b = bit(digit);
        rowUsed[row] |= b;
        colUsed[col] |= b;
        boxUsed[box(row, col)] |= b;
    }

    /** Kopiert die drei Masken hintereinander in ein neues Array (Zeilen, Spalten, Blöcke). */
    private int[] saveMasks() {
        int[] saved = new int[3 * Board.SIZE];
        System.arraycopy(rowUsed, 0, saved, 0, Board.SIZE);
        System.arraycopy(colUsed, 0, saved, Board.SIZE, Board.SIZE);
        System.arraycopy(boxUsed, 0, saved, 2 * Board.SIZE, Board.SIZE);
        return saved;
    }

    /**
     * Stellt den Zustand vor einem Rateversuch wieder her. Das Board wird kopiert,
     * damit die Sicherung für den nächsten Kandidaten unverändert bleibt.
     */
    private void restore(Board savedBoard, int[] savedMasks) {
        board = savedBoard.copy();
        System.arraycopy(savedMasks, 0, rowUsed, 0, Board.SIZE);
        System.arraycopy(savedMasks, Board.SIZE, colUsed, 0, Board.SIZE);
        System.arraycopy(savedMasks, 2 * Board.SIZE, boxUsed, 0, Board.SIZE);
    }

    /** Baut die 27 Einheiten für {@link #UNITS}. */
    private static int[][] buildUnits() {
        int[][] units = new int[3 * Board.SIZE][Board.SIZE];
        for (int i = 0; i < Board.SIZE; i++) {
            int boxRow = (i / BOX_SIZE) * BOX_SIZE;
            int boxCol = (i % BOX_SIZE) * BOX_SIZE;
            for (int j = 0; j < Board.SIZE; j++) {
                units[i][j] = Board.index(i, j);                                // Zeile i
                units[Board.SIZE + i][j] = Board.index(j, i);                   // Spalte i
                units[2 * Board.SIZE + i][j] =
                        Board.index(boxRow + j / BOX_SIZE, boxCol + j % BOX_SIZE); // Block i
            }
        }
        return units;
    }

    /** Bit für eine Ziffer: 1 → Bit 0, 9 → Bit 8. */
    private static int bit(int digit) {
        return 1 << (digit - 1);
    }

    private static int row(int cell) {
        return cell / Board.SIZE;
    }

    private static int col(int cell) {
        return cell % Board.SIZE;
    }

    /** Nummer des 3×3-Blocks, zeilenweise von 0 (oben links) bis 8 (unten rechts).
     *
     * 0 | 1 | 2
     * 3 | 4 | 5
     * 6 | 7 | 8
     *
    */
    private static int box(int row, int col) {
        return (row / BOX_SIZE) * BOX_SIZE + col / BOX_SIZE;
    }
}
