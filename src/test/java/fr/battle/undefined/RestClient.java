package fr.battle.undefined;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import fr.battle.undefined.util.Constants;

@Slf4j
@RequiredArgsConstructor
public class RestClient implements Closeable {

	private final String server;
	private final CloseableHttpClient httpclient = HttpClients.createDefault();

	public long createGame() throws IOException, URISyntaxException {
		final URI uri = new URIBuilder().setScheme("http").setHost(server + ":8080/test").setPath("/createBattle")
				.setParameter("teamId", Long.toString(Constants.TEAMID)).setParameter("secret", Constants.SECRET)
				.build();

		final HttpGet httpget = new HttpGet(uri);
		final ResponseHandler<String> handler = new BasicResponseHandler();
		final long gameId = Long.parseLong(httpclient.execute(httpget, handler));
		LOGGER.info("{}", gameId);
		return gameId;
	}

	public void startGame(final long gameId) throws IOException, URISyntaxException {
		LOGGER.info("Starting Game...");
		// http://xxxxxx:8080/test/startBattle?gameId=xxxx&teamId=10&secret=bobsecret
		final URI startUri = new URIBuilder().setScheme("http").setHost(server + ":8080").setPath("/test/startBattle")
				.setParameter("gameId", Long.toString(gameId)).setParameter("teamId", Long.toString(Constants.TEAMID))
				.setParameter("secret", Constants.SECRET).build();

		final HttpGet startGet = new HttpGet(startUri);
		final ResponseHandler<String> handler2 = new BasicResponseHandler();
		httpclient.execute(startGet, handler2);

		LOGGER.info("To Stop the battle : http://{}:8080/test/stopBattle?gameId={}&teamId={}&secret={}", new Object[] {
				server, gameId, Constants.TEAMID, Constants.SECRET });
	}

	public void stopGame(final long gameId) throws IOException, URISyntaxException {
		final URI stopUri = new URIBuilder().setScheme("http").setHost(server + ":8080").setPath("/test/stopBattle")
				.setParameter("gameId", Long.toString(gameId)).setParameter("teamId", Long.toString(Constants.TEAMID))
				.setParameter("secret", Constants.SECRET).build();

		final HttpGet stopGet = new HttpGet(stopUri);
		final ResponseHandler<String> handler3 = new BasicResponseHandler();
		httpclient.execute(stopGet, handler3);
	}

	@Override
	public void close() throws IOException {
		httpclient.close();
	}
}
