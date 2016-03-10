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
	private static final int FRAME_HISTORY = 4;
	protected final BattleNN nn;

	public static long minErrorCounter;

	private WorldState ws;

	protected int[] history = new int[Constants.BOARD_SIZE];

	public DeepQNetworkIA() {
		final InputStream stream = DeepQNetworkIA.class.getResourceAsStream(
				NN_RESOURCE);
		if (stream == null) {
			nn = new BattleNN(Constants.BOARD_SIZE, Constants.NUMBER_OF_ACTION);
		} else {
			nn = (BattleNN) NeuralNetwork.load(stream);
		}
	}

	public void save() {
		nn.save("nn.bak");
	}

	/*
	 * (non-Javadoc)
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
		// Shift old screen
		System.arraycopy(history, Constants.BOARD_SIZE / FRAME_HISTORY - 1,
				history, 0, Constants.BOARD_SIZE - Constants.BOARD_SIZE
						/ FRAME_HISTORY - 1);
		// Add new frame
		System.arraycopy(ws.getAsArray(), 0, history, Constants.BOARD_SIZE / 3
				- 1, ws.getAsArray().length);
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

}
