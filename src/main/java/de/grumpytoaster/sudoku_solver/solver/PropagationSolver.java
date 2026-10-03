package de.grumpytoaster.sudoku_solver.solver;

import de.grumpytoaster.sudoku_solver.model.Board;

/**
 * Solver mit Kandidaten-Bitmasken und MRV-Heuristik.
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
 * <p><b>MRV</b> (<i>Minimum Remaining Values</i>). 
 * Geraten wird immer in der leeren Zelle mit den wenigsten Kandidaten. 
 * So ist die Trefferwahrscheinlichkeit am höchsten,
 * und eine Zelle ganz ohne Kandidaten fällt sofort auf, statt erst, 
 * wenn die Suche zufällig dort ankommt.
 *
 * <p>Ableitungsregeln (Naked und Hidden Singles) kommen in der nächsten Etappe dazu.
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

    /** Anzahl der probeweise gesetzten Ziffern im aktuellen Lösungsversuch. */
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
     * Sucht rekursiv eine Lösung: wählt die leere Zelle mit den wenigsten Kandidaten
     * und probiert deren Kandidaten der Reihe nach.
     *
     * @return {@code true}, wenn das Board vollständig gelöst wurde
     */
    private boolean search() {
        int bestRow = -1;
        int bestCol = -1;
        int bestCount = Board.SIZE + 1;

        // MRV (Minimum Remaining Values): leere Zelle mit den wenigsten Kandidaten finden
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (!board.isEmpty(row, col)) {
                    continue;
                }
                int count = Integer.bitCount(candidates(row, col));
                if (count == 0) {
                    return false;           // Sackgasse: diese Zelle kann nichts mehr aufnehmen
                }
                if (count < bestCount) {
                    bestCount = count;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        if (bestRow == -1) {
            return true;                    // keine leere Zelle mehr: gelöst
        }

        // Nur die Kandidaten der gewählten Zelle durchgehen, kleinste Ziffer zuerst
        int remaining = candidates(bestRow, bestCol);
        while (remaining != 0) {
            int digit = Integer.numberOfTrailingZeros(remaining) + 1;   // niedrigstes gesetztes Bit
            remaining &= remaining - 1;                                 // dieses Bit löschen

            place(bestRow, bestCol, digit);
            steps++;
            if (search()) {
                return true;
            }
            remove(bestRow, bestCol, digit);                            // Backtracking
        }
        return false;
    }

    /** Ziffern, die in Zelle ({@code row}, {@code col}) noch erlaubt sind, als Bitmaske. */
    private int candidates(int row, int col) {
        int used = rowUsed[row] | colUsed[col] | boxUsed[box(row, col)];
        return ~used & ALL_DIGITS;
    }

    /** Setzt eine Ziffer ins Board und trägt sie in alle drei Masken ein. */
    private void place(int row, int col, int digit) {
        board.set(row, col, digit);
        markUsed(row, col, digit);
    }

    /** Nimmt eine Ziffer zurück: Zelle leeren und das Bit aus allen drei Masken löschen. */
    private void remove(int row, int col, int digit) {
        board.set(row, col, Board.EMPTY);
        int clear = ~bit(digit);
        rowUsed[row] &= clear;
        colUsed[col] &= clear;
        boxUsed[box(row, col)] &= clear;
    }

    /** Trägt eine Ziffer in die Masken von Zeile, Spalte und Block ein. */
    private void markUsed(int row, int col, int digit) {
        int b = bit(digit);
        rowUsed[row] |= b;
        colUsed[col] |= b;
        boxUsed[box(row, col)] |= b;
    }

    /** Bit für eine Ziffer: 1 → Bit 0, 9 → Bit 8. */
    private static int bit(int digit) {
        return 1 << (digit - 1);
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
