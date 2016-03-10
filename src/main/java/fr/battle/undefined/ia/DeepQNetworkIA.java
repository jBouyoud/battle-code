package fr.battle.undefined.ia;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.learning.BackPropagation;

import fr.battle.undefined.ia.nn.BattleNN;

public class DeepQNetworkIA {

	public DeepQNetworkIA() {
		// NeuralNetwork.load("file.nn");
		final NeuralNetwork<BackPropagation> nn = new BattleNN(100, 4);
		nn.save("file.nn");
		nn.setInput(new double[100]);
		nn.calculate();
		nn.getOutput();
		nn.save("file.nn");

	}

}
