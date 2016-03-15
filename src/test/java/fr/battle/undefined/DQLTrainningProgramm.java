package fr.battle.undefined;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import fr.battle.undefined.ia.AvaIA;
import fr.battle.undefined.ia.BlinkyIA;
import fr.battle.undefined.ia.MothIA;
import fr.battle.undefined.ia.SprintRunner;
import fr.battle.undefined.ia.TrainingAvaIA;
import fr.battle.undefined.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DQLTrainningProgramm {

	@SuppressWarnings("unchecked")
	private static final Class<? extends IA>[] OTHER_IAS = new Class[] {
			//
			MothIA.class,
			//
			MothIA.class,
			//
			BlinkyIA.class,
			//
			SprintRunner.class,
			//
			SprintRunner.class };

	private static final Random rand = new Random();

	private static final int MAX_ITERATION = 1000;

	public static void main(final String[] args) throws Exception {
		LOGGER.info("Demarrage du client de test");

		final AvaIA dql = new TrainingAvaIA();

		try (RestClient restClient = new RestClient(LauncherTest.SERVER)) {
			for (int i = 0; i < MAX_ITERATION; ++i) {
				final long gameId = restClient.createGame();
				if (gameId == -1) {
					throw new IllegalStateException("Already in game");
				}
				playGame(dql, restClient, gameId);
				if (i % 10 == 0) {
					dql.save();
				}
			}
		}
		dql.save();
	}

	private static void playGame(final AvaIA dql, final RestClient restClient, final long gameId) throws Exception {
		try {
			// Creation des joueurs et leur enregistrement a la partie
			Client dqlClient = null;
			final int dqlIdx = rand.nextInt((int) (LauncherTest.MAX_TEAM_ID - Constants.TEAMID));
			final List<Class<? extends IA>> ias = new ArrayList<>(Arrays.asList(OTHER_IAS.clone()));
			for (long i = Constants.TEAMID; i < LauncherTest.MAX_TEAM_ID; i++) {
				final long teamId = i;
				IA ia = null;
				if (i == Constants.TEAMID + dqlIdx) {
					ia = dql;
				} else {
					final int idx = rand.nextInt(ias.size());
					ia = ias.get(idx).newInstance();
					ias.remove(idx);
				}
				final Client c = new Client(LauncherTest.SERVER, teamId, LauncherTest.SOCKET_NUMBER, gameId, ia);
				if (i == Constants.TEAMID + dqlIdx) {
					dqlClient = c;
				}

				new Thread(() -> {
					try {
						c.init(0).start();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}).start();
			}
			if (dqlClient == null) {
				return;
			}
			Thread.sleep(2000);
			// Demarrage de la game
			restClient.startGame(gameId);

			while (!dqlClient.isEnded()) {
				Thread.sleep(500);
			}
		} finally {
			restClient.stopGame(gameId);
		}
	}
}
