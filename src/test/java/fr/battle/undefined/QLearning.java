package fr.battle.undefined;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * @author Kunuk Nykjaer
 */
public class QLearning {
	final DecimalFormat df = new DecimalFormat("#.##");

	// path finding
	final double alpha = 1;
	final double gamma = 0.9;

	// states A,B,C,D,E,F
	// e.g. from A we can go to B or D
	// from C we can only go to C
	// C is goal state, reward 100 when B->C or F->C
	//
	// _______
	// |A|B|C|
	// |_____|
	// |D|E|F|
	// |_____|
	//

	final int stateA = 0;
	final int stateB = 1;
	final int stateC = 2;
	final int stateD = 3;
	final int stateE = 4;
	final int stateF = 5;

	final int statesCount = 6;
	final int[] states = new int[] { stateA, stateB, stateC, stateD, stateE,
			stateF };

	// http://en.wikipedia.org/wiki/Q-learning
	// http://people.revoledu.com/kardi/tutorial/ReinforcementLearning/Q-Learning.htm

	// Q(s,a)= Q(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) -
	// Q(s,a))

	int[][] R = new int[statesCount][statesCount]; // reward lookup

	// Will be neural network
	double[][] Q = new double[statesCount][statesCount]; // Q learning

	int[] actionsFromA = new int[] { stateB, stateD };
	int[] actionsFromB = new int[] { stateA, stateC, stateE };
	int[] actionsFromC = new int[] { stateC };
	int[] actionsFromD = new int[] { stateA, stateE };
	int[] actionsFromE = new int[] { stateB, stateD, stateF };
	int[] actionsFromF = new int[] { stateC, stateE };
	int[][] actions = new int[][] { actionsFromA, actionsFromB, actionsFromC,
			actionsFromD, actionsFromE, actionsFromF };

	String[] stateNames = new String[] { "A", "B", "C", "D", "E", "F" };

	public QLearning() {
		init();
	}

	public void init() {
		R[stateB][stateC] = 100; // from b to c
		R[stateF][stateC] = 100; // from f to c
	}

	public static void main(final String[] args) {

		// Should start a party

		final QLearning obj = new QLearning();
		obj.run();
		obj.printResult();
		obj.showPolicy();
	}

	void run() {
		/*
		 * 1. Set parameter , and environment reward matrix R 2. Initialize
		 * matrix Q as zero matrix 3. For each episode: Select random initial
		 * state Do while not reach goal state o Select one among all possible
		 * actions for the current state o Using this possible action, consider
		 * to go to the next state o Get maximum Q value of this next state
		 * based on all possible actions o Compute o Set the next state as the
		 * current state
		 */

		// For each episode
		final Random rand = new Random();
		for (int i = 0; i < 50; i++) { // train episodes
			// Select random initial state
			int state = rand.nextInt(statesCount);
			while (state != stateC) // goal state
			{
				// Select one among all possible actions for the current state
				final int[] actionsFromState = actions[state];

				// Selection strategy is random in this example
				final int index = rand.nextInt(actionsFromState.length);
				// Appelé le NN courant pour avoir l'action (max des stats) (pas
				// de learning) TODO fa
				final int action = actionsFromState[index];

				// Pour chaque action calculé le r et le s'
				// final Map<Action, Double> results = Q(state, action);
				// MaxQ = max value de result
				// Caclculé le reward pour cete action

				// Action outcome is set to deterministic in this example
				// Transition probability is 1
				final int nextState = action; // data structure

				// Using this possible action, consider to go to the next state

				// pour chaque S' appelé Q avec
				final double q = Q(state, action);
				final double maxQ = maxQ(nextState);
				final int r = R(state, action);
				// voir pour le alpha à 1
				final double value = q + alpha * (r + gamma * maxQ - q);
				setQ(state, action, value);

				// Set the next state as the current state
				state = nextState;
			}
		}
	}

	double maxQ(final int s) {
		final int[] actionsFromState = actions[s];
		double maxValue = Double.MIN_VALUE;
		for (final int nextState : actionsFromState) {
			final double value = Q[s][nextState];

			if (value > maxValue) {
				maxValue = value;
			}
		}
		return maxValue;
	}

	// get policy from state
	int policy(final int state) {
		final int[] actionsFromState = actions[state];
		double maxValue = Double.MIN_VALUE;
		int policyGotoState = state; // default goto self if not found
		for (final int nextState : actionsFromState) {
			final double value = Q[state][nextState];

			if (value > maxValue) {
				maxValue = value;
				policyGotoState = nextState;
			}
		}
		return policyGotoState;
	}

	double Q(final int s, final int a) {
		return Q[s][a];
	}

	void setQ(final int s, final int a, final double value) {
		Q[s][a] = value;
	}

	int R(final int s, final int a) {
		return R[s][a];
	}

	void printResult() {
		System.out.println("Print result");
		for (int i = 0; i < Q.length; i++) {
			System.out.print("out from " + stateNames[i] + ":  ");
			for (int j = 0; j < Q[i].length; j++) {
				System.out.print(df.format(Q[i][j]) + " ");
			}
			System.out.println();
		}
	}

	// policy is maxQ(states)
	void showPolicy() {
		System.out.println("\nshowPolicy");
		for (final int from : states) {
			final int to = policy(from);
			System.out.println("from " + stateNames[from] + " goto "
					+ stateNames[to]);
		}
	}
}