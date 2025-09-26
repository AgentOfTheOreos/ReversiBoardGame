import java.util.Comparator;
import java.util.List;
/**
 * Represents an AI player which performs moves that guarantee the most amount of enemy disc flips.
 */
public class GreedyAI extends AIPlayer {
	static {
		// Allow to register this AI type when the class is loaded
		registerAIPlayerType("GreedyAI", GreedyAI.class);
	}

	public GreedyAI(boolean isPlayerOne) {
		super(isPlayerOne);
	}

	/**
	 * The logic for a GreedyAI player's behaviour.
	 * Consistently picks a simple/regular disc at every turn.
	 * Places the disc at a precomputed position guaranteed to flip as many enemy discs as possible.
	 * This logic method has a priority order to follow in the following order:
	 * Place the disc at a position that executes highest flip count, if more than 1 such position found
	 * , place on rightmost position, if more than 1 such position found, place on the bottom-most position.
	 * @param logic The status of the current ongoing game.
	 * @return The move to execute for the GreedyAI player if valid, else, returns null.
	 */
	@Override
	public Move makeMove(PlayableLogic logic) {
		List<Position> validMoves = logic.ValidMoves();

		// Create a position comparator that considers flips, column (rightmost), and row (bottom-most)
		Comparator<Position> moveComparator = (pos1, pos2) -> {
			// First compare by number of flips (descending order)
			int flipsComparison = Integer.compare(logic.countFlips(pos2), logic.countFlips(pos1));
			if (flipsComparison != 0) {
				return flipsComparison;
			}

			// If flips are equal, compare by column (rightmost preferred)
			int columnComparison = Integer.compare(pos2.col(), pos1.col());
			if (columnComparison != 0) {
				return columnComparison;
			}

			// If columns are equal, compare by row (bottom-most preferred)
			return Integer.compare(pos2.row(), pos1.row());
		};

		// Find the best position using the comparator
		Position bestPosition = validMoves.stream()
				.max(moveComparator)
				.orElseThrow(() -> new IllegalStateException("No valid moves available"));

		// Always use SimpleDisc as per requirements
		return new Move(bestPosition, new SimpleDisc(this));
	}
}