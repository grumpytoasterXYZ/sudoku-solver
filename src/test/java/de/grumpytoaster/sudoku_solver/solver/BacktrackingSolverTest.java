package de.grumpytoaster.sudoku_solver.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.grumpytoaster.sudoku_solver.model.Board;

import org.junit.jupiter.api.Test;

/**
 * Prüft {@link BacktrackingSolver}: alle Tests aus {@link SudokuSolverContractTest}
 * plus die Besonderheiten des Schrittzählers.
 */
class BacktrackingSolverTest extends SudokuSolverContractTest {

    @Override
    protected SudokuSolver createSolver() {
        return new BacktrackingSolver();
    }

    @Test
    void alreadySolvedBoardNeedsNoSteps() {
        SolveResult result = createSolver().solve(Board.fromString(EXAMPLE_SOLUTION));
        assertEquals(0, result.steps());
    }

    @Test
    void stepsAreAtLeastTheNumberOfEmptyCells() {
        // Jede leere Zelle muss mindestens einmal gesetzt werden
        long emptyCells = EXAMPLE.chars().filter(c -> c == '.').count();
        SolveResult result = createSolver().solve(Board.fromString(EXAMPLE));

        assertTrue(result.steps() >= emptyCells,
                "erwartet mindestens " + emptyCells + ", waren " + result.steps());
    }

    @Test
    void singleEmptyCellNeedsExactlyOneStep() {
        String oneMissing = "." + EXAMPLE_SOLUTION.substring(1);
        SolveResult result = createSolver().solve(Board.fromString(oneMissing));

        assertEquals(SolveStatus.SOLVED, result.status());
        assertEquals(1, result.steps());
    }
}
