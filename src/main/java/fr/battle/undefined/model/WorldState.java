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

	public Player getCurrentPlayer() {
		// TODO find our player
		return null;
	}

	public PlayerInfo getPlayerInfo(@NonNull final Player player) {
		if (!playersState.containsKey(player)) {
			throw new IllegalArgumentException("Unknown player");
		}
		return playersState.get(player);
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
