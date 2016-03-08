package fr.battle.undefined.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@RequiredArgsConstructor
public class WorldState {

	private final Player[] players;

	private int round;
	private Map<Player, PlayerInfo> playersState;
	private List<Position> logos;

	public void update(final int round,
			final Map<Player, PlayerInfo> playersState,
			final List<Position> logos) {
		this.round = round;
		this.playersState = new LinkedHashMap<>(playersState);
		this.logos = new ArrayList<>(logos);
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
		private final Position position;
		private final int score;
		private final PlayerState state;
	}
}
