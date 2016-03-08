package fr.battle.undefined;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import fr.battle.undefined.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LauncherTest {

	private static final String SERVER = "52.29.48.22";
	private static final long MAX_TEAM_ID = Constants.TEAMID + 6;
	private static final int SOCKET_NUMBER = 2160;

	public static void main(final String[] zero) throws IOException,
			URISyntaxException, InterruptedException {
		LOGGER.info("Demarrage du client de test");

		// Creation la partie
		try (
				final CloseableHttpClient httpclient = HttpClients
						.createDefault()) {
			final URI uri = new URIBuilder().setScheme("http").setHost(SERVER
					+ ":8080/test").setPath("/createBattle").setParameter(
							"teamId", Long.toString(Constants.TEAMID))
					.setParameter("secret", Constants.SECRET).build();

			final HttpGet httpget = new HttpGet(uri);
			final ResponseHandler<String> handler = new BasicResponseHandler();
			final long gameId = Long.parseLong(httpclient.execute(httpget,
					handler));
			LOGGER.info("{}", gameId);

			// Creation des joueurs et leur enregistrement a la partie
			for (long i = Constants.TEAMID; i < MAX_TEAM_ID; i++) {
				final long teamId = i;
				new Thread(new Runnable() {

					@Override
					public void run() {
						new Client(SERVER, teamId, SOCKET_NUMBER, gameId)
								.start();
					}
				}).start();
			}

			// On attend une seconde pour être ser que les threads ont bien
			// demarre
			Thread.sleep(1000);

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
			// http://xxxxxx:8080/?gameId=votre game Id
		}
	}
}
