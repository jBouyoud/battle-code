package fr.battle.undefined.ia;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;

@Slf4j
public class SprintRunner implements IA {

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

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		// Case we already got a logo, we go back home
		final Player current = ws.getMe().getPlayer();
		final Position currentPosition = ws.getMe().getPosition();
		final Position target;
		if (ws.isCarrying(current.getId())) {
			target = current.getCaddy();
		} else {
			// Get closest
			// TODO If no more logo in game.... Slape other dude
			final Optional<Distance> potentialTarget = getClosestPosition(currentPosition);
			if (potentialTarget.isPresent()) {
				target = potentialTarget.get().getPosition();
			} else {
				target = current.getCaddy();
			}
		}

		final Optional<Pair<Action, Double>> bestAction = getPossibleActionsToPerform(currentPosition, target).stream()
		// Restrict to allowed actions
				.filter(a -> a.isAllowed(ws, teamId))
				// Retreive next position for all actions
				.map(a -> Pair.of(a, ws.getReward(a)))
				// Get the best one
				.max((a, b) -> a.getValue().compareTo(b.getValue()));

		if (!bestAction.isPresent()) {
			LOGGER.trace("Unable to find any action");
			return null;
		}
		LOGGER.trace("target {}, actions: {}", target, bestAction.get().getKey());
		return bestAction.get().getKey();
	}

	/**
	 * Determine which logo is the closest one from the list
	 *
	 * @param currentPosition
	 *            current position
	 * @return position of the closest logo
	 */
	private Optional<Distance> getClosestPosition(@NonNull final Position currentPosition) {
		// TODO that is not the nearest of an other player
		// TODO remove logo too far away
		return ws.getLogos().parallelStream().filter(logo -> !ws.isCarredBySomeone(logo)).filter(
				logo -> !ws.isLogoInCaddy(logo)).map(
				p -> {
					final double distance = Math.sqrt(Math.pow(Math.abs(currentPosition.getX() - p.getX()), 2)
							+ Math.pow(Math.abs(currentPosition.getY() - p.getY()), 2));
					return new Distance(p, distance);
				}).min((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
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
		LOGGER.trace("angle {}", angle);
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
	 * Internal class used to find closest logo to catch up
	 */
	@Getter
	@RequiredArgsConstructor
	private static class Distance {
		private final Position position;
		private final double distance;
	}
}
