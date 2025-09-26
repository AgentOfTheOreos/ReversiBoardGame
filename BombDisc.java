import java.util.Objects;

/**
 * Represents a bomb disc in the game that explodes upon activation. Causes chain reaction if other bombs in
 * explosion proximity.
 */
public class BombDisc implements Disc {
	private Player owner;
	public BombDisc(Player owner) {
		this.owner = owner;
	}

	@Override
	public String getType() {
		return "💣"; // Represents a Bomb Disc
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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BombDisc otherDisc = (BombDisc) obj;
		return Objects.equals(owner, otherDisc.owner);
	}

	@Override
	public int hashCode() {
		return owner != null ? owner.hashCode() : 0;
	}
}
