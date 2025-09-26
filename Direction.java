/**
 * Class which represents changes in movement direction, takes care of cases for all 8 directions.
 */
public enum Direction {
	NORTH(-1, 0),
	NORTHEAST(-1, 1),
	EAST(0, 1),
	SOUTHEAST(1, 1),
	SOUTH(1, 0),
	SOUTHWEST(1, -1),
	WEST(0, -1),
	NORTHWEST(-1, -1);

	private final int rowChange;
	private final int columnChange;

	Direction(int rowChange, int columnChange) {
		this.rowChange = rowChange;
		this.columnChange = columnChange;
	}

	public int getRowChange() {
		return rowChange;
	}

	public int getColumnChange() {
		return columnChange;
	}
}