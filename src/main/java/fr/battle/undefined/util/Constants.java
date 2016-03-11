package fr.battle.undefined.util;

import fr.battle.undefined.model.Position;

public abstract class Constants {

	public static final long TEAMID = 160;
	public static final String SECRET = "P2389D8FKL";

	public static final int PLAYERS_COUNT = 6;

	public static final int MAX_ROUND = 50;

	public static final int MAP_WIDTH = 16;// x
	public static final int MAP_HEIGHT = 13; // y
	public static final int BOARD_SIZE = MAP_WIDTH * MAP_HEIGHT;

	public static final Position ORIGIN = new Position(0, 0);
	public static final Position END_OF_MAP = new Position(MAP_WIDTH, MAP_HEIGHT);

}
