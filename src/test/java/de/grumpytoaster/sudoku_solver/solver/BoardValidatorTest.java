package de.grumpytoaster.sudoku_solver.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.grumpytoaster.sudoku_solver.model.Board;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoardValidatorTest {

    /** Bekanntes Beispielpuzzle, unvollständig, aber regelkonform. */
    private static final String PUZZLE =
            "53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79";

    /** Die eindeutige Lösung von {@link #PUZZLE}. */
    private static final String SOLUTION =
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179";

    // --- Gültige Boards ---

    @Test
    void emptyBoardIsValid() {
        Board board = new Board();
        assertTrue(BoardValidator.isValid(board));
        assertEquals(List.of(), BoardValidator.findConflicts(board));
    }

    @Test
    void solvedSudokuIsValid() {
        assertTrue(BoardValidator.isValid(Board.fromString(SOLUTION)));
    }

    @Test
    void partiallyFilledPuzzleIsValid() {
        assertTrue(BoardValidator.isValid(Board.fromString(PUZZLE)));
    }

    @Test
    void singleFilledCellIsValid() {
        Board board = new Board();
        board.set(4, 4, 5);
        assertTrue(BoardValidator.isValid(board));
    }

    @Test
    void fullRowWithDigitsOneToNineIsValid() {
        Board board = Board.fromString("123456789" + ".".repeat(72));
        assertTrue(BoardValidator.isValid(board));
    }

    @Test
    void oneDigitPlacedOncePerRowColumnAndBoxIsValid() {
        // Alle Einsen aus der Lösung: neun gleiche Ziffern, aber in jeder Einheit nur einmal
        Board board = Board.fromString(SOLUTION.replaceAll("[^1]", "."));
        assertTrue(BoardValidator.isValid(board));
    }

    @Test
    void sameDigitDiagonallyAcrossBoxBorderIsValid() {
        // Direkt benachbart, aber in verschiedenen Zeilen, Spalten UND Blöcken
        int[][] pairs = {{2, 2, 3, 3}, {5, 5, 6, 6}, {2, 6, 3, 5}, {6, 2, 5, 3}};
        for (int[] p : pairs) {
            Board board = new Board();
            board.set(p[0], p[1], 4);
            board.set(p[2], p[3], 4);
            assertTrue(BoardValidator.isValid(board),
                    "(" + p[0] + "," + p[1] + ") und (" + p[2] + "," + p[3] + ") kollidieren nicht");
        }
    }

    @Test
    void validButUnsolvablePuzzleIsValid() {
        // Oben links ist keine Ziffer mehr möglich (1-8 in der Zeile, 9 in der Spalte).
        // Der Validator prüft nur doppelte Ziffern, nicht die Lösbarkeit.
        Board board = Board.fromString(".12345678" + "9........" + ".".repeat(63));
        assertTrue(BoardValidator.isValid(board));
    }

    // --- Je eine Konfliktart ---

    @Test
    void detectsDuplicateInRow() {
        Board board = new Board();
        board.set(0, 0, 5);
        board.set(0, 8, 5);

        assertEquals(List.of(Board.index(0, 0), Board.index(0, 8)), BoardValidator.findConflicts(board));
    }

    @Test
    void detectsDuplicateInColumn() {
        Board board = new Board();
        board.set(0, 4, 7);
        board.set(8, 4, 7);

        assertEquals(List.of(Board.index(0, 4), Board.index(8, 4)), BoardValidator.findConflicts(board));
    }

    @Test
    void detectsDuplicateInBoxOnly() {
        // (3,3) und (5,5) liegen im selben Block, aber in verschiedenen Zeilen und Spalten
        Board board = new Board();
        board.set(3, 3, 2);
        board.set(5, 5, 2);

        assertEquals(List.of(Board.index(3, 3), Board.index(5, 5)), BoardValidator.findConflicts(board));
    }

    @Test
    void sameDigitInRowAcrossBoxBorderIsConflict() {
        // Verschiedene Blöcke, aber dieselbe Zeile
        Board board = new Board();
        board.set(0, 2, 6);
        board.set(0, 3, 6);

        assertEquals(List.of(Board.index(0, 2), Board.index(0, 3)), BoardValidator.findConflicts(board));
    }

    @Test
    void adjacentDuplicatesAtStartOfBoardAreDetected() {
        Board board = Board.fromString("77" + ".".repeat(79));
        assertEquals(List.of(0, 1), BoardValidator.findConflicts(board));
    }

    // --- Jede Ziffer und jede Einheit ---

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
    void detectsDuplicateOfEveryDigit(int digit) {
        Board board = new Board();
        board.set(0, 0, digit);
        board.set(0, 8, digit);

        assertFalse(BoardValidator.isValid(board));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8})
    void detectsDuplicateInEveryRow(int row) {
        Board board = new Board();
        board.set(row, 0, 3);
        board.set(row, 8, 3);

        assertEquals(List.of(Board.index(row, 0), Board.index(row, 8)), BoardValidator.findConflicts(board));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8})
    void detectsDuplicateInEveryColumn(int col) {
        Board board = new Board();
        board.set(0, col, 3);
        board.set(8, col, 3);

        assertEquals(List.of(Board.index(0, col), Board.index(8, col)), BoardValidator.findConflicts(board));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8})
    void detectsDuplicateInEveryBox(int box) {
        // Obere linke und untere rechte Ecke desselben Blocks: weder Zeile noch Spalte gleich
        int startRow = (box / 3) * 3;
        int startCol = (box % 3) * 3;
        Board board = new Board();
        board.set(startRow, startCol, 8);
        board.set(startRow + 2, startCol + 2, 8);

        assertEquals(List.of(Board.index(startRow, startCol), Board.index(startRow + 2, startCol + 2)),
                BoardValidator.findConflicts(board));
    }

    // --- Mehrere Konflikte ---

    @Test
    void cellInRowAndColumnConflictIsListedOnce() {
        // (4,4) kollidiert mit (4,0) in der Zeile UND mit (0,4) in der Spalte
        Board board = new Board();
        board.set(4, 4, 9);
        board.set(4, 0, 9);
        board.set(0, 4, 9);

        assertEquals(List.of(Board.index(0, 4), Board.index(4, 0), Board.index(4, 4)),
                BoardValidator.findConflicts(board));
    }

    @Test
    void reportsAllCellsOfTripleDigit() {
        Board board = new Board();
        board.set(2, 0, 1);
        board.set(2, 5, 1);
        board.set(2, 8, 1);

        assertEquals(List.of(Board.index(2, 0), Board.index(2, 5), Board.index(2, 8)),
                BoardValidator.findConflicts(board));
    }

    @Test
    void twoDifferentDuplicatesInSameRow() {
        Board board = Board.fromString("1122" + ".".repeat(77));
        assertEquals(List.of(0, 1, 2, 3), BoardValidator.findConflicts(board));
    }

    @Test
    void rowWithOneToEightPlusRepeatedOneIsInvalid() {
        Board board = Board.fromString("123456781" + ".".repeat(72));
        assertEquals(List.of(Board.index(0, 0), Board.index(0, 8)), BoardValidator.findConflicts(board));
    }

    @Test
    void conflictsInRowColumnAndBoxAtOnce() {
        Board board = new Board();
        board.set(0, 0, 1); // Zeile 0
        board.set(0, 8, 1);
        board.set(2, 4, 2); // Spalte 4
        board.set(8, 4, 2);
        board.set(6, 6, 3); // Block 8
        board.set(7, 7, 3);

        assertEquals(List.of(Board.index(0, 0), Board.index(0, 8), Board.index(2, 4),
                        Board.index(6, 6), Board.index(7, 7), Board.index(8, 4)),
                BoardValidator.findConflicts(board));
    }

    @Test
    void resultIsSortedAcrossSeveralConflicts() {
        Board board = new Board();
        board.set(8, 8, 3);
        board.set(8, 0, 3);
        board.set(0, 1, 6);
        board.set(0, 2, 6);

        assertEquals(List.of(Board.index(0, 1), Board.index(0, 2), Board.index(8, 0), Board.index(8, 8)),
                BoardValidator.findConflicts(board));
    }

    @Test
    void changedCellInSolutionIsDetected() {
        // Oben links steht eine 5; als 3 kollidiert sie mit (0,1) in Zeile/Block und (8,0) in der Spalte
        Board board = Board.fromString(SOLUTION);
        board.set(0, 0, 3);

        assertFalse(BoardValidator.isValid(board));
        assertEquals(List.of(Board.index(0, 0), Board.index(0, 1), Board.index(8, 0)),
                BoardValidator.findConflicts(board));
    }

    @Test
    void boardFilledWithOneDigitHasEveryCellInConflict() {
        Board board = Board.fromString("5".repeat(Board.CELLS));
        assertEquals(Board.CELLS, BoardValidator.findConflicts(board).size());
    }

    @Test
    void validRowsAndColumnsButInvalidBoxesAreDetected() {
        // Lateinisches Quadrat: jede Zeile um eins verschoben ("123456789", "234567891", ...).
        // Zeilen und Spalten sind fehlerfrei, die Blöcke nicht. Prüft, dass die Blockregel greift.
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                sb.append((row + col) % Board.SIZE + 1);
            }
        }
        assertFalse(BoardValidator.isValid(Board.fromString(sb.toString())));
    }

    // --- Vertrag der Methoden ---

    @Test
    void rejectsNullBoard() {
        assertThrows(NullPointerException.class, () -> BoardValidator.findConflicts(null));
        assertThrows(NullPointerException.class, () -> BoardValidator.isValid(null));
    }

    @Test
    void resultListIsUnmodifiable() {
        Board board = new Board();
        board.set(0, 0, 5);
        board.set(0, 1, 5);
        List<Integer> conflicts = BoardValidator.findConflicts(board);

        assertThrows(UnsupportedOperationException.class, () -> conflicts.add(42));
    }

    @Test
    void validatorDoesNotModifyBoard() {
        Board board = Board.fromString(SOLUTION);
        board.set(0, 0, 3);
        Board before = board.copy();

        BoardValidator.findConflicts(board);
        BoardValidator.isValid(board);

        assertEquals(before, board);
    }

    @Test
    void repeatedCallsReturnSameResult() {
        Board board = Board.fromString("1122" + ".".repeat(77));
        assertEquals(BoardValidator.findConflicts(board), BoardValidator.findConflicts(board));
    }
}
