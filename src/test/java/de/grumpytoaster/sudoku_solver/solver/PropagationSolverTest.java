package de.grumpytoaster.sudoku_solver.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.grumpytoaster.sudoku_solver.model.Board;

import org.junit.jupiter.api.Test;

/**
 * Prüft {@link PropagationSolver}: alle Tests aus {@link SudokuSolverContractTest},
 * die Ableitungsregeln und Vergleiche mit dem naiven {@link BacktrackingSolver}.
 *
 * <p>{@link SolveResult#steps()} zählt hier nur Rateversuche. 0 Schritte heißt also:
 * rein durch Ableiten gelöst bzw. als unlösbar erkannt.
 */
class PropagationSolverTest extends SudokuSolverContractTest {

    /**
     * Ohne Raten nur mit Naked <i>und</i> Hidden Singles zusammen lösbar.
     * Aus {@link #EXAMPLE_SOLUTION} erzeugt, hat also genau diese Lösung.
     * Gemessen: nur Naked Singles 585 Rateversuche, nur Hidden Singles 4.
     */
    static final String NEEDS_BOTH_SINGLES =
            ".34...9..67.....4......25..85..6......6..3.9....9..8..9..5.......7....35.4..8....";

    /**
     * Regelkonform, aber unlösbar, und das erst nach einer Ableitung: In Zeile 0 fehlen
     * 1 und 9, die 1 ist in beiden freien Spalten blockiert. Oben links bleibt nur die 9,
     * danach hat oben rechts keinen Kandidaten mehr.
     */
    static final String DEAD_END_AFTER_DEDUCTION =
            ".2345678." + ".".repeat(27) + "........1" + "1........" + ".".repeat(27);

    /**
     * Regelkonform, aber unlösbar: Die 1 hat in Zeile 0 keinen Platz mehr, obwohl jede
     * freie Zelle dort noch mehrere Kandidaten hat. Fällt nur über die Einheit auf.
     */
    static final String NO_PLACE_FOR_ONE =
            "......234" + "1........" + "...1....." + ".".repeat(54);

    /**
     * Wenige Vorgaben aus {@link #EXAMPLE_SOLUTION}, mehrere Lösungen möglich. Auf einer
     * Rateebene scheitern zwei Versuche nacheinander, erst der dritte führt weiter. Prüft,
     * dass die Sicherung vor dem Raten dabei unverändert bleibt.
     */
    static final String SEVERAL_FAILED_GUESSES =
            ".....8....7....3...9............1..3.2.85......3..4............2.7....3.......1.9";

    @Override
    protected SudokuSolver createSolver() {
        return new PropagationSolver();
    }

    private static long steps(SudokuSolver solver, String puzzle) {
        return solver.solve(Board.fromString(puzzle)).steps();
    }

    // --- Ableiten ohne Raten ---

    @Test
    void alreadySolvedBoardNeedsNoSteps() {
        assertEquals(0, steps(createSolver(), EXAMPLE_SOLUTION));
    }

    @Test
    void easyPuzzleIsSolvedWithoutGuessing() {
        assertEquals(0, steps(createSolver(), EASY));
    }

    @Test
    void examplePuzzleIsSolvedWithoutGuessing() {
        assertEquals(0, steps(createSolver(), EXAMPLE));
    }

    @Test
    void puzzleNeedingBothKindsOfSinglesIsSolvedWithoutGuessing() {
        SolveResult result = createSolver().solve(Board.fromString(NEEDS_BOTH_SINGLES));

        assertSolvedCorrectly(NEEDS_BOTH_SINGLES, result);
        assertEquals(EXAMPLE_SOLUTION, result.solution().toCompactString());
        assertEquals(0, result.steps(), "Naked oder Hidden Singles fehlen");
    }

    @Test
    void detectsUnsolvablePuzzleWithoutAnyStep() {
        // Oben links hat keinen Kandidaten – das fällt vor dem ersten Rateversuch auf
        assertEquals(0, steps(createSolver(), UNSOLVABLE));
    }

    @Test
    void detectsContradictionAfterDeductionWithoutGuessing() {
        SolveResult result = createSolver().solve(Board.fromString(DEAD_END_AFTER_DEDUCTION));

        assertEquals(SolveStatus.UNSOLVABLE, result.status());
        assertEquals(0, result.steps());
    }

    @Test
    void detectsDigitWithoutPlaceWithoutGuessing() {
        SolveResult result = createSolver().solve(Board.fromString(NO_PLACE_FOR_ONE));

        assertEquals(SolveStatus.UNSOLVABLE, result.status());
        assertEquals(0, result.steps());
    }

    // --- Raten nach dem Ableiten ---

    @Test
    void guessesAreUndoneCompletelyOnHardPuzzles() {
        // INKALA und HARD gehen nicht ohne Raten. Eine korrekte Lösung zeigt, dass
        // Fehlversuche samt aller daraus abgeleiteten Ziffern zurückgenommen wurden.
        for (String puzzle : new String[] {INKALA, HARD}) {
            SolveResult result = createSolver().solve(Board.fromString(puzzle));

            assertTrue(result.steps() > 0, "Puzzle sollte Raten erfordern: " + puzzle);
            assertSolvedCorrectly(puzzle, result);
        }
    }

    @Test
    void stateBackupSurvivesSeveralFailedGuessesInARow() {
        assertSolvedCorrectly(SEVERAL_FAILED_GUESSES,
                createSolver().solve(Board.fromString(SEVERAL_FAILED_GUESSES)));
    }

    // --- Vergleich mit dem naiven Backtracking ---

    @Test
    void needsFarFewerStepsThanBacktrackingOnHardPuzzle() {
        long naive = steps(new BacktrackingSolver(), HARD);
        long propagation = steps(createSolver(), HARD);

        assertTrue(propagation * 1000 < naive,
                "Propagation sollte mindestens 1000-mal weniger raten: " + propagation + " vs. " + naive);
    }

    @Test
    void needsNoMoreStepsThanBacktrackingOnTestPuzzles() {
        // Keine allgemeine Garantie, aber für diese Puzzles gemessen und stabil
        for (String puzzle : new String[] {EASY, EXAMPLE, INKALA, UNSOLVABLE}) {
            long naive = steps(new BacktrackingSolver(), puzzle);
            long propagation = steps(createSolver(), puzzle);
            assertTrue(propagation <= naive, puzzle + ": " + propagation + " > " + naive);
        }
    }
}
