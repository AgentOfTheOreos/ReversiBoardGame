import java.util.Objects;
/**
 * Represents an unflippable disc that cannot be flipped by the opponent's moves.
 */
public class Position {
	private final int row;
	private final int column;

	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public int row() {
		return row;
	}

	public int col() {
		return column;
	}

	/**
	 * Checks if position is within board bounds.
	 * @return true if the position is within board bounds, false otherwise.
	 */
	public boolean isValid() {
		return row >= 0 && row < 8 && column >= 0 && column < 8;
	}

	@Override
	public String toString() {
		return "Row: " + (row + 1) + ", Column: " + (column + 1);
	}

	// Added for position comparison in game logic
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Position position = (Position) o;
		return row == position.row && column == position.column;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, column);
	}
}