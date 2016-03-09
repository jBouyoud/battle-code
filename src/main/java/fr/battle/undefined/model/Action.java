package fr.battle.undefined.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import fr.battle.undefined.model.WorldState.PlayerInfo;

@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
public enum Action {
	NORD("N", false, 0, -1), SUD("S", false, 0, 1), EST("E", false, 1, 0), OUEST(
			"O", false, -1, 0), JUMP_NORD("JN", true, 0, -2), JUMP_SUD("JS",
			true, 0, 2), JUMP_EST("JE", true, 2, 0), JUMP_OUEST("JO", true, -2,
			0);

	private final String code;
	private final boolean isSuperPower;
	private final int dx;
	private final int dy;

	/**
	 * Apply this action to the position
	 *
	 * @param actual
	 *            actual player position
	 * @return the postion after the action
	 */
	public Position getNextPosition(final Position actual) {
		return new Position(actual.getX() + dx, actual.getY() + dy);
	}

	/**
	 * Indicates if an ation is allowed
	 *
	 * @param ws
	 * @param teamId
	 * @return
	 */
	public boolean isAllowed(final WorldState ws, final long teamId) {
		final PlayerInfo me = ws.getPlayersState().get(teamId);
		// Utilisation d'un superpouvoir interdit si compte dépassé
		if (isSuperPower && me.getPlayer().getSuperPowerCount() < 1) {
			return false;
		}

		// Move hors de la map
		final Position newPos = getNextPosition(me.getPosition());
		if (!newPos.isInMap()) {
			return false;
		}
		LOGGER.debug("{} at {} move to {}", new Object[] { teamId,
				me.getPosition(), newPos });
		// Move sur la position d'un autre joueurs
		if (ws.getPlayersState().values().parallelStream().map(
				pi -> pi.getPosition()).anyMatch(p -> p.equals(newPos))) {
			return false;
		}
		return true;
	}

	/**
	 * Return all allowed actions
	 *
	 * @param ws
	 * @param teamId
	 * @return
	 */
	public static List<Action> allowed(final WorldState ws, final long teamId) {
		final List<Action> allowed = new ArrayList<>();
		for (final Action a : values()) {
			if (a.isAllowed(ws, teamId)) {
				allowed.add(a);
			}
		}
		return allowed;
	}
}
