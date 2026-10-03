package de.grumpytoaster.sudoku_solver.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BoardTest {

    /** Bekanntes Beispielpuzzle, 81 Zeichen, leere Felder als Punkt. */
    private static final String PUZZLE = "53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79";

    // Leeres Board
    @Test
    void newBoardIsEmpty() {
        Board board = new Board();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                assertTrue(board.isEmpty(row, col), "Zelle (" + row + ", " + col + ") sollte leer sein");
            }
        }
    }

    // getter und setter
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

    // Zelle leeren
    @Test
    void setEmptyClearsCell() {
        Board board = new Board();
        board.set(2, 3, 7);
        board.set(2, 3, Board.EMPTY);

        assertTrue(board.isEmpty(2, 3));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9})
    void rejectsRowOutOfRange(int row) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.get(row, 0));
        assertThrows(IllegalArgumentException.class, () -> board.set(row, 0, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9})
    void rejectsColumnOutOfRange(int col) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.get(0, col));
        assertThrows(IllegalArgumentException.class, () -> board.set(0, col, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 10})
    void rejectsDigitOutOfRange(int digit) {
        Board board = new Board();
        assertThrows(IllegalArgumentException.class, () -> board.set(0, 0, digit));
    }

    // fromString
    @Test
    void fromStringMapsCharacterPositionToCell() {
        Board board = Board.fromString(PUZZLE);

        assertEquals(5, board.get(0, 0), "Zeichen 0 -> Zelle (0, 0)");
        assertEquals(6, board.get(1, 0), "Zeichen 9 -> Zelle (1, 0)");
        assertEquals(9, board.get(8, 8), "Zeichen 80 -> Zelle (8, 8)");
        assertTrue(board.isEmpty(0, 2), "Zeichen 2 ist ein Punkt");
    }

    @Test
    void fromStringTreatsZeroAndDotAsEmpty() {
        assertEquals(Board.fromString(PUZZLE), Board.fromString(PUZZLE.replace('.', '0')));
    }

    // toCompactString
    @Test
    void compactStringRoundTrip() {
        assertEquals(PUZZLE, Board.fromString(PUZZLE).toCompactString());
    }

    @Test
    void compactStringAlwaysUsesDotForEmptyCells() {
        assertEquals(PUZZLE, Board.fromString(PUZZLE.replace('.', '0')).toCompactString());
    }

    @Test
    void fromStringRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 80, 82})
    void fromStringRejectsWrongLength(int length) {
        String input = ".".repeat(length);
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(input));
    }

    @ParameterizedTest
    @ValueSource(chars = {'x', ' ', '-'})
    void fromStringRejectsInvalidCharacter(char invalid) {
        String input = invalid + PUZZLE.substring(1);
        assertThrows(IllegalArgumentException.class, () -> Board.fromString(input));
    }

    // copy, equals und hashCode
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
    void equalBoardsHaveEqualHashCodes() {
        Board a = Board.fromString(PUZZLE);
        Board b = Board.fromString(PUZZLE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void boardIsNotEqualToNullOrOtherType() {
        Board board = new Board();
        assertFalse(board.equals(null));
        assertFalse(board.equals("kein Board"));
    }

    // toString

    @Test
    void toStringShowsGridWithBlockSeparators() {
        String[] lines = Board.fromString(PUZZLE).toString().split("\n");

        assertEquals(11, lines.length, "9 Rasterzeilen und 2 Trennlinien");
        assertEquals("5 3 . | . 7 . | . . .", lines[0].stripTrailing());
        assertEquals("------+-------+------", lines[3]);
        assertEquals(". . . | . 8 . | . 7 9", lines[10].stripTrailing());
    }
}
