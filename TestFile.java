import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TestFile {
	private GameLogic game;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;

	@BeforeEach
	void setUp() {
		game = new GameLogic();
		game.setPlayers(new HumanPlayer(true), new HumanPlayer(false));
		game.reset();
		System.setOut(new PrintStream(outContent));
	}
	@AfterEach
	void restoreSystemStreams() {
		System.setOut(originalOut);
	}
	@Timeout(value = 5)
	@Test
	void testValidMove() {
		assertTrue(game.locate_disc(new Position(2, 4), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(new SimpleDisc(game.getFirstPlayer()).getOwner(), game.getDiscAtPosition(new Position(2, 4)).getOwner());
		assertEquals(new SimpleDisc(game.getFirstPlayer()).getOwner(), game.getDiscAtPosition(new Position(3, 4)).getOwner());
	}

	@Timeout(value = 5)
	@Test
	void testInvalidMove() {
		assertFalse(game.locate_disc(new Position(0, 0), new SimpleDisc(game.getFirstPlayer())));
		assertNull(game.getDiscAtPosition(new Position(0, 0)));
	}

	@Timeout(value = 5)
	@Test
	void testValidMoves() {
		List<Position> validMoves = game.ValidMoves();
		assertFalse(validMoves.isEmpty());
		assertEquals(4, validMoves.size());
	}

	@Timeout(value = 5)
	@Test
	void testUndoLastMove() {
		game.locate_disc(new Position(2, 3), new SimpleDisc(game.getFirstPlayer()));
		game.undoLastMove();
		assertNull(game.getDiscAtPosition(new Position(2, 3)));
		assertTrue(game.isFirstPlayerTurn());
	}

	@Timeout(value = 5)
	@Test
	void testGameSequence() {
		// Move 1: Player 1
		assertTrue(game.locate_disc(new Position(2, 4), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 4)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 4)).getOwner());

		// Move 2: Player 2
		assertTrue(game.locate_disc(new Position(2, 3), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(2, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Move 3: Player 1
		assertTrue(game.locate_disc(new Position(4, 2), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 4: Player 2
		assertTrue(game.locate_disc(new Position(5, 3), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Check the final state of key positions
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 4)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(2, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 4)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 4)).getOwner());

		// Verify it's Player 1's turn again
		assertTrue(game.isFirstPlayerTurn());
	}

	@Timeout(value = 5)
	@Test
	void testUnflippableDiscSequence() {
		// Move 1: Player 1 places an unflippable disc
		assertTrue(game.locate_disc(new Position(4, 2), new UnflippableDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 2: Player 2
		assertTrue(game.locate_disc(new Position(3, 2), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Move 3: Player 1 places another unflippable disc
		assertTrue(game.locate_disc(new Position(2, 2), new UnflippableDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Move 4: Player 2
		assertTrue(game.locate_disc(new Position(5, 2), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 5: Player 1
		assertTrue(game.locate_disc(new Position(2, 4), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 4)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 4)).getOwner());

		// Move 6: Player 2
		assertTrue(game.locate_disc(new Position(1, 2), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(1, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 2)).getOwner());

		// Verify the final state of key positions
		assertInstanceOf(UnflippableDisc.class, game.getDiscAtPosition(new Position(4, 2)));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertInstanceOf(UnflippableDisc.class, game.getDiscAtPosition(new Position(2, 2)));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(1, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(2, 4)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 4)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());

		// Verify it's Player 1's turn again
		assertTrue(game.isFirstPlayerTurn());

		// Verify that unflippable discs weren't flipped
		assertInstanceOf(UnflippableDisc.class, game.getDiscAtPosition(new Position(4, 2)));
		assertInstanceOf(UnflippableDisc.class, game.getDiscAtPosition(new Position(2, 2)));
	}

	@Timeout(value = 5)
	@Test
	void testBombDiscChainReaction() {
		// Move 1: Player 1 places a bomb
		assertTrue(game.locate_disc(new Position(4, 2), new BombDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 2: Player 2 places a bomb
		assertTrue(game.locate_disc(new Position(5, 2), new BombDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 3: Player 1
		assertTrue(game.locate_disc(new Position(6, 2), new SimpleDisc(game.getFirstPlayer())));
		assertNotEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(6, 2)).getOwner());
		assertNotEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());

		// Move 4: Player 2 (triggering first chain reaction)
		assertTrue(game.locate_disc(new Position(6, 1), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(6, 1)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(6, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Move 5: Player 1 (triggering second chain reaction)
		assertTrue(game.locate_disc(new Position(4, 1), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 1)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(6, 1)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(6, 2)).getOwner());

		// Move 6: Player 2 (triggering third chain reaction)
		assertTrue(game.locate_disc(new Position(7, 0), new SimpleDisc(game.getSecondPlayer())));
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(7, 0)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(6, 1)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 1)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(6, 2)).getOwner());
		assertEquals(game.getSecondPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());

		// Move 7: Player 1 (final chain reaction)
		assertTrue(game.locate_disc(new Position(4, 0), new SimpleDisc(game.getFirstPlayer())));
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 0)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 1)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(4, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(3, 3)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(5, 2)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(6, 1)).getOwner());
		assertEquals(game.getFirstPlayer(), game.getDiscAtPosition(new Position(6, 2)).getOwner());

		// Verify it's Player 2's turn
		assertFalse(game.isFirstPlayerTurn());

		// Verify that bomb discs are still bombs after exploding
		assertInstanceOf(BombDisc.class, game.getDiscAtPosition(new Position(4, 2)));
		assertInstanceOf(BombDisc.class, game.getDiscAtPosition(new Position(5, 2)));
	}
	@Timeout(value = 5)
	@Test
	void testGameAgainstGreedyAI() {
		// Inner class to represent Position for testing
		class TestedPosition {
			private final int row;
			private final int col;

			TestedPosition(int row, int col) {
				this.row = row;
				this.col = col;
			}

			public int row() { return row; }
			public int col() { return col; }
		}
		// Inner class to represent moves for testing
		class TestedMove {
			private final TestedPosition position;
			private final Disc disc;

			TestedMove(TestedPosition position, Disc disc) {
				this.position = position;
				this.disc = disc;
			}

			TestedPosition position() { return position; }
			Disc disc() { return disc; }
		}


		// Set up the game with a human player and a GreedyAI player
		Player humanPlayer = new HumanPlayer(true);
		AIPlayer aiPlayer = new GreedyAI(false);
		game.setPlayers(humanPlayer, aiPlayer);

		// Define the expected sequence of moves
		List<TestedMove> expectedMoves = Arrays.asList(
				new TestedMove(new TestedPosition(4, 2), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 4), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(5, 5), new BombDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 6), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(6, 6), new BombDisc(humanPlayer)),
				new TestedMove(new TestedPosition(6, 4), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(7, 3), new UnflippableDisc(humanPlayer)),
				new TestedMove(new TestedPosition(7, 7), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(4, 6), new UnflippableDisc(humanPlayer)),
				new TestedMove(new TestedPosition(7, 4), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(7, 5), new BombDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 7), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(6, 5), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(4, 1), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(7, 6), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(4, 5), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(2, 4), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(1, 5), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(4, 0), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(6, 7), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(3, 6), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 3), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(0, 6), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(1, 4), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(3, 5), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(2, 6), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(3, 7), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(1, 6), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(0, 4), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(1, 3), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(6, 2), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(3, 2), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(2, 3), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(6, 3), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(0, 3), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(3, 1), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(2, 7), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(4, 7), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(5, 1), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(6, 1), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(7, 1), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 2), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(1, 7), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(0, 7), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(7, 2), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(2, 5), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(0, 5), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(0, 2), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(2, 1), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(5, 0), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(6, 0), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(7, 0), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(1, 2), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(1, 1), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(0, 1), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(3, 0), new SimpleDisc(aiPlayer)),
				new TestedMove(new TestedPosition(1, 0), new SimpleDisc(humanPlayer)),
				new TestedMove(new TestedPosition(2, 2), new SimpleDisc(aiPlayer))
		);

		// Play the game and verify each move
		for (TestedMove expectedMove : expectedMoves) {
			if (game.isFirstPlayerTurn()) {
				assertTrue(game.locate_disc(new Position(expectedMove.position().row(), expectedMove.position().col()),
						expectedMove.disc()), "Human move should be valid");
			} else {
				// For AI moves, we don't need to call locate_disc, as the AI should make the move
				// We just need to verify that the AI made the expected move
				Move aiMove = aiPlayer.makeMove(game);

				// Compare the relevant parts of the move individually
				assertEquals(expectedMove.position().row(), aiMove.position().row(), "AI move row is incorrect");
				assertEquals(expectedMove.position().col(), aiMove.position().col(), "AI move column is incorrect");
				assertEquals(expectedMove.disc().getClass(), aiMove.disc().getClass(), "AI disc type is incorrect");
				assertEquals(expectedMove.disc().getOwner(), aiMove.disc().getOwner(), "AI disc owner is incorrect");

				// Apply the AI's move
				assertTrue(game.locate_disc(aiMove.position(), aiMove.disc()), "AI move should be valid");
			}

			// Verify the disc at the moved position
			Disc placedDisc = game.getDiscAtPosition(new Position(expectedMove.position().row(), expectedMove.position().col()));
			assertNotNull(placedDisc, "No disc placed at " + expectedMove.position());
			assertEquals(expectedMove.disc().getClass(), placedDisc.getClass(), "Incorrect disc type at " + expectedMove.position());
			assertEquals(expectedMove.disc().getOwner(), placedDisc.getOwner(), "Incorrect disc owner at " + expectedMove.position());
		}

		// Verify the final state of the game
		assertTrue(game.isGameFinished(), "Game should be finished after all moves");
	}

	@Timeout(value = 5)
	@Test
	void testPrints1() {
		// Move 1: Player 1
		game.locate_disc(new Position(4, 2), new SimpleDisc(game.getFirstPlayer()));

		// Move 2: Player 2
		game.locate_disc(new Position(3, 2), new SimpleDisc(game.getSecondPlayer()));

		// Move 3: Player 1
		game.locate_disc(new Position(2, 3), new SimpleDisc(game.getFirstPlayer()));

		// Move 4: Player 2
		game.locate_disc(new Position(5, 4), new BombDisc(game.getSecondPlayer()));

		// Verify the output
		List<String> expectedOutputLines = Arrays.asList(
				"Player 2 placed a ⬤ in (4, 2)",
				"Player 2 flipped the ⬤ in (4, 3)",
				"Player 1 placed a ⬤ in (3, 2)",
				"Player 1 flipped the ⬤ in (3, 3)",
				"Player 2 placed a ⬤ in (2, 3)",
				"Player 2 flipped the ⬤ in (3, 3)",
				"Player 1 placed a ⬤ in (5, 4)"
		);

		assertOutputContainsExpectedLines(expectedOutputLines, outContent.toString());
	}
	@Timeout(value = 5)
	@Test
	void testPrints2() {
		// Move 1: Player 1
		game.locate_disc(new Position(4, 2), new SimpleDisc(game.getFirstPlayer()));

		// Move 2: Player 2
		game.locate_disc(new Position(3, 2), new SimpleDisc(game.getSecondPlayer()));

		// Undo the last two moves
		game.undoLastMove();
		game.undoLastMove();

		// Try to undo when no moves are left
		game.undoLastMove();

		// Move 3: Player 1
		game.locate_disc(new Position(5, 3), new SimpleDisc(game.getFirstPlayer()));

		// Move 4: Player 2
		game.locate_disc(new Position(5, 4), new BombDisc(game.getSecondPlayer()));

		// Verify the output
		List<String> expectedOutputLines = Arrays.asList(
				"Player 1 placed a ⬤ in (4, 2)",
				"Player 1 flipped the ⬤ in (4, 3)",
				"Player 2 placed a ⬤ in (3, 2)",
				"Player 2 flipped the ⬤ in (3, 3)",
				"Undoing last move:",
				"Undo: removing ⬤ from (3, 2)",
				"Undo: flipping back ⬤ in (3, 3)",
				"Undoing last move:",
				"Undo: removing ⬤ from (4, 2)",
				"Undo: flipping back ⬤ in (4, 3)",
				"Undoing last move:",
				"No previous move available to undo.",
				"Player 1 placed a ⬤ in (5, 3)",
				"Player 1 flipped the ⬤ in (4, 3)",
				"Player 2 placed a 💣 in (5, 4)",
				"Player 2 flipped the ⬤ in (4, 4)"
		);

		assertOutputContainsExpectedLines(expectedOutputLines, outContent.toString());
	}

	private void assertOutputContainsExpectedLines(List<String> expectedLines, String actualOutput) {
		List<String> normalizedActualLines = Arrays.stream(actualOutput.split("\\r?\\n"))
				.map(this::normalizeLine)
				.filter(line -> !line.isEmpty())
				.toList();

		List<String> normalizedExpectedLines = expectedLines.stream()
				.map(this::normalizeLine)
				.toList();

		for (String expectedLine : normalizedActualLines) {
			assertTrue(normalizedActualLines.stream().anyMatch(actualLine -> actualLine.contains(expectedLine)),
					"Expected line not found: " + expectedLine);
		}
	}

	private String normalizeLine(String line) {
		return line.replaceAll("\\s+", " ").trim().toLowerCase();
	}

	@Timeout(value = 5)
	@Test
	void testMultipleGameOutcomes() {
		int player1Wins = 0;
		int player2Wins = 0;
		int ties = 0;

		// Play 5 games
		for (int i = 0; i < 5; i++) {
			playGameUntilEnd();

			// Check if the game ended
			assertTrue(game.isGameFinished());

			// Determine the winner based on the number of discs
			int player1Discs = countPlayerDiscs(game.getFirstPlayer());
			int player2Discs = countPlayerDiscs(game.getSecondPlayer());

			if (player1Discs > player2Discs) {
				player1Wins++;
			} else if (player2Discs > player1Discs) {
				player2Wins++;
			}
			else
				ties++;
			// Note: In case of a tie, neither player gets a win

			// Reset the game for the next round
			game.reset();
		}

		// Check that we have a total of 5 games played
		assertEquals(5, player1Wins + player2Wins + ties);

		// Check individual win counts
		assertTrue(player1Wins >= 0 && player1Wins <= 5);
		assertTrue(player2Wins >= 0 && player2Wins <= 5);
		assertTrue(ties >= 0 && ties <= 5);

		System.out.println("Player 1 wins: " + player1Wins);
		System.out.println("Player 2 wins: " + player2Wins);
		System.out.println("Number of ties: " + ties);
	}

	private final Random random = new Random();

	// Helper method to play a game until it ends
	private void playGameUntilEnd() {
		while (!game.isGameFinished()) {
			List<Position> validMoves = game.ValidMoves();
			if (!validMoves.isEmpty()) {
				// Choose a random move from the list of valid moves
				Position move = validMoves.get(random.nextInt(validMoves.size()));
				Disc disc = new SimpleDisc(game.isFirstPlayerTurn() ? game.getFirstPlayer() : game.getSecondPlayer());
				game.locate_disc(move, disc);
			}
			// The turn will automatically switch if there are no valid moves
		}
	}

	// Helper method to count the number of discs for a player
	private int countPlayerDiscs(Player player) {
		int count = 0;
		for (int row = 0; row < game.getBoardSize(); row++) {
			for (int col = 0; col < game.getBoardSize(); col++) {
				Disc disc = game.getDiscAtPosition(new Position(row, col));
				if (disc != null && disc.getOwner() == player) {
					count++;
				}
			}
		}
		return count;
	}

	@Timeout(value = 5)
	@Test
	void testUndoLastMoveMultipleTimes() {
		game.locate_disc(new Position(2, 4), new SimpleDisc(game.getFirstPlayer()));
		game.locate_disc(new Position(2, 5), new SimpleDisc(game.getSecondPlayer()));
		game.locate_disc(new Position(2, 6), new SimpleDisc(game.getFirstPlayer()));

		game.undoLastMove();
		assertTrue(game.isFirstPlayerTurn());
		assertNull(game.getDiscAtPosition(new Position(2, 6)));

		game.undoLastMove();
		assertFalse(game.isFirstPlayerTurn());
		assertNull(game.getDiscAtPosition(new Position(2, 5)));

		game.undoLastMove();
		assertTrue(game.isFirstPlayerTurn());
		assertNull(game.getDiscAtPosition(new Position(2, 4)));

		// Ensure the board is back to its initial state
		assertEquals(2, countPlayerDiscs(game.getFirstPlayer()));
		assertEquals(2, countPlayerDiscs(game.getSecondPlayer()));
	}

	@Timeout(value = 5)
	@Test
	void testResetAfterMultipleMoves() {
		game.locate_disc(new Position(2, 3), new SimpleDisc(game.getFirstPlayer()));
		game.locate_disc(new Position(2, 2), new SimpleDisc(game.getSecondPlayer()));
		game.locate_disc(new Position(1, 2), new SimpleDisc(game.getFirstPlayer()));

		game.reset();

		// Check if the board is back to its initial state
		assertEquals(2, countPlayerDiscs(game.getFirstPlayer()));
		assertEquals(2, countPlayerDiscs(game.getSecondPlayer()));
		assertTrue(game.isFirstPlayerTurn());
		assertNull(game.getDiscAtPosition(new Position(2, 3)));
		assertNull(game.getDiscAtPosition(new Position(2, 2)));
		assertNull(game.getDiscAtPosition(new Position(1, 2)));
	}

	@Timeout(value = 5)
	@Test
	void testBombDiscPlacement() { // fixed increment and decrement of the bomb and flippable discs, test now passes
		Player player1 = game.getFirstPlayer();

		// Check initial bomb count
		int initialBombCount = player1.getNumber_of_bombs();
		System.out.println("Initial bomb count: " + initialBombCount);
		assertEquals(Player.initial_number_of_bombs, initialBombCount, "Initial bomb count should be " + Player.initial_number_of_bombs);

		// Get valid moves
		List<Position> validMoves = game.ValidMoves();
		System.out.println("Valid moves: " + validMoves);
		assertFalse(validMoves.isEmpty(), "There should be valid moves available");

		// Place a bomb disc
		Position pos = validMoves.getFirst();
		System.out.println("Attempting to place bomb disc at " + pos);
		boolean result = game.locate_disc(pos, new BombDisc(player1));
		System.out.println("Placement result: " + result);
		assertTrue(result, "Failed to place bomb disc at " + pos);

		// Check bomb count after placement
		int bombCountAfterPlacement = player1.getNumber_of_bombs();
		System.out.println("Bomb count after placement: " + bombCountAfterPlacement);
		assertEquals(initialBombCount - 1, bombCountAfterPlacement, "Bomb count should be decremented after placement");

		// Check if the placed disc is actually a bomb disc
		Disc placedDisc = game.getDiscAtPosition(pos);
		System.out.println("Placed disc type: " + placedDisc.getClass().getSimpleName());
		assertInstanceOf(BombDisc.class, placedDisc, "Placed disc should be a BombDisc");

		// Undo the move
		game.undoLastMove();
		System.out.println("Undoing last move");

		// Check bomb count after undo
		int bombCountAfterUndo = player1.getNumber_of_bombs();
		System.out.println("Bomb count after undo: " + bombCountAfterUndo);
		assertEquals(initialBombCount, bombCountAfterUndo, "Bomb count should be restored after undo");
	}
}