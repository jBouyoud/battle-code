package fr.battle.undefined.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
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

	public int getRoundLeft() {
		return MAX_ROUND - round;
	}

	public PlayerInfo getPlayerInfo(@NonNull final long teamId) {
		if (!playersState.containsKey(teamId)) {
			throw new IllegalArgumentException("Unknown team Id");
		}
		return playersState.get(teamId);
	}

	public boolean isCarrying(final long teamId) {
		return logos.parallelStream().filter(p -> p.equals(playersState.get(
				teamId).getPosition())).count() == 1;
	}

	public boolean isCarredBySomeone(@NonNull final Position logo) {
		return playersState.values().parallelStream().filter(
				playerInfo -> playerInfo.getPosition().equals(logo))
				.count() == 1;
	}

	@Getter
	@ToString
	@RequiredArgsConstructor
	public static final class PlayerInfo {
		private final Player player;
		private final Position position;
		private final int score;
		private final PlayerState state;
	}
}
