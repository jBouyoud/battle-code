package fr.battle.undefined.ia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.battle.undefined.IA;
import fr.battle.undefined.ia.nn.data.Weights;
import fr.battle.undefined.ia.nn.impl.NN;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;
import fr.battle.undefined.util.Constants;
import fr.battle.undefined.util.FileTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvaIA implements IA {

	protected final NN nn;

	public static long minErrorCounter;

	protected static final int SIZE = Constants.BOARD_SIZE + 1;
	protected static final int HISTORY_LENGTH = 4;
	protected static final int STATE_SIZE = SIZE * HISTORY_LENGTH;
	protected static final int OUTPUT_SIZE = Action.values().length + 1;

	protected WorldState ws;
	protected long teamId;

	protected final int[] history = new int[STATE_SIZE];

	public AvaIA() {
		Weights weigths = null;
		try {
			weigths = (Weights) FileTool.loadObject("./nn-weight.ser");
		} catch (final IOException e) {
			LOGGER.error("No weights or unable to load them");
		}
		nn = new NN(STATE_SIZE, (OUTPUT_SIZE + STATE_SIZE) * 2 / 3, OUTPUT_SIZE);
		if (weigths != null) {
			nn.setAllWeights(weigths);
		}

	}

	public void save() {
		nn.printAllWeights();
		FileTool.saveObject(nn.getAllWeights(), "./nn-weight.ser");
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

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.
	 * WorldState)
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		updateToNewState(history);
		final double[] state = new double[history.length];
		for (int i = 0; i < history.length; i++) {
			state[i] = history[1];
		}
		nn.setInput(state);
		nn.activate();
		final double[] result = nn.getOutput();
		LOGGER.info("NN next action :: {} for results {}", Action.values()[maxQIdx(result)], result);
		return Action.values()[maxQIdx(result)];
	}

	protected int maxQIdx(final double[] output) {
		// Get index of max value
		final List<Integer> actionIndex = new ArrayList<>();
		Double refValue = -Double.MAX_VALUE;
		for (int i = 0; i < output.length; i++) {
			if (output[i] > refValue) {
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
