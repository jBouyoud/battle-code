package fr.battle.undefined;

import java.util.List;
import java.util.Random;

import fr.battle.undefined.model.Action;

public class Utils {
	private final static double EPSILON = 0.1;

	private Utils() {

	}

	public static Action getAction(final Action a, final List<Action> actions) {
		final Random rand = new Random();
		final double roll = rand.nextDouble();
		if (roll <= EPSILON) {
			return actions.get(rand.nextInt(actions.size()));
		}
		return a;
	}

}
