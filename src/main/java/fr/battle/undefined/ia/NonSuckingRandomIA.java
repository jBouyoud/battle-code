package fr.battle.undefined.ia;

import java.util.List;
import java.util.Random;

import lombok.NoArgsConstructor;
import fr.battle.undefined.IA;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

@NoArgsConstructor
public class NonSuckingRandomIA implements IA {

	private final Random rand = new Random();

	private long teamId;

	private WorldState ws = null;

	/*
	 * (non-Javadoc)
	 * @see
	 * fr.battle.undefined.IA#setWorldState(fr.battle.undefined.model.WorldState
	 * )
	 */
	@Override
	public void setWorldState(final WorldState ws) {
		this.ws = ws;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.battle.undefined.IA#getNextAction()
	 */
	@Override
	public Action getNextAction() {
		final List<Action> allowed = Action.allowed(ws, teamId);
		return allowed.get(rand.nextInt(allowed.size()));
	}

	@Override
	public void setTeamId(final long teamId) {
		this.teamId = teamId;
	}
}
