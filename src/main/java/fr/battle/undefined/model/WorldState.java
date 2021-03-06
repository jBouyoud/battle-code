package fr.battle.undefined.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.battle.undefined.util.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class WorldState {

	private static final int TILE_ME = 0x1000;
	private static final int TILE_OPPONENT = 0x0100;
	private static final int TILE_LOGO = 0x0010;
	private static final int TILE_CADDY = 0x0001;

	private final int round;
	private final Map<Long, PlayerInfo> playersState;
	private final List<Position> logos;
	private final PlayerInfo me;

	public int getRoundLeft() {
		return Constants.MAX_ROUND - round;
	}

	public PlayerInfo getPlayerInfo(final long teamId) {
		if (!playersState.containsKey(teamId)) {
			throw new IllegalArgumentException("Unknown team Id");
		}
		return playersState.get(teamId);
	}

	public boolean isCarrying(final long teamId) {
		return logos.parallelStream().filter(p -> p.equals(playersState.get(teamId).getPosition())).count() == 1;
	}

	public boolean isCarredBySomeone(final Position logo) {
		return playersState.values().parallelStream().filter(playerInfo -> playerInfo.getPosition().equals(logo))
				.count() == 1;
	}

	public boolean isLogoInCaddy(final Position logo) {
		return playersState.values().parallelStream()
				.filter(playerInfo -> playerInfo.getPlayer().getCaddy().equals(logo)).count() == 1;
	}

	public Stream<PlayerInfo> getSlappedPlayers(final Position newPos) {
		return getPlayersState().values().parallelStream()
				// Cannot re-slap sames players
				.filter(pi -> !me.getLastSlaped().contains(pi))
				// Cannot slap players in their home
				.filter(pi -> !pi.isAtHome())
				// Cannot self-slap
				.filter(pi -> pi.getPlayer().getId() != me.getPlayer().getId())
				// Able to slap only on adjacent position
				.filter(pi -> pi.getPosition().isAdjacent(newPos));
	}

	public double getReward(final Action a) {
		if (a == null) {
			return -5d;
		}
		final Position newPos = a.getNextPosition(me.getPosition(), me.getState());
		if (PlayerState.STUNNED.equals(me.getState())) {
			return .0d;
		}
		double reward = -.01d;
		// Unauthorized actions
		if (!a.isAllowed(this, me.getPlayer().getId())) {
			reward += -5d;
		}

		// It a slapping action
		reward += getSlappedPlayers(newPos).count() * 2;
		reward += logos.stream().filter(p -> p.equals(newPos)).count() * 1;
		if (isCarrying(me.getPlayer().getId()) && newPos.equals(me.getPlayer().getCaddy())) {
			reward += 30;
		}
		return reward;
	}

	/**
	 * Retourne l'état du monde comme une liste d'Integer
	 *
	 * @return int[] etat du monde
	 */
	public int[] getAsArray() {
		final int[] world = new int[Constants.BOARD_SIZE + 1];
		// Logo disponibles
		for (final Position position : logos) {
			world[position.getX() + position.getY() * Constants.MAP_WIDTH] |= TILE_LOGO;
		}

		for (final Map.Entry<Long, PlayerInfo> entry : playersState.entrySet()) {
			final PlayerInfo playerInfo = entry.getValue();
			// Caddy
			world[playerInfo.getPlayer().getCaddy().getX()
					+ playerInfo.getPlayer().getCaddy().getY() * Constants.MAP_WIDTH] |= TILE_CADDY;

			// Joueur
			world[playerInfo.getPosition().getX() + playerInfo.getPosition().getY() * Constants.MAP_WIDTH] |= me
					.equals(playerInfo) ? TILE_ME : TILE_OPPONENT;
		}
		// Nombre de tour restants
		world[Constants.BOARD_SIZE] = getRoundLeft();
		return world;
	}

	@Getter
	@Setter
	@ToString
	@RequiredArgsConstructor
	public static final class PlayerInfo {
		private final Player player;
		private Position position;
		private int score;
		private PlayerState state;
		private List<Player> lastSlaped = new ArrayList<>();

		public PlayerInfo(final Player player, final Position position, final int score, final PlayerState state) {
			super();
			this.player = player;
			this.position = position;
			this.score = score;
			this.state = state;
		}

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
