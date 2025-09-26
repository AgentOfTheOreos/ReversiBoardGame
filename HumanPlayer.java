
// Standard human player class
public class HumanPlayer extends Player {

	public HumanPlayer(boolean isPlayerOne) {
		super(isPlayerOne);
	}

	@Override
	public boolean isHuman() {
		return true;
	}
}