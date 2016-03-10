package fr.battle.undefined.ia;

import java.util.Random;

import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RandomIA implements IA {

	private final Random rand = new Random();

	/*
	 * (non-Javadoc)
	 * @see
	 * fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.WorldState
	 * )
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		// Nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		return Action.values()[rand.nextInt(Action.values().length)];
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#setTeamId(long)
	 */
	@Override
	public void setTeamId(final long teamId) {
		// No Op
	}

}
