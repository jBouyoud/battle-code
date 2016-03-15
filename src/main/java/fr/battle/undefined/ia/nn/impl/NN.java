package fr.battle.undefined.ia.nn.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.battle.undefined.ia.nn.data.Weight;
import fr.battle.undefined.ia.nn.data.Weights;

/**
 * @author Kunuk Nykjaer and others
 *
 */
public class NN {

	private static final int MAX_SCORE = 1;

	public final boolean DEBUG = false;

	final int INPUT = 0;
	final int HIDDEN = 1;
	final int OUTPUT = 2;

	final DecimalFormat df;
	final Random rand;
	final double epsilon = Double.MIN_VALUE;

	// this framework only supports one hidden layer
	final ArrayList<Neuron> inputLayer = new ArrayList<Neuron>();
	final ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
	final ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();
	final Neuron bias = new Neuron();

	// for weight update all
	final HashMap<String, Double> weightUpdate = new HashMap<String, Double>();

	/**
	 * Tic tac toe xs is the input, action is the output first nine input is 0
	 * or 1 for fields occupied with hero pieces next nine input is 0 or 1 for
	 * fields occupied villain pieces
	 *
	 */
	final int[] layers; // input, hidden, output
	final int randomWeightMultiplier = 1;

	// old was 0.1
	final double learningRate = 0.2f;
	final double momentum = 0.9f;

	public NN(final int input, final int hidden, final int output) {
		layers = new int[] { input, hidden, output };
		rand = new Random();

		df = new DecimalFormat("#.0#");

		/**
		 * create all Neurons and connections connections are created in the
		 * neuron class
		 */
		for (int i = 0; i < layers.length; i++) {
			if (i == INPUT) { // input layer
				for (int j = 0; j < layers[INPUT]; j++) {
					final Neuron neuron = new Neuron();
					inputLayer.add(neuron);
				}
			} else if (i == HIDDEN) { // hidden layer
				for (int j = 0; j < layers[HIDDEN]; j++) {
					final Neuron neuron = new Neuron();
					neuron.addInConnectionsS(inputLayer);
					neuron.addBiasConnection(bias);
					hiddenLayer.add(neuron);
				}
			}

			else if (i == OUTPUT) { // output layer
				for (int j = 0; j < layers[OUTPUT]; j++) {
					final Neuron neuron = new Neuron();
					neuron.addInConnectionsS(hiddenLayer);
					neuron.addBiasConnection(bias);
					outputLayer.add(neuron);
				}
			} else {
				System.out.println("!Error NeuralNetwork init");
			}
		}

		// initialize random weights
		for (final Neuron neuron : hiddenLayer) {
			final ArrayList<Connection> connections = neuron.getAllInConnections();
			for (final Connection conn : connections) {
				conn.setWeight(getRandom());
			}
		}
		for (final Neuron neuron : outputLayer) {
			final ArrayList<Connection> connections = neuron.getAllInConnections();
			for (final Connection conn : connections) {
				conn.setWeight(getRandom());
			}
		}

		// reset id counters
		Neuron.counter = 0;
		Connection.counter = 0;
	}

	// random
	double getRandom() {
		return randomWeightMultiplier * (rand.nextDouble() * 2 - 1); // [-1;1[
	}

	/**
	 *
	 * @param inputs
	 *            There is equally many neurons in the input layer as in input
	 *            variables
	 */
	public void setInput(final double inputs[]) {

		for (int i = 0; i < inputLayer.size(); i++) {
			inputLayer.get(i).setOutput(inputs[i]);
		}
	}

	public double[] getOutput() {
		final double[] outputs = new double[outputLayer.size()];
		for (int i = 0; i < outputLayer.size(); i++) {
			outputs[i] = outputLayer.get(i).getOutput();
		}
		return outputs;
	}

	/**
	 * Calculate the output of the neural network based on the input The forward
	 * operation
	 */
	public void activate() {
		for (final Neuron n : hiddenLayer) {
			n.calculateOutput();
		}

		for (final Neuron n : outputLayer) {
			n.calculateOutput();
		}
	}

	public void printAllWeights() {
		// weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double w = con.getWeight();
				System.out.println("n=" + n.id + " c=" + con.id + " w=" + df.format(w));
			}
		}
		// weights for the output layer
		for (final Neuron n : outputLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double w = con.getWeight();
				System.out.println("n=" + n.id + " c=" + con.id + " w=" + df.format(w));
			}
		}
	}

	public void setAllWeights(final Weights ws) {
		weightUpdate.clear();

		for (final Weight w : ws.weights) {
			weightUpdate.put(weightKey(w.nodeId, w.connectionId), w.weight);
		}

		updateAllWeights();
	}

	public Weights getAllWeights() {
		final Weights weights = new Weights();

		// weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double w = con.getWeight();
				final Weight weight = new Weight(n.id, con.id, w);
				weights.weights.add(weight);
			}
		}
		// weights for the output layer
		for (final Neuron n : outputLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double w = con.getWeight();
				final Weight weight = new Weight(n.id, con.id, w);
				weights.weights.add(weight);
			}
		}

		return weights;
	}

	String weightKey(final int neuronId, final int conId) {
		return "N" + neuronId + "_C" + conId;
	}

	/**
	 * Take from hash table and put into all weights
	 */
	public void updateAllWeights() {
		// update weights for the output layer
		for (final Neuron n : outputLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final String key = weightKey(n.id, con.id);
				final double newWeight = weightUpdate.get(key);
				con.setWeight(newWeight);
			}
		}
		// update weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final String key = weightKey(n.id, con.id);
				final double newWeight = weightUpdate.get(key);
				con.setWeight(newWeight);
			}
		}
	}

	public void applyBackpropagation(final double expectedOutput, final int index) {

		int i = 0;
		final Map<Neuron, Double> outputErrs = new HashMap<>();
		for (final Neuron n : outputLayer) {
			if (index != i) {
				i++;
				continue;
			}

			final double ak = n.getOutput();
			final double deltaK = expectedOutput - ak;
			final double errorSignal = (1 - ak) * ak * deltaK;
			outputErrs.put(n, errorSignal);
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				con.setWeight(con.getWeight() + learningRate * errorSignal * con.getFromNeuron().output);
			}
			// Update bias
			n.biasConnection.setWeight(n.biasConnection.getWeight() + learningRate * errorSignal);
			i++;
		}

		// update weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final Map<Neuron, Double> inputErr = new HashMap<>();
			int j = 0;
			for (final Neuron outNeuron : outputLayer) {
				if (index != j) {
					j++;
					continue;
				}
				final Connection conn = outNeuron.getAllInConnections().stream().filter(c -> c.leftNeuron.id == n.id)
						.findFirst().get();
				inputErr.put(n, outputErrs.get(outNeuron) * conn.weight);
				j++;
			}
			inputErr.put(n, inputErr.get(n) * n.output * (1 - n.output));

			// Get all connections from input layer to this hidden neuron
			final List<Connection> connInputToHidden = n.getAllInConnections();
			connInputToHidden.parallelStream().forEach(conn -> {
				conn.setWeight(conn.getWeight() + learningRate * inputErr.get(n) * conn.getFromNeuron().output);
			});

			// Upadate bias
			n.biasConnection.setWeight(n.biasConnection.getWeight() + learningRate * inputErr.get(n));
		}
	}
}