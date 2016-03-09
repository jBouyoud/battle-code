package fr.battle.undefined.ia;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	 * @see fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.
	 * WorldState)
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		// Case we already got a logo, we go back home
		final Player current = ws.getPlayerInfo(teamId).getPlayer();
		final Position currentPosition = ws.getPlayerInfo(teamId).getPosition();
		final Position target;
		if (ws.isCarrying(teamId)) {
			target = current.getCaddy();
		} else {
			// Get closest
			final Optional<Distance> potentialTarget = getClosestPosition(
					currentPosition);
			if (potentialTarget.isPresent()) {
				target = potentialTarget.get().getPosition();
			} else {
				target = current.getCaddy();
			}
		}

		final List<Action> actions = getPossibleActionsToPerform(
				currentPosition, target);

		final List<Position> futurePosition = getFuturePositions(
				currentPosition, actions);
		LOGGER.info("target {}, actions: {}", target, actions);
		return getBestSolutions(futurePosition, actions);
	}

	/**
	 * Determine which logo is the closest one from the list
	 *
	 * @param currentPosition
	 *            current position
	 * @return position of the closest logo
	 */
	private Optional<Distance> getClosestPosition(
			@NonNull final Position currentPosition) {
		return ws.getLogos().parallelStream().filter(logo -> !ws
				.isCarredBySomeone(logo)).filter(logo -> !ws.isLogoInCaddy(
						logo)).map(p -> {
							final double distance = Math.sqrt(Math.pow(Math.abs(
									currentPosition.getX() - p.getX()), 2)
									+ Math.pow(Math.abs(currentPosition.getY()
											- p.getY()), 2));
							return new Distance(p, distance);
						}).min((a, b) -> a.getDistance().compareTo(b
								.getDistance()));
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
	private List<Action> getPossibleActionsToPerform(
			@NonNull final Position current, @NonNull final Position target) {
		double angle = Math.toDegrees(Math.atan2(target.getY() - current.getY(),
				target.getX() - current.getX()));
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
	 * Method returning futures position for a list of action
	 *
	 * @param position
	 *            current position
	 * @param actions
	 *            list of actions
	 * @return list of position
	 */
	private List<Position> getFuturePositions(@NonNull final Position current,
			@NonNull final List<Action> actions) {
		return actions.parallelStream().map((a) -> appendAction(current, a))
				.collect(Collectors.toList());
	}

	/**
	 * Return the best action to perform depending on the list of action and
	 * future position
	 *
	 * @param positions
	 *            future possible positions
	 * @param actions
	 *            action performed to get future positions
	 * @return best actions to run
	 */
	private Action getBestSolutions(@NonNull final List<Position> positions,
			@NonNull final List<Action> actions) {
		// TODO determine wheter or not we can be knock out on the next round
		// ...
		return actions.get(0);
	}

	/**
	 * Append an action to a position
	 *
	 * @param position
	 *            position to modify
	 * @param action
	 *            action to perform
	 */
	private Position appendAction(@NonNull final Position position,
			@NonNull final Action action) {
		if (Action.NORD.equals(action)) {
			return new Position(position.getX() + 1, position.getY());
		}
		if (Action.OUEST.equals(action)) {
			return new Position(position.getX(), position.getY() - 1);
		}
		if (Action.SUD.equals(action)) {
			return new Position(position.getX() - 1, position.getY());
		}
		if (Action.EST.equals(action)) {
			return new Position(position.getX(), position.getY() + 1);
		}
		throw new IllegalArgumentException("Unknown action");
	}

	/**
	 * Internal class used to find closest logo to catch up
	 */
	@Getter
	@RequiredArgsConstructor
	private static class Distance {
		private final Position position;
		private final Double distance;
	}
}
