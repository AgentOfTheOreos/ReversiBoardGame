import java.util.List;
import java.util.Random;
/**
 * Represents an AI player which performs moves randomly and picks disc types randomly.
 */
// Updated RandomAI to work with the registry
public class RandomAI extends AIPlayer {
	private final Random random = new Random();

	static {
		// Allow registering this AI type when the class is loaded
		registerAIPlayerType("RandomAI", RandomAI.class);
	}

	public RandomAI(boolean isPlayerOne) {
		super(isPlayerOne);
	}

	/**
	 * The logic for a RandomAI player's behavior.
	 * Picks one of the possible discs in the player's inventory (if there are any discs left) in an arbitrary and random manner.
	 * Places the arbitrarily picked disc at an arbitrary position picked from the possible positions to execute.
	 * @param logic The status of the current ongoing game.
	 * @return Returns the move to execute for the RandomAI player if valid, else, returns null.
	 */
	@Override
	public Move makeMove(PlayableLogic logic) {
		List<Position> validMoves = logic.ValidMoves();

		// Check if there are any valid moves
		if (validMoves.isEmpty()) {
			logic.isGameFinished();
		}

		// Randomly select a valid position
		Position randomPosition = validMoves.get(random.nextInt(validMoves.size()));

		// Randomly decide a disc type
		Disc selectedDisc = selectRandomDisc();

		return new Move(randomPosition, selectedDisc);
	}


	/**
	 * Logic for selecting a random disc from the RandomAI player's inventory.
	 *  Selects the disc based on random probability of a generated number's result in the range [0, 3].
	 *  0: Select the Simple Disc.
	 *  1: Select the Bomb Disc.
	 *  2: Select the Unflippable Disc.
	 * @return The randomly selected disc.
	 */
	private Disc selectRandomDisc() {
		// Calculate probabilities based on available special discs
		int totalOptions = 1; // Always have SimpleDisc as an option
		if (getNumber_of_bombs() > 0) totalOptions++;
		if (getNumber_of_unflippedable() > 0) totalOptions++;

		int choice = random.nextInt(totalOptions);

		if (choice == 0) {
			return new SimpleDisc(this);
		} else if (choice == 1 && getNumber_of_bombs() > 0) {
			reduce_bomb();
			return new BombDisc(this);
		} else if (getNumber_of_unflippedable() > 0) {
			reduce_unflippedable();
			return new UnflippableDisc(this);
		}

		// Fallback to SimpleDisc if no special discs available
		return new SimpleDisc(this);
	}
}