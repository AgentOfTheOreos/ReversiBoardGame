import java.util.*;

/**
 * The {@code GameLogic} class implements the core logic of a Reversi game, handling player turns, board state,
 * move validation, and special disc interactions. It maintains the game board, tracks the history of moves,
 * and enforces the rules for placing discs, flipping opponent discs, and managing special disc effects
 * (e.g., bombs and unflippable discs). The class also provides functionality for undoing moves,
 * resetting the game, and determining the end-of-game conditions and the winner. Designed to support
 * both human and AI players, it ensures a flexible and interactive gameplay experience.
 */

public class GameLogic implements PlayableLogic {

	private static final int BOARD_SIZE = 8;
	private Disc[][] board;
	private Player firstPlayer;
	private Player secondPlayer;
	private boolean isFirstPlayerTurn;
	private final Stack<Move> moveHistory;

	public GameLogic(Player firstPlayer, Player secondPlayer) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
		this.isFirstPlayerTurn = true;
		this.moveHistory = new Stack<>();
	}

	public GameLogic() {
		this.board = new Disc[BOARD_SIZE][BOARD_SIZE];
		this.moveHistory = new Stack<>();
		this.isFirstPlayerTurn = true;
	}


	/**
	 * Initializes the board by resetting it, effectively setting the positions to null, and then placing the initial
	 * game discs according to the Reversi game rules.
	 */
	private void initializeBoard() {
		// Clear the board
		board = new Disc[BOARD_SIZE][BOARD_SIZE];

		// Place initial discs
		board[BOARD_SIZE / 2 - 1][BOARD_SIZE / 2 - 1] = new SimpleDisc(firstPlayer);
		board[BOARD_SIZE / 2][BOARD_SIZE / 2] = new SimpleDisc(firstPlayer);
		board[BOARD_SIZE / 2 - 1][BOARD_SIZE / 2] = new SimpleDisc(secondPlayer);
		board[BOARD_SIZE / 2][BOARD_SIZE / 2 - 1] = new SimpleDisc(secondPlayer);
	}


	/**
	 * Attempts to place the input disc at the specified input position.
	 * First, the method validates the input position and makes sure it is within board bounds.
	 * Second, the method makes sure that the targeted position is empty.
	 * Third, the method checks if the position is valid, according to the Reversi game rules (flips are larger than 1)
	 * Finally, it places the disc, flips the enemy discs as a result, and then switches the player turns.
	 * @param pos  The position for locating a new disc on the board.
	 * @param disc The disc to place in the position.
	 * @return true if the disc was placed successfully, false otherwise.
	 */
	@Override
	public boolean locate_disc(Position pos, Disc disc) {
		boolean isValid = true;
		boolean posEmpty = true;
		boolean discsToFlipExist = true;
		// Validate position is within bounds
		if (!pos.isValid()) {
			System.out.printf("Move not valid at position (%d, %d).", pos.row(), pos.col()); // Modified after submission
			System.out.flush(); // Modified after submission
			return false;
		}

		// Check if the position is empty
		if (board[pos.row()][pos.col()] != null) {
			System.out.printf("Spot not empty at (%d, %d).", pos.row(), pos.col()); // Modified after submission
			System.out.flush(); // Modified after submission
			return false;
		}

		// Validate the move according to Reversi rules and handle special discs
		List<Disc> flippedDiscs = getFlippedDiscs(pos, disc);
		if (flippedDiscs.isEmpty()) {
			System.out.printf("No discs to flip at position: (%d, %d).", pos.row(), pos.col()); // Modified after submission
			System.out.flush(); // Modified after submission
			return false;
		}

		// Place the disc and perform flips
		board[pos.row()][pos.col()] = disc;

		// Create move record
		boolean bombUsed = disc.getType().equals("💣");
		boolean unflippableUsed = disc.getType().equals("⭕");
		boolean specialDiscUsed = bombUsed || unflippableUsed;

		if (bombUsed) {
			currentPlayer().reduce_bomb();
		} else if(unflippableUsed) {
			currentPlayer().reduce_unflippedable();
		}

		Move move = new Move(pos, disc, flippedDiscs, specialDiscUsed);
		moveHistory.push(move);

		// Print move information
		System.out.printf("Player %d placed a %s in (%d, %d)%n",
				disc.getOwner().isPlayerOne() ? 1 : 2,
				disc.getType(),
				pos.row() + 1,
				pos.col() + 1);

		// Perform flips and handle bomb explosions
		handleMoveEffects(move);

		// Switch turns
		isFirstPlayerTurn = !isFirstPlayerTurn;

		// Print empty line after turn
		System.out.println();

		return true;
	}


	/**
	 * Receives a list of discs for flipping, handles the case for bomb discs separately.
	 * @param discsToFlip List of discs to flip.
	 */
	private void performFlips(List<Disc> discsToFlip) {
		Player currentPlayer = currentPlayer();

		for (Disc disc : discsToFlip) {
			// Find the position of the disc on the board
			Position discPos = findDiscPosition(disc);

			if (discPos != null) {
				// Check if we're flipping a bomb disc
				if (disc instanceof BombDisc) {
					// Handle bomb explosion separately to allow chain reactions
					Set<Position> explodedPositions = new HashSet<>();
					handleBombExplosions(discPos, explodedPositions);
				} else {
					// Regular flip for non-bomb discs
					disc.setOwner(currentPlayer);
					System.out.printf("Player %d flipped the %s in (%d, %d)%n",
							currentPlayer.isPlayerOne() ? 1 : 2,
							disc.getType(),
							discPos.row() + 1,
							discPos.col() + 1);
				}
			}
		}
	}

	/**
	 * Method for handling an executed move, performs flips when necessary, and handles bomb explosions.
	 * @param move The move to handle.
	 */
	private void handleMoveEffects(Move move) {
		performFlips(move.getFlippedDiscs());
	}

	/**
	 * Recursive method to handle bomb explosions, also deals with chain reactions (cases where a bomb explosion
	 * affects another bomb).
	 * @param bombPos Position of the bomb for explosion.
	 * @param explodedPositions Set of coordinates to apply explosion effects on.
	 */
	private void handleBombExplosions(Position bombPos, Set<Position> explodedPositions) {
		// Prevent infinite recursion and double flipping
		if (explodedPositions.contains(bombPos)) {
			return;
		}

		Disc bombDisc = board[bombPos.row()][bombPos.col()];
		if (!(bombDisc instanceof BombDisc)) {
			return;
		}

		// Mark this position as processed
		explodedPositions.add(bombPos);

		// Get all valid neighboring positions
		List<Position> neighbors = getNeighboringPositions(bombPos);

		// Flip all neighboring discs except unflippable ones
		for (Position neighbor : neighbors) {
			// Skip already exploded positions
			if (explodedPositions.contains(neighbor)) {
				continue;
			}

			Disc neighborDisc = board[neighbor.row()][neighbor.col()];
			if (neighborDisc != null && !(neighborDisc instanceof UnflippableDisc)) {
				if (neighborDisc instanceof BombDisc) {
					// Trigger chain reaction
					handleBombExplosions(neighbor, explodedPositions);
				} else {
					// Only flip if not already affected by explosion
					Player newOwner = neighborDisc.getOwner().isPlayerOne() ? secondPlayer : firstPlayer;
					neighborDisc.setOwner(newOwner);
					System.out.printf("Player %d flipped the %s in (%d, %d)%n",
							currentPlayer().isPlayerOne() ? 1 : 2,
							neighborDisc.getType(),
							neighbor.row() + 1,
							neighbor.col() + 1);
					explodedPositions.add(neighbor);
				}
			}
		}
	}

	/**
	 * Method to return a list of neighboring positions to an input position.
	 * @param pos Base position to begin searching for neighbor positions.
	 * @return List of neighboring positions to input position.
	 */
	private List<Position> getNeighboringPositions(Position pos) {
		List<Position> neighbors = new ArrayList<>();
		for (Direction dir : Direction.values()) {
			Position neighbor = new Position(
					pos.row() + dir.getRowChange(),
					pos.col() + dir.getColumnChange()
			);
			if (neighbor.isValid()) {
				neighbors.add(neighbor);
			}
		}
		return neighbors;
	}

	/**
	 * Returns a list of discs for flipping, used for when we place a disc on the board.
	 * @param pos Position from which to add discs to list for flipping.
	 * @param disc Disc which will result the flipping after placement.
	 * @return List of discs for flipping.
	 */
	private List<Disc> getFlippedDiscs(Position pos, Disc disc) {
		List<Disc> flippedDiscs = new ArrayList<>();

		// Check all directions
		for (Direction dir : Direction.values()) {
			List<Disc> discsToFlip = getFlippedDiscsInDirection(pos, disc, dir);
			flippedDiscs.addAll(discsToFlip);
		}

		return flippedDiscs;
	}

	private List<Disc> getFlippedDiscsInDirection(Position pos, Disc placedDisc, Direction dir) {
		List<Disc> discsToFlip = new ArrayList<>();
		int row = pos.row() + dir.getRowChange();
		int col = pos.col() + dir.getColumnChange();


		// First position must contain opponent's disc
		while (new Position(row, col).isValid()) {
			Disc currentDisc = board[row][col];

			// Empty space - invalid direction
			if (currentDisc == null) {
				return new ArrayList<>();
			}

			// If we find our own disc, the flip is valid
			if (currentDisc.getOwner() == placedDisc.getOwner()) {
				return discsToFlip;
			}

			// Skip unflippable discs and invalidate the direction
			if (currentDisc instanceof UnflippableDisc) {
				return new ArrayList<>();
			}

			// Add opponent's disc to flip list
			discsToFlip.add(currentDisc);

			// Move to next position
			row += dir.getRowChange();
			col += dir.getColumnChange();
		}

		// Reached board edge without finding own disc
		return new ArrayList<>();
	}

	/**
	 * Populates the list of valid moves to make for both players.
	 * @return List of valid moves based on the current game state.
	 */
	@Override
	public List<Position> ValidMoves() {
		List<Position> validMoves = new ArrayList<>();

		// Check all empty positions
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				Position pos = new Position(row, col);
				if (board[row][col] == null) {
					// Test with a simple disc - if it's valid, any disc type can be placed there
					Disc testDisc = new SimpleDisc(currentPlayer());
					if (!getFlippedDiscs(pos, testDisc).isEmpty()) {
						validMoves.add(pos);
					}
				}
			}
		}
		return validMoves;
	}

	/**
	 * Returns the count of possible flips based on given position.
	 * @param pos Position to begin counting flips from.
	 * @return Number of flips based from input position.
	 */
	@Override
	public int countFlips(Position pos) {
		Player currentPlayer = isFirstPlayerTurn ? firstPlayer : secondPlayer;
		Disc testDisc = new SimpleDisc(currentPlayer);
		return getFlippedDiscs(pos, testDisc).size();
	}

	/**
	 * Undoes the last move made in the game, reverting the board to its state before the move.
	 * The method performs the following actions:
	 * <ul>
	 *   <li>If there are no moves to undo, it prints a message indicating so.</li>
	 *   <li>If the game is not between two human players (i.e., AI is involved), it prints a message indicating that undo is only available for human vs. human matches.</li>
	 *   <li>If there is a valid move to undo and the game is between two human players, it:
	 *       <ul>
	 *         <li>Removes the last placed disc from the board.</li>
	 *         <li>Reverts the state of all flipped discs back to their original owner.</li>
	 *         <li>Switches the player's turn back to the previous player who made the move.</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 * This method modifies the state of the game board and changes the turn to the opposite player.
	 */
	@Override
	public void undoLastMove() {
		if (moveHistory.isEmpty()) {
			System.out.println("\tNo previous move available to undo");
			System.out.println();
			return;
		}

		// Check if either player is AI
		if (!(firstPlayer.isHuman() && secondPlayer.isHuman())) {
			System.out.println("\tUndo is only available in Human vs Human matches");
			System.out.println();
			return;
		}

		System.out.println("Undoing last move:");
		Move lastMove = moveHistory.pop();

		if (lastMove.disc().getType().equals("💣")) {
			nextPrevPlayer().number_of_bombs++;
		} else if (lastMove.disc().getType().equals("⭕")) {
			nextPrevPlayer().number_of_unflippedable++;
		}

		// Remove the placed disc
		Position pos = lastMove.position();
		System.out.printf("\tUndo: removing %s from (%d, %d)%n",
				board[pos.row()][pos.col()].getType(),
				pos.row() + 1,
				pos.col() + 1);
		board[pos.row()][pos.col()] = null;

		// Revert all flipped discs
		for (Disc flippedDisc : lastMove.getFlippedDiscs()) {
			Player originalOwner = flippedDisc.getOwner().isPlayerOne() ? secondPlayer : firstPlayer;
			flippedDisc.setOwner(originalOwner);
			// Find the position of the disc on the board
			Position discPos = findDiscPosition(flippedDisc);
			if (discPos != null) {
				System.out.printf("\tUndo: flipping back %s in (%d, %d)%n",
						flippedDisc.getType(),
						discPos.row() + 1,
						discPos.col() + 1);
			}
		}

		// Switch turn back
		isFirstPlayerTurn = !isFirstPlayerTurn;

		System.out.println();
	}


	/**
	 * Attempts to find input disc's position on the game board.
	 * @param disc disc to attempt to search for.
	 * @return position object if the input disc was found on the board, null otherwise.
	 */
	private Position findDiscPosition(Disc disc) {
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				if (board[row][col] == disc) {
					return new Position(row, col);
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the game is finished by determining if either player has any valid moves left.
	 * The game is considered finished if the current player has no valid moves and the other player also has no valid moves.
	 * The method also updates the winner if the game is finished.
	 *
	 * <p>The method performs the following steps:
	 * <ul>
	 *   <li>First, it checks if the current player has any valid moves. If there are valid moves, the game is not finished, and the method returns {@code false}.</li>
	 *   <li>If the current player has no valid moves, it switches the turn to check if the other player has any valid moves.</li>
	 *   <li>If the other player also has no valid moves, the game is finished, and the winner is determined by counting the discs and updating the win state.</li>
	 *   <li>If either player has valid moves, the game is not finished and the method returns {@code false}.</li>
	 * </ul>
	 * </p>
	 *
	 * @return {@code true} if the game is finished (both players have no valid moves left),
	 *         {@code false} if the game is still ongoing (at least one player has valid moves).
	 */
	@Override
	public boolean isGameFinished() {
		// Game is finished if current player has no valid moves
		if (!ValidMoves().isEmpty()) {
			return false;
		}

		// Switch turns to check if the other player has moves
		isFirstPlayerTurn = !isFirstPlayerTurn;
		boolean otherPlayerHasMoves = !ValidMoves().isEmpty();
		isFirstPlayerTurn = !isFirstPlayerTurn;  // Switch back

		if (!otherPlayerHasMoves) {
			// Game is finished - count discs and update wins
			updateWinner();
			return true;
		}

		return false;
	}

	/*
	 * Method to update score for the winning player at the end of a Reversi match. Adds no points if both players
	 * have the same number of discs on the board.
	 */
	private void updateWinner() {
		int firstPlayerCount = 0;
		int secondPlayerCount = 0;

		// Count discs
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				Disc disc = board[row][col];
				if (disc != null) {
					if (disc.getOwner().isPlayerOne()) {
						firstPlayerCount++;
					} else {
						secondPlayerCount++;
					}
				}
			}
		}

		// Update wins
		if (firstPlayerCount > secondPlayerCount) {
			firstPlayer.addWin();
		} else if (secondPlayerCount > firstPlayerCount) {
			secondPlayer.addWin();
		}
		// No win added in case of a tie
	}

	@Override
	public Disc getDiscAtPosition(Position position) {
		return board[position.row()][position.col()];
	}

	@Override
	public int getBoardSize() {
		return BOARD_SIZE;
	}

	@Override
	public Player getFirstPlayer() {
		return firstPlayer;
	}

	@Override
	public Player getSecondPlayer() {
		return secondPlayer;
	}

	@Override
	public void setPlayers(Player player1, Player player2) {
		this.firstPlayer = player1;
		this.secondPlayer = player2;
	}

	@Override
	public boolean isFirstPlayerTurn() {
		return isFirstPlayerTurn;
	}

	@Override
	public void reset() {
		moveHistory.clear();
		isFirstPlayerTurn = true;
		initializeBoard();

		// Reset special disc counts for both players
		firstPlayer.reset_bombs_and_unflippedable();
		secondPlayer.reset_bombs_and_unflippedable();
	}

	public Player currentPlayer() {
		return isFirstPlayerTurn ? firstPlayer : secondPlayer;
	}

	public Player nextPrevPlayer() {
		if (currentPlayer() == firstPlayer) {
			return secondPlayer;
		}
		return firstPlayer;
	}
}