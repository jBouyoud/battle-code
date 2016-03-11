package fr.battle.undefined.ia;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlinkyIA implements IA {

	private WorldState ws;
	private long teamId;

	@Override
	public void setTeamId(final long teamId) {
		this.teamId = teamId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.
	 * WorldState)
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	Map<Long, Long> lastSlappedMemory = new HashMap<>();

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		final Position currentPosition = ws.getMe().getPosition();

		final List<Action> allowed = Action.allowed(ws, teamId);

		final Map<Long, PlayerInfo> players = ws.getPlayersState();

		PlayerInfo bestVictim = null;
		int bestVictimDistance = Integer.MAX_VALUE;

		for (final Map.Entry<Long, PlayerInfo> entry : players.entrySet()) {
			final PlayerInfo victim = entry.getValue();

			LOGGER.debug("Analyzing victim: " + victim.getPlayer().getId());

			// Ne pas s'attaquer soit-même...
			if (ws.getMe().equals(victim)) {
				continue;
			}

			// Pas déja slappée
			if (lastSlappedMemory.get(ws.getMe().getPlayer().getId()) != null
					&& lastSlappedMemory.get(ws.getMe().getPlayer().getId()).equals(victim.getPlayer().getId())) {
				LOGGER.debug("Victim already slapped");
				continue;
			}

			final int distanceToVictim = tilesDistance(currentPosition, victim.getPosition());

			// Pas assez trop loin et accessible avant la fin de la partie
			if (distanceToVictim >= bestVictimDistance || distanceToVictim > ws.getRoundLeft()) {
				LOGGER.debug("Victim too far");
				continue;
			}

			bestVictimDistance = distanceToVictim;
			bestVictim = victim;
			LOGGER.info("New victim found: " + victim.getPlayer().getId() + ", distance: " + bestVictimDistance);
		}

		if (bestVictim == null) {
			LOGGER.warn("Unable to find a victim");
			return Action.NORD;
		}

		if (bestVictimDistance == 2) {
			lastSlappedMemory.put(ws.getMe().getPlayer().getId(), bestVictim.getPlayer().getId());
		}

		// TODO pick second action if first is blocked
		// TODO handle both action blocked
		final Action bestAction = getPossibleActionsToPerform(currentPosition, bestVictim.getPosition()).get(0);

		LOGGER.info("target {}, actions: {}", bestVictim.getPlayer().getId(), bestAction);
		return bestAction;
	}

	/**
	 * Caclulate the angle between the player and the target, return a list of
	 * actions leading to the target
	 *
	 * @param current
	 *            current position
	 * @param target
	 *            objective
	 * @return list of action
	 */
	private List<Action> getPossibleActionsToPerform(@NonNull final Position current, @NonNull final Position target) {
		double angle = Math.toDegrees(Math.atan2(target.getY() - current.getY(), target.getX() - current.getX()));
		if (angle < 0) {
			angle += 360;
		}
		return convertAngleToDirection(angle);
	}

	/**
	 * Method returning a list of possible actions from a given angle
	 *
	 * @param angle
	 *            between the current player and the target
	 * @return list of possible actions
	 */
	private List<Action> convertAngleToDirection(final double angle) {
		LOGGER.info("angle {}", angle);
		// TODO check if list with one element are better or not
		if (angle == 0) {
			return Arrays.asList(Action.EST);
		} else if (angle == 90) {
			return Arrays.asList(Action.SUD);
		} else if (angle == 180) {
			return Arrays.asList(Action.OUEST);
		} else if (angle == 270) {
			return Arrays.asList(Action.NORD);
		} else if (angle > 0 && angle < 90) {
			return Arrays.asList(Action.EST, Action.SUD);
		} else if (angle > 90 && angle < 180) {
			return Arrays.asList(Action.SUD, Action.OUEST);
		} else if (angle > 180 && angle < 270) {
			return Arrays.asList(Action.OUEST, Action.NORD);
		}
		return Arrays.asList(Action.NORD, Action.EST);
	}

	/**
	 *
	 * @param p1
	 * @param p2
	 * @return
	 */
	private int tilesDistance(final Position p1, final Position p2) {
		return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
	}

}
