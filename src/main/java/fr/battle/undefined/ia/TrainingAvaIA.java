package fr.battle.undefined.ia;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.Player;
import fr.battle.undefined.model.PlayerState;
import fr.battle.undefined.model.Position;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.model.WorldState.PlayerInfo;
import fr.battle.undefined.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrainingAvaIA extends AvaIA {

	private static final DecimalFormat df = new DecimalFormat("#.000000#");

	private static final double GAMMA = .9;
	private static final double EPSILON = 0.6;

	private final Map<Long, IA> randomIA = new HashMap<>();

	private final Map<double[], Map<Pair<Integer, Double>, Pair<Double, double[]>>> replayMemory = new LinkedHashMap<>();

	private final Random rand = new Random();
	private int frameCount = 0;

	private double[] inputsCurr = new double[STATE_SIZE];

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.ia.DeepQNetworkIA#setTeamId(long)
	 */
	@Override
	public void setTeamId(final long teamId) {
		super.setTeamId(teamId);
		if (!randomIA.containsKey(teamId)) {
			randomIA.put(teamId, new SprintRunner());
			randomIA.get(teamId).setTeamId(teamId);
		}
	}

	private void createRandIAForTeam(final long teamId) {
		randomIA.put(teamId, new NonSuckingRandomIA());
		randomIA.get(teamId).setTeamId(teamId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * fr.battle.undefined.ia.DeepQNetworkIA#setWorldState(fr.battle.undefined
	 * .model.WorldState)
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		super.setWorldState(ws);
		randomIA.get(teamId).setWorldState(ws);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.ia.DeepQNetworkIA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		updateToNewState(history);

		inputsCurr = asDouble(history);

		LOGGER.info("Get next action {}", inputsCurr.hashCode());
		replayMemory.put(inputsCurr, new LinkedHashMap<>());

		nn.setInput(inputsCurr);
		nn.activate();
		final double[] outputs = nn.getOutput();
		String s = "";
		final DecimalFormat df = new DecimalFormat("#.000000#");
		for (final double d : outputs) {
			s += df.format(d) + ", ";
		}
		LOGGER.debug("NN output : {}", s);

		int idx = 0;
		for (final double out : outputs) {
			replayMemory.get(inputsCurr).put(Pair.of(idx++, out), null);
		}
		LOGGER.info("NN next action :: {} for results {}", Action.values()[maxQIdx(outputs)], outputs);
		Action a = null;
		if (rand.nextDouble() <= EPSILON) {
			a = randomIA.get(teamId).getNextAction();
		} else {
			final int maxIdx = maxQIdx(outputs);
			if (maxIdx < Action.values().length) {
				a = Action.values()[maxIdx];
			}
		}
		return a;
	}

	@Override
	public void afterAction() {
		LOGGER.info("afterAction {} - {}", inputsCurr.hashCode(), ws.getRound());
		super.afterAction();
		//
		for (final Pair<Integer, Double> out : replayMemory.get(inputsCurr).keySet()) {
			Action a = null;
			if (out.getKey() < Action.values().length) {
				a = Action.values()[out.getKey()];
			}
			replayMemory.get(inputsCurr).put(out, Pair.of(ws.getReward(a), generateNextRandomState(inputsCurr, ws, a)));
		}
		frameCount++;
		if (frameCount < HISTORY_LENGTH) {
			replayMemory.clear();
			return;
		}
		LOGGER.info("Compute NN learning data of {}", replayMemory.size());
		// Mini batch learn
		double err = 0d;
		int count = 0;
		for (final Entry<double[], Map<Pair<Integer, Double>, Pair<Double, double[]>>> inputEntry : replayMemory
				.entrySet()) {

			LOGGER.trace("History at {} with {} actions", count, inputEntry.getValue().size());
			final double[] rrs = new double[AvaIA.OUTPUT_SIZE];
			final double[] qPrev = new double[AvaIA.OUTPUT_SIZE];
			final double[] q = new double[AvaIA.OUTPUT_SIZE];
			for (final Entry<Pair<Integer, Double>, Pair<Double, double[]>> inputPrimEntry : inputEntry.getValue()
					.entrySet()) {
				qPrev[inputPrimEntry.getKey().getKey()] = inputPrimEntry.getKey().getValue();
				final double rr = inputPrimEntry.getValue().getKey();
				rrs[inputPrimEntry.getKey().getKey()] = rr;
				final double[] ssPrim = inputPrimEntry.getValue().getValue();

				if (ssPrim[SIZE * 3 + Constants.BOARD_SIZE] == 0) {
					LOGGER.trace("Game is ended");
					q[inputPrimEntry.getKey().getKey()] = rr;
				} else {

					nn.setInput(ssPrim);
					nn.activate();
					final double[] qSsPrimAa = nn.getOutput();
					q[inputPrimEntry.getKey().getKey()] = rr + GAMMA * maxQ(qSsPrimAa);
					nn.applyBackpropagation(q[inputPrimEntry.getKey().getKey()], inputPrimEntry.getKey().getKey());
				}
				count++;
				err += Math.pow(inputPrimEntry.getKey().getValue() - q[inputPrimEntry.getKey().getKey()], 2);
			}
			LOGGER.debug("rr    : {}", rrs);
			LOGGER.debug("qPrev : {}", qPrev);
			LOGGER.debug("q     : {}", q);
			LOGGER.trace("------ {}", teamId);
		}
		LOGGER.info("Start NN learning of count {}", count);
		LOGGER.warn("Avg Error {}", df.format(Math.sqrt(err / count)));
		// Clean last scene from memory in order to learn faster (Online
		// training instead of offline training)
		// if (replayMemory.size() > 100) {
		// final Optional<Entry<double[], Map<Pair<Integer, Double>,
		// Pair<Double, double[]>>>> olderEvent = replayMemory
		// .entrySet().stream().filter(e -> e.getValue().keySet().stream().map(p
		// -> p.getValue()).filter(
		// v -> v == 0.0).count() == e.getValue().size()).findFirst();
		final Optional<Entry<double[], Map<Pair<Integer, Double>, Pair<Double, double[]>>>> olderEvent = replayMemory
				.entrySet().stream().findFirst();
		if (olderEvent.isPresent()) {
			replayMemory.remove(olderEvent.get().getKey());
		}
		// }
	}

	private double[] generateNextRandomState(final double[] currentState, final WorldState ws, final Action a) {
		final List<Position> logos = new ArrayList<>(ws.getLogos().size());
		for (final Position logo : ws.getLogos()) {
			logos.add(new Position(logo.getX(), logo.getY()));
		}
		final Map<Long, PlayerInfo> playersState = new LinkedHashMap<>(ws.getPlayersState().size());
		for (final Entry<Long, PlayerInfo> playerStateEntry : playersState.entrySet()) {
			playersState.put(playerStateEntry.getKey(),
					new PlayerInfo(
							new Player(playerStateEntry.getValue().getPlayer().getId(),
									new Position(playerStateEntry.getValue().getPlayer().getCaddy().getX(),
											playerStateEntry.getValue().getPlayer().getCaddy().getY())),
					playerStateEntry.getValue().getPosition(), playerStateEntry.getValue().getScore(),
					playerStateEntry.getValue().getState()));
		}
		final WorldState ws2 = new WorldState(ws.getRound() + 1, playersState, logos, playersState.get(teamId));

		final List<Long> ids = Arrays.asList(playersState.keySet().toArray(new Long[playersState.size()]));
		final int myTeamIdx = ids.indexOf(teamId);
		for (int teamIdx = myTeamIdx; teamIdx != myTeamIdx; teamIdx = teamIdx++ % (ids.size() - 1)) {
			// Choix de l'action
			Action userAction = null;
			if (ids.get(teamIdx).equals(teamId)) {
				userAction = a;
			} else {
				if (randomIA.containsKey(ids.get(teamIdx))) {
					createRandIAForTeam(ids.get(teamIdx));
				}
				randomIA.get(ids.get(teamIdx)).setWorldState(ws2);
				userAction = randomIA.get(ids.get(teamIdx)).getNextAction();
			}
			// Résolution de l'action
			PlayerState state = PlayerState.PLAYING;
			if (!userAction.isAllowed(ws2, ids.get(teamIdx))) {
				state = PlayerState.STUNNED;
			}
			final PlayerInfo pi = playersState.get(ids.get(teamIdx));
			final Position newPlayerPos = userAction.getNextPosition(pi.getPosition(), state);

			pi.setPosition(newPlayerPos);
			pi.setScore(pi.getScore() + (int) ws2.getReward(userAction));
			pi.setState(state);
			if (userAction.isSuperPower()) {
				pi.getPlayer().decreaseSuperPower();
			}
			pi.getLastSlaped().clear();
			ws.getSlappedPlayers(newPlayerPos).map(pi2 -> pi2.getPlayer()).forEach(pi.getLastSlaped()::add);

			// Update de la position du logo si il eté porté par l'équipe
			final Optional<Position> logoOpt = logos.parallelStream().filter(p -> p.equals(pi.getPosition()))
					.findFirst();
			logoOpt.ifPresent(logo -> {
				logos.remove(logo);
				logos.add(newPlayerPos);
			});

			// Mise en stunned des players suivant si nécessaire
			pi.getLastSlaped().stream().map(p -> p.getId())
					.forEach(id -> playersState.get(id).setState(PlayerState.STUNNED));
			// TODO Gerer le fait que les logo re(spawn enrandom a plus ou moins
			// deux cases
		}

		// Shift old screen
		final double[] state = new double[currentState.length];
		System.arraycopy(currentState, SIZE, state, 0, (HISTORY_LENGTH - 1) * SIZE);
		// Add new frame
		System.arraycopy(asDouble(ws2.getAsArray()), 0, state, (HISTORY_LENGTH - 1) * SIZE, SIZE);

		return state;
	}

	private double[] asDouble(final int[] input) {
		final double[] asDouble = new double[input.length];
		int i = 0;
		for (final int in : input) {
			asDouble[i++] = in;
		}
		return asDouble;
	}

	private double maxQ(final double[] output) {
		double max = -Double.MAX_VALUE;
		// Get index of max value
		for (final double element : output) {
			max = Math.max(element, max);
		}
		return Math.min(Math.max(max, -1000), 1000);
	}

}
