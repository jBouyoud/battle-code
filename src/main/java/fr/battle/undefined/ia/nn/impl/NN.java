package fr.battle.undefined.ia.nn.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

	/**
	 *
	 * @param expectedOutput
	 *            first calculate the partial derivative of the error with
	 *            respect to each of the weight leading into the output neurons
	 *            bias is also updated here
	 */
	public void applyBackpropagation(final double expectedOutput[]) {

		int i = 0;
		for (final Neuron n : outputLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double ak = n.getOutput();
				final double ai = con.leftNeuron.getOutput();
				final double desiredOutput = expectedOutput[i];

				final double partialDerivative = -ak * (MAX_SCORE - ak) * ai * (desiredOutput - ak);
				final double deltaWeight = -learningRate * partialDerivative;
				final double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
			i++;
		}

		// update weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double aj = n.getOutput();
				final double ai = con.leftNeuron.getOutput();
				double sumKoutputs = 0;
				int j = 0;
				for (final Neuron out_neu : outputLayer) {
					final double wjk = out_neu.getConnection(n.getId()).getWeight();
					final double desiredOutput = expectedOutput[j];
					final double ak = out_neu.getOutput();
					j++;
					sumKoutputs = sumKoutputs + -(desiredOutput - ak) * ak * (MAX_SCORE - ak) * wjk;
				}

				final double partialDerivative = aj * (MAX_SCORE - aj) * ai * sumKoutputs;
				final double deltaWeight = -learningRate * partialDerivative;
				final double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
		}
	}

	public void applyBackpropagation(final double expectedOutput, final int index) {

		int i = 0;
		for (final Neuron n : outputLayer) {
			if (index != i) {
				continue;
			}
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {

				final double ak = n.getOutput();
				final double ai = con.leftNeuron.getOutput();
				final double desiredOutput = expectedOutput;

				final double deltaK = expectedOutput - ak;
				final double errorSignal = (1 - ak) * ak * deltaK;
				// final double partialDerivative =
				final double deltaWeight = learningRate * errorSignal;
				final double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
			i++;
		}

		// update weights for the hidden layer
		for (final Neuron n : hiddenLayer) {
			final ArrayList<Connection> connections = n.getAllInConnections();
			for (final Connection con : connections) {
				final double aj = n.getOutput();
				final double ai = con.leftNeuron.getOutput();
				double sumKoutputs = 0;
				int j = 0;
				for (final Neuron out_neu : outputLayer) {
					if (index != j) {
						continue;
					}
					final double wjk = out_neu.getConnection(n.getId()).getWeight();
					final double desiredOutput = expectedOutput;
					final double ak = out_neu.getOutput();
					j++;
					sumKoutputs += (desiredOutput - ak) * ak * (1 - ak) * wjk;
				}

				// final double partialDerivative = aj * (1 - aj) * ai *
				// sumKoutputs;
				final double partialDerivative = aj * (1 - aj) * sumKoutputs;
				final double deltaWeight = learningRate * partialDerivative;
				final double newWeight = con.getWeight() + deltaWeight;
				con.setDeltaWeight(deltaWeight);
				con.setWeight(newWeight + momentum * con.getPrevDeltaWeight());
			}
		}
	}
}