package fr.battle.undefined;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.PlayerState;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import fr.battle.undefined.util.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Client {

	private final String ipServer;
	private final long teamId;
	private final int socketNumber;
	private final long gameId;

	private final List<IA> ias = new ArrayList<>();

	public void start() {
		LOGGER.info("Demarrage du client");

		try (
				final Socket socket = new Socket(ipServer, socketNumber);
				final BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				final PrintWriter out = new PrintWriter(socket
						.getOutputStream());) {

			LOGGER.info("Envoi de l'incription");
			out.println(Constants.SECRET + "%%inscription::" + gameId + ";"
					+ teamId);
			out.flush();

			boolean end = false;
			String message = null;
			Player[] players = null;
			WorldState ws = null;

			while ((message = in.readLine()) != null && !end) {
				LOGGER.info("Message recu : " + message);
				if ("Inscription OK".equalsIgnoreCase(message)) {
					LOGGER.info("Je me suis bien inscrit a la battle");
					LOGGER.info("Pour voir la partie : http://" + ipServer
							+ ":8080/?gameId=" + gameId);
				} else if (message.startsWith("worldstate::")) {
					final String[] components = message.substring("worldstate::"
							.length()).split(";", -1);
					if (ws == null) {
						final String[] playerInfos = components[2].split(":");
						players = new Player[playerInfos.length];
						int i = 0;
						for (final String playerInfo : playerInfos) {
							final String[] pComponents = playerInfo.split(",");
							players[i++] = new Player(i, pComponents[0],
									new Position(Integer.parseInt(
											pComponents[1]), Integer.parseInt(
													pComponents[2])));
						}
						ws = new WorldState(players);
					}
					// Mise à jour de l'etat
					ws.update(Integer.parseInt(components[0]), parsePlayerState(
							players, components[1]), parseLogos(components[2]));
					//
					final WorldState actualWs = ws;
					ias.forEach(ia -> ia.setWorldState(actualWs));
					// Récupération de la derniere action
					final Optional<Action> a = ias.stream().map(ia -> ia
							.getNextAction()).distinct().findAny();
					// .max(a -> a.rate())

					// On joue
					final String action = Constants.SECRET + "%%action::"
							+ teamId + ";" + gameId + ";" + ws.getRound() + ";"
							+ a.orElse(Action.EST).getCode();
					LOGGER.info(action);
					out.println(action);
					out.flush();
				} else if ("Inscription KO".equalsIgnoreCase(message)) {
					LOGGER.info("inscription KO");
				} else if ("game over".equalsIgnoreCase(message)) {
					LOGGER.info("game over");
					end = true;
				} else if ("action OK".equalsIgnoreCase(message)) {
					LOGGER.info("Action bien pris en compte");
				}
			}
		} catch (final IOException e) {
			LOGGER.error("Socker IO Error", e);
		}
	}

	private List<Position> parseLogos(final String rawLogos) {
		final List<Position> logos = new ArrayList<>();
		for (final String logo : rawLogos.split(":")) {
			final String[] pos = logo.split(",");
			logos.add(new Position(Integer.parseInt(pos[0]), Integer.parseInt(
					pos[1])));
		}
		return logos;
	}

	private Map<Player, PlayerInfo> parsePlayerState(final Player[] players,
			final String rawPlayersInfos) {
		final Map<Player, PlayerInfo> playersStates = new LinkedHashMap<>();
		final String[] playersInfos = rawPlayersInfos.split(":");
		for (final Player player : players) {
			final String[] info = playersInfos[player.getId()].split(",");
			playersStates.put(player, new PlayerInfo(new Position(Integer
					.parseInt(info[0]), Integer.parseInt(info[1])), Integer
							.parseInt(info[2]), PlayerState.valueOf(info[3]
									.toUpperCase())));
		}
		return playersStates;
	}

}
