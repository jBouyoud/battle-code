package fr.battle.undefined.ia;

import java.util.Arrays;
import java.util.Random;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

public class TrainingDeepQNetworkIA extends DeepQNetworkIA {

	private static final double GAMMA = 0.9;
	private static final double ALPHA = 0.1;
	private static final double EPSILON = 0.1;

	private final IA random = new NonSuckingRandomIA();

	private int[] inputsPrev = new int[STATE_SIZE];
	private int[] inputsCurr = new int[STATE_SIZE];

	private WorldState wsPrev;

	private Action actionSelectionCurr = null;
	private Action actionSelectionPrev;

	private double[] actionCurr = new double[OUTPUT_SIZE];
	private double[] actionPrev = new double[OUTPUT_SIZE];

	private long stateCounter = 0;

	private final Random rand = new Random();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.battle.undefined.ia.DeepQNetworkIA#setTeamId(long)
	 */
	@Override
	public void setTeamId(final long teamId) {
		super.setTeamId(teamId);
		random.setTeamId(teamId);
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
		random.setWorldState(ws);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.battle.undefined.ia.DeepQNetworkIA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		updateToNewState(history);
		inputsPrev = copy(inputsCurr);
		inputsCurr = copy(history);

		nn.setInput(history);
		nn.calculate();
		final double[] outputs = nn.getOutput();

		actionSelectionPrev = actionSelectionCurr;
		actionSelectionCurr = getAction();

		actionPrev = copy(actionCurr);
		actionCurr = copy(outputs);

		if (stateCounter > 0) {
			/**
			 * deltaQ(s,a) = R(s,a) + gamma * Max(next state, all actions) -
			 * Q(s,a) Q(s,a) = Q(s,a) + alpha * deltaQ
			 */

			// One step later update because we don't have the next state
			// Q(s,a) prev
			final double qPrev = actionPrev[actionSelectionPrev.ordinal()];
			// R(inputsPrev, actionSelectionPrev) + GAMMA * maxQ(actionCurr) -
			// qPrev;
			final double deltaQPrev = wsPrev.getReward(actionSelectionPrev) + GAMMA * maxQ(actionCurr) - qPrev;
			final double q = qPrev + ALPHA * deltaQPrev;

			// FIXME Check that
			actionPrev[actionSelectionPrev.ordinal()] = q;
			nn.learn(inputsPrev, actionPrev);

			final double error = Math.pow(qPrev - q, 2); // NN error
			if (error < 0.001) {
				minErrorCounter++;
			}
		}

		wsPrev = ws;
		stateCounter++;
		return actionSelectionCurr;
	}

	private Action getAction() {
		if (rand.nextDouble() <= EPSILON) {
			return random.getNextAction();
		}
		return super.getNextActionFrom(history);
	}

	public void notifyGameEnded(final int winner) {
		// gameEnded = true;
		//
		// if (winner == this.getMark()) {
		// reward = rewardWin;
		// } else if (winner == TicTacToe.NONE) {
		// reward = rewardDraw;
		// } else {
		// reward = rewardLoose;
		// }
		// play();
	}

	private double maxQ(final double[] a) {
		// if (gameEnded) {
		// return 0;
		// }
		double max = -Double.MAX_VALUE;
		for (final double d : a) {
			if (d > max /*
						 * Filtre sur les actions autorisés &&
						 * freeCellsHash.contains(i)
						 */) {
				max = d; // update
			}
		}
		return max == -Double.MAX_VALUE ? 0 : max;
	}

	private double[] copy(final double[] arr) {
		return Arrays.copyOf(arr, arr.length);
	}

	private int[] copy(final int[] arr) {
		return Arrays.copyOf(arr, arr.length);
	}
}
