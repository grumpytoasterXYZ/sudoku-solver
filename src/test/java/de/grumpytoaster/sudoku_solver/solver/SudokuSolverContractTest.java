package de.grumpytoaster.sudoku_solver.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.grumpytoaster.sudoku_solver.model.Board;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Tests, die jeder {@link SudokuSolver} bestehen muss.
 *
 * <p>Die Klasse ist abstrakt und wird nicht selbst ausgeführt. Jeder Solver bekommt
 * eine kleine Unterklasse, die nur {@link #createSolver()} implementiert, und erbt
 * damit alle Tests hier. So prüfen später Backtracking, Propagation und DLX gegen
 * genau dieselben Anforderungen.
 */
abstract class SudokuSolverContractTest {

    /** Liefert eine neue Instanz des zu testenden Solvers. */
    protected abstract SudokuSolver createSolver();

    // --- Testpuzzles ---

    /** Leicht: rein durch Ableitung lösbar (Norvig, easy #1). */
    static final String EASY =
            "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

    /** Bekanntes Beispiel (Wikipedia) mit eindeutiger Lösung {@link #EXAMPLE_SOLUTION}. */
    static final String EXAMPLE =
            "53..7....6..195....98....6.8...6...34..8.3..17...2...6.6....28....419..5....8..79";

    static final String EXAMPLE_SOLUTION =
            "534678912672195348198342567859761423426853791713924856961537284287419635345286179";

    /** Arto Inkala, 2010 – als „schwerstes Sudoku der Welt“ veröffentlicht. */
    static final String INKALA =
            "..53.....8......2..7..1.5..4....53...1..7...6..32...8..6.5....9..4....3......97..";

    /** Schwer für Backtracking (Norvig, hard #1): rund 10 Millionen Schritte beim naiven Solver. */
    static final String HARD =
            "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......";

    /** Doppelte 7 in der ersten Zeile. */
    static final String INVALID =
            "77" + ".".repeat(79);

    /**
     * Regelkonform, aber unlösbar: Oben links fehlt jede Möglichkeit
     * (1–8 stehen in der Zeile, die 9 in der Spalte).
     */
    static final String UNSOLVABLE =
            ".12345678" + "9........" + ".".repeat(63);

    // --- Hilfsmethode ---

    /**
     * Prüft, dass {@code result} eine korrekte Lösung von {@code puzzle} enthält:
     * Status SOLVED, kein leeres Feld, keine doppelten Ziffern, alle Vorgaben erhalten.
     */
    static void assertSolvedCorrectly(String puzzle, SolveResult result) {
        assertEquals(SolveStatus.SOLVED, result.status(), "Status");
        Board solution = result.solution();
        assertNotNull(solution, "Lösung fehlt");

        String compact = solution.toCompactString();
        assertTrue(compact.indexOf('.') < 0, "Lösung enthält leere Felder:\n" + solution);
        assertTrue(BoardValidator.isValid(solution), "Lösung verletzt die Regeln:\n" + solution);

        for (int i = 0; i < Board.CELLS; i++) {
            char given = puzzle.charAt(i);
            if (given != '.' && given != '0') {
                assertEquals(given, compact.charAt(i), "Vorgabe an Position " + i + " verändert");
            }
        }
    }

    private SolveResult solve(String puzzle) {
        return createSolver().solve(Board.fromString(puzzle));
    }

    // --- Lösbare Puzzles ---

    @Test
    void solvesEasyPuzzle() {
        assertSolvedCorrectly(EASY, solve(EASY));
    }

    @Test
    void solvesExampleToItsUniqueSolution() {
        SolveResult result = solve(EXAMPLE);

        assertSolvedCorrectly(EXAMPLE, result);
        assertEquals(EXAMPLE_SOLUTION, result.solution().toCompactString());
    }

    @Test
    void solvesInkalaPuzzle() {
        assertSolvedCorrectly(INKALA, solve(INKALA));
    }

    @Test
    void solvesHardPuzzleWithinTimeLimit() {
        SolveResult result = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> solve(HARD));
        assertSolvedCorrectly(HARD, result);
    }

    @Test
    void solvesEmptyBoard() {
        // Kein echtes Sudoku (viele Lösungen), aber jede gültige Füllung ist richtig
        assertSolvedCorrectly(".".repeat(Board.CELLS), solve(".".repeat(Board.CELLS)));
    }

    @Test
    void alreadySolvedBoardIsReturnedAsSolution() {
        SolveResult result = solve(EXAMPLE_SOLUTION);

        assertSolvedCorrectly(EXAMPLE_SOLUTION, result);
        assertEquals(EXAMPLE_SOLUTION, result.solution().toCompactString());
    }

    // --- Nicht lösbare Eingaben ---

    @Test
    void invalidPuzzleIsRejectedWithoutSearching() {
        SolveResult result = solve(INVALID);

        assertEquals(SolveStatus.INVALID, result.status());
        assertNull(result.solution());
        assertEquals(0, result.steps());
    }

    @Test
    void invalidCompletelyFilledBoardIsRejected() {
        String broken = "3" + EXAMPLE_SOLUTION.substring(1);   // 5 oben links durch 3 ersetzt
        assertEquals(SolveStatus.INVALID, solve(broken).status());
    }

    @Test
    void unsolvablePuzzleIsDetected() {
        SolveResult result = solve(UNSOLVABLE);

        assertEquals(SolveStatus.UNSOLVABLE, result.status());
        assertNull(result.solution());
    }

    @Test
    void rejectsNullPuzzle() {
        SudokuSolver solver = createSolver();
        assertThrows(NullPointerException.class, () -> solver.solve(null));
    }

    // --- Vertrag ---

    @Test
    void inputBoardIsNotModified() {
        for (String puzzle : new String[] {EASY, EXAMPLE, INVALID, UNSOLVABLE}) {
            Board board = Board.fromString(puzzle);
            Board before = board.copy();

            createSolver().solve(board);

            assertEquals(before, board, "Eingabe verändert bei " + puzzle);
        }
    }

    @Test
    void solutionIsIndependentOfInput() {
        Board puzzle = Board.fromString(EXAMPLE);
        SolveResult result = createSolver().solve(puzzle);

        result.solution().set(0, 2, Board.EMPTY);   // (0,2) war im Puzzle leer, in der Lösung eine 4

        assertTrue(puzzle.isEmpty(0, 2));
        assertEquals(EXAMPLE, puzzle.toCompactString());
    }

    @Test
    void sameSolverInstanceCanSolveSeveralPuzzles() {
        SudokuSolver solver = createSolver();

        SolveResult first = solver.solve(Board.fromString(EASY));
        solver.solve(Board.fromString(INVALID));
        solver.solve(Board.fromString(EXAMPLE));
        SolveResult again = solver.solve(Board.fromString(EASY));

        assertSolvedCorrectly(EASY, again);
        assertEquals(first.solution(), again.solution(), "gleiche Eingabe, gleiche Lösung");
        assertEquals(first.steps(), again.steps(), "Schrittzähler wird pro Aufruf zurückgesetzt");
    }
}
