package fr.battle.undefined;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.Properties;

import fr.battle.undefined.util.Constants;

/**
 * http://52.29.48.22:8080/
 *
 *
 */
@Slf4j
public class LauncherTest {

	private static final String SERVER = "52.29.48.22";
	private static final int SOCKET_NUMBER = 2160;
	private static final long MAX_TEAM_ID = Constants.TEAMID + 6;

	public static void main(final String[] zero) throws IOException,
			URISyntaxException, InterruptedException {
		LOGGER.info("Demarrage du client de test");

		final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
				ProxySelector.getDefault());
		// Creation la partie
		try (	RestClient restClient = new RestClient(SERVER)) {
			final long gameId = restClient.createGame();
			if (gameId == -1) {
				return;
			}

			final Properties prop = new Properties();
			prop.load(LauncherTest.class
					.getResourceAsStream("/test.properties"));

			final String[] ias = prop.getProperty("ias").split(",");

			// Creation des joueurs et leur enregistrement a la partie
			for (long i = Constants.TEAMID; i < MAX_TEAM_ID; i++) {
				final long teamId = i;
				final String className = ias[(int) (i - Constants.TEAMID)];
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							new Client(SERVER, teamId, SOCKET_NUMBER, gameId,
									(IA) Class.forName(className).newInstance())
									.init(1).start();
						} catch (InstantiationException
								| IllegalAccessException
								| ClassNotFoundException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			// On attend une seconde pour être ser que les threads ont bien
			// demarre
			Thread.sleep(1000);

			if (gameId == -1) {
				return;
			}
			// Demarrage de la game
			restClient.startGame(gameId);

			// Pour voir le jeu

			System.in.read();

			restClient.stopGame(gameId);
		}
	}

}
