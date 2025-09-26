/**
 * Represents an unflippable disc that cannot be flipped by the opponent's moves.
 */
public class UnflippableDisc implements Disc {
	private Player owner;

	public UnflippableDisc(Player owner) {
		this.owner = owner;
	}

	@Override
	public Player getOwner() {
		return owner;
	}

	@Override
	public void setOwner(Player player) {
		this.owner = player;
	}

	@Override
	public String getType() {
		return "⭕";
	}
}