package fr.battle.undefined.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class WorldState {

	private static final int MAX_ROUND = 50;

	private final int round;
	private final Map<Long, PlayerInfo> playersState;
	private final List<Position> logos;
	private final PlayerInfo me;

	public int getRoundLeft() {
		return MAX_ROUND - round;
	}

	public PlayerInfo getPlayerInfo(final long teamId) {
		if (!playersState.containsKey(teamId)) {
			throw new IllegalArgumentException("Unknown team Id");
		}
		return playersState.get(teamId);
	}

	public boolean isCarrying(final long teamId) {
		return logos.parallelStream().filter(
				p -> p.equals(playersState.get(teamId).getPosition())).count() == 1;
	}

	public boolean isCarredBySomeone(final Position logo) {
		return playersState.values().parallelStream().filter(
				playerInfo -> playerInfo.getPosition().equals(logo)).count() == 1;
	}

	public boolean isLogoInCaddy(final Position logo) {
		return playersState.values().parallelStream().filter(
				playerInfo -> playerInfo.getPlayer().getCaddy().equals(logo))
				.count() == 1;
	}

	public Stream<PlayerInfo> getSlappedPlayers(final Position newPos) {
		return getPlayersState().values().parallelStream()
		// Cannot re-slap sames players
				.filter(pi -> me.getLastSlaped().contains(pi))
				// Cannot slap players in their home
				.filter(pi -> pi.isAtHome())
				// Able to slap only on adjacent position
				.filter(pi -> pi.getPosition().isAdjacent(newPos));
	}

	public double getReward(final Action a) {
		final Position newPos = a.getNextPosition(me.getPosition());

		double reward = .0d;
		// Unauthorized actions
		if (!a.isAllowed(this, me.getPlayer().getId())) {
			reward += -5d;
		}
		// It a slapping action
		reward += getSlappedPlayers(newPos).count() * 2;

		if (isCarrying(me.getPlayer().getId())
				&& newPos.equals(me.getPlayer().getCaddy())) {
			reward += 30;
		}
		return reward;
	}

	@Getter
	@ToString
	@RequiredArgsConstructor
	public static final class PlayerInfo {
		private final Player player;
		private final Position position;
		private final int score;
		private final PlayerState state;
		private final List<Player> lastSlaped = new ArrayList<>();

		/**
		 * Indique si le joueur est sur son caddy
		 *
		 * @return <code>true</code> si il l est, <code>false</code> else
		 */
		public boolean isAtHome() {
			return position.equals(player.getCaddy());
		}

	}
}
