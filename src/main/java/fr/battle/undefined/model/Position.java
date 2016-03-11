package fr.battle.undefined.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import fr.battle.undefined.util.Constants;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "x", "y" })
public class Position {

	private final int x;
	private final int y;

	/**
	 * Indicates if the other position is an adjacent to the current position
	 *
	 * @param other
	 *            the position to test
	 * @return <code>true</code> if the other position is adjacent,
	 *         <code>false</code> else
	 */
	public boolean isAdjacent(final Position other) {
		Position p = new Position(x - 1, y);
		if (p.isInMap() && p.equals(other)) {
			return true;
		}
		p = new Position(x + 1, y);
		if (p.isInMap() && p.equals(other)) {
			return true;
		}
		p = new Position(x, y - 1);
		if (p.isInMap() && p.equals(other)) {
			return true;
		}
		p = new Position(x, y + 1);
		if (p.isInMap() && p.equals(other)) {
			return true;
		}
		return false;
	}

	/**
	 * Indicates if this position is in map
	 *
	 * @return <code>true</code> if it is, <code>false</code> else
	 */
	public boolean isInMap() {
		return getX() >= Constants.ORIGIN.getX() && getX() <= Constants.END_OF_MAP.getX()
				&& getY() >= Constants.ORIGIN.getY() && getY() <= Constants.END_OF_MAP.getY();
	}
}
