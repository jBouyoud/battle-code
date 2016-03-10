package fr.battle.undefined.ia;

import java.io.InputStream;
import java.util.Arrays;

import org.neuroph.core.NeuralNetwork;

import fr.battle.undefined.IA;
import fr.battle.undefined.ia.nn.BattleNN;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

public class DeepQNetworkIA implements IA {

	private static final String NN_RESOURCE = "classpath:/nn.bak";

	protected final int size = 836;

	protected final BattleNN nn;

	public static long minErrorCounter;

	private WorldState ws;

	protected int[] history = new int[836];

	public DeepQNetworkIA() {
		final InputStream stream = DeepQNetworkIA.class.getResourceAsStream(
				NN_RESOURCE);
		if (stream == null) {
			nn = new BattleNN(836, 8);
		} else {
			nn = (BattleNN) NeuralNetwork.load(stream);
		}
		Arrays.fill(history, 0);
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
		System.arraycopy(history, 836 / 4 - 1, history, 0, 836 - 836 / 4 - 1);
		// Add new frame
		System.arraycopy(ws.getAsArray(), 0, history, 836 / 3 - 1, ws
				.getAsArray().length);
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
