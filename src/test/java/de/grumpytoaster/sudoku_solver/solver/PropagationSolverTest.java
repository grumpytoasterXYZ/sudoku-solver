package de.grumpytoaster.sudoku_solver.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.grumpytoaster.sudoku_solver.model.Board;

import org.junit.jupiter.api.Test;

/**
 * Prüft {@link PropagationSolver}: alle Tests aus {@link SudokuSolverContractTest}
 * plus Vergleiche mit dem naiven {@link BacktrackingSolver}.
 */
class PropagationSolverTest extends SudokuSolverContractTest {

    @Override
    protected SudokuSolver createSolver() {
        return new PropagationSolver();
    }

    private static long steps(SudokuSolver solver, String puzzle) {
        return solver.solve(Board.fromString(puzzle)).steps();
    }

    @Test
    void alreadySolvedBoardNeedsNoSteps() {
        assertEquals(0, steps(createSolver(), EXAMPLE_SOLUTION));
    }

    @Test
    void easyPuzzleIsSolvedWithoutAnyWrongGuess() {
        // MRV findet hier immer eine Zelle mit genau einem Kandidaten:
        // Schritte = Anzahl leerer Felder, kein einziger Fehlversuch
        long emptyCells = EASY.chars().filter(c -> c == '0').count();
        assertEquals(emptyCells, steps(createSolver(), EASY));
    }

    @Test
    void needsFarFewerStepsThanBacktrackingOnHardPuzzle() {
        long naive = steps(new BacktrackingSolver(), HARD);
        long mrv = steps(createSolver(), HARD);

        assertTrue(mrv * 100 < naive,
                "MRV sollte mindestens 100-mal weniger Schritte brauchen: " + mrv + " vs. " + naive);
    }

    @Test
    void needsNoMoreStepsThanBacktrackingOnTestPuzzles() {
        // Keine allgemeine Garantie, aber für diese Puzzles gemessen und stabil
        for (String puzzle : new String[] {EASY, EXAMPLE, INKALA, UNSOLVABLE}) {
            long naive = steps(new BacktrackingSolver(), puzzle);
            long mrv = steps(createSolver(), puzzle);
            assertTrue(mrv <= naive, puzzle + ": " + mrv + " > " + naive);
        }
    }

    @Test
    void detectsUnsolvablePuzzleWithoutAnyStep() {
        // Oben links hat keinen Kandidaten – MRV sieht das, bevor überhaupt geraten wird
        assertEquals(0, steps(createSolver(), UNSOLVABLE));
    }
}
