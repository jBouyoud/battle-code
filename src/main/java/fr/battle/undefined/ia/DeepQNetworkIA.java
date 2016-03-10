package fr.battle.undefined.ia;

import java.io.InputStream;

import org.neuroph.core.NeuralNetwork;

import fr.battle.undefined.IA;
import fr.battle.undefined.ia.nn.BattleNN;
import fr.battle.undefined.model.Action;
import fr.battle.undefined.model.WorldState;

public class DeepQNetworkIA implements IA {

	private static final String NN_RESOURCE = "classpath:/nn.bak";

	private static BattleNN nn;

	public DeepQNetworkIA() {
		final InputStream stream = DeepQNetworkIA.class.getResourceAsStream(NN_RESOURCE);
		if (stream == null) {
			nn = new BattleNN(836, 8);
		} else {
			nn = (BattleNN) NeuralNetwork.load(stream);
		}
	}

	public void save() {
		nn.save("nn.bak");
	}

	@Override
	public void setTeamId(final long teamId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWorldState(final WorldState ws) {
		// TODO Auto-generated method stub

	}

	@Override
	public Action getNextAction() {
		// TODO Auto-generated method stub
		nn.setInput(new double[100]);
		nn.calculate();
		nn.getOutput();
		return null;
	}

}
