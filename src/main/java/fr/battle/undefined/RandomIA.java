package fr.battle.undefined;

import java.util.Random;

import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

public class RandomIA implements IA {

	private final Random rand = new Random();

	@Override
	public void setWorldState(final WorldState ws) {
		// Nothing to do here
	}

	@Override
	public Action getNextAction() {
		return Action.values()[rand.nextInt(Action.values().length)];
	}

}
