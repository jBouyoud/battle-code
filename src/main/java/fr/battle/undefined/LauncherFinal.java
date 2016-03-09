package fr.battle.undefined;

import fr.battle.undefined.ia.RandomIA;
import fr.battle.undefined.util.Constants;

public class LauncherFinal {

	private static final String IP_SERVER = "xxxxxxxx";
	private static final int SOCKER_NUMBER = 0000000000;
	private static final long GAME_ID = 0000000;

	public static void main(final String[] zero) throws Exception {
		new Client(IP_SERVER, Constants.TEAMID, SOCKER_NUMBER, GAME_ID,
				new RandomIA()).init(7000).start();
	}
}
