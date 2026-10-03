package de.grumpytoaster.sudoku_solver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoardTest {

    /** Bekanntes Beispielpuzzle, 81 Zeichen, leere Felder als Punkt. */
    private static final String PUZZLE =
            "53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79";

    /** Die Lösung von {@link #PUZZLE}, komplett gefüllt. */
    private static final String SOLUTION =
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179";

    /** 81 leere Zellen. */
    private static final String ALL_EMPTY = ".".repeat(Board.CELLS);

    // --- Konstanten ---

    @Test
    void constantsDescribeStandardSudoku() {
        assertEquals(9, Board.SIZE);
        assertEquals(81, Board.CELLS);
        assertEquals(0, Board.EMPTY);
    }

    // --- Leeres Board, get und set ---

    @Test
    void newBoardIsEmpty() {
        Board board = new Board();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                assertTrue(board.isEmpty(row, col), "Zelle (" + row + ", " + col + ") sollte leer sein");
            }
        }
    }

    @Test
    void setAndGetAtCornersAndInside() {
        Board board = new Board();
        board.set(0, 0, 1);
        board.set(8, 8, 9);
        board.set(4, 7, 5);

        assertEquals(1, board.get(0, 0));
        assertEquals(9, board.get(8, 8));
        assertEquals(5, board.get(4, 7));
        assertFalse(board.isEmpty(4, 7));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9})
    void setAcceptsEveryDigit(int digit) {
        Board board = new Board();
        board.set(4, 4, digit);
        assertEquals(digit, board.get(4, 4));
    }

    @Test
    void setEmptyClearsCell() {
        Board board = new Board();
        board.set(2, 3, 7);
        board.set(2, 3, Board.EMPTY);

        assertTrue(board.isEmpty(2, 3));
    }

    @Test
    void setOverwritesPreviousValue() {
        Board board = new Board();
        board.set(6, 1, 3);
        board.set(6, 1, 8);

        assertEquals(8, board.get(6, 1));
    }

    @Test
    void setChangesOnlyTheTargetCell() {
        Board board = new Board();
        board.set(4, 4, 7);

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (row != 4 || col != 4) {
                    assertTrue(board.isEmpty(row, col), "Zelle (" + row + ", " + col + ") wurde mitverändert");
                }
            }
        }
    }

    // --- Ungültige Eingaben für get, set und isEmpty ---

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsRowOutOfRange(int row) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.get(row, 0));
        assertThrows(IllegalArgumentException.class, () -> board.set(row, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> board.isEmpty(row, 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsColumnOutOfRange(int col) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.get(0, col));
        assertThrows(IllegalArgumentException.class, () -> board.set(0, col, 1));
        assertThrows(IllegalArgumentException.class, () -> board.isEmpty(0, col));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 10, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsDigitOutOfRange(int digit) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.set(0, 0, digit));
    }

    @Test
    void failedSetLeavesBoardUnchanged() {
        Board board = Board.fromString(PUZZLE);

        assertThrows(IllegalArgumentException.class, () -> board.set(0, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> board.set(9, 0, 1));

        assertEquals(Board.fromString(PUZZLE), board);
    }

    // --- index ---

    @Test
    void indexOfCornersAndRowStarts() {
        assertEquals(0, Board.index(0, 0));
        assertEquals(8, Board.index(0, 8));
        assertEquals(9, Board.index(1, 0));
        assertEquals(72, Board.index(8, 0));
        assertEquals(80, Board.index(8, 8));
    }

    @Test
    void indexIsUniqueAndCoversAllCells() {
        boolean[] seen = new boolean[Board.CELLS];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                int index = Board.index(row, col);
                assertFalse(seen[index], "Index " + index + " kommt doppelt vor");
                seen[index] = true;
            }
        }
        for (int i = 0; i < Board.CELLS; i++) {
            assertTrue(seen[i], "Index " + i + " wird nie erreicht");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9})
    void indexRejectsOutOfRange(int value) {
        assertThrows(IllegalArgumentException.class, () -> Board.index(value, 0));
        assertThrows(IllegalArgumentException.class, () -> Board.index(0, value));
    }

    // --- fromString: gültige Eingaben ---

    @Test
    void fromStringMapsCharacterPositionToCell() {
        Board board = Board.fromString(PUZZLE);

        assertEquals(5, board.get(0, 0), "Zeichen 0 -> Zelle (0, 0)");
        assertEquals(6, board.get(1, 0), "Zeichen 9 -> Zelle (1, 0)");
        assertEquals(9, board.get(8, 8), "Zeichen 80 -> Zelle (8, 8)");
        assertTrue(board.isEmpty(0, 2), "Zeichen 2 ist ein Punkt");
    }

    @Test
    void fromStringReadsEveryCellOfSolution() {
        Board board = Board.fromString(SOLUTION);
        for (int i = 0; i < Board.CELLS; i++) {
            int expected = SOLUTION.charAt(i) - '0';
            assertEquals(expected, board.get(i / Board.SIZE, i % Board.SIZE), "Zeichen " + i);
        }
    }

    @Test
    void fromStringTreatsZeroAndDotAsEmpty() {
        assertEquals(Board.fromString(PUZZLE), Board.fromString(PUZZLE.replace('.', '0')));
    }

    @Test
    void fromStringWithOnlyEmptyCellsEqualsNewBoard() {
        assertEquals(new Board(), Board.fromString(ALL_EMPTY));
        assertEquals(new Board(), Board.fromString("0".repeat(Board.CELLS)));
        assertEquals(new Board(), Board.fromString(".0".repeat(40) + "."), "Mischung aus Punkt und Null");
    }

    @Test
    void boardsFromSameStringAreIndependent() {
        Board a = Board.fromString(PUZZLE);
        Board b = Board.fromString(PUZZLE);

        a.set(0, 2, 1);

        assertTrue(b.isEmpty(0, 2));
    }

    // --- fromString: ungültige Eingaben ---

    @Test
    void fromStringRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 80, 82, 162})
    void fromStringRejectsWrongLength(int length) {
        String input = ".".repeat(length);
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(input));
    }

    /**
     * Neben Buchstaben und Sonderzeichen auch Ziffern aus anderen Schriften:
     * '٣' (arabisch-indische 3) und '５' (vollbreite 5). Character.isDigit
     * würde beide akzeptieren, als Sudoku-Ziffer sind sie aber ungültig.
     */
    @ParameterizedTest
    @ValueSource(chars = {'x', 'A', ' ', '-', '+', '*', '\t', '\n', '٣', '５'})
    void fromStringRejectsInvalidCharacter(char invalid) {
        String input = invalid + PUZZLE.substring(1);
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(input));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 40, 80})
    void fromStringErrorNamesThePosition(int position) {
        String input = PUZZLE.substring(0, position) + 'x' + PUZZLE.substring(position + 1);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Board.fromString(input));

        assertTrue(e.getMessage().contains(String.valueOf(position)),
                "Meldung sollte Position " + position + " nennen: " + e.getMessage());
    }

    @Test
    void fromStringRejectsPrettyPrintedGrid() {
        // toString() ist zum Lesen gedacht und lässt sich bewusst nicht zurück einlesen
        String pretty = Board.fromString(PUZZLE).toString();
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(pretty));
    }

    // --- toCompactString ---

    @Test
    void compactStringRoundTrip() {
        assertEquals(PUZZLE, Board.fromString(PUZZLE).toCompactString());
    }

    @Test
    void compactStringRoundTripForSolution() {
        assertEquals(SOLUTION, Board.fromString(SOLUTION).toCompactString());
    }

    @Test
    void compactStringAlwaysUsesDotForEmptyCells() {
        assertEquals(PUZZLE, Board.fromString(PUZZLE.replace('.', '0')).toCompactString());
    }

    @Test
    void emptyBoardCompactStringIsAllDots() {
        assertEquals(ALL_EMPTY, new Board().toCompactString());
    }

    @Test
    void compactStringReflectsChangesViaSet() {
        Board board = new Board();
        board.set(8, 8, 9);
        board.set(0, 0, 1);

        assertEquals("1" + ".".repeat(79) + "9", board.toCompactString());
    }

    // --- copy ---

    @Test
    void copyIsEqualButIndependent() {
        Board original = Board.fromString(PUZZLE);
        Board copy = original.copy();
        assertEquals(original, copy);

        copy.set(0, 0, 9);

        assertEquals(5, original.get(0, 0), "Original darf sich nicht ändern");
        assertNotEquals(original, copy);
    }

    @Test
    void changingOriginalDoesNotAffectCopy() {
        Board original = Board.fromString(PUZZLE);
        Board copy = original.copy();

        original.set(0, 2, 4);

        assertTrue(copy.isEmpty(0, 2));
    }

    @Test
    void copyReturnsNewObject() {
        Board board = new Board();
        assertNotSame(board, board.copy());
    }

    @Test
    void copyOfEmptyBoardIsEmpty() {
        assertEquals(new Board(), new Board().copy());
    }

    // --- equals und hashCode ---

    @Test
    void equalBoardsHaveEqualHashCodes() {
        Board a = Board.fromString(PUZZLE);
        Board b = Board.fromString(PUZZLE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsIsReflexiveSymmetricAndTransitive() {
        Board a = Board.fromString(PUZZLE);
        Board b = Board.fromString(PUZZLE);
        Board c = a.copy();

        assertTrue(a.equals(a), "reflexiv");
        assertTrue(a.equals(b) && b.equals(a), "symmetrisch");
        assertTrue(a.equals(b) && b.equals(c) && a.equals(c), "transitiv");
    }

    @Test
    void boardsDifferingOnlyInLastCellAreNotEqual() {
        Board a = Board.fromString(SOLUTION);
        Board b = a.copy();
        b.set(8, 8, 1);

        assertNotEquals(a, b);
    }

    @Test
    void hashCodeIsStableForUnchangedBoard() {
        Board board = Board.fromString(PUZZLE);
        assertEquals(board.hashCode(), board.hashCode());
    }

    @Test
    void boardIsNotEqualToNullOrOtherType() {
        Board board = new Board();
        assertFalse(board.equals(null));
        assertFalse(board.equals("kein Board"));
        assertFalse(board.equals(ALL_EMPTY), "auch nicht dem gleichwertigen String");
    }

    // --- toString ---

    @Test
    void toStringShowsGridWithBlockSeparators() {
        String[] lines = Board.fromString(PUZZLE).toString().split("\n");

        assertEquals(11, lines.length, "9 Rasterzeilen und 2 Trennlinien");
        assertEquals("5 3 . | . 7 . | . . .", lines[0].stripTrailing());
        assertEquals("------+-------+------", lines[3]);
        assertEquals("------+-------+------", lines[7]);
        assertEquals(". . . | . 8 . | . 7 9", lines[10].stripTrailing());
    }

    @Test
    void everyGridLineHasTwoBlockSeparators() {
        String[] lines = Board.fromString(SOLUTION).toString().split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i == 3 || i == 7) {
                continue;
            }
            long bars = lines[i].chars().filter(ch -> ch == '|').count();
            assertEquals(2L, bars, "Zeile " + i + ": " + lines[i]);
        }
    }

    @Test
    void toStringOfEmptyBoardShowsOnlyDots() {
        String[] lines = new Board().toString().split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i == 3 || i == 7) {
                continue;
            }
            assertEquals(".........", lines[i].replace(" ", "").replace("|", ""), "Zeile " + i);
        }
    }
}
