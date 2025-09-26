import java.util.Objects;

/**
 * Represents a simple disc in the game that follows basic Reversi rules.
 */
public class SimpleDisc implements Disc {
	private Player owner;

	public SimpleDisc(Player owner) {
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
		return "⬤";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SimpleDisc otherDisc = (SimpleDisc) obj;
		return Objects.equals(owner, otherDisc.owner);
	}

	@Override
	public int hashCode() {
		return owner != null ? owner.hashCode() : 0;
	}
}