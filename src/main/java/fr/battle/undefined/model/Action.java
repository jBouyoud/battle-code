package fr.battle.undefined.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import fr.battle.undefined.util.Constants;

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

	public boolean isAllowed(final WorldState ws, final long teamId) {
		final PlayerInfo me = ws.getPlayersState().get(teamId);
		// Utilisation d'un superpouvoir interdit si compte dépassé
		if (isSuperPower && me.getPlayer().getSuperPowerCount() < 1) {
			return false;
		}
		// ???
		if (me.getState().equals(PlayerState.STUNNED)) {
			// FIXME Not really sure about that
			return true;
		}
		// Move hors de la map
		final Position myPos = me.getPosition();
		final Position newPos = new Position(myPos.getX() + dx, myPos.getY()
				+ dy);

		if (newPos.getX() < Constants.ORIGIN.getX()
				|| newPos.getX() > Constants.END_OF_MAP.getX()
				|| newPos.getY() < Constants.ORIGIN.getY()
				|| newPos.getY() > Constants.END_OF_MAP.getY()) {
			return false;
		}
		LOGGER.debug("{} at {} move to {}", new Object[] { teamId, myPos,
				newPos });
		// Move sur la position d'un autre joueurs
		if (ws.getPlayersState().values().stream().map(pi -> pi.getPosition())
				.anyMatch(p -> p.equals(newPos))) {
			return false;
		}
		return true;
	}

	public boolean isUnefficient(final WorldState ws, final long teamId) {
		if (!isAllowed(ws, teamId)) {
			// TODO Si Baffe d'un partenaire déja baffé
			// TODO Ne pas baffer un dude sur son caddy
			return true;
		}
		return false;
	}

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
