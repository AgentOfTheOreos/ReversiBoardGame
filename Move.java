import java.util.ArrayList;
import java.util.List;
/**
 * The {@code Move} class encapsulates the details of a single move in the game of Reversi. 
 * It records the position where a disc was placed, the disc itself, the list of opponent discs 
 * that were flipped as a result of the move, and whether a special disc (e.g., bomb or unflippable) 
 * was used. This class provides immutability for its fields by storing defensive copies of mutable data, 
 * ensuring the integrity of move records. It supports undo functionality by keeping track of all 
 * relevant changes caused by a move, making it essential for maintaining game history and state.
 */

public class Move {
	private final Position position;
	private final Disc disc;
	private final List<Disc> flippedDiscs;
	private final boolean wasSpecialDiscUsed;

	public Move(Position position, Disc disc, List<Disc> flippedDiscs, boolean wasSpecialDiscUsed) {
		this.position = position;
		this.disc = disc;
		this.flippedDiscs = new ArrayList<>(flippedDiscs); // Creating defensive copy
		// Added for undo functionality
		this.wasSpecialDiscUsed = wasSpecialDiscUsed;
	}

	public Move(Position position, Disc disc) {
		this(position, disc, new ArrayList<>(), false);
	}

	public Position position() {
		return position;
	}

	public Disc disc() {
		return disc;
	}

	public List<Disc> getFlippedDiscs() {
		return new ArrayList<>(flippedDiscs); // Return defensive copy
	}
}