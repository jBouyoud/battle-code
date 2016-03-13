package fr.battle.undefined.ia;

import java.util.List;
import java.util.Map;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MothIA implements IA {

	private WorldState ws;
	private long teamId;

	private static double slapRatio = 10d;
	private static double pickRatio = 1d;
	private static double dropRation = 1d;
	private static double uncertaintyRatio = 0.9d;

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
		final Position currentPosition = ws.getMe().getPosition();

		Action bestAction = null;
		double bestActionReward = Double.MIN_VALUE;

		final List<Action> allowedActions = Action.allowed(ws, teamId);

		LOGGER.debug("Number of possible actions: " + allowedActions.size());

		for (final Action action : allowedActions) {

			LOGGER.debug("Start processionc action " + action.getCode());

			final Position nextPosition = new Position(currentPosition.getX() + action.getDx(),
					currentPosition.getY() + action.getDy());

			final double slapRatioed = slapRatio * computeSlapReward(nextPosition);
			final double pickRatioed = pickRatio * computePickReward(nextPosition);
			final double dropRatioed = dropRation * computeDropReward(nextPosition);
			final double totalReward = slapRatioed + pickRatioed + dropRatioed;

			LOGGER.debug("Action " + action.getCode());
			LOGGER.debug("Slap " + slapRatioed);
			LOGGER.debug("Pick " + pickRatioed);
			LOGGER.debug("Drop " + dropRatioed);
			LOGGER.debug("Total " + totalReward);
			LOGGER.debug("####################################################");

			if (bestActionReward < totalReward) {
				bestAction = action;
				bestActionReward = totalReward;

				LOGGER.debug("Action is elected as best");
			}
		}

		if (bestAction == null) {
			LOGGER.warn("No action found, using NORTH as default");
			bestAction = Action.NORD;
		}

		return bestAction;
	}

	/**
	 *
	 * @param nextPosition
	 * @return
	 */
	private double computeSlapReward(final Position nextPosition) {

		double reward = 0d;
		for (final Map.Entry<Long, PlayerInfo> entry : ws.getPlayersState().entrySet()) {
			final PlayerInfo victim = entry.getValue();

			final List<Player> lastSlapped = ws.getMe().getLastSlaped();
			// System.out.println(lastSlapped);

			// Ne pas s'attaquer soit-même...
			if (ws.getMe().equals(victim)) {
				continue;
			}

			// Pas déja slappée
			if (ws.getMe().getLastSlaped().contains(victim.getPlayer())) {
				LOGGER.debug("Victim already slapped");
				continue;
			}

			final double distance = tilesDistance(nextPosition, victim.getPosition());
			final double localReward = distance <= ws.getRoundLeft() ? 2d / distance * uncertaintyRatio : 0;
			reward += localReward;

			LOGGER.debug("Slap reward detail:");
			LOGGER.debug("Victim:" + victim.getPlayer().getId());
			LOGGER.debug("Distance:" + distance);
			LOGGER.debug("Local Reward:" + localReward);
			LOGGER.debug("Sum Reward:" + reward);
		}

		LOGGER.debug("TOTAL Slap reward: " + reward);
		LOGGER.debug("==============================");
		return reward;
	}

	/**
	 *
	 * @param nextPosition
	 * @return
	 */
	private double computePickReward(final Position nextPosition) {
		double reward = 0d;

		if (!ws.isCarrying(ws.getMe().getPlayer().getId())) {

			for (final Position position : ws.getLogos()) {
				if (!ws.isLogoInCaddy(position)) {

					final double distance = tilesDistance(nextPosition, position);
					final double localReward = distance <= ws.getRoundLeft() ? 1d / distance * uncertaintyRatio : 0;
					reward += localReward;

					LOGGER.debug("Pick reward detail:");
					LOGGER.debug("Logo:" + position);
					LOGGER.debug("Distance:" + distance);
					LOGGER.debug("Local Reward:" + localReward);
					LOGGER.debug("Sum Reward:" + reward);
				}
			}
		}

		LOGGER.debug("TOTAL Pick reward: " + reward);
		LOGGER.debug("==============================");
		return reward;
	}

	/**
	 *
	 * @param nextPosition
	 * @return
	 */
	private double computeDropReward(final Position nextPosition) {
		double reward = 0;

		if (ws.isCarrying(ws.getMe().getPlayer().getId())) {
			LOGGER.debug("I am carying!");

			final double distance = tilesDistance(ws.getMe().getPlayer().getCaddy(), nextPosition);

			final double localReward = 30d / distance;
			reward += localReward;

			LOGGER.debug("Drop reward detail:");
			LOGGER.debug("Distance:" + distance);
			LOGGER.debug("Local Reward:" + localReward);
			LOGGER.debug("Sum Reward:" + reward);
		} else {
			LOGGER.debug("I am NOT carying!");
			for (final Position position : ws.getLogos()) {
				if (!ws.isLogoInCaddy(position)) {

					final double distance = tilesDistance(ws.getMe().getPlayer().getCaddy(), position)
							+ tilesDistance(nextPosition, position);
					final double localReward = distance <= ws.getRoundLeft() ? 30d / distance * uncertaintyRatio : 0;
					reward += localReward;

					LOGGER.debug("Drop reward detail:");
					LOGGER.debug("Logo:" + position);
					LOGGER.debug("Distance:" + distance);
					LOGGER.debug("Local Reward:" + localReward);
					LOGGER.debug("Sum Reward:" + reward);
				}
			}
		}

		LOGGER.debug("TOTAL Drop reward: " + reward);
		LOGGER.debug("==============================");
		return reward;
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
