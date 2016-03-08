package fr.battle.undefined;

import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

public interface IA {

	void setWorldState(WorldState ws);

	Action getNextAction();
}
