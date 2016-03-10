package fr.battle.undefined.ia;

import java.io.InputStream;

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
	protected static final int OUTPUT_SIZE = Action.values().length;

	protected WorldState ws;

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
		// Nothing to do here
	}

	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	@Override
	public Action getNextAction() {
		// Update history
		updateToNewState(history);
		return getNextActionFrom(history);
	}

	protected Action getNextActionFrom(final int[] input) {
		nn.setInput(history);
		nn.calculate();
		final double[] results = nn.getOutput();
		// Get index of max value
		int actionIndex = 0;
		double refValue = Double.MIN_VALUE;
		for (int i = 0; i < results.length; i++) {
			if (results[i] > refValue) {
				actionIndex = i;
				refValue = results[i];
			}
		}
		return Action.values()[actionIndex];
	}

	protected void updateToNewState(final int[] state) {
		// Shift old screen
		System.arraycopy(state, STATE_SIZE / HISTORY_LENGTH - 1, state, 0, STATE_SIZE - STATE_SIZE / HISTORY_LENGTH - 1);
		// Add new frame
		System.arraycopy(ws.getAsArray(), 0, state, STATE_SIZE / (HISTORY_LENGTH - 1) - 1, SIZE);
	}
}
