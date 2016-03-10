package fr.battle.undefined;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.learning.BackPropagation;

import fr.battle.undefined.ia.nn.BattleNN;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QLearning implements IA {

	// path finding
	private final double alpha = 1;
	private final double gamma = 0.9;

	// http://en.wikipedia.org/wiki/Q-learning
	// http://people.revoledu.com/kardi/tutorial/ReinforcementLearning/Q-Learning.htm

	// Q(s,a)= Q(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) -
	// Q(s,a))

	// Will be neural network
	private final static NeuralNetwork<BackPropagation> Q = new BattleNN(4
			* 209, 8);
	// World representation
	private WorldState ws;
	// Available actions
	private final List<Action> actions = Arrays.asList(Action.EST,
			Action.JUMP_EST, Action.JUMP_EST, Action.JUMP_NORD,
			Action.JUMP_OUEST, Action.JUMP_SUD, Action.NORD, Action.OUEST,
			Action.SUD);
	private static final String SERVER = "52.29.48.22";
	private static final long MAX_TEAM_ID = Constants.TEAMID + 6;
	private static final int SOCKET_NUMBER = 2160;

	public static void main(final String[] args) throws IOException,
			URISyntaxException, InterruptedException {
		LOGGER.info("Demarrage du client de test");

		// Should start a party
		startGame(0L);
		Q.save("trained.ia");
	}

	/**
	 * Start parti
	 *
	 */
	private static void startGame(final long iaPosition) throws IOException,
			URISyntaxException, InterruptedException {
		final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(
				ProxySelector.getDefault());
		// Creation la partie
		try (
				final CloseableHttpClient httpclient = HttpClients.custom()
						.setRoutePlanner(routePlanner).build()) {

			final URI uri = new URIBuilder().setScheme("http").setHost(SERVER
					+ ":8080/test").setPath("/createBattle").setParameter(
							"teamId", Long.toString(Constants.TEAMID))
					.setParameter("secret", Constants.SECRET).build();

			final HttpGet httpget = new HttpGet(uri);
			final ResponseHandler<String> handler = new BasicResponseHandler();
			final long gameId = Long.parseLong(httpclient.execute(httpget,
					handler));
			LOGGER.info("{}", gameId);

			final Properties prop = new Properties();
			prop.load(LauncherTest.class.getResourceAsStream(
					"/test.properties"));

			final String[] ias = prop.getProperty("ias").split(",");

			// Creation des joueurs et leur enregistrement a la partie
			for (long i = Constants.TEAMID; i < MAX_TEAM_ID; i++) {
				final long teamId = i;
				final String className = ias[(int) (i - Constants.TEAMID)];
				if (i != iaPosition) {

					new Thread(() -> {
						try {

							new Client(SERVER, teamId, SOCKET_NUMBER, gameId,
									(IA) Class.forName(className).newInstance())
											.init(1).start();

						} catch (InstantiationException | IllegalAccessException
								| ClassNotFoundException
								| InterruptedException e) {
							e.printStackTrace();
						}
					}).start();
				} else {
					new Thread(() -> {
						try {
							new Client(SERVER, teamId, SOCKET_NUMBER, gameId,
									QLearning.class.newInstance()).init(1)
											.start();
						} catch (InstantiationException | IllegalAccessException
								| InterruptedException e) {
							e.printStackTrace();
						}
					}).start();
				}
			}

			// On attend une seconde pour être ser que les threads ont bien
			// demarre
			Thread.sleep(1000);
			if (gameId == -1) {
				return;
			}
			// Demarrage de la game
			// http://xxxxxx:8080/test/startBattle?gameId=xxxx&teamId=10&secret=bobsecret
			final URI startUri = new URIBuilder().setScheme("http").setHost(
					SERVER + ":8080").setPath("/test/startBattle").setParameter(
							"gameId", Long.toString(gameId)).setParameter(
									"teamId", Long.toString(Constants.TEAMID))
					.setParameter("secret", Constants.SECRET).build();

			final HttpGet startGet = new HttpGet(startUri);
			final ResponseHandler<String> handler2 = new BasicResponseHandler();
			httpclient.execute(startGet, handler2);

			// Pour voir le jeu
			LOGGER.info(
					"To Stop the battle : http://{}:8080/test/stopBattle?gameId={}&teamId={}&secret={}",
					new Object[] { SERVER, gameId, Constants.TEAMID,
							Constants.SECRET });

			System.in.read();

			final URI stopUri = new URIBuilder().setScheme("http").setHost(
					SERVER + ":8080").setPath("/test/stopBattle").setParameter(
							"gameId", Long.toString(gameId)).setParameter(
									"teamId", Long.toString(Constants.TEAMID))
					.setParameter("secret", Constants.SECRET).build();

			final HttpGet stopGet = new HttpGet(stopUri);
			final ResponseHandler<String> handler3 = new BasicResponseHandler();
			httpclient.execute(stopGet, handler3);
			// http://xxxxxx:8080/?gameId=votre game Id
		}

	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		// Train neural network
		run();
		// Return best action
		return null;
	}

	private double run() {
		/*
		 * 1. Set parameter , and environment reward matrix R 2. Initialize
		 * matrix Q as zero matrix 3. For each episode: Select random initial
		 * state Do while not reach goal state o Select one among all possible
		 * actions for the current state o Using this possible action, consider
		 * to go to the next state o Get maximum Q value of this next state
		 * based on all possible actions o Compute o Set the next state as the
		 * current state
		 */
		final Map<Action, Double> results = new HashMap<>();
		Q.setInput(ws.getWorld());
		final double[] result = Q.getOutput();

		for (int i = 0; i < actions.size(); i++) {
			results.put(actions.get(i), result[i]);
		}

		final Action maxQ = results.entrySet().stream().max((a, b) -> a
				.getValue().compareTo(b.getValue())).get().getKey();

		// Choose actions using epsilon greedy
		final Action nextAct = Utils.getAction(maxQ, actions);

		// Update world with the future of our actions
		Q.setInput(ws.getFutureWorld(nextAct));
		Q.calculate();
		final Map<Action, Double> futureResults = new HashMap<>();
		final double[] futureResult = Q.getOutput();
		// pour chaque S' appelé Q avec
		for (int i = 0; i < actions.size(); i++) {
			results.put(actions.get(i), result[i]);
		}
		final Action futureMaxQ = results.entrySet().stream().max((a, b) -> a
				.getValue().compareTo(b.getValue())).get().getKey();

		// TODO caclculate reward with future world
		final int r = ws.getReward(maxQ);
		// voir pour le alpha à 1
		final double value = r + gamma * maxQ;
		setQ(ws.getWorld(), nextAct, value);
	}

	private void setQ(final int s, final int a, final double value) {
		// TODO update neural network with value
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#setTeamId(long)
	 */
	@Override
	public void setTeamId(final long teamId) {
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.
	 * WorldState)
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}
}