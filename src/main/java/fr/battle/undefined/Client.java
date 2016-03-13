package fr.battle.undefined;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.battle.undefined.ia.NonSuckingRandomIA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.PlayerState;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import fr.battle.undefined.util.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Client {

	// http://52.29.48.22:8080/api/display/?gameId=<gameID>
	// Permettrait de se mettre à jour à chaque move d'un autre joueur et de ne
	// pas attendre la demande via socket ... laisse la seconde complète pour
	// répondre

	private final String ipServer;
	private final long teamId;
	private final int socketNumber;
	private final long gameId;
	private final IA ia;
	private final IA fallbackIA = new NonSuckingRandomIA();
	private WorldState ws = null;

	@Getter
	private volatile boolean ended = false;

	public Client init(final long sleepTime) throws InterruptedException {
		ia.setTeamId(teamId);
		fallbackIA.setTeamId(teamId);

		if (sleepTime > 0) {
			Thread.sleep(sleepTime);
		}
		return this;
	}

	public void start() {
		LOGGER.info("Demarrage du client");

		try (final Socket socket = new Socket(ipServer, socketNumber);
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				final PrintWriter out = new PrintWriter(socket.getOutputStream());) {

			LOGGER.info("Envoi de l'incription");
			out.println(Constants.SECRET + "%%inscription::" + gameId + ";" + teamId);
			out.flush();

			ended = false;
			String message = null;
			Map<Long, Player> players = null;

			while ((message = in.readLine()) != null && !ended) {
				LOGGER.trace("Message recu : " + message);
				if ("Inscription OK".equalsIgnoreCase(message)) {
					LOGGER.info("Je me suis bien inscrit a la battle");
					LOGGER.info("Pour voir la partie : http://" + ipServer + ":8080/?gameId=" + gameId);
				} else if (message.startsWith("worldstate::")) {
					final String[] components = message.substring("worldstate::".length()).split(";", -1);
					if (players == null) {
						final String[] playerInfos = components[3].split(":");
						players = new HashMap<>(playerInfos.length);
						for (final String playerInfo : playerInfos) {
							final String[] pComponents = playerInfo.split(",");
							final long id = Long.parseLong(pComponents[0]);
							players.put(id, new Player(id,
									new Position(Integer.parseInt(pComponents[1]), Integer.parseInt(pComponents[2]))));
						}
					}
					// Mise à jour de l'etat
					final Map<Long, PlayerInfo> playersState = parsePlayerState(players, components[1]);

					final WorldState newWs = new WorldState(Integer.parseInt(components[0]), playersState,
							parseLogos(components[2]), playersState.get(teamId));

					// Inject last slapped players from previous world state
					if (ws != null) {
						newWs.getMe().setLastSlaped(ws.getMe().getLastSlaped());
					}
					ws = newWs;

					//
					fallbackIA.setWorldState(ws);

					Action action = Action.EST;
					try {
						ia.setWorldState(ws);

						action = ia.getNextAction();
					} catch (final Throwable th) {
						LOGGER.error("catch IA throwable", th);
					}
					if (action == null /*
										 * || !action.isAllowed(ws, teamId)
										 */) {
						LOGGER.warn("Use fallback to avoid point loss");
						action = fallbackIA.getNextAction();
						if (!action.isAllowed(ws, teamId)) {
							action = Action.EST;
						}
					}

					// On joue
					final String actionMessage = Constants.SECRET + "%%action::" + teamId + ";" + gameId + ";"
							+ ws.getRound() + ";" + action.getCode();
					LOGGER.trace(actionMessage);
					out.println(actionMessage);
					out.flush();

					// Update for the next round
					try {
						ia.afterAction();
						fallbackIA.afterAction();
					} catch (final Throwable th) {
						LOGGER.error("catch IA throwable", th);
					}
					if (action.isSuperPower()) {
						ws.getMe().getPlayer().decreaseSuperPower();
					}

					if (ws.getSlappedPlayers(action.getNextPosition(ws.getMe().getPosition(), ws.getMe().getState()))
							.count() > 0) {
						ws.getMe().getLastSlaped().clear();
						ws.getSlappedPlayers(action.getNextPosition(ws.getMe().getPosition(), ws.getMe().getState()))
								.map(pi -> pi.getPlayer()).forEach(ws.getMe().getLastSlaped()::add);
					}

					ended = ws.getRound() == Constants.MAX_ROUND;
				} else if ("Inscription KO".equalsIgnoreCase(message)) {
					LOGGER.info("inscription KO");
				} else if ("game over".equalsIgnoreCase(message)) {
					LOGGER.info("game over");
					ended = true;
				} else if ("action OK".equalsIgnoreCase(message)) {
					LOGGER.trace("Action bien pris en compte");
				}
			}
		} catch (final IOException e) {
			LOGGER.error("Socker IO Error", e);
		}
	}

	private List<Position> parseLogos(final String rawLogos) {
		final List<Position> logos = new ArrayList<>();
		if (rawLogos.isEmpty()) {
			return logos;
		}
		for (final String logo : rawLogos.split(":")) {
			final String[] pos = logo.split(",");
			logos.add(new Position(Integer.parseInt(pos[0]), Integer.parseInt(pos[1])));
		}
		return logos;
	}

	private Map<Long, PlayerInfo> parsePlayerState(final Map<Long, Player> players, final String rawPlayersInfos) {
		final Map<Long, PlayerInfo> playersStates = new LinkedHashMap<>();
		final String[] playersInfos = rawPlayersInfos.split(":");
		for (final String playersInfo : playersInfos) {
			final String[] info = playersInfo.split(",");
			final long playerId = Long.parseLong(info[0]);
			final Player p = players.get(playerId);
			playersStates.put(playerId,
					new PlayerInfo(p, new Position(Integer.parseInt(info[1]), Integer.parseInt(info[2])),
							Integer.parseInt(info[3]), PlayerState.valueOf(info[4].toUpperCase())));
		}
		return playersStates;
	}

}
