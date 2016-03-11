package fr.battle.undefined.ia;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.neuroph.core.NeuralNetwork;

import fr.battle.undefined.IA;
import fr.battle.undefined.ia.nn.BattleNN;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.util.Constants;

public class DeepQNetworkIA implements IA {

	private static final String NN_RESOURCE = "classpath:/nn.bak";
	protected final BattleNN nn;

	public static long minErrorCounter;

	protected static final int SIZE = Constants.BOARD_SIZE + 1;
	protected static final int HISTORY_LENGTH = 4;
	protected static final int STATE_SIZE = SIZE * HISTORY_LENGTH;
	protected static final int OUTPUT_SIZE = Action.values().length + 1;

	protected WorldState ws;
	protected long teamId;

	protected final int[] history = new int[STATE_SIZE];

	public DeepQNetworkIA() {
		final InputStream stream = DeepQNetworkIA.class.getResourceAsStream(NN_RESOURCE);
		if (stream == null) {
			nn = new BattleNN(STATE_SIZE, OUTPUT_SIZE);
		} else {
			nn = (BattleNN) NeuralNetwork.load(stream);
		}
	}

	public void save() {
		nn.save("nn.bak");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#setTeamId(long)
	 */
	@Override
	public void setTeamId(final long teamId) {
		this.teamId = teamId;
	}

	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	@Override
	public Action getNextAction() {
		// Update history
		updateToNewState(history);
		nn.setInput(history);
		nn.calculate();
		final double[] results = nn.getOutput();

		final int maxIdx = maxQIdx(results);
		if (maxIdx < Action.values().length) {
			return Action.values()[maxIdx];
		}
		return null;
	}

	protected int maxQIdx(final double[] output) {
		// Get index of max value
		final List<Integer> actionIndex = new ArrayList<>();
		Double refValue = Double.MIN_VALUE;
		for (int i = 0; i < output.length; i++) {
			if (refValue.compareTo(output[i]) == 1) {
				actionIndex.clear();
				actionIndex.add(i);
				refValue = Double.valueOf(output[i]);
			} else if (Double.valueOf(refValue).equals(output[i])) {
				actionIndex.add(i);
			}
		}
		if (actionIndex.size() <= 1) {
			if (actionIndex.isEmpty()) {
				return 0;
			}
			return actionIndex.get(0);
		}
		final Random r = new Random();
		return actionIndex.get(r.nextInt(actionIndex.size()));
	}

	protected void updateToNewState(final int[] state) {
		// Shift old screen
		System.arraycopy(state, SIZE, state, 0, (HISTORY_LENGTH - 1) * SIZE);
		// Add new frame
		System.arraycopy(ws.getAsArray(), 0, state, (HISTORY_LENGTH - 1) * SIZE, SIZE);
	}
}
