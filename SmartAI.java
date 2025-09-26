import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * The {@code SmartAI} class represents a sophisticated AI player for the game of Reversi.
 * This AI evaluates moves based on a combination of positional strategy, flipping potential,
 * and the use of special discs. By leveraging a weighted position matrix and heuristics for
 * mobility and strategic disc usage, the AI aims to make decisions that balance short-term
 * gains with long-term board control.
 * Key Features:
 * <ul>
 *     <li>Position Evaluation: Uses a predefined weight matrix to prioritize strategic board positions
 *         such as corners and edges.</li>
 *     <li>Move Evaluation: Considers the number of discs flipped, special disc benefits, and the
 *         resulting impact on opponent mobility.</li>
 *     <li>Special Discs: Supports the use of "Bomb" and "Unflippable" discs, optimizing their
 *         deployment for maximum advantage.</li>
 *     <li>Randomization: Adds a slight randomness factor to move evaluation to avoid predictable
 *         gameplay.</li>
 * </ul>
 *
 * Author: Alaa Al Deen Abu Hegly. <br>
 * GitHub: <a href="https://github.com/AgentOfTheOreos">AgentOfTheOreos</a> <br>
 * Date: 22/11/2024. <br>
 * Version: 1.2.
 */

public class SmartAI extends AIPlayer {
	private final Random random = new Random();
	private static final int[][] POSITION_WEIGHTS = {
			{100, -20, 10,  5,  5, 10, -20, 100},
			{-20, -50,  1,  1,  1,  1, -50, -20},
			{10,   1,  3,  2,  2,  3,   1,  10},
			{5,    1,  2,  1,  1,  2,   1,   5},
			{5,    1,  2,  1,  1,  2,   1,   5},
			{10,   1,  3,  2,  2,  3,   1,  10},
			{-20, -50,  1,  1,  1,  1, -50, -20},
			{100, -20, 10,  5,  5, 10, -20, 100}
	};

	static {
		registerAIPlayerType("SmartAI", SmartAI.class);
	}

	public SmartAI(boolean isPlayerOne) {
		super(isPlayerOne);
	}

	/**
	 * Chooses the best possible move based on the current game state.
	 *
	 * @param logic The {@link PlayableLogic} interface providing game rules and current board state.
	 * @return A {@link Move} object representing the selected move, or {@code null} if no valid moves are available.
	 */
	@Override
	public Move makeMove(PlayableLogic logic) {
		List<Position> validMoves = logic.ValidMoves();
		if (validMoves.isEmpty()) {
			return null;
		}

		// Evaluate each move
		Position bestPosition = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		Disc bestDisc = null;

		for (Position pos : validMoves) {
			// Try each possible disc type
			List<Disc> discOptions = getAvailableDiscOptions();
			for (Disc disc : discOptions) {
				double score = evaluateMove(pos, disc, logic);

				// Add some randomization to avoid predictable play
				score += random.nextDouble() * 2;  // Small random factor

				if (score > bestScore) {
					bestScore = score;
					bestPosition = pos;
					bestDisc = disc;
				}
			}
		}

		// Reduce special disc count if used
		if (bestDisc instanceof BombDisc) {
			reduce_bomb();
		} else if (bestDisc instanceof UnflippableDisc) {
			reduce_unflippedable();
		}

		return new Move(bestPosition, bestDisc);
	}

	/**
	 * Retrieves the available disc options for this AI, including any special discs.
	 *
	 * @return A list of {@link Disc} objects that this AI can use for its next move.
	 */
	private List<Disc> getAvailableDiscOptions() {
		List<Disc> options = new ArrayList<>();
		options.add(new SimpleDisc(this));  // Always available

		if (getNumber_of_bombs() > 0) {
			options.add(new BombDisc(this));
		}
		if (getNumber_of_unflippedable() > 0) {
			options.add(new UnflippableDisc(this));
		}
		return options;
	}

	/**
	 * Evaluates the effectiveness of a potential move based on position, flips, and strategic factors.
	 *
	 * @param pos   The {@link Position} where the move is being considered.
	 * @param disc  The {@link Disc} to be placed at the specified position.
	 * @param logic The {@link PlayableLogic} interface providing game rules and current board state.
	 * @return A score representing the desirability of the move.
	 */
	private double evaluateMove(Position pos, Disc disc, PlayableLogic logic) {
		double score = 0;

		// Base position score from weight matrix
		score += POSITION_WEIGHTS[pos.row()][pos.col()];

		// Number of flips score (but not as important as position)
		int flips = logic.countFlips(pos);
		score += flips * 2;

		// Special disc strategic scoring
		if (disc instanceof BombDisc) {
			score += evaluateBombMove(pos, logic);
		} else if (disc instanceof UnflippableDisc) {
			score += evaluateUnflippableMove(pos);
		}

		// Mobility score - count opponent's possible moves after this move
		score += evaluateMobility(pos);

		return score;
	}

	/**
	 * Evaluates the strategic benefit of using a BombDisc at a specific position.
	 *
	 * @param pos   The {@link Position} where the BombDisc would be placed.
	 * @param logic The {@link PlayableLogic} interface providing game rules and current board state.
	 * @return A score representing the desirability of using a BombDisc at the specified position.
	 */
	private double evaluateBombMove(Position pos, PlayableLogic logic) {
		double score = 0;

		// Count nearby opponent discs
		int opponentDiscs = countNearbyOpponentDiscs(pos, logic);
		score += opponentDiscs * 15;  // High value for positions with many opponent discs

		// Avoid wasting bombs on edge positions
		if (isEdgePosition(pos)) {
			score -= 30;
		}

		return score;
	}

	/**
	 * Evaluates the strategic benefit of using an UnflippableDisc at a specific position.
	 *
	 * @param pos The {@link Position} where the UnflippableDisc would be placed.
	 * @return A score representing the desirability of using an UnflippableDisc at the specified position.
	 */
	private double evaluateUnflippableMove(Position pos) {
		double score = 0;

		// Heavily favor corner positions
		if (isCornerPosition(pos)) {
			score += 200;
		}
		// Favor edge positions
		else if (isEdgePosition(pos)) {
			score += 100;
		}

		return score;
	}

	/**
	 * Assesses the mobility impact of making a move, considering its effect on opponent options.
	 *
	 * @param pos The {@link Position} where the move would be made.
	 * @return A score representing the mobility impact of the move.
	 */
	private double evaluateMobility(Position pos) {
		// This would require making a move and checking opponent's options
		// Since we can't modify the board state, we can use a simpler heuristic approach.
		double score = 0;

		// Prefer moves that don't give easy access to corners
		if (isNextToCorner(pos) && !isCornerPosition(pos)) {
			score -= 50;
		}

		return score;
	}

	/**
	 * Checks if the specified position is a corner position on the board.
	 *
	 * @param pos The {@link Position} to check.
	 * @return {@code true} if the position is a corner; {@code false} otherwise.
	 */
	private boolean isCornerPosition(Position pos) {
		return (pos.row() == 0 || pos.row() == 7) &&
				(pos.col() == 0 || pos.col() == 7);
	}

	/**
	 * Checks if the specified position is an edge position on the board (not including corners).
	 *
	 * @param pos The {@link Position} to check.
	 * @return {@code true} if the position is an edge; {@code false} otherwise.
	 */
	private boolean isEdgePosition(Position pos) {
		return pos.row() == 0 || pos.row() == 7 ||
				pos.col() == 0 || pos.col() == 7;
	}

	/**
	 * Checks if the specified position is adjacent to a corner.
	 *
	 * @param pos The {@link Position} to check.
	 * @return {@code true} if the position is next to a corner; {@code false} otherwise.
	 */
	private boolean isNextToCorner(Position pos) {
		int r = pos.row();
		int c = pos.col();
		return (r <= 1 && c <= 1) || (r <= 1 && c >= 6) ||
				(r >= 6 && c <= 1) || (r >= 6 && c >= 6);
	}

	/**
	 * Counts the number of opponent's discs in the 8 positions surrounding the given position.
	 * Used for evaluating potential bomb disc placements.
	 *
	 * @param pos The position to check around
	 * @param logic The game logic instance
	 * @return The number of opponent discs in adjacent positions
	 */
	private int countNearbyOpponentDiscs(Position pos, PlayableLogic logic) {
		int count = 0;
		for (int dr = -1; dr <= 1; dr++) {
			for (int dc = -1; dc <= 1; dc++) {
				if (dr == 0 && dc == 0) continue;
				Position nearby = new Position(pos.row() + dr, pos.col() + dc);
				if (nearby.isValid()) {
					Disc disc = logic.getDiscAtPosition(nearby);
					if (disc != null && disc.getOwner() != this) {
						count++;
					}
				}
			}
		}
		return count;
	}
}